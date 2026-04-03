import { useState } from "react";
import { ChevronLeft, Search, Store, Star, Eye, Ban } from "lucide-react";
import { useNavigate } from "react-router";

interface StoreData {
  id: string;
  name: string;
  ownerName: string;
  ownerEmail: string;
  rating: number;
  totalSales: string;
  totalProducts: number;
  totalOrders: number;
  status: "active" | "disabled";
  joinDate: string;
}

const initialStores: StoreData[] = [
  {
    id: "1",
    name: "TechStore Pro",
    ownerName: "Store Owner",
    ownerEmail: "store@test.com",
    rating: 4.9,
    totalSales: "$2,548,000",
    totalProducts: 145,
    totalOrders: 1250,
    status: "active",
    joinDate: "2023-06-15",
  },
  {
    id: "2",
    name: "Gadget Hub",
    ownerName: "Mike Anderson",
    ownerEmail: "mike@store.com",
    rating: 4.8,
    totalSales: "$1,823,500",
    totalProducts: 98,
    totalOrders: 890,
    status: "active",
    joinDate: "2023-08-20",
  },
  {
    id: "3",
    name: "ElectroWorld",
    ownerName: "Sarah Martinez",
    ownerEmail: "sarah@electroworld.com",
    rating: 4.7,
    totalSales: "$1,567,200",
    totalProducts: 112,
    totalOrders: 720,
    status: "active",
    joinDate: "2023-07-10",
  },
  {
    id: "4",
    name: "TechMart",
    ownerName: "David Lee",
    ownerEmail: "contact@techmart.com",
    rating: 4.5,
    totalSales: "$987,400",
    totalProducts: 76,
    totalOrders: 450,
    status: "active",
    joinDate: "2023-09-05",
  },
  {
    id: "5",
    name: "SmartDevices Co",
    ownerName: "Emma Wilson",
    ownerEmail: "emma@smartdevices.com",
    rating: 3.8,
    totalSales: "$245,600",
    totalProducts: 34,
    totalOrders: 120,
    status: "disabled",
    joinDate: "2024-01-15",
  },
];

export function StoreManagement() {
  const navigate = useNavigate();
  const [stores, setStores] = useState<StoreData[]>(initialStores);
  const [searchQuery, setSearchQuery] = useState("");

  const filteredStores = stores.filter(
    (store) =>
      store.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      store.ownerName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      store.ownerEmail.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const toggleStoreStatus = (storeId: string) => {
    const store = stores.find((s) => s.id === storeId);
    const action = store?.status === "active" ? "disable" : "enable";
    
    if (window.confirm(`Are you sure you want to ${action} this store?`)) {
      setStores(
        stores.map((s) =>
          s.id === storeId
            ? { ...s, status: s.status === "active" ? "disabled" : "active" }
            : s
        )
      );
    }
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-20">
      {/* Header */}
      <header className="bg-white px-6 py-4 shadow-sm sticky top-0 z-10">
        <div className="flex items-center gap-4 mb-4">
          <button
            onClick={() => navigate(-1)}
            className="p-2 -ml-2 hover:bg-gray-100 rounded-xl transition-colors"
          >
            <ChevronLeft className="w-6 h-6" />
          </button>
          <div>
            <h1 className="font-bold text-gray-900">Store Management</h1>
            <p className="text-sm text-gray-500">{filteredStores.length} stores</p>
          </div>
        </div>

        {/* Search Bar */}
        <div className="relative">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            placeholder="Search stores..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-12 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent"
          />
        </div>
      </header>

      {/* Store List */}
      <div className="px-6 py-6 space-y-3">
        {filteredStores.map((store) => (
          <div
            key={store.id}
            className={`bg-white rounded-2xl p-5 shadow-sm border ${
              store.status === "disabled"
                ? "border-red-200 bg-red-50/30"
                : "border-gray-100"
            }`}
          >
            {/* Store Header */}
            <div className="flex items-start gap-3 mb-4">
              <div className="w-14 h-14 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center flex-shrink-0">
                <Store className="w-7 h-7 text-white" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2 mb-1">
                  <h3 className="font-bold text-gray-900">{store.name}</h3>
                  {store.status === "disabled" && (
                    <span className="px-2 py-1 bg-red-100 text-red-700 rounded-lg text-xs font-semibold">
                      Disabled
                    </span>
                  )}
                </div>
                <p className="text-sm text-gray-600 mb-1">{store.ownerName}</p>
                <p className="text-xs text-gray-500">{store.ownerEmail}</p>
              </div>
            </div>

            {/* Rating */}
            <div className="flex items-center gap-2 mb-4">
              <div className="flex items-center gap-1">
                {[...Array(5)].map((_, i) => (
                  <Star
                    key={i}
                    className={`w-4 h-4 ${
                      i < Math.floor(store.rating)
                        ? "fill-yellow-400 text-yellow-400"
                        : "text-gray-300"
                    }`}
                  />
                ))}
              </div>
              <span className="font-semibold text-sm">{store.rating}</span>
              <span className="text-xs text-gray-400">
                ({store.totalOrders} orders)
              </span>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-3 gap-2 mb-4">
              <div className="bg-blue-50 rounded-lg p-3 text-center">
                <p className="text-xs text-blue-600 mb-1">Products</p>
                <p className="font-bold text-blue-700">{store.totalProducts}</p>
              </div>
              <div className="bg-green-50 rounded-lg p-3 text-center">
                <p className="text-xs text-green-600 mb-1">Orders</p>
                <p className="font-bold text-green-700">
                  {store.totalOrders.toLocaleString()}
                </p>
              </div>
              <div className="bg-purple-50 rounded-lg p-3 text-center">
                <p className="text-xs text-purple-600 mb-1">Sales</p>
                <p className="font-bold text-purple-700 text-sm">
                  {store.totalSales}
                </p>
              </div>
            </div>

            {/* Join Date */}
            <p className="text-xs text-gray-400 mb-4">
              Joined {new Date(store.joinDate).toLocaleDateString("en-US", {
                year: "numeric",
                month: "long",
                day: "numeric",
              })}
            </p>

            {/* Action Buttons */}
            <div className="grid grid-cols-2 gap-2">
              <button 
                onClick={() => navigate(`/store-detail/${store.id}`)}
                className="flex items-center justify-center gap-2 px-4 py-3 bg-indigo-50 text-indigo-700 rounded-xl font-semibold hover:bg-indigo-100 transition-colors"
              >
                <Eye className="w-4 h-4" />
                View Store
              </button>
              <button
                onClick={() => toggleStoreStatus(store.id)}
                className={`flex items-center justify-center gap-2 px-4 py-3 rounded-xl font-semibold transition-colors ${
                  store.status === "active"
                    ? "bg-red-50 text-red-600 hover:bg-red-100"
                    : "bg-green-50 text-green-600 hover:bg-green-100"
                }`}
              >
                <Ban className="w-4 h-4" />
                {store.status === "active" ? "Disable" : "Enable"}
              </button>
            </div>
          </div>
        ))}

        {filteredStores.length === 0 && (
          <div className="text-center py-12">
            <Store className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500">No stores found</p>
          </div>
        )}
      </div>
    </div>
  );
}