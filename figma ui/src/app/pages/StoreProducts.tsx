import { Plus, Edit2, Trash2, MoreVertical, Search, Filter, Package } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router";

interface Product {
  id: string;
  name: string;
  price: number;
  image: string;
  views: number;
  sales: number;
  stock: number;
  category: string;
}

export function StoreProducts() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const [products, setProducts] = useState<Product[]>([
    {
      id: "1",
      name: "Wireless Headphones Pro",
      price: 299.99,
      category: "Electronics",
      image: "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3aXJlbGVzcyUyMGhlYWRwaG9uZXMlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5NDYwMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      views: 1243,
      sales: 89,
      stock: 45,
    },
    {
      id: "2",
      name: "Smart Watch Series 5",
      price: 399.99,
      category: "Electronics",
      image: "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHdhdGNoJTIwdGVjaG5vbG9neSUyMHByb2R1Y3R8ZW58MXx8fHwxNzc0MTk5MzQxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      views: 2156,
      sales: 134,
      stock: 23,
    },
    {
      id: "3",
      name: "Laptop Pro 15\"",
      price: 1299.99,
      category: "Electronics",
      image: "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxsYXB0b3AlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5OTMzMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      views: 876,
      sales: 45,
      stock: 12,
    },
    {
      id: "4",
      name: "Smartphone X12 Pro",
      price: 999.99,
      category: "Electronics",
      image: "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwbW9iaWxlJTIwcGhvbmV8ZW58MXx8fHwxNzc0MTg1MzcxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      views: 3421,
      sales: 201,
      stock: 8,
    },
  ]);

  const filteredProducts = products.filter((product) =>
    product.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleDelete = (id: string) => {
    setProducts(products.filter((p) => p.id !== id));
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-24">
      {/* Header */}
      <header className="bg-gradient-to-br from-indigo-700 to-purple-700 px-6 pt-6 pb-8 rounded-b-3xl shadow-lg sticky top-0 z-10">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-xl font-bold text-white mb-1">My Products</h1>
            <p className="text-indigo-100 text-sm">{products.length} products</p>
          </div>
          <button
            onClick={() => navigate("/product-form")}
            className="bg-white text-indigo-700 px-4 py-2 rounded-full font-semibold hover:bg-indigo-50 transition-colors flex items-center gap-2 shadow-lg"
          >
            <Plus className="w-5 h-5" />
            Add
          </button>
        </div>

        {/* Search Bar */}
        <div className="relative">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-indigo-300" />
          <input
            type="text"
            placeholder="Search products..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-12 pr-12 py-3 bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl text-white placeholder-indigo-200 focus:outline-none focus:ring-2 focus:ring-white/50"
          />
          <button className="absolute right-4 top-1/2 -translate-y-1/2 text-white hover:bg-white/10 p-1.5 rounded-lg transition-colors">
            <Filter className="w-5 h-5" />
          </button>
        </div>
      </header>

      {/* Products List */}
      <div className="px-6 mt-6">
        {filteredProducts.length === 0 ? (
          <div className="text-center py-12">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Package className="w-10 h-10 text-gray-400" />
            </div>
            <h3 className="font-semibold text-gray-900 mb-2">No products found</h3>
            <p className="text-gray-500 text-sm mb-6">
              {searchQuery ? "Try a different search term" : "Start by adding your first product"}
            </p>
            {!searchQuery && (
              <button
                onClick={() => navigate("/product-form")}
                className="px-6 py-3 bg-indigo-700 text-white rounded-xl font-semibold hover:bg-indigo-800 transition-colors inline-flex items-center gap-2"
              >
                <Plus className="w-5 h-5" />
                Add Product
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-3">
            {filteredProducts.map((product) => (
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
                      <div>
                        <h3 className="font-semibold text-gray-900 line-clamp-1 mb-1">
                          {product.name}
                        </h3>
                        <span className="inline-block px-2 py-0.5 bg-indigo-50 text-indigo-700 text-xs font-medium rounded">
                          {product.category}
                        </span>
                      </div>
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
                  <button
                    onClick={() => navigate(`/product-form/${product.id}`)}
                    className="flex items-center justify-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-700 rounded-xl font-semibold hover:bg-indigo-100 transition-colors"
                  >
                    <Edit2 className="w-4 h-4" />
                    Edit
                  </button>
                  <button
                    onClick={() => handleDelete(product.id)}
                    className="flex items-center justify-center gap-2 px-4 py-2 bg-red-50 text-red-500 rounded-xl font-semibold hover:bg-red-100 transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Floating Action Button */}
      <button
        onClick={() => navigate("/product-form")}
        className="fixed bottom-24 right-6 w-14 h-14 bg-indigo-700 rounded-full shadow-lg flex items-center justify-center text-white hover:bg-indigo-800 transition-all hover:scale-110 z-10"
      >
        <Plus className="w-6 h-6" />
      </button>
    </div>
  );
}
