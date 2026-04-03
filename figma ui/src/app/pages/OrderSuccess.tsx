import { Check, Package, MapPin, Calendar, ChevronRight } from "lucide-react";
import { Link } from "react-router";

export function OrderSuccess() {
  // Mock order data
  const orderNumber = "ORD-" + Math.random().toString(36).substring(2, 9).toUpperCase();
  const estimatedDelivery = new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'short',
    day: 'numeric'
  });

  const orderItems = [
    {
      id: "1",
      name: "Wireless Headphones Pro",
      price: 299.99,
      quantity: 1,
      variant: {
        color: "Midnight Black",
        storage: "Standard",
      },
    },
    {
      id: "4",
      name: "Smartphone X12 Pro",
      price: 999.99,
      quantity: 2,
      variant: {
        color: "Deep Purple",
        storage: "256GB",
      },
    },
  ];

  const subtotal = orderItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const shipping = 15.0;
  const tax = subtotal * 0.08;
  const total = subtotal + shipping + tax;

  return (
    <div className="bg-gray-50 min-h-screen flex flex-col">
      {/* Success Animation Section */}
      <div className="bg-gradient-to-b from-indigo-700 to-indigo-600 px-6 pt-12 pb-8 text-center">
        {/* Success Icon with Animation */}
        <div className="mb-6 flex justify-center">
          <div className="relative">
            {/* Outer ring */}
            <div className="absolute inset-0 bg-white/20 rounded-full animate-ping"></div>
            {/* Main circle */}
            <div className="relative w-24 h-24 bg-white rounded-full flex items-center justify-center shadow-lg">
              <Check className="w-12 h-12 text-indigo-700 stroke-[3]" />
            </div>
          </div>
        </div>

        {/* Success Message */}
        <h1 className="text-2xl font-bold text-white mb-2">
          Order Placed Successfully!
        </h1>
        <p className="text-indigo-100 mb-4">
          Thank you for your purchase
        </p>

        {/* Order Number */}
        <div className="inline-block bg-white/10 backdrop-blur-sm border border-white/20 rounded-full px-6 py-2">
          <p className="text-sm text-indigo-100">Order Number</p>
          <p className="font-bold text-white text-lg">{orderNumber}</p>
        </div>
      </div>

      {/* Content Section */}
      <div className="flex-1 px-6 py-6 space-y-4">
        {/* Delivery Information */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div className="flex items-start gap-4 mb-4">
            <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
              <Package className="w-6 h-6 text-green-600" />
            </div>
            <div>
              <h3 className="font-bold text-gray-900 mb-1">Estimated Delivery</h3>
              <p className="text-gray-600">{estimatedDelivery}</p>
            </div>
          </div>

          <div className="flex items-start gap-4">
            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
              <MapPin className="w-6 h-6 text-blue-600" />
            </div>
            <div>
              <h3 className="font-bold text-gray-900 mb-1">Delivering to</h3>
              <p className="text-gray-600">123 Main Street</p>
              <p className="text-gray-600">New York, NY 10001</p>
            </div>
          </div>
        </div>

        {/* Order Summary */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-bold text-gray-900 mb-4">Order Summary</h2>

          {/* Order Items */}
          <div className="space-y-3 mb-4 pb-4 border-b border-gray-200">
            {orderItems.map((item) => (
              <div key={item.id} className="flex justify-between items-start">
                <div className="flex-1">
                  <p className="font-semibold text-gray-900 text-sm">{item.name}</p>
                  {item.variant && (
                    <div className="flex gap-2 mt-1">
                      {item.variant.color && (
                        <span className="text-xs text-gray-500">{item.variant.color}</span>
                      )}
                      {item.variant.storage && (
                        <span className="text-xs text-gray-500">• {item.variant.storage}</span>
                      )}
                    </div>
                  )}
                  <p className="text-xs text-gray-500 mt-1">Qty: {item.quantity}</p>
                </div>
                <p className="font-semibold text-gray-900">
                  ${(item.price * item.quantity).toFixed(2)}
                </p>
              </div>
            ))}
          </div>

          {/* Price Breakdown */}
          <div className="space-y-3">
            <div className="flex justify-between text-gray-600">
              <span>Subtotal</span>
              <span>${subtotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Shipping</span>
              <span>${shipping.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Tax (8%)</span>
              <span>${tax.toFixed(2)}</span>
            </div>
            <div className="h-px bg-gray-200"></div>
            <div className="flex justify-between font-bold text-gray-900 text-lg">
              <span>Total Paid</span>
              <span>${total.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Confirmation Message */}
        <div className="bg-indigo-50 rounded-2xl p-6 border border-indigo-100">
          <div className="flex items-start gap-3">
            <Calendar className="w-5 h-5 text-indigo-700 flex-shrink-0 mt-0.5" />
            <div>
              <p className="text-sm text-gray-700">
                A confirmation email has been sent to your email address with order details and tracking information.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Actions */}
      <div className="px-6 py-4 space-y-3 bg-white border-t border-gray-200">
        <Link
          to="/profile"
          className="w-full border-2 border-indigo-700 text-indigo-700 py-4 rounded-full font-bold hover:bg-indigo-50 transition-colors flex items-center justify-center gap-2"
        >
          Track Order
          <ChevronRight className="w-5 h-5" />
        </Link>
        <Link
          to="/"
          className="w-full bg-indigo-700 text-white py-4 rounded-full font-bold hover:bg-indigo-800 transition-colors shadow-md flex items-center justify-center gap-2"
        >
          Continue Shopping
        </Link>
      </div>
    </div>
  );
}
