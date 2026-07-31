package com.bookworm.service;

import com.bookworm.dto.order.OrderResponse;
import com.bookworm.dto.order.PaymentRequest;
import com.bookworm.dto.order.PaymentResultResponse;
import com.bookworm.entity.*;
import com.bookworm.entity.enums.AccessType;
import com.bookworm.entity.enums.ItemType;
import com.bookworm.entity.enums.LibraryAccessType;
import com.bookworm.entity.enums.LibraryStatus;
import com.bookworm.entity.enums.MembershipStatus;
import com.bookworm.entity.enums.OrderStatus;
import com.bookworm.entity.enums.PaymentStatus;
import com.bookworm.exception.EmptyCartException;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.*;
import com.bookworm.service.InvoiceCalculator.BillBreakdown;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ShelfRepository shelfRepository;
    private final LibraryRepository libraryRepository;
    private final UserLibraryMembershipRepository userLibraryMembershipRepository;
    private final RoyaltyService royaltyService;

    public OrderService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentRepository paymentRepository,
            ShelfRepository shelfRepository,
            LibraryRepository libraryRepository,
            UserLibraryMembershipRepository userLibraryMembershipRepository,
            RoyaltyService royaltyService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.shelfRepository = shelfRepository;
        this.libraryRepository = libraryRepository;
        this.userLibraryMembershipRepository = userLibraryMembershipRepository;
        this.royaltyService = royaltyService;
    }

    /** Creates the order snapshot from the current cart and immediately attempts payment (BRD §12). */
    public PaymentResultResponse checkout(User user, PaymentRequest request) {
        Cart cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(EmptyCartException::new);
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        BigDecimal subtotal = cartItems.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BillBreakdown bill = InvoiceCalculator.compute(subtotal);

        Order order = Order.builder()
                .user(user)
                .subtotalAmount(bill.subtotal())
                .discountAmount(bill.discount())
                .vatAmount(bill.vat())
                .serviceChargeAmount(bill.serviceCharge())
                .totalPayableAmount(bill.total())
                .paymentMode(request.getPaymentMode())
                .orderStatus(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            OrderItem.OrderItemBuilder builder = OrderItem.builder()
                    .order(order)
                    .itemType(cartItem.getItemType())
                    .product(cartItem.getProduct())
                    .libraryPackage(cartItem.getLibraryPackage())
                    .accessType(cartItem.getAccessType())
                    .rentDays(cartItem.getRentDays())
                    .unitPrice(cartItem.getUnitPrice())
                    .quantity(cartItem.getQuantity());

            if (cartItem.getAccessType() == AccessType.RENT && cartItem.getRentDays() != null) {
                LocalDate from = LocalDate.now();
                builder.validFrom(from).validTo(from.plusDays(cartItem.getRentDays()));
            }
            orderItemRepository.save(builder.build());
        }

        return attemptPayment(order, request);
    }

    /** Retries payment on an existing PENDING order — e.g. after a declined attempt (BRD §12). */
    public PaymentResultResponse retryPay(User user, Integer orderId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not awaiting payment: " + order.getOrderStatus());
        }
        order.setPaymentMode(request.getPaymentMode());
        return attemptPayment(order, request);
    }

    public OrderResponse cancel(User user, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        Cart cart = cartRepository.findByUser_UserId(user.getUserId()).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCart_CartId(cart.getCartId());
        }
        return OrderResponse.from(order, orderItemRepository);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(User user, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return OrderResponse.from(order, orderItemRepository);
    }

    private PaymentResultResponse attemptPayment(Order order, PaymentRequest request) {
        boolean succeeds = !request.isSimulateFailure();

        Payment payment = Payment.builder()
                .order(order)
                .amountAttempted(order.getTotalPayableAmount())
                .paymentMode(request.getPaymentMode())
                .gatewayReference("SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .paymentStatus(succeeds ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .build();
        paymentRepository.save(payment);

        if (succeeds) {
            order.setOrderStatus(OrderStatus.PAID);
            orderRepository.save(order);
            fulfillOrder(order);
            Cart cart = cartRepository.findByUser_UserId(order.getUser().getUserId()).orElse(null);
            if (cart != null) {
                cartItemRepository.deleteByCart_CartId(cart.getCartId());
            }
            return new PaymentResultResponse(true, "Payment successful — added to My Shelf / My Library.",
                    OrderResponse.from(order, orderItemRepository));
        }

        return new PaymentResultResponse(false, "Payment declined by the gateway. You can try again.",
                OrderResponse.from(order, orderItemRepository));
    }

    /** BUY -> Shelf, RENT -> Library (RENTED), SUBSCRIBE -> a new library membership. Royalty fires for BUY/RENT immediately (BRD §13.1). */
    private void fulfillOrder(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        for (OrderItem item : items) {
            if (item.getItemType() == ItemType.PRODUCT && item.getAccessType() == AccessType.BUY) {
                shelfRepository.save(Shelf.builder()
                        .user(order.getUser())
                        .product(item.getProduct())
                        .orderItem(item)
                        .build());
                royaltyService.calculateForPurchase(item);

            } else if (item.getItemType() == ItemType.PRODUCT && item.getAccessType() == AccessType.RENT) {
                libraryRepository.save(Library.builder()
                        .user(order.getUser())
                        .product(item.getProduct())
                        .accessType(LibraryAccessType.RENTED)
                        .orderItem(item)
                        .startDate(LocalDateTime.now())
                        .expiryDate(item.getValidTo().atStartOfDay())
                        .status(LibraryStatus.ACTIVE)
                        .build());
                royaltyService.calculateForRent(item);

            } else if (item.getItemType() == ItemType.LIBRARY_PACKAGE && item.getAccessType() == AccessType.SUBSCRIBE) {
                LocalDate start = LocalDate.now();
                userLibraryMembershipRepository.save(UserLibraryMembership.builder()
                        .user(order.getUser())
                        .libraryPackage(item.getLibraryPackage())
                        .orderItem(item)
                        .booksUsed(0)
                        .startDate(start)
                        .expiryDate(start.plusDays(item.getLibraryPackage().getValidDays()))
                        .status(MembershipStatus.ACTIVE)
                        .build());
            }
        }
    }
}
