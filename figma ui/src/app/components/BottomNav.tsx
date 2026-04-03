import { Home, ShoppingCart, User, LayoutDashboard, Package, Users, Store, BarChart3 } from "lucide-react";
import { NavLink } from "react-router";
import { useAuth } from "../context/AuthContext";

export function BottomNav() {
  const { user } = useAuth();

  // Different navigation based on user role
  const getNavItems = () => {
    if (!user || user.role === "customer") {
      return [
        { to: "/", icon: Home, label: "Home" },
        { to: "/cart", icon: ShoppingCart, label: "Cart" },
        { to: "/profile", icon: User, label: "Profile" },
      ];
    }

    if (user.role === "store") {
      return [
        { to: "/store-dashboard", icon: LayoutDashboard, label: "Dashboard" },
        { to: "/store-products", icon: Package, label: "Products" },
        { to: "/store-orders", icon: ShoppingCart, label: "Orders" },
        { to: "/profile", icon: User, label: "Profile" },
      ];
    }

    if (user.role === "admin") {
      return [
        { to: "/admin-dashboard", icon: BarChart3, label: "Dashboard" },
        { to: "/user-management", icon: Users, label: "Users" },
        { to: "/store-management", icon: Store, label: "Stores" },
        { to: "/profile", icon: User, label: "Analytics" },
      ];
    }

    return [];
  };

  const navItems = getNavItems();

  return (
    <nav className="fixed bottom-0 left-0 right-0 max-w-md mx-auto bg-white border-t border-gray-200 shadow-lg z-50">
      <div className="flex justify-around items-center h-16 px-4">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex flex-col items-center justify-center gap-1 flex-1 py-2 rounded-lg transition-all ${
                isActive
                  ? "text-indigo-700"
                  : "text-gray-500 hover:text-gray-700"
              }`
            }
          >
            {({ isActive }) => (
              <>
                <item.icon className={`w-6 h-6 ${isActive ? "stroke-[2.5]" : "stroke-2"}`} />
                <span className={`text-xs ${isActive ? "font-semibold" : "font-medium"}`}>
                  {item.label}
                </span>
              </>
            )}
          </NavLink>
        ))}
      </div>
    </nav>
  );
}