import { ChevronLeft, Package, MapPin, Phone, Mail, User } from "lucide-react";
import { useNavigate, useParams } from "react-router";

interface OrderItem {
  id: string;
  name: string;
  image: string;
  variant: string;
  quantity: number;
  price: number;
}

interface OrderDetail {
  id: string;
  orderNumber: string;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  status: "pending" | "processing" | "completed" | "cancelled";
  date: string;
  items: OrderItem[];
  subtotal: number;
  shipping: number;
  tax: number;
  total: number;
  shippingAddress: {
    street: string;
    city: string;
    state: string;
    zip: string;
  };
}

// Mock data for order details
const mockOrders: Record<string, OrderDetail> = {
  "1": {
    id: "1",
    orderNumber: "ORD-2024-001",
    customerName: "John Doe",
    customerEmail: "john.doe@email.com",
    customerPhone: "+1 (555) 123-4567",
    status: "processing",
    date: "2024-03-20",
    items: [
      {
        id: "1",
        name: "Wireless Headphones Pro",
        image: "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3aXJlbGVzcyUyMGhlYWRwaG9uZXMlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5NDYwMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
        variant: "Black, 256GB",
        quantity: 2,
        price: 299.99,
      },
    ],
    subtotal: 599.98,
    shipping: 15.00,
    tax: 85.00,
    total: 699.98,
    shippingAddress: {
      street: "123 Main Street, Apt 4B",
      city: "New York",
      state: "NY",
      zip: "10001",
    },
  },
  "2": {
    id: "2",
    orderNumber: "ORD-2024-002",
    customerName: "Sarah Smith",
    customerEmail: "sarah.smith@email.com",
    customerPhone: "+1 (555) 234-5678",
    status: "completed",
    date: "2024-03-19",
    items: [
      {
        id: "2",
        name: "Laptop Pro 15\"",
        image: "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxsYXB0b3AlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5OTMzMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
        variant: "Space Gray, 512GB",
        quantity: 1,
        price: 1299.99,
      },
    ],
    subtotal: 1299.99,
    shipping: 0.00,
    tax: 0.00,
    total: 1299.99,
    shippingAddress: {
      street: "456 Oak Avenue",
      city: "Los Angeles",
      state: "CA",
      zip: "90001",
    },
  },
  "3": {
    id: "3",
    orderNumber: "ORD-2024-003",
    customerName: "Mike Johnson",
    customerEmail: "mike.j@email.com",
    customerPhone: "+1 (555) 345-6789",
    status: "pending",
    date: "2024-03-21",
    items: [
      {
        id: "3",
        name: "Smart Watch Series 5",
        image: "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHdhdGNoJTIwdGVjaG5vbG9neSUyMHByb2R1Y3R8ZW58MXx8fHwxNzc0MTk5MzQxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
        variant: "Silver, 42mm",
        quantity: 1,
        price: 399.99,
      },
    ],
    subtotal: 399.99,
    shipping: 10.00,
    tax: 50.00,
    total: 459.99,
    shippingAddress: {
      street: "789 Pine Road",
      city: "Chicago",
      state: "IL",
      zip: "60601",
    },
  },
  "4": {
    id: "4",
    orderNumber: "ORD-2024-004",
    customerName: "Emily Davis",
    customerEmail: "emily.davis@email.com",
    customerPhone: "+1 (555) 456-7890",
    status: "processing",
    date: "2024-03-21",
    items: [
      {
        id: "4",
        name: "Smartphone X12 Pro",
        image: "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwbW9iaWxlJTIwcGhvbmV8ZW58MXx8fHwxNzc0MTg1MzcxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
        variant: "Midnight Blue, 256GB",
        quantity: 2,
        price: 999.99,
      },
      {
        id: "5",
        name: "Wireless Headphones Pro",
        image: "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3aXJlbGVzcyUyMGhlYWRwaG9uZXMlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5NDYwMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
        variant: "White",
        quantity: 1,
        price: 299.99,
      },
    ],
    subtotal: 2299.97,
    shipping: 20.00,
    tax: 280.00,
    total: 2599.97,
    shippingAddress: {
      street: "321 Elm Street",
      city: "Houston",
      state: "TX",
      zip: "77001",
    },
  },
  "5": {
    id: "5",
    orderNumber: "ORD-2024-005",
    customerName: "Tom Wilson",
    customerEmail: "tom.w@email.com",
    customerPhone: "+1 (555) 567-8901",
    status: "cancelled",
    date: "2024-03-18",
    items: [
      {
        id: "6",
        name: "Smartphone X12 Pro",
        image: "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwbW9iaWxlJTIwcGhvbmV8ZW58MXx8fHwxNzc0MTg1MzcxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
        variant: "Graphite, 512GB",
        quantity: 1,
        price: 999.99,
      },
    ],
    subtotal: 999.99,
    shipping: 15.00,
    tax: 100.00,
    total: 1114.99,
    shippingAddress: {
      street: "654 Maple Drive",
      city: "Phoenix",
      state: "AZ",
      zip: "85001",
    },
  },
  "6": {
    id: "6",
    orderNumber: "ORD-2024-006",
    customerName: "Lisa Anderson",
    customerEmail: "lisa.anderson@email.com",
    customerPhone: "+1 (555) 678-9012",
    status: "completed",
    date: "2024-03-17",
    items: [
      {
        id: "7",
        name: "Laptop Pro 15\"",
        image: "https://images.unsplash.com/photo-1516826435551-36a8a09e4526?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxsYXB0b3AlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5OTMzMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
        variant: "Silver, 1TB",
        quantity: 2,
        price: 1499.99,
      },
    ],
    subtotal: 2999.98,
    shipping: 0.00,
    tax: 350.00,
    total: 3349.98,
    shippingAddress: {
      street: "987 Cedar Lane",
      city: "Philadelphia",
      state: "PA",
      zip: "19101",
    },
  },
};

const statusConfig = {
  pending: {
    label: "Pending",
    bgColor: "bg-yellow-50",
    textColor: "text-yellow-700",
  },
  processing: {
    label: "Processing",
    bgColor: "bg-blue-50",
    textColor: "text-blue-700",
  },
  completed: {
    label: "Completed",
    bgColor: "bg-green-50",
    textColor: "text-green-700",
  },
  cancelled: {
    label: "Cancelled",
    bgColor: "bg-red-50",
    textColor: "text-red-700",
  },
};

export function StoreOrderDetail() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();

  const order = id ? mockOrders[id] : null;

  if (!order) {
    return (
      <div className="bg-gray-50 min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-xl font-bold text-gray-900 mb-2">Order not found</h2>
          <button
            onClick={() => navigate("/store-orders")}
            className="text-indigo-700 font-semibold"
          >
            Back to Orders
          </button>
        </div>
      </div>
    );
  }

  const config = statusConfig[order.status];

  return (
    <div className="bg-gray-50 min-h-screen pb-24">
      {/* Header */}
      <header className="bg-white px-6 py-4 shadow-sm sticky top-0 z-10">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate("/store-orders")}
            className="p-2 -ml-2 hover:bg-gray-100 rounded-xl transition-colors"
          >
            <ChevronLeft className="w-6 h-6" />
          </button>
          <div className="flex-1">
            <h1 className="font-bold text-gray-900">{order.orderNumber}</h1>
            <p className="text-sm text-gray-500">
              {new Date(order.date).toLocaleDateString("en-US", {
                month: "long",
                day: "numeric",
                year: "numeric",
              })}
            </p>
          </div>
          <span
            className={`px-3 py-1.5 ${config.bgColor} ${config.textColor} text-sm font-semibold rounded-full`}
          >
            {config.label}
          </span>
        </div>
      </header>

      <div className="px-6 py-6 space-y-4">
        {/* Customer Information */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
            <User className="w-5 h-5 text-indigo-700" />
            Customer Information
          </h2>
          <div className="space-y-3">
            <div className="flex items-start gap-3">
              <User className="w-4 h-4 text-gray-400 mt-0.5" />
              <div>
                <p className="text-sm text-gray-500">Name</p>
                <p className="font-semibold text-gray-900">{order.customerName}</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <Mail className="w-4 h-4 text-gray-400 mt-0.5" />
              <div>
                <p className="text-sm text-gray-500">Email</p>
                <p className="font-semibold text-gray-900">{order.customerEmail}</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <Phone className="w-4 h-4 text-gray-400 mt-0.5" />
              <div>
                <p className="text-sm text-gray-500">Phone</p>
                <p className="font-semibold text-gray-900">{order.customerPhone}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Shipping Address */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
            <MapPin className="w-5 h-5 text-indigo-700" />
            Shipping Address
          </h2>
          <div className="text-gray-700">
            <p>{order.shippingAddress.street}</p>
            <p>
              {order.shippingAddress.city}, {order.shippingAddress.state}{" "}
              {order.shippingAddress.zip}
            </p>
          </div>
        </div>

        {/* Order Items */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
            <Package className="w-5 h-5 text-indigo-700" />
            Order Items ({order.items.length})
          </h2>
          <div className="space-y-4">
            {order.items.map((item) => (
              <div key={item.id} className="flex gap-4">
                <div className="w-20 h-20 rounded-xl overflow-hidden bg-gray-100 flex-shrink-0">
                  <img
                    src={item.image}
                    alt={item.name}
                    className="w-full h-full object-cover"
                  />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-semibold text-gray-900 line-clamp-1 mb-1">
                    {item.name}
                  </h3>
                  <p className="text-sm text-gray-500 mb-2">{item.variant}</p>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-500">Qty: {item.quantity}</span>
                    <span className="font-bold text-indigo-700">
                      ${(item.price * item.quantity).toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Order Summary */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-bold text-gray-900 mb-4">Order Summary</h2>
          <div className="space-y-3">
            <div className="flex justify-between text-gray-700">
              <span>Subtotal</span>
              <span>${order.subtotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-gray-700">
              <span>Shipping</span>
              <span>${order.shipping.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-gray-700">
              <span>Tax</span>
              <span>${order.tax.toFixed(2)}</span>
            </div>
            <div className="border-t border-gray-200 pt-3 flex justify-between font-bold text-gray-900 text-lg">
              <span>Total</span>
              <span className="text-indigo-700">${order.total.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        {order.status === "pending" && (
          <div className="grid grid-cols-2 gap-3">
            <button className="bg-green-600 text-white py-3 rounded-xl font-semibold hover:bg-green-700 transition-colors">
              Accept Order
            </button>
            <button className="bg-red-600 text-white py-3 rounded-xl font-semibold hover:bg-red-700 transition-colors">
              Cancel Order
            </button>
          </div>
        )}

        {order.status === "processing" && (
          <button className="w-full bg-indigo-700 text-white py-3 rounded-xl font-semibold hover:bg-indigo-800 transition-colors">
            Mark as Completed
          </button>
        )}
      </div>
    </div>
  );
}
