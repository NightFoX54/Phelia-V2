import { Search, Filter, Package, Clock, CheckCircle, XCircle } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router";

interface Order {
  id: string;
  orderNumber: string;
  customerName: string;
  items: number;
  total: number;
  status: "pending" | "processing" | "completed" | "cancelled";
  date: string;
  productImage: string;
}

const statusConfig = {
  pending: {
    label: "Pending",
    icon: Clock,
    bgColor: "bg-yellow-50",
    textColor: "text-yellow-700",
    iconColor: "text-yellow-600",
  },
  processing: {
    label: "Processing",
    icon: Package,
    bgColor: "bg-blue-50",
    textColor: "text-blue-700",
    iconColor: "text-blue-600",
  },
  completed: {
    label: "Completed",
    icon: CheckCircle,
    bgColor: "bg-green-50",
    textColor: "text-green-700",
    iconColor: "text-green-600",
  },
  cancelled: {
    label: "Cancelled",
    icon: XCircle,
    bgColor: "bg-red-50",
    textColor: "text-red-700",
    iconColor: "text-red-600",
  },
};

export function StoreOrders() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedFilter, setSelectedFilter] = useState<"all" | "pending" | "processing" | "completed" | "cancelled">("all");

  const [orders] = useState<Order[]>([
    {
      id: "1",
      orderNumber: "ORD-2024-001",
      customerName: "John Doe",
      items: 2,
      total: 699.98,
      status: "processing",
      date: "2024-03-20",
      productImage: "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3aXJlbGVzcyUyMGhlYWRwaG9uZXMlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5NDYwMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    },
    {
      id: "2",
      orderNumber: "ORD-2024-002",
      customerName: "Sarah Smith",
      items: 1,
      total: 1299.99,
      status: "completed",
      date: "2024-03-19",
      productImage: "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxsYXB0b3AlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5OTMzMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    },
    {
      id: "3",
      orderNumber: "ORD-2024-003",
      customerName: "Mike Johnson",
      items: 1,
      total: 399.99,
      status: "pending",
      date: "2024-03-21",
      productImage: "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHdhdGNoJTIwdGVjaG5vbG9neSUyMHByb2R1Y3R8ZW58MXx8fHwxNzc0MTk5MzQxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    },
    {
      id: "4",
      orderNumber: "ORD-2024-004",
      customerName: "Emily Davis",
      items: 3,
      total: 2599.97,
      status: "processing",
      date: "2024-03-21",
      productImage: "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwbW9iaWxlJTIwcGhvbmV8ZW58MXx8fHwxNzc0MTg1MzcxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    },
    {
      id: "5",
      orderNumber: "ORD-2024-005",
      customerName: "Tom Wilson",
      items: 1,
      total: 999.99,
      status: "cancelled",
      date: "2024-03-18",
      productImage: "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwbW9iaWxlJTIwcGhvbmV8ZW58MXx8fHwxNzc0MTg1MzcxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    },
    {
      id: "6",
      orderNumber: "ORD-2024-006",
      customerName: "Lisa Anderson",
      items: 2,
      total: 1699.98,
      status: "completed",
      date: "2024-03-17",
      productImage: "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxsYXB0b3AlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5OTMzMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    },
  ]);

  const filteredOrders = orders.filter((order) => {
    const matchesSearch =
      order.orderNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      order.customerName.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesFilter = selectedFilter === "all" || order.status === selectedFilter;
    return matchesSearch && matchesFilter;
  });

  const orderStats = {
    all: orders.length,
    pending: orders.filter((o) => o.status === "pending").length,
    processing: orders.filter((o) => o.status === "processing").length,
    completed: orders.filter((o) => o.status === "completed").length,
    cancelled: orders.filter((o) => o.status === "cancelled").length,
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-24">
      {/* Header */}
      <header className="bg-gradient-to-br from-indigo-700 to-purple-700 px-6 pt-6 pb-8 rounded-b-3xl shadow-lg sticky top-0 z-10">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-xl font-bold text-white mb-1">Orders</h1>
            <p className="text-indigo-100 text-sm">{orders.length} total orders</p>
          </div>
        </div>

        {/* Search Bar */}
        <div className="relative mb-4">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-indigo-300" />
          <input
            type="text"
            placeholder="Search orders..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-12 pr-4 py-3 bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl text-white placeholder-indigo-200 focus:outline-none focus:ring-2 focus:ring-white/50"
          />
        </div>

        {/* Filter Chips */}
        <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
          {(["all", "pending", "processing", "completed", "cancelled"] as const).map((filter) => (
            <button
              key={filter}
              onClick={() => setSelectedFilter(filter)}
              className={`px-4 py-2 rounded-full text-sm font-semibold whitespace-nowrap transition-all ${
                selectedFilter === filter
                  ? "bg-white text-indigo-700 shadow-lg"
                  : "bg-white/10 text-white border border-white/20 hover:bg-white/20"
              }`}
            >
              {filter === "all" ? "All" : statusConfig[filter].label} ({orderStats[filter]})
            </button>
          ))}
        </div>
      </header>

      {/* Orders List */}
      <div className="px-6 mt-6">
        {filteredOrders.length === 0 ? (
          <div className="text-center py-12">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Package className="w-10 h-10 text-gray-400" />
            </div>
            <h3 className="font-semibold text-gray-900 mb-2">No orders found</h3>
            <p className="text-gray-500 text-sm">
              {searchQuery ? "Try a different search term" : "Orders will appear here"}
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {filteredOrders.map((order) => {
              const config = statusConfig[order.status];
              const StatusIcon = config.icon;

              return (
                <div
                  key={order.id}
                  onClick={() => navigate(`/store-orders/${order.id}`)}
                  className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 cursor-pointer hover:shadow-md transition-shadow"
                >
                  <div className="flex gap-4">
                    {/* Product Image */}
                    <div className="w-20 h-20 rounded-xl overflow-hidden bg-gray-100 flex-shrink-0">
                      <img
                        src={order.productImage}
                        alt="Product"
                        className="w-full h-full object-cover"
                      />
                    </div>

                    {/* Order Info */}
                    <div className="flex-1 min-w-0">
                      <div className="mb-2">
                        <h3 className="font-semibold text-gray-900 mb-1">
                          {order.orderNumber}
                        </h3>
                        <p className="text-sm text-gray-500">{order.customerName}</p>
                      </div>

                      <div className="flex items-center justify-between mb-3">
                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 ${config.bgColor} ${config.textColor} text-xs font-semibold rounded-full`}>
                          <StatusIcon className={`w-3.5 h-3.5 ${config.iconColor}`} />
                          {config.label}
                        </span>
                        <p className="text-lg font-bold text-indigo-700">
                          ${order.total.toFixed(2)}
                        </p>
                      </div>

                      {/* Order Details */}
                      <div className="flex items-center justify-between text-xs text-gray-500">
                        <span>{order.items} item{order.items > 1 ? "s" : ""}</span>
                        <span>{new Date(order.date).toLocaleDateString("en-US", {
                          month: "short",
                          day: "numeric",
                          year: "numeric"
                        })}</span>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
