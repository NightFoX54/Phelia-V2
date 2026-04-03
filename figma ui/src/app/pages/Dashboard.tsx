import { Plus, Eye, DollarSign, ShoppingCart, TrendingUp, Edit2, Trash2, MoreVertical } from "lucide-react";
import { useState } from "react";
import { Link } from "react-router";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

interface Product {
  id: string;
  name: string;
  price: number;
  image: string;
  views: number;
  sales: number;
  stock: number;
}

const salesData = [
  { name: "Mon", sales: 12 },
  { name: "Tue", sales: 19 },
  { name: "Wed", sales: 15 },
  { name: "Thu", sales: 25 },
  { name: "Fri", sales: 22 },
  { name: "Sat", sales: 30 },
  { name: "Sun", sales: 28 },
];

export function Dashboard() {
  const [products, setProducts] = useState<Product[]>([
    {
      id: "1",
      name: "Wireless Headphones Pro",
      price: 299.99,
      image: "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3aXJlbGVzcyUyMGhlYWRwaG9uZXMlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5NDYwMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      views: 1243,
      sales: 89,
      stock: 45,
    },
    {
      id: "2",
      name: "Smart Watch Series 5",
      price: 399.99,
      image: "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHdhdGNoJTIwdGVjaG5vbG9neSUyMHByb2R1Y3R8ZW58MXx8fHwxNzc0MTk5MzQxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      views: 2156,
      sales: 134,
      stock: 23,
    },
    {
      id: "3",
      name: "Laptop Pro 15\"",
      price: 1299.99,
      image: "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxsYXB0b3AlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5OTMzMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      views: 876,
      sales: 45,
      stock: 12,
    },
    {
      id: "4",
      name: "Smartphone X12 Pro",
      price: 999.99,
      image: "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwbW9iaWxlJTIwcGhvbmV8ZW58MXx8fHwxNzc0MTg1MzcxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      views: 3421,
      sales: 201,
      stock: 8,
    },
  ]);

  const totalViews = products.reduce((sum, p) => sum + p.views, 0);
  const totalSales = products.reduce((sum, p) => sum + p.sales, 0);
  const totalOrders = 151;
  const totalRevenue = products.reduce((sum, p) => sum + p.sales * p.price, 0);

  return (
    <div className="bg-gray-50 min-h-screen pb-20">
      {/* Header */}
      <header className="bg-gradient-to-br from-indigo-700 to-purple-700 px-6 pt-6 pb-8 rounded-b-3xl shadow-lg">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-xl font-bold text-white mb-1">Dashboard</h1>
            <p className="text-indigo-100 text-sm">Store Owner</p>
          </div>
          <button
            className="bg-white text-indigo-700 px-4 py-2 rounded-full font-semibold hover:bg-indigo-50 transition-colors flex items-center gap-2 shadow-lg"
          >
            <Plus className="w-5 h-5" />
            Add Product
          </button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <Eye className="w-5 h-5 text-white" />
              <p className="text-indigo-100 text-sm">Total Views</p>
            </div>
            <p className="text-2xl font-bold text-white">{totalViews.toLocaleString()}</p>
          </div>
          <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <DollarSign className="w-5 h-5 text-white" />
              <p className="text-indigo-100 text-sm">Total Sales</p>
            </div>
            <p className="text-2xl font-bold text-white">{totalSales}</p>
          </div>
          <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <ShoppingCart className="w-5 h-5 text-white" />
              <p className="text-indigo-100 text-sm">Total Orders</p>
            </div>
            <p className="text-2xl font-bold text-white">{totalOrders}</p>
          </div>
          <div className="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl p-4">
            <div className="flex items-center gap-2 mb-2">
              <TrendingUp className="w-5 h-5 text-white" />
              <p className="text-indigo-100 text-sm">Revenue</p>
            </div>
            <p className="text-2xl font-bold text-white">${(totalRevenue / 1000).toFixed(1)}k</p>
          </div>
        </div>
      </header>

      {/* Sales Chart */}
      <div className="px-6 mt-6">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h2 className="font-bold text-gray-900 mb-4">Sales This Week</h2>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={salesData} id="dashboard-sales-chart">
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} stroke="#9ca3af" />
              <YAxis tick={{ fontSize: 12 }} stroke="#9ca3af" />
              <Tooltip
                contentStyle={{
                  backgroundColor: "#ffffff",
                  border: "1px solid #e5e7eb",
                  borderRadius: "8px",
                  fontSize: "12px",
                }}
              />
              <Bar dataKey="sales" fill="#4338ca" radius={[8, 8, 0, 0]} isAnimationActive={false} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Product List */}
      <div className="px-6 mt-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-bold text-gray-900">Products</h2>
          <span className="text-sm text-gray-500">{products.length} items</span>
        </div>

        <div className="space-y-3">
          {products.map((product) => (
            <div
              key={product.id}
              className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100"
            >
              <div className="flex gap-4">
                {/* Product Image */}
                <div className="w-20 h-20 rounded-xl overflow-hidden bg-gray-100 flex-shrink-0">
                  <img
                    src={product.image}
                    alt={product.name}
                    className="w-full h-full object-cover"
                  />
                </div>

                {/* Product Info */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <h3 className="font-semibold text-gray-900 line-clamp-1">
                      {product.name}
                    </h3>
                    <button className="p-1 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors flex-shrink-0">
                      <MoreVertical className="w-5 h-5" />
                    </button>
                  </div>

                  <p className="text-lg font-bold text-indigo-700 mb-3">
                    ${product.price.toFixed(2)}
                  </p>

                  {/* Stats */}
                  <div className="grid grid-cols-3 gap-2 text-xs">
                    <div className="bg-blue-50 rounded-lg px-2 py-1.5">
                      <p className="text-blue-600 font-semibold">{product.views}</p>
                      <p className="text-blue-500">Views</p>
                    </div>
                    <div className="bg-green-50 rounded-lg px-2 py-1.5">
                      <p className="text-green-600 font-semibold">{product.sales}</p>
                      <p className="text-green-500">Sales</p>
                    </div>
                    <div className="bg-orange-50 rounded-lg px-2 py-1.5">
                      <p className="text-orange-600 font-semibold">{product.stock}</p>
                      <p className="text-orange-500">Stock</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="grid grid-cols-2 gap-2 mt-3">
                <button className="flex items-center justify-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-700 rounded-xl font-semibold hover:bg-indigo-100 transition-colors">
                  <Edit2 className="w-4 h-4" />
                  Edit
                </button>
                <button className="flex items-center justify-center gap-2 px-4 py-2 bg-red-50 text-red-500 rounded-xl font-semibold hover:bg-red-100 transition-colors">
                  <Trash2 className="w-4 h-4" />
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}