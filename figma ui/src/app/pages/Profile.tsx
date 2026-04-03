import {
  User,
  MapPin,
  CreditCard,
  Bell,
  Heart,
  HelpCircle,
  Settings,
  LogOut,
  ChevronRight,
  ShoppingBag,
  Package,
} from "lucide-react";
import { useNavigate } from "react-router";
import { useAuth } from "../context/AuthContext";

const menuItems = [
  { icon: User, label: "Edit Profile", color: "text-indigo-700", path: "/profile/edit" },
  { icon: MapPin, label: "Shipping Address", color: "text-blue-600", path: "/profile/address" },
  { icon: CreditCard, label: "Payment Methods", color: "text-green-600", path: "/profile/payment" },
  { icon: Bell, label: "Notifications", color: "text-orange-600", path: "/profile/notifications" },
  { icon: HelpCircle, label: "Help & Support", color: "text-purple-600", path: null },
  { icon: Settings, label: "Settings", color: "text-gray-600", path: null },
];

export function Profile() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate("/auth");
  };

  const getRoleBadge = () => {
    if (user?.role === "admin") return "bg-purple-100 text-purple-700";
    if (user?.role === "store") return "bg-blue-100 text-blue-700";
    return "bg-green-100 text-green-700";
  };

  const getRoleLabel = () => {
    if (user?.role === "admin") return "Admin";
    if (user?.role === "store") return "Store Owner";
    return "Customer";
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-20">
      {/* Header */}
      <header className="bg-gradient-to-br from-indigo-700 to-purple-700 px-6 pt-6 pb-8 rounded-b-3xl shadow-lg">
        <h1 className="text-xl font-bold text-white mb-6">Profile</h1>
        <div className="flex items-center gap-4">
          <div className="w-20 h-20 rounded-full bg-white flex items-center justify-center shadow-lg">
            <User className="w-10 h-10 text-indigo-700" />
          </div>
          <div className="flex-1">
            <h2 className="text-xl font-bold text-white mb-1">
              {user?.name || "Guest User"}
            </h2>
            <p className="text-indigo-100 text-sm mb-2">
              {user?.email || "guest@example.com"}
            </p>
            <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getRoleBadge()}`}>
              {getRoleLabel()}
            </span>
          </div>
        </div>
      </header>

      {/* Quick Action Cards */}
      <div className="px-6 -mt-6 mb-6">
        <div className="grid grid-cols-2 gap-4">
          {/* Order History Card */}
          <button
            onClick={() => navigate("/profile/orders")}
            className="bg-white rounded-2xl shadow-lg p-4 hover:shadow-xl transition-shadow"
          >
            <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center mb-3 mx-auto">
              <Package className="w-6 h-6 text-indigo-700" />
            </div>
            <p className="text-2xl font-bold text-indigo-700 text-center">12</p>
            <p className="text-sm text-gray-600 mt-1 text-center">Order History</p>
          </button>

          {/* Favorites Card */}
          <button
            onClick={() => navigate("/profile/favorites")}
            className="bg-white rounded-2xl shadow-lg p-4 hover:shadow-xl transition-shadow"
          >
            <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mb-3 mx-auto">
              <Heart className="w-6 h-6 text-red-500" />
            </div>
            <p className="text-2xl font-bold text-red-500 text-center">8</p>
            <p className="text-sm text-gray-600 mt-1 text-center">Favorites</p>
          </button>
        </div>
      </div>

      {/* Menu Items */}
      <div className="px-6 pb-6">
        <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
          {menuItems.map((item, index) => (
            <button
              key={item.label}
              onClick={() => item.path && navigate(item.path)}
              className={`w-full flex items-center gap-4 px-6 py-4 hover:bg-gray-50 transition-colors ${
                index !== menuItems.length - 1 ? "border-b border-gray-100" : ""
              }`}
            >
              <div className={`w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center ${item.color}`}>
                <item.icon className="w-5 h-5" />
              </div>
              <span className="flex-1 text-left font-medium text-gray-900">
                {item.label}
              </span>
              <ChevronRight className="w-5 h-5 text-gray-400" />
            </button>
          ))}
        </div>

        {/* Logout Button */}
        <button
          onClick={handleLogout}
          className="w-full mt-4 bg-white rounded-2xl shadow-sm px-6 py-4 flex items-center gap-4 hover:bg-red-50 transition-colors group"
        >
          <div className="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center text-red-500 group-hover:bg-red-100">
            <LogOut className="w-5 h-5" />
          </div>
          <span className="flex-1 text-left font-medium text-red-500">
            Logout
          </span>
        </button>

        {/* Version */}
        <p className="text-center text-sm text-gray-400 mt-6">Version 1.0.0</p>
      </div>
    </div>
  );
}