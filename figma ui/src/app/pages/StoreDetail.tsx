import { useState } from "react";
import { ChevronLeft, Store, Star, Package, ShoppingCart, TrendingUp, Mail, Calendar, MapPin, Phone, Clock, Globe } from "lucide-react";
import { useNavigate, useParams } from "react-router";
import { BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

// Mock data for store details
const storeDetailsData: Record<string, any> = {
  "1": {
    id: "1",
    name: "TechStore Pro",
    ownerName: "Store Owner",
    ownerEmail: "store@test.com",
    ownerPhone: "+1 (555) 123-4567",
    rating: 4.9,
    totalSales: "$2,548,000",
    totalProducts: 145,
    totalOrders: 1250,
    status: "active",
    joinDate: "2023-06-15",
    description: "Premium technology store offering the latest gadgets and electronics from top brands worldwide.",
    address: "123 Tech Street, Silicon Valley, CA 94025",
    website: "www.techstorepro.com",
    businessHours: "Mon-Fri: 9AM-6PM, Sat: 10AM-4PM",
    monthlyRevenue: [
      { month: "Jan", revenue: 180000 },
      { month: "Feb", revenue: 220000 },
      { month: "Mar", revenue: 195000 },
      { month: "Apr", revenue: 245000 },
      { month: "May", revenue: 210000 },
      { month: "Jun", revenue: 280000 },
    ],
    categoryDistribution: [
      { category: "Phones", count: 45 },
      { category: "Laptops", count: 35 },
      { category: "Accessories", count: 40 },
      { category: "Tablets", count: 25 },
    ],
    topProducts: [
      { name: "iPhone 15 Pro Max", sales: 234, revenue: "$234,000" },
      { name: "MacBook Pro M3", sales: 156, revenue: "$312,000" },
      { name: "AirPods Pro 2", sales: 189, revenue: "$47,250" },
    ],
    recentOrders: [
      { id: "ORD-1234", date: "2024-03-20", customer: "John Doe", amount: "$1,299" },
      { id: "ORD-1235", date: "2024-03-20", customer: "Jane Smith", amount: "$899" },
      { id: "ORD-1236", date: "2024-03-19", customer: "Mike Johnson", amount: "$2,199" },
    ],
  },
  "2": {
    id: "2",
    name: "Gadget Hub",
    ownerName: "Mike Anderson",
    ownerEmail: "mike@store.com",
    ownerPhone: "+1 (555) 234-5678",
    rating: 4.8,
    totalSales: "$1,823,500",
    totalProducts: 98,
    totalOrders: 890,
    status: "active",
    joinDate: "2023-08-20",
    description: "Your one-stop shop for innovative gadgets and smart home devices.",
    address: "456 Innovation Blvd, Austin, TX 78701",
    website: "www.gadgethub.com",
    businessHours: "Mon-Sat: 10AM-7PM, Sun: 11AM-5PM",
    monthlyRevenue: [
      { month: "Jan", revenue: 145000 },
      { month: "Feb", revenue: 165000 },
      { month: "Mar", revenue: 178000 },
      { month: "Apr", revenue: 189000 },
      { month: "May", revenue: 195000 },
      { month: "Jun", revenue: 210000 },
    ],
    categoryDistribution: [
      { category: "Smart Home", count: 28 },
      { category: "Wearables", count: 22 },
      { category: "Audio", count: 30 },
      { category: "Gaming", count: 18 },
    ],
    topProducts: [
      { name: "Samsung Galaxy Watch", sales: 178, revenue: "$71,200" },
      { name: "Google Nest Hub", sales: 145, revenue: "$21,750" },
      { name: "Sony WH-1000XM5", sales: 123, revenue: "$49,200" },
    ],
    recentOrders: [
      { id: "ORD-2234", date: "2024-03-20", customer: "Sarah Lee", amount: "$399" },
      { id: "ORD-2235", date: "2024-03-19", customer: "Tom Brown", amount: "$599" },
      { id: "ORD-2236", date: "2024-03-19", customer: "Lisa White", amount: "$249" },
    ],
  },
  "3": {
    id: "3",
    name: "ElectroWorld",
    ownerName: "Sarah Martinez",
    ownerEmail: "sarah@electroworld.com",
    ownerPhone: "+1 (555) 345-6789",
    rating: 4.7,
    totalSales: "$1,567,200",
    totalProducts: 112,
    totalOrders: 720,
    status: "active",
    joinDate: "2023-07-10",
    description: "Electronics superstore with a wide range of products at competitive prices.",
    address: "789 Electronics Ave, Seattle, WA 98101",
    website: "www.electroworld.com",
    businessHours: "Mon-Sun: 9AM-9PM",
    monthlyRevenue: [
      { month: "Jan", revenue: 125000 },
      { month: "Feb", revenue: 138000 },
      { month: "Mar", revenue: 155000 },
      { month: "Apr", revenue: 168000 },
      { month: "May", revenue: 172000 },
      { month: "Jun", revenue: 185000 },
    ],
    categoryDistribution: [
      { category: "TVs", count: 25 },
      { category: "Cameras", count: 20 },
      { category: "Computers", count: 35 },
      { category: "Accessories", count: 32 },
    ],
    topProducts: [
      { name: "LG OLED TV 65\"", sales: 89, revenue: "$178,000" },
      { name: "Canon EOS R6", sales: 67, revenue: "$167,500" },
      { name: "Dell XPS 15", sales: 102, revenue: "$204,000" },
    ],
    recentOrders: [
      { id: "ORD-3234", date: "2024-03-20", customer: "David Kim", amount: "$2,499" },
      { id: "ORD-3235", date: "2024-03-20", customer: "Emma Wilson", amount: "$1,299" },
      { id: "ORD-3236", date: "2024-03-19", customer: "Chris Evans", amount: "$899" },
    ],
  },
  "4": {
    id: "4",
    name: "TechMart",
    ownerName: "David Lee",
    ownerEmail: "contact@techmart.com",
    ownerPhone: "+1 (555) 456-7890",
    rating: 4.5,
    totalSales: "$987,400",
    totalProducts: 76,
    totalOrders: 450,
    status: "active",
    joinDate: "2023-09-05",
    description: "Affordable tech solutions for everyone, from students to professionals.",
    address: "321 Market St, Denver, CO 80202",
    website: "www.techmart.com",
    businessHours: "Mon-Fri: 10AM-6PM, Sat: 11AM-5PM",
    monthlyRevenue: [
      { month: "Jan", revenue: 78000 },
      { month: "Feb", revenue: 85000 },
      { month: "Mar", revenue: 92000 },
      { month: "Apr", revenue: 98000 },
      { month: "May", revenue: 105000 },
      { month: "Jun", revenue: 112000 },
    ],
    categoryDistribution: [
      { category: "Laptops", count: 22 },
      { category: "Peripherals", count: 28 },
      { category: "Storage", count: 15 },
      { category: "Networking", count: 11 },
    ],
    topProducts: [
      { name: "Lenovo ThinkPad", sales: 78, revenue: "$78,000" },
      { name: "Logitech MX Master", sales: 156, revenue: "$15,600" },
      { name: "Samsung SSD 1TB", sales: 134, revenue: "$16,750" },
    ],
    recentOrders: [
      { id: "ORD-4234", date: "2024-03-20", customer: "Alex Turner", amount: "$899" },
      { id: "ORD-4235", date: "2024-03-19", customer: "Maria Garcia", amount: "$499" },
      { id: "ORD-4236", date: "2024-03-19", customer: "Ryan Cooper", amount: "$1,199" },
    ],
  },
  "5": {
    id: "5",
    name: "SmartDevices Co",
    ownerName: "Emma Wilson",
    ownerEmail: "emma@smartdevices.com",
    ownerPhone: "+1 (555) 567-8901",
    rating: 3.8,
    totalSales: "$245,600",
    totalProducts: 34,
    totalOrders: 120,
    status: "disabled",
    joinDate: "2024-01-15",
    description: "Specialized in smart home and IoT devices for modern living.",
    address: "654 Smart Ave, Portland, OR 97201",
    website: "www.smartdevices.com",
    businessHours: "Currently Disabled",
    monthlyRevenue: [
      { month: "Jan", revenue: 35000 },
      { month: "Feb", revenue: 42000 },
      { month: "Mar", revenue: 38000 },
      { month: "Apr", revenue: 45000 },
      { month: "May", revenue: 48000 },
      { month: "Jun", revenue: 37600 },
    ],
    categoryDistribution: [
      { category: "Smart Lights", count: 10 },
      { category: "Security", count: 8 },
      { category: "Climate", count: 9 },
      { category: "Other", count: 7 },
    ],
    topProducts: [
      { name: "Philips Hue Kit", sales: 45, revenue: "$9,000" },
      { name: "Ring Doorbell", sales: 38, revenue: "$11,400" },
      { name: "Nest Thermostat", sales: 32, revenue: "$8,000" },
    ],
    recentOrders: [
      { id: "ORD-5234", date: "2024-03-15", customer: "Kate Miller", amount: "$299" },
      { id: "ORD-5235", date: "2024-03-14", customer: "Paul Adams", amount: "$199" },
      { id: "ORD-5236", date: "2024-03-13", customer: "Nina Patel", amount: "$399" },
    ],
  },
};

export function StoreDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [activeTab, setActiveTab] = useState<"overview" | "products" | "orders">("overview");

  const store = id ? storeDetailsData[id] : null;

  if (!store) {
    return (
      <div className="bg-gray-50 min-h-screen flex items-center justify-center">
        <div className="text-center">
          <Store className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-gray-900 mb-2">Store Not Found</h2>
          <p className="text-gray-500 mb-6">The store you're looking for doesn't exist.</p>
          <button
            onClick={() => navigate("/store-management")}
            className="px-6 py-3 bg-indigo-700 text-white rounded-xl font-semibold"
          >
            Back to Stores
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-50 min-h-screen pb-20">
      {/* Header */}
      <header className="bg-gradient-to-br from-purple-700 to-indigo-700 px-6 pt-6 pb-8 rounded-b-3xl shadow-lg">
        <div className="flex items-center gap-4 mb-6">
          <button
            onClick={() => navigate("/store-management")}
            className="p-2 -ml-2 hover:bg-white/10 rounded-xl transition-colors"
          >
            <ChevronLeft className="w-6 h-6 text-white" />
          </button>
          <div className="flex-1">
            <h1 className="text-xl font-bold text-white mb-1">Store Details</h1>
            <p className="text-purple-100 text-sm">Complete store information</p>
          </div>
        </div>

        {/* Store Info Card */}
        <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-2xl p-5">
          <div className="flex items-start gap-4 mb-4">
            <div className="w-16 h-16 bg-white rounded-xl flex items-center justify-center flex-shrink-0">
              <Store className="w-8 h-8 text-indigo-700" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-start justify-between gap-2 mb-2">
                <h2 className="font-bold text-white text-lg">{store.name}</h2>
                <span
                  className={`px-3 py-1 rounded-lg text-xs font-semibold ${
                    store.status === "active"
                      ? "bg-green-400 text-green-900"
                      : "bg-red-400 text-red-900"
                  }`}
                >
                  {store.status === "active" ? "Active" : "Disabled"}
                </span>
              </div>
              <div className="flex items-center gap-2 mb-2">
                {[...Array(5)].map((_, i) => (
                  <Star
                    key={i}
                    className={`w-4 h-4 ${
                      i < Math.floor(store.rating)
                        ? "fill-yellow-300 text-yellow-300"
                        : "text-white/30"
                    }`}
                  />
                ))}
                <span className="text-white font-semibold text-sm">{store.rating}</span>
              </div>
              <p className="text-purple-100 text-sm">{store.description}</p>
            </div>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-2 mt-4">
            <div className="bg-white/10 backdrop-blur-sm rounded-lg p-3 text-center">
              <p className="text-xs text-purple-100 mb-1">Products</p>
              <p className="font-bold text-white">{store.totalProducts}</p>
            </div>
            <div className="bg-white/10 backdrop-blur-sm rounded-lg p-3 text-center">
              <p className="text-xs text-purple-100 mb-1">Orders</p>
              <p className="font-bold text-white">{store.totalOrders.toLocaleString()}</p>
            </div>
            <div className="bg-white/10 backdrop-blur-sm rounded-lg p-3 text-center">
              <p className="text-xs text-purple-100 mb-1">Sales</p>
              <p className="font-bold text-white text-sm">{store.totalSales}</p>
            </div>
          </div>
        </div>
      </header>

      {/* Tabs */}
      <div className="px-6 mt-6">
        <div className="bg-white rounded-xl p-1 shadow-sm border border-gray-100 flex gap-1">
          <button
            onClick={() => setActiveTab("overview")}
            className={`flex-1 py-2 rounded-lg font-semibold text-sm transition-all ${
              activeTab === "overview"
                ? "bg-indigo-700 text-white"
                : "text-gray-600 hover:bg-gray-50"
            }`}
          >
            Overview
          </button>
          <button
            onClick={() => setActiveTab("products")}
            className={`flex-1 py-2 rounded-lg font-semibold text-sm transition-all ${
              activeTab === "products"
                ? "bg-indigo-700 text-white"
                : "text-gray-600 hover:bg-gray-50"
            }`}
          >
            Products
          </button>
          <button
            onClick={() => setActiveTab("orders")}
            className={`flex-1 py-2 rounded-lg font-semibold text-sm transition-all ${
              activeTab === "orders"
                ? "bg-indigo-700 text-white"
                : "text-gray-600 hover:bg-gray-50"
            }`}
          >
            Orders
          </button>
        </div>
      </div>

      {/* Content */}
      <div className="px-6 mt-6 space-y-6 pb-6">
        {activeTab === "overview" && (
          <>
            {/* Contact Information */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h3 className="font-bold text-gray-900 mb-4">Contact Information</h3>
              <div className="space-y-3">
                <div className="flex items-start gap-3">
                  <Mail className="w-5 h-5 text-indigo-600 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Owner Email</p>
                    <p className="font-semibold text-gray-900">{store.ownerEmail}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <Phone className="w-5 h-5 text-indigo-600 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Phone</p>
                    <p className="font-semibold text-gray-900">{store.ownerPhone}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <MapPin className="w-5 h-5 text-indigo-600 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Address</p>
                    <p className="font-semibold text-gray-900">{store.address}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <Globe className="w-5 h-5 text-indigo-600 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Website</p>
                    <p className="font-semibold text-gray-900">{store.website}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <Clock className="w-5 h-5 text-indigo-600 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Business Hours</p>
                    <p className="font-semibold text-gray-900">{store.businessHours}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <Calendar className="w-5 h-5 text-indigo-600 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Join Date</p>
                    <p className="font-semibold text-gray-900">
                      {new Date(store.joinDate).toLocaleDateString("en-US", {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                      })}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Revenue Chart */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center gap-2 mb-4">
                <TrendingUp className="w-5 h-5 text-green-600" />
                <h3 className="font-bold text-gray-900">Monthly Revenue</h3>
              </div>
              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={store.monthlyRevenue}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} stroke="#9ca3af" />
                  <YAxis
                    tick={{ fontSize: 12 }}
                    stroke="#9ca3af"
                    tickFormatter={(value) => `$${value / 1000}k`}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#ffffff",
                      border: "1px solid #e5e7eb",
                      borderRadius: "8px",
                      fontSize: "12px",
                    }}
                    formatter={(value: number) => [`$${value.toLocaleString()}`, "Revenue"]}
                  />
                  <Line
                    type="monotone"
                    dataKey="revenue"
                    stroke="#10b981"
                    strokeWidth={2}
                    dot={{ fill: "#10b981", r: 4 }}
                    activeDot={{ r: 6 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>

            {/* Category Distribution */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center gap-2 mb-4">
                <Package className="w-5 h-5 text-indigo-600" />
                <h3 className="font-bold text-gray-900">Product Categories</h3>
              </div>
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={store.categoryDistribution}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="category" tick={{ fontSize: 11 }} stroke="#9ca3af" />
                  <YAxis tick={{ fontSize: 12 }} stroke="#9ca3af" />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#ffffff",
                      border: "1px solid #e5e7eb",
                      borderRadius: "8px",
                      fontSize: "12px",
                    }}
                    formatter={(value: number) => [value, "Products"]}
                  />
                  <Bar dataKey="count" fill="#4338ca" radius={[8, 8, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </>
        )}

        {activeTab === "products" && (
          <>
            {/* Top Products */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center gap-2 mb-4">
                <TrendingUp className="w-5 h-5 text-green-600" />
                <h3 className="font-bold text-gray-900">Top Selling Products</h3>
              </div>
              <div className="space-y-3">
                {store.topProducts.map((product: any, index: number) => (
                  <div
                    key={index}
                    className="flex items-center justify-between p-4 bg-gray-50 rounded-xl"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                        <span className="font-bold text-green-700">#{index + 1}</span>
                      </div>
                      <div>
                        <p className="font-semibold text-gray-900">{product.name}</p>
                        <p className="text-xs text-gray-500">{product.sales} sales</p>
                      </div>
                    </div>
                    <p className="font-bold text-green-600">{product.revenue}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* Product Stats */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h3 className="font-bold text-gray-900 mb-4">Product Statistics</h3>
              <div className="grid grid-cols-2 gap-3">
                <div className="bg-blue-50 rounded-xl p-4 text-center">
                  <Package className="w-6 h-6 text-blue-600 mx-auto mb-2" />
                  <p className="text-2xl font-bold text-blue-700">{store.totalProducts}</p>
                  <p className="text-xs text-blue-600 mt-1">Total Products</p>
                </div>
                <div className="bg-purple-50 rounded-xl p-4 text-center">
                  <ShoppingCart className="w-6 h-6 text-purple-600 mx-auto mb-2" />
                  <p className="text-2xl font-bold text-purple-700">
                    {store.totalOrders.toLocaleString()}
                  </p>
                  <p className="text-xs text-purple-600 mt-1">Total Orders</p>
                </div>
              </div>
            </div>
          </>
        )}

        {activeTab === "orders" && (
          <>
            {/* Recent Orders */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center gap-2 mb-4">
                <ShoppingCart className="w-5 h-5 text-indigo-600" />
                <h3 className="font-bold text-gray-900">Recent Orders</h3>
              </div>
              <div className="space-y-3">
                {store.recentOrders.map((order: any) => (
                  <div
                    key={order.id}
                    className="p-4 bg-gray-50 rounded-xl"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <p className="font-semibold text-gray-900">{order.id}</p>
                      <p className="font-bold text-indigo-700">{order.amount}</p>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <p className="text-gray-600">{order.customer}</p>
                      <p className="text-gray-500">{order.date}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Order Stats */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h3 className="font-bold text-gray-900 mb-4">Order Statistics</h3>
              <div className="space-y-3">
                <div className="flex items-center justify-between p-4 bg-green-50 rounded-xl">
                  <div>
                    <p className="text-sm text-green-600 mb-1">Total Orders</p>
                    <p className="text-2xl font-bold text-green-700">
                      {store.totalOrders.toLocaleString()}
                    </p>
                  </div>
                  <ShoppingCart className="w-10 h-10 text-green-600" />
                </div>
                <div className="flex items-center justify-between p-4 bg-purple-50 rounded-xl">
                  <div>
                    <p className="text-sm text-purple-600 mb-1">Total Revenue</p>
                    <p className="text-2xl font-bold text-purple-700">{store.totalSales}</p>
                  </div>
                  <TrendingUp className="w-10 h-10 text-purple-600" />
                </div>
                <div className="flex items-center justify-between p-4 bg-blue-50 rounded-xl">
                  <div>
                    <p className="text-sm text-blue-600 mb-1">Average Order Value</p>
                    <p className="text-2xl font-bold text-blue-700">
                      $
                      {(
                        parseInt(store.totalSales.replace(/[$,]/g, "")) / store.totalOrders
                      ).toFixed(0)}
                    </p>
                  </div>
                  <Package className="w-10 h-10 text-blue-600" />
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
