import { createBrowserRouter } from "react-router";
import { Home } from "./pages/Home";
import { Cart } from "./pages/Cart";
import { Profile } from "./pages/Profile";
import { ProductDetail } from "./pages/ProductDetail";
import { ProductListing } from "./pages/ProductListing";
import { Auth } from "./pages/Auth";
import { Checkout } from "./pages/Checkout";
import { OrderSuccess } from "./pages/OrderSuccess";
import { Dashboard } from "./pages/Dashboard";
import { StoreDashboard } from "./pages/StoreDashboard";
import { ProductForm } from "./pages/ProductForm";
import { StoreProducts } from "./pages/StoreProducts";
import { StoreOrders } from "./pages/StoreOrders";
import { StoreOrderDetail } from "./pages/StoreOrderDetail";
import { AdminDashboard } from "./pages/AdminDashboard";
import { UserManagement } from "./pages/UserManagement";
import { StoreManagement } from "./pages/StoreManagement";
import { StoreDetail } from "./pages/StoreDetail";
import { Root } from "./components/Root";
import {
  EditProfile,
  ShippingAddress,
  PaymentMethods,
  Notifications,
  Favorites,
  OrderHistory,
} from "./pages/ProfileSubPages";

export const router = createBrowserRouter([
  {
    path: "/auth",
    Component: Auth,
  },
  {
    path: "/",
    Component: Root,
    children: [
      { index: true, Component: Home },
      { path: "products", Component: ProductListing },
      { path: "cart", Component: Cart },
      { path: "checkout", Component: Checkout },
      { path: "order-success", Component: OrderSuccess },
      { path: "dashboard", Component: Dashboard },
      { path: "store-dashboard", Component: StoreDashboard },
      { path: "product-form", Component: ProductForm },
      { path: "store-products", Component: StoreProducts },
      { path: "store-orders", Component: StoreOrders },
      { path: "store-orders/:id", Component: StoreOrderDetail },
      { path: "admin-dashboard", Component: AdminDashboard },
      { path: "user-management", Component: UserManagement },
      { path: "store-management", Component: StoreManagement },
      { path: "store-detail/:id", Component: StoreDetail },
      { path: "profile", Component: Profile },
      { path: "profile/edit", Component: EditProfile },
      { path: "profile/address", Component: ShippingAddress },
      { path: "profile/payment", Component: PaymentMethods },
      { path: "profile/notifications", Component: Notifications },
      { path: "profile/favorites", Component: Favorites },
      { path: "profile/orders", Component: OrderHistory },
      { path: "product/:id", Component: ProductDetail },
    ],
  },
]);