import { Search, Bell } from "lucide-react";
import { ProductCard } from "../components/ProductCard";
import { Link } from "react-router";

const products = [
  {
    id: "1",
    name: "Wireless Headphones Pro",
    price: 299.99,
    image: "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3aXJlbGVzcyUyMGhlYWRwaG9uZXMlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5NDYwMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    rating: 4.8,
  },
  {
    id: "2",
    name: "Smart Watch Series 5",
    price: 399.99,
    image: "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHdhdGNoJTIwdGVjaG5vbG9neSUyMHByb2R1Y3R8ZW58MXx8fHwxNzc0MTk5MzQxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    rating: 4.9,
  },
  {
    id: "3",
    name: "Ultra Laptop Pro 15",
    price: 1299.99,
    image: "https://images.unsplash.com/photo-1759668358660-0d06064f0f84?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxsYXB0b3AlMjBjb21wdXRlciUyMG1vZGVybnxlbnwxfHx8fDE3NzQxNTM1MzF8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    rating: 4.7,
  },
  {
    id: "4",
    name: "Smartphone X12 Pro",
    price: 999.99,
    image: "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwbW9iaWxlJTIwcGhvbmV8ZW58MXx8fHwxNzc0MTg1MzcxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    rating: 4.6,
  },
  {
    id: "5",
    name: "Professional Camera Kit",
    price: 1899.99,
    image: "https://images.unsplash.com/photo-1729655669048-a667a0b01148?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjYW1lcmElMjBwaG90b2dyYXBoeSUyMGVxdWlwbWVudHxlbnwxfHx8fDE3NzQxNzEwNzF8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    rating: 4.9,
  },
  {
    id: "6",
    name: "Tablet Pro 12.9",
    price: 799.99,
    image: "https://images.unsplash.com/photo-1769603795371-ad63bd85d524?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0YWJsZXQlMjBkZXZpY2UlMjBlbGVjdHJvbmljfGVufDF8fHx8MTc3NDEzOTEyMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    rating: 4.8,
  },
];

const categories = [
  { name: "All", active: true },
  { name: "Phones", active: false },
  { name: "Laptops", active: false },
  { name: "Audio", active: false },
  { name: "Watches", active: false },
];

export function Home() {
  return (
    <div className="bg-gray-50 min-h-screen">
      {/* Header */}
      <header className="bg-white px-6 pt-6 pb-4 shadow-sm">
        <div className="flex items-center justify-between mb-6">
          <div>
            <p className="text-sm text-gray-500">Welcome back,</p>
            <h1 className="text-xl font-bold text-gray-900">John Doe</h1>
          </div>
          <button className="p-2 bg-gray-100 rounded-full hover:bg-gray-200 transition-colors">
            <Bell className="w-6 h-6 text-gray-700" />
          </button>
        </div>

        {/* Search Bar */}
        <div className="relative">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            placeholder="Search products..."
            className="w-full pl-12 pr-4 py-3 bg-gray-100 rounded-xl border-none focus:outline-none focus:ring-2 focus:ring-indigo-300 transition-all"
          />
        </div>
      </header>

      {/* Categories */}
      <div className="px-6 py-4 bg-white">
        <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
          {categories.map((category) => (
            <button
              key={category.name}
              className={`px-6 py-2 rounded-full whitespace-nowrap transition-all ${
                category.active
                  ? "bg-indigo-700 text-white shadow-md"
                  : "bg-gray-100 text-gray-600 hover:bg-gray-200"
              }`}
            >
              {category.name}
            </button>
          ))}
        </div>
      </div>

      {/* Featured Banner */}
      <div className="px-6 py-4">
        <div className="bg-gradient-to-br from-indigo-700 to-purple-700 rounded-2xl p-6 text-white shadow-lg">
          <p className="text-sm opacity-90 mb-1">Special Offer</p>
          <h2 className="text-2xl font-bold mb-2">Up to 50% OFF</h2>
          <p className="text-sm opacity-90 mb-4">On selected items this week</p>
          <button className="bg-white text-indigo-700 px-6 py-2 rounded-full font-semibold hover:bg-gray-100 transition-colors">
            Shop Now
          </button>
        </div>
      </div>

      {/* Products Grid */}
      <div className="px-6 pb-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-bold text-gray-900">Featured Products</h2>
          <Link to="/products" className="text-sm text-indigo-700 font-semibold">
            See All
          </Link>
        </div>
        <div className="grid grid-cols-2 gap-4">
          {products.map((product) => (
            <ProductCard key={product.id} {...product} />
          ))}
        </div>
      </div>
    </div>
  );
}