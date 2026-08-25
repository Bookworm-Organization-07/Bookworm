import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import NavBar from './components/NavBar'
import Footer from './components/Footer'
import ProtectedRoute from './components/ProtectedRoute'
import AdminRoute from './components/AdminRoute'
import HomePage from './pages/HomePage'
import GenresPage from './pages/GenresPage'
import LendingLibraryPage from './pages/LendingLibraryPage'
import ItemDescriptionPage from './pages/ItemDescriptionPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import CheckoutPage from './pages/CheckoutPage'
import MyShelfPage from './pages/MyShelfPage'
import MyLibraryPage from './pages/MyLibraryPage'
import AdminLayout from './pages/admin/AdminLayout'
import AdminDashboardPage from './pages/admin/AdminDashboardPage'
import AdminProductsPage from './pages/admin/AdminProductsPage'
import AdminUploadPage from './pages/admin/AdminUploadPage'
import AdminBulkUploadPage from './pages/admin/AdminBulkUploadPage'
import AdminRoyaltiesPage from './pages/admin/AdminRoyaltiesPage'
import AdminBeneficiariesPage from './pages/admin/AdminBeneficiariesPage'
import AdminRoyaltyLedgerPage from './pages/admin/AdminRoyaltyLedgerPage'
import AdminUsersPage from './pages/admin/AdminUsersPage'
import AdminOrdersPage from './pages/admin/AdminOrdersPage'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <div className="flex min-h-screen flex-col">
            <NavBar />
            <main className="flex-1">
              <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/genres" element={<GenresPage />} />
                <Route path="/lending-library" element={<LendingLibraryPage />} />
                <Route path="/products/:productId" element={<ItemDescriptionPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route
                  path="/checkout"
                  element={
                    <ProtectedRoute>
                      <CheckoutPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/my-shelf"
                  element={
                    <ProtectedRoute>
                      <MyShelfPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/my-library"
                  element={
                    <ProtectedRoute>
                      <MyLibraryPage />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/admin"
                  element={
                    <AdminRoute>
                      <AdminLayout />
                    </AdminRoute>
                  }
                >
                  <Route index element={<AdminDashboardPage />} />
                  <Route path="products" element={<AdminProductsPage />} />
                  <Route path="upload" element={<AdminUploadPage />} />
                  <Route path="bulk-upload" element={<AdminBulkUploadPage />} />
                  <Route path="royalties" element={<AdminRoyaltiesPage />} />
                  <Route path="beneficiaries" element={<AdminBeneficiariesPage />} />
                  <Route path="royalty-ledger" element={<AdminRoyaltyLedgerPage />} />
                  <Route path="users" element={<AdminUsersPage />} />
                  <Route path="orders" element={<AdminOrdersPage />} />
                </Route>
              </Routes>
            </main>
            <Footer />
          </div>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
