import { ArrowLeft, MapPin, CreditCard, Check, ChevronRight } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router";

interface CheckoutItem {
  id: string;
  name: string;
  price: number;
  quantity: number;
  variant?: {
    color?: string;
    storage?: string;
  };
}

export function Checkout() {
  const [selectedAddress, setSelectedAddress] = useState(0);
  const [selectedPayment, setSelectedPayment] = useState(0);

  // Mock cart items for checkout
  const checkoutItems: CheckoutItem[] = [
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

  const addresses = [
    {
      id: "1",
      name: "Home",
      address: "123 Main Street",
      city: "New York, NY 10001",
      phone: "+1 (555) 123-4567",
    },
    {
      id: "2",
      name: "Office",
      address: "456 Business Ave",
      city: "New York, NY 10002",
      phone: "+1 (555) 987-6543",
    },
  ];

  const paymentMethods = [
    {
      id: "1",
      type: "Credit Card",
      name: "Visa ending in 4242",
      icon: "💳",
    },
    {
      id: "2",
      type: "Credit Card",
      name: "Mastercard ending in 8888",
      icon: "💳",
    },
    {
      id: "3",
      type: "Digital Wallet",
      name: "Apple Pay",
      icon: "📱",
    },
  ];

  const subtotal = checkoutItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const shipping = 15.0;
  const tax = subtotal * 0.08;
  const total = subtotal + shipping + tax;

  const navigate = useNavigate();

  return (
    <div className="bg-gray-50 min-h-screen pb-32">
      {/* Header */}
      <header className="bg-white px-6 py-4 shadow-sm sticky top-0 z-10">
        <div className="flex items-center gap-4">
          <Link to="/cart" className="p-2 -ml-2 hover:bg-gray-100 rounded-full transition-colors">
            <ArrowLeft className="w-6 h-6 text-gray-700" />
          </Link>
          <h1 className="text-xl font-bold text-gray-900">Checkout</h1>
        </div>
      </header>

      <div className="px-6 py-4 space-y-4">
        {/* Shipping Address Section */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center">
              <MapPin className="w-5 h-5 text-indigo-700" />
            </div>
            <h2 className="font-bold text-gray-900">Shipping Address</h2>
          </div>

          <div className="space-y-3">
            {addresses.map((address, index) => (
              <button
                key={address.id}
                onClick={() => setSelectedAddress(index)}
                className={`w-full text-left p-4 rounded-xl border-2 transition-all ${
                  selectedAddress === index
                    ? "border-indigo-700 bg-indigo-50"
                    : "border-gray-200 bg-white hover:border-gray-300"
                }`}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="font-semibold text-gray-900">{address.name}</span>
                      {selectedAddress === index && (
                        <div className="w-5 h-5 bg-indigo-700 rounded-full flex items-center justify-center">
                          <Check className="w-3 h-3 text-white" />
                        </div>
                      )}
                    </div>
                    <p className="text-sm text-gray-600">{address.address}</p>
                    <p className="text-sm text-gray-600">{address.city}</p>
                    <p className="text-sm text-gray-500 mt-1">{address.phone}</p>
                  </div>
                </div>
              </button>
            ))}

            <button className="w-full p-4 rounded-xl border-2 border-dashed border-gray-300 text-indigo-700 font-semibold hover:border-indigo-700 hover:bg-indigo-50 transition-all flex items-center justify-center gap-2">
              <span className="text-xl">+</span>
              Add New Address
            </button>
          </div>
        </div>

        {/* Payment Method Section */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center">
              <CreditCard className="w-5 h-5 text-indigo-700" />
            </div>
            <h2 className="font-bold text-gray-900">Payment Method</h2>
          </div>

          <div className="space-y-3">
            {paymentMethods.map((payment, index) => (
              <button
                key={payment.id}
                onClick={() => setSelectedPayment(index)}
                className={`w-full text-left p-4 rounded-xl border-2 transition-all flex items-center justify-between ${
                  selectedPayment === index
                    ? "border-indigo-700 bg-indigo-50"
                    : "border-gray-200 bg-white hover:border-gray-300"
                }`}
              >
                <div className="flex items-center gap-3">
                  <span className="text-2xl">{payment.icon}</span>
                  <div>
                    <p className="text-sm text-gray-500">{payment.type}</p>
                    <p className="font-semibold text-gray-900">{payment.name}</p>
                  </div>
                </div>
                {selectedPayment === index && (
                  <div className="w-5 h-5 bg-indigo-700 rounded-full flex items-center justify-center">
                    <Check className="w-3 h-3 text-white" />
                  </div>
                )}
              </button>
            ))}

            <button className="w-full p-4 rounded-xl border-2 border-dashed border-gray-300 text-indigo-700 font-semibold hover:border-indigo-700 hover:bg-indigo-50 transition-all flex items-center justify-center gap-2">
              <span className="text-xl">+</span>
              Add New Card
            </button>
          </div>
        </div>

        {/* Order Summary Section */}
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h2 className="font-bold text-gray-900 mb-4">Order Summary</h2>

          {/* Order Items */}
          <div className="space-y-3 mb-4 pb-4 border-b border-gray-200">
            {checkoutItems.map((item) => (
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
              <span>Total</span>
              <span>${total.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Terms and Conditions */}
        <div className="px-2">
          <p className="text-xs text-gray-500 text-center">
            By placing your order, you agree to our{" "}
            <button className="text-indigo-700 font-semibold">Terms & Conditions</button> and{" "}
            <button className="text-indigo-700 font-semibold">Privacy Policy</button>
          </p>
        </div>
      </div>

      {/* Fixed Bottom Confirm Order Button */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-6 py-4 shadow-lg max-w-md mx-auto">
        <button
          className="w-full bg-indigo-700 text-white py-4 rounded-full font-bold hover:bg-indigo-800 transition-colors shadow-md flex items-center justify-center gap-2"
          onClick={() => navigate("/order-success")}
        >
          Confirm Order - ${total.toFixed(2)}
          <ChevronRight className="w-5 h-5" />
        </button>
      </div>
    </div>
  );
}