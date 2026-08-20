package com.example.Controller;

import com.example.Services.ShelfService;
import com.example.models.MyShelf;
import com.example.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shelf")
public class MyShelfController {

    private final ShelfService shelfService;
    private final CurrentUser currentUser;

    public MyShelfController(ShelfService shelfService, CurrentUser currentUser) {
        this.shelfService = shelfService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<MyShelf> getShelfItems() {
        return shelfService.getShelfByUser(currentUser.require().getUserId());
    }

    @DeleteMapping("/{shelfId}")
    public ResponseEntity<Void> deleteShelfItem(@PathVariable Integer shelfId) {
        shelfService.deleteShelfItem(currentUser.require(), shelfId);
        return ResponseEntity.noContent().build();
    }
}
