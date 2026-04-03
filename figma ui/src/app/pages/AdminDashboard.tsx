import { Users, Store, Package, ShoppingCart, TrendingUp, TrendingDown } from "lucide-react";
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import { useNavigate } from "react-router";

const ordersData = [
  { date: "Jan", orders: 120, id: "jan" },
  { date: "Feb", orders: 190, id: "feb" },
  { date: "Mar", orders: 150, id: "mar" },
  { date: "Apr", orders: 250, id: "apr" },
  { date: "May", orders: 220, id: "may" },
  { date: "Jun", orders: 300, id: "jun" },
];

const topProductsData = [
  { name: "iPhone 15 Pro", sales: 450, id: "iphone" },
  { name: "MacBook Pro", sales: 320, id: "macbook" },
  { name: "AirPods Pro", sales: 280, id: "airpods" },
  { name: "iPad Air", sales: 210, id: "ipad" },
  { name: "Apple Watch", sales: 180, id: "watch" },
];

const topProducts = [
  { id: "1", name: "iPhone 15 Pro Max", sales: 1234, revenue: "$1.23M" },
  { id: "2", name: "Samsung Galaxy S24", sales: 987, revenue: "$987K" },
  { id: "3", name: "MacBook Pro M3", sales: 756, revenue: "$1.51M" },
];

const worstProducts = [
  { id: "1", name: "Old Phone Case", sales: 12, revenue: "$240" },
  { id: "2", name: "Screen Protector", sales: 23, revenue: "$345" },
  { id: "3", name: "USB Cable 2.0", sales: 34, revenue: "$170" },
];

const topStores = [
  { id: "1", name: "TechStore Pro", sales: "$2.5M", rating: 4.9, orders: 1250 },
  { id: "2", name: "Gadget Hub", sales: "$1.8M", rating: 4.8, orders: 890 },
  { id: "3", name: "ElectroWorld", sales: "$1.5M", rating: 4.7, orders: 720 },
];

export function AdminDashboard() {
  const navigate = useNavigate();

  return (
    <div className="bg-gray-50 min-h-screen pb-20">
      {/* Header */}
      <header className="bg-gradient-to-br from-purple-700 to-indigo-700 px-6 pt-6 pb-8 rounded-b-3xl shadow-lg">
        <div className="mb-6">
          <h1 className="text-xl font-bold text-white mb-1">Admin Dashboard</h1>
          <p className="text-purple-100 text-sm">Platform Analytics & Management</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <Users className="w-5 h-5 text-white" />
              <p className="text-purple-100 text-sm">Total Users</p>
            </div>
            <p className="text-2xl font-bold text-white">12,458</p>
            <p className="text-xs text-purple-200 mt-1">+12% this month</p>
          </div>

          <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <Store className="w-5 h-5 text-white" />
              <p className="text-purple-100 text-sm">Total Stores</p>
            </div>
            <p className="text-2xl font-bold text-white">234</p>
            <p className="text-xs text-purple-200 mt-1">+8 new stores</p>
          </div>

          <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <Package className="w-5 h-5 text-white" />
              <p className="text-purple-100 text-sm">Total Products</p>
            </div>
            <p className="text-2xl font-bold text-white">8,932</p>
            <p className="text-xs text-purple-200 mt-1">Across all stores</p>
          </div>

          <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <ShoppingCart className="w-5 h-5 text-white" />
              <p className="text-purple-100 text-sm">Total Orders</p>
            </div>
            <p className="text-2xl font-bold text-white">45,231</p>
            <p className="text-xs text-purple-200 mt-1">+23% this month</p>
          </div>
        </div>
      </header>

      {/* Orders Chart */}
      <div className="px-6 mt-6">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h2 className="font-bold text-gray-900 mb-4">Orders Over Time</h2>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={ordersData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} stroke="#9ca3af" />
              <YAxis tick={{ fontSize: 12 }} stroke="#9ca3af" />
              <Tooltip
                contentStyle={{
                  backgroundColor: "#ffffff",
                  border: "1px solid #e5e7eb",
                  borderRadius: "8px",
                  fontSize: "12px",
                }}
              />
              <Line
                type="monotone"
                dataKey="orders"
                stroke="#7c3aed"
                strokeWidth={2}
                dot={{ fill: "#7c3aed", r: 4 }}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Top Selling Products Chart */}
      <div className="px-6 mt-6">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h2 className="font-bold text-gray-900 mb-4">Top Selling Products</h2>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={topProductsData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis type="number" tick={{ fontSize: 12 }} stroke="#9ca3af" />
              <YAxis
                type="category"
                dataKey="name"
                tick={{ fontSize: 11 }}
                stroke="#9ca3af"
                width={100}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: "#ffffff",
                  border: "1px solid #e5e7eb",
                  borderRadius: "8px",
                  fontSize: "12px",
                }}
              />
              <Bar dataKey="sales" fill="#4338ca" radius={[0, 8, 8, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Top Products List */}
      <div className="px-6 mt-6">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp className="w-5 h-5 text-green-600" />
            <h2 className="font-bold text-gray-900">Top Products</h2>
          </div>
          <div className="space-y-3">
            {topProducts.map((product, index) => (
              <div
                key={product.id}
                className="flex items-center justify-between p-3 bg-gray-50 rounded-xl"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                    <span className="font-bold text-green-700 text-sm">
                      #{index + 1}
                    </span>
                  </div>
                  <div>
                    <p className="font-semibold text-gray-900 text-sm">
                      {product.name}
                    </p>
                    <p className="text-xs text-gray-500">{product.sales} sales</p>
                  </div>
                </div>
                <p className="font-bold text-green-600">{product.revenue}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Worst Products List */}
      <div className="px-6 mt-6">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex items-center gap-2 mb-4">
            <TrendingDown className="w-5 h-5 text-red-600" />
            <h2 className="font-bold text-gray-900">Worst Performing Products</h2>
          </div>
          <div className="space-y-3">
            {worstProducts.map((product) => (
              <div
                key={product.id}
                className="flex items-center justify-between p-3 bg-gray-50 rounded-xl"
              >
                <div>
                  <p className="font-semibold text-gray-900 text-sm">
                    {product.name}
                  </p>
                  <p className="text-xs text-gray-500">{product.sales} sales</p>
                </div>
                <p className="font-bold text-red-600">{product.revenue}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Top Stores List */}
      <div className="px-6 mt-6">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-bold text-gray-900">Top Stores</h2>
            <Store className="w-5 h-5 text-indigo-600" />
          </div>
          <div className="space-y-3">
            {topStores.map((store) => (
              <div
                key={store.id}
                className="p-4 bg-gray-50 rounded-xl"
              >
                <div className="flex items-center justify-between mb-2">
                  <p className="font-semibold text-gray-900">{store.name}</p>
                  <div className="flex items-center gap-1">
                    <span className="text-yellow-500">★</span>
                    <span className="font-semibold text-sm">{store.rating}</span>
                  </div>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <p className="text-gray-500">{store.orders} orders</p>
                  <p className="font-bold text-indigo-700">{store.sales}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="px-6 mt-6 grid grid-cols-2 gap-3 pb-6">
        <button 
          onClick={() => navigate('/user-management')}
          className="bg-indigo-700 text-white py-4 rounded-xl font-semibold hover:bg-indigo-800 transition-all shadow-lg flex items-center justify-center gap-2"
        >
          <Users className="w-5 h-5" />
          Manage Users
        </button>
        <button 
          onClick={() => navigate('/store-management')}
          className="bg-purple-600 text-white py-4 rounded-xl font-semibold hover:bg-purple-700 transition-all shadow-lg flex items-center justify-center gap-2"
        >
          <Store className="w-5 h-5" />
          Manage Stores
        </button>
      </div>
    </div>
  );
}