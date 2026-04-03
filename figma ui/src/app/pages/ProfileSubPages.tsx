import { useState } from "react";
import {
  ArrowLeft,
  User,
  Mail,
  Lock,
  Eye,
  EyeOff,
  MapPin,
  Plus,
  Pencil,
  Trash2,
  CreditCard,
  Bell,
  Heart,
  Star,
  Package,
  ChevronRight,
  Check,
  Camera,
  Shield,
  Tag,
  ShoppingBag,
  Truck,
  RotateCcw,
} from "lucide-react";
import { useNavigate } from "react-router";
import { useAuth } from "../context/AuthContext";

// ─── Shared back header ────────────────────────────────────────────────────
function SubPageHeader({ title }: { title: string }) {
  const navigate = useNavigate();
  return (
    <header className="bg-white border-b border-gray-100 px-4 py-4 flex items-center gap-3 sticky top-0 z-10 shadow-sm">
      <button
        onClick={() => navigate("/profile")}
        className="p-2 rounded-full hover:bg-gray-100 transition-colors"
      >
        <ArrowLeft className="w-5 h-5 text-gray-700" />
      </button>
      <h1 className="text-lg font-bold text-gray-900">{title}</h1>
    </header>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. EDIT PROFILE
// ─────────────────────────────────────────────────────────────────────────────
export function EditProfile() {
  const { user } = useAuth();
  const [name, setName] = useState(user?.name || "John Doe");
  const [email, setEmail] = useState(user?.email || "user@test.com");
  const [currentPw, setCurrentPw] = useState("");
  const [newPw, setNewPw] = useState("");
  const [confirmPw, setConfirmPw] = useState("");
  const [showCurrentPw, setShowCurrentPw] = useState(false);
  const [showNewPw, setShowNewPw] = useState(false);
  const [showConfirmPw, setShowConfirmPw] = useState(false);
  const [saved, setSaved] = useState(false);

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-10">
      <SubPageHeader title="Edit Profile" />

      {/* Avatar */}
      <div className="flex flex-col items-center py-8">
        <div className="relative">
          <div className="w-24 h-24 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-lg">
            <span className="text-3xl font-bold text-white">
              {name.charAt(0).toUpperCase()}
            </span>
          </div>
          <button className="absolute bottom-0 right-0 w-8 h-8 bg-indigo-700 rounded-full flex items-center justify-center shadow-md">
            <Camera className="w-4 h-4 text-white" />
          </button>
        </div>
        <p className="mt-3 text-sm text-gray-500">Tap to change photo</p>
      </div>

      {/* Personal Info */}
      <div className="px-4 mb-5">
        <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest mb-3 px-1">
          Personal Information
        </p>
        <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
          {/* Name */}
          <div className="px-4 py-3 border-b border-gray-100">
            <label className="text-xs font-medium text-gray-400 mb-1 block">
              Full Name
            </label>
            <div className="flex items-center gap-3">
              <User className="w-4 h-4 text-indigo-500 shrink-0" />
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="flex-1 text-gray-900 bg-transparent outline-none placeholder-gray-400"
                placeholder="Your full name"
              />
            </div>
          </div>
          {/* Email */}
          <div className="px-4 py-3">
            <label className="text-xs font-medium text-gray-400 mb-1 block">
              Email Address
            </label>
            <div className="flex items-center gap-3">
              <Mail className="w-4 h-4 text-indigo-500 shrink-0" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="flex-1 text-gray-900 bg-transparent outline-none placeholder-gray-400"
                placeholder="your@email.com"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Change Password */}
      <div className="px-4 mb-6">
        <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest mb-3 px-1">
          Change Password
        </p>
        <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
          {/* Current Password */}
          <div className="px-4 py-3 border-b border-gray-100">
            <label className="text-xs font-medium text-gray-400 mb-1 block">
              Current Password
            </label>
            <div className="flex items-center gap-3">
              <Lock className="w-4 h-4 text-indigo-500 shrink-0" />
              <input
                type={showCurrentPw ? "text" : "password"}
                value={currentPw}
                onChange={(e) => setCurrentPw(e.target.value)}
                className="flex-1 text-gray-900 bg-transparent outline-none placeholder-gray-400"
                placeholder="••••••••"
              />
              <button onClick={() => setShowCurrentPw(!showCurrentPw)}>
                {showCurrentPw ? (
                  <EyeOff className="w-4 h-4 text-gray-400" />
                ) : (
                  <Eye className="w-4 h-4 text-gray-400" />
                )}
              </button>
            </div>
          </div>
          {/* New Password */}
          <div className="px-4 py-3 border-b border-gray-100">
            <label className="text-xs font-medium text-gray-400 mb-1 block">
              New Password
            </label>
            <div className="flex items-center gap-3">
              <Lock className="w-4 h-4 text-indigo-500 shrink-0" />
              <input
                type={showNewPw ? "text" : "password"}
                value={newPw}
                onChange={(e) => setNewPw(e.target.value)}
                className="flex-1 text-gray-900 bg-transparent outline-none placeholder-gray-400"
                placeholder="••••••••"
              />
              <button onClick={() => setShowNewPw(!showNewPw)}>
                {showNewPw ? (
                  <EyeOff className="w-4 h-4 text-gray-400" />
                ) : (
                  <Eye className="w-4 h-4 text-gray-400" />
                )}
              </button>
            </div>
          </div>
          {/* Confirm Password */}
          <div className="px-4 py-3">
            <label className="text-xs font-medium text-gray-400 mb-1 block">
              Confirm New Password
            </label>
            <div className="flex items-center gap-3">
              <Lock className="w-4 h-4 text-indigo-500 shrink-0" />
              <input
                type={showConfirmPw ? "text" : "password"}
                value={confirmPw}
                onChange={(e) => setConfirmPw(e.target.value)}
                className="flex-1 text-gray-900 bg-transparent outline-none placeholder-gray-400"
                placeholder="••••••••"
              />
              <button onClick={() => setShowConfirmPw(!showConfirmPw)}>
                {showConfirmPw ? (
                  <EyeOff className="w-4 h-4 text-gray-400" />
                ) : (
                  <Eye className="w-4 h-4 text-gray-400" />
                )}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Save Button */}
      <div className="px-4">
        <button
          onClick={handleSave}
          className={`w-full py-4 rounded-2xl font-bold text-white transition-all shadow-md flex items-center justify-center gap-2 ${
            saved
              ? "bg-green-500"
              : "bg-indigo-700 hover:bg-indigo-800 active:scale-95"
          }`}
        >
          {saved ? (
            <>
              <Check className="w-5 h-5" /> Saved!
            </>
          ) : (
            "Save Changes"
          )}
        </button>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. SHIPPING ADDRESS
// ─────────────────────────────────────────────────────────────────────────────
const dummyAddresses = [
  {
    id: 1,
    label: "Home",
    name: "John Doe",
    line1: "123 Maple Street",
    line2: "Apt 4B",
    city: "New York",
    state: "NY",
    zip: "10001",
    country: "United States",
    isDefault: true,
  },
  {
    id: 2,
    label: "Work",
    name: "John Doe",
    line1: "456 Tech Avenue",
    line2: "Floor 12",
    city: "San Francisco",
    state: "CA",
    zip: "94107",
    country: "United States",
    isDefault: false,
  },
];

export function ShippingAddress() {
  const [addresses, setAddresses] = useState(dummyAddresses);
  const [showForm, setShowForm] = useState(false);
  const [newLabel, setNewLabel] = useState("");
  const [newLine1, setNewLine1] = useState("");
  const [newCity, setNewCity] = useState("");
  const [newState, setNewState] = useState("");
  const [newZip, setNewZip] = useState("");

  const handleDelete = (id: number) => {
    setAddresses((prev) => prev.filter((a) => a.id !== id));
  };

  const handleSetDefault = (id: number) => {
    setAddresses((prev) =>
      prev.map((a) => ({ ...a, isDefault: a.id === id }))
    );
  };

  const handleAdd = () => {
    if (!newLabel || !newLine1 || !newCity) return;
    setAddresses((prev) => [
      ...prev,
      {
        id: Date.now(),
        label: newLabel,
        name: "John Doe",
        line1: newLine1,
        line2: "",
        city: newCity,
        state: newState,
        zip: newZip,
        country: "United States",
        isDefault: false,
      },
    ]);
    setShowForm(false);
    setNewLabel("");
    setNewLine1("");
    setNewCity("");
    setNewState("");
    setNewZip("");
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-10">
      <SubPageHeader title="Shipping Address" />

      <div className="px-4 py-5 space-y-4">
        {addresses.map((addr) => (
          <div
            key={addr.id}
            className="bg-white rounded-2xl shadow-sm p-4 relative"
          >
            {addr.isDefault && (
              <span className="absolute top-3 right-3 bg-indigo-100 text-indigo-700 text-xs font-semibold px-2 py-0.5 rounded-full">
                Default
              </span>
            )}
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center shrink-0 mt-0.5">
                <MapPin className="w-5 h-5 text-indigo-700" />
              </div>
              <div className="flex-1 min-w-0 pr-14">
                <p className="font-bold text-gray-900 mb-0.5">{addr.label}</p>
                <p className="text-gray-600 text-sm">{addr.name}</p>
                <p className="text-gray-500 text-sm">{addr.line1}{addr.line2 ? `, ${addr.line2}` : ""}</p>
                <p className="text-gray-500 text-sm">
                  {addr.city}, {addr.state} {addr.zip}
                </p>
                <p className="text-gray-400 text-sm">{addr.country}</p>
              </div>
            </div>
            <div className="flex gap-2 mt-3 pt-3 border-t border-gray-100">
              {!addr.isDefault && (
                <button
                  onClick={() => handleSetDefault(addr.id)}
                  className="flex-1 text-sm text-indigo-700 font-semibold py-2 rounded-xl bg-indigo-50 hover:bg-indigo-100 transition-colors"
                >
                  Set as Default
                </button>
              )}
              <button className="flex items-center justify-center gap-1 px-4 py-2 rounded-xl bg-gray-100 hover:bg-gray-200 transition-colors text-sm font-medium text-gray-700">
                <Pencil className="w-4 h-4" /> Edit
              </button>
              <button
                onClick={() => handleDelete(addr.id)}
                className="flex items-center justify-center gap-1 px-4 py-2 rounded-xl bg-red-50 hover:bg-red-100 transition-colors text-sm font-medium text-red-500"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>
        ))}

        {/* Add New Address */}
        {!showForm ? (
          <button
            onClick={() => setShowForm(true)}
            className="w-full py-4 rounded-2xl border-2 border-dashed border-indigo-300 text-indigo-700 font-semibold flex items-center justify-center gap-2 hover:bg-indigo-50 transition-colors"
          >
            <Plus className="w-5 h-5" /> Add New Address
          </button>
        ) : (
          <div className="bg-white rounded-2xl shadow-sm p-4">
            <p className="font-bold text-gray-900 mb-3">New Address</p>
            <div className="space-y-3">
              {[
                { label: "Label (e.g. Home)", value: newLabel, setter: setNewLabel, placeholder: "Home" },
                { label: "Street Address", value: newLine1, setter: setNewLine1, placeholder: "123 Main St" },
                { label: "City", value: newCity, setter: setNewCity, placeholder: "New York" },
                { label: "State", value: newState, setter: setNewState, placeholder: "NY" },
                { label: "ZIP Code", value: newZip, setter: setNewZip, placeholder: "10001" },
              ].map((field) => (
                <div key={field.label}>
                  <label className="text-xs font-medium text-gray-400 mb-1 block">{field.label}</label>
                  <input
                    type="text"
                    value={field.value}
                    onChange={(e) => field.setter(e.target.value)}
                    placeholder={field.placeholder}
                    className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-gray-900 outline-none focus:border-indigo-400 transition-colors"
                  />
                </div>
              ))}
            </div>
            <div className="flex gap-3 mt-4">
              <button
                onClick={() => setShowForm(false)}
                className="flex-1 py-3 rounded-xl border border-gray-200 text-gray-600 font-semibold hover:bg-gray-50 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleAdd}
                className="flex-1 py-3 rounded-xl bg-indigo-700 text-white font-semibold hover:bg-indigo-800 transition-colors"
              >
                Save Address
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. PAYMENT METHODS
// ─────────────────────────────────────────────────────────────────────────────
const dummyCards = [
  { id: 1, type: "Visa", last4: "4242", expiry: "12/27", holder: "John Doe", isDefault: true, color: "from-indigo-600 to-blue-500" },
  { id: 2, type: "Mastercard", last4: "8821", expiry: "09/26", holder: "John Doe", isDefault: false, color: "from-purple-600 to-pink-500" },
];

function CardIcon({ type }: { type: string }) {
  if (type === "Visa") {
    return (
      <span className="bg-white/20 text-white text-xs font-extrabold px-2 py-0.5 rounded tracking-wider">
        VISA
      </span>
    );
  }
  return (
    <div className="flex -space-x-1.5">
      <div className="w-6 h-6 rounded-full bg-red-400 opacity-90" />
      <div className="w-6 h-6 rounded-full bg-yellow-400 opacity-90" />
    </div>
  );
}

export function PaymentMethods() {
  const [cards, setCards] = useState(dummyCards);
  const [showForm, setShowForm] = useState(false);

  const handleDelete = (id: number) => {
    setCards((prev) => prev.filter((c) => c.id !== id));
  };

  const handleSetDefault = (id: number) => {
    setCards((prev) => prev.map((c) => ({ ...c, isDefault: c.id === id })));
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-10">
      <SubPageHeader title="Payment Methods" />

      <div className="px-4 py-5 space-y-4">
        {cards.map((card) => (
          <div key={card.id} className="relative">
            {/* Card visual */}
            <div className={`bg-gradient-to-br ${card.color} rounded-2xl p-5 shadow-lg`}>
              {card.isDefault && (
                <span className="absolute top-3 right-3 bg-white/20 text-white text-xs font-semibold px-2 py-0.5 rounded-full backdrop-blur-sm">
                  Default
                </span>
              )}
              <div className="flex justify-between items-start mb-6">
                <CreditCard className="w-8 h-8 text-white/80" />
                <CardIcon type={card.type} />
              </div>
              <p className="text-white/70 text-sm mb-1 tracking-widest">
                •••• •••• •••• {card.last4}
              </p>
              <div className="flex justify-between items-end mt-3">
                <div>
                  <p className="text-white/60 text-xs">Card Holder</p>
                  <p className="text-white font-semibold">{card.holder}</p>
                </div>
                <div className="text-right">
                  <p className="text-white/60 text-xs">Expires</p>
                  <p className="text-white font-semibold">{card.expiry}</p>
                </div>
              </div>
            </div>
            {/* Actions */}
            <div className="flex gap-2 mt-2">
              {!card.isDefault && (
                <button
                  onClick={() => handleSetDefault(card.id)}
                  className="flex-1 text-sm text-indigo-700 font-semibold py-2 rounded-xl bg-indigo-50 hover:bg-indigo-100 transition-colors"
                >
                  Set as Default
                </button>
              )}
              <button
                onClick={() => handleDelete(card.id)}
                className="flex items-center justify-center gap-1 px-4 py-2 rounded-xl bg-red-50 hover:bg-red-100 transition-colors text-sm font-medium text-red-500"
              >
                <Trash2 className="w-4 h-4" /> Remove
              </button>
            </div>
          </div>
        ))}

        {/* Add Card */}
        {!showForm ? (
          <button
            onClick={() => setShowForm(true)}
            className="w-full py-4 rounded-2xl border-2 border-dashed border-indigo-300 text-indigo-700 font-semibold flex items-center justify-center gap-2 hover:bg-indigo-50 transition-colors"
          >
            <Plus className="w-5 h-5" /> Add New Card
          </button>
        ) : (
          <div className="bg-white rounded-2xl shadow-sm p-4">
            <p className="font-bold text-gray-900 mb-3">Add New Card</p>
            <div className="space-y-3">
              {[
                { label: "Card Number", placeholder: "1234 5678 9012 3456" },
                { label: "Cardholder Name", placeholder: "John Doe" },
                { label: "Expiry Date", placeholder: "MM/YY" },
                { label: "CVV", placeholder: "•••" },
              ].map((f) => (
                <div key={f.label}>
                  <label className="text-xs font-medium text-gray-400 mb-1 block">{f.label}</label>
                  <input
                    type="text"
                    placeholder={f.placeholder}
                    className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-gray-900 outline-none focus:border-indigo-400 transition-colors"
                  />
                </div>
              ))}
            </div>
            <div className="flex gap-3 mt-4">
              <button
                onClick={() => setShowForm(false)}
                className="flex-1 py-3 rounded-xl border border-gray-200 text-gray-600 font-semibold hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={() => setShowForm(false)}
                className="flex-1 py-3 rounded-xl bg-indigo-700 text-white font-semibold hover:bg-indigo-800"
              >
                Add Card
              </button>
            </div>
          </div>
        )}

        {/* Security Note */}
        <div className="flex items-center gap-3 bg-green-50 rounded-2xl p-4">
          <Shield className="w-5 h-5 text-green-600 shrink-0" />
          <p className="text-sm text-green-700">
            Your payment information is encrypted and secure.
          </p>
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. NOTIFICATIONS
// ─────────────────────────────────────────────────────────────────────────────
const notificationGroups = [
  {
    title: "Order Updates",
    items: [
      { id: "order_status", icon: Package, label: "Order Status", description: "Get notified when your order status changes", color: "text-indigo-600", bg: "bg-indigo-100", defaultOn: true },
      { id: "shipping", icon: Truck, label: "Shipping Updates", description: "Track your package in real-time", color: "text-blue-600", bg: "bg-blue-100", defaultOn: true },
      { id: "returns", icon: RotateCcw, label: "Returns & Refunds", description: "Updates on return and refund status", color: "text-orange-600", bg: "bg-orange-100", defaultOn: true },
    ],
  },
  {
    title: "Promotions",
    items: [
      { id: "deals", icon: Tag, label: "Deals & Offers", description: "Exclusive discounts just for you", color: "text-red-600", bg: "bg-red-100", defaultOn: false },
      { id: "new_arrivals", icon: ShoppingBag, label: "New Arrivals", description: "Be first to know about new products", color: "text-green-600", bg: "bg-green-100", defaultOn: false },
    ],
  },
  {
    title: "Account",
    items: [
      { id: "security", icon: Shield, label: "Security Alerts", description: "Important account security notifications", color: "text-purple-600", bg: "bg-purple-100", defaultOn: true },
      { id: "newsletter", icon: Mail, label: "Newsletter", description: "Weekly product roundups and tips", color: "text-gray-600", bg: "bg-gray-100", defaultOn: false },
    ],
  },
];

function Toggle({ enabled, onToggle }: { enabled: boolean; onToggle: () => void }) {
  return (
    <button
      onClick={onToggle}
      className={`w-12 h-6 rounded-full transition-colors duration-200 relative shrink-0 ${
        enabled ? "bg-indigo-600" : "bg-gray-200"
      }`}
    >
      <span
        className={`absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-transform duration-200 ${
          enabled ? "translate-x-7" : "translate-x-1"
        }`}
      />
    </button>
  );
}

export function Notifications() {
  const allIds = notificationGroups.flatMap((g) => g.items).map((i) => i.id);
  const [enabled, setEnabled] = useState<Record<string, boolean>>(() =>
    Object.fromEntries(
      notificationGroups.flatMap((g) => g.items).map((i) => [i.id, i.defaultOn])
    )
  );

  const toggleAll = (val: boolean) => {
    setEnabled(Object.fromEntries(allIds.map((id) => [id, val])));
  };

  const allOn = allIds.every((id) => enabled[id]);

  return (
    <div className="bg-gray-50 min-h-screen pb-10">
      <SubPageHeader title="Notifications" />

      {/* Master toggle */}
      <div className="px-4 py-4">
        <div className="bg-white rounded-2xl shadow-sm px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center">
              <Bell className="w-5 h-5 text-indigo-700" />
            </div>
            <div>
              <p className="font-bold text-gray-900">All Notifications</p>
              <p className="text-xs text-gray-400">Enable or disable all at once</p>
            </div>
          </div>
          <Toggle enabled={allOn} onToggle={() => toggleAll(!allOn)} />
        </div>
      </div>

      {/* Groups */}
      <div className="px-4 space-y-5">
        {notificationGroups.map((group) => (
          <div key={group.title}>
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest mb-2 px-1">
              {group.title}
            </p>
            <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
              {group.items.map((item, idx) => (
                <div
                  key={item.id}
                  className={`flex items-center gap-3 px-4 py-3.5 ${
                    idx !== group.items.length - 1 ? "border-b border-gray-100" : ""
                  }`}
                >
                  <div className={`w-10 h-10 ${item.bg} rounded-full flex items-center justify-center shrink-0`}>
                    <item.icon className={`w-5 h-5 ${item.color}`} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-gray-900">{item.label}</p>
                    <p className="text-xs text-gray-400 leading-snug">{item.description}</p>
                  </div>
                  <Toggle
                    enabled={enabled[item.id]}
                    onToggle={() =>
                      setEnabled((prev) => ({ ...prev, [item.id]: !prev[item.id] }))
                    }
                  />
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. FAVORITES
// ─────────────────────────────────────────────────────────────────────────────
const dummyFavorites = [
  { id: 1, name: "Wireless Headphones Pro", price: 299.99, rating: 4.8, category: "Audio", image: "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?w=400&q=80" },
  { id: 2, name: "Smart Watch Series 5", price: 399.99, rating: 4.9, category: "Wearables", image: "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?w=400&q=80" },
  { id: 3, name: "Laptop Stand Pro", price: 79.99, rating: 4.6, category: "Accessories", image: "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=400&q=80" },
  { id: 4, name: "Mechanical Keyboard", price: 149.99, rating: 4.7, category: "Peripherals", image: "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400&q=80" },
  { id: 5, name: "USB-C Hub 7-in-1", price: 59.99, rating: 4.5, category: "Accessories", image: "https://images.unsplash.com/photo-1625989144351-50c1f2c05b79?w=400&q=80" },
  { id: 6, name: "Portable SSD 1TB", price: 119.99, rating: 4.8, category: "Storage", image: "https://images.unsplash.com/photo-1627557726569-4cf5c6a76c2f?w=400&q=80" },
];

export function Favorites() {
  const [favorites, setFavorites] = useState(dummyFavorites);
  const navigate = useNavigate();

  const handleRemove = (id: number) => {
    setFavorites((prev) => prev.filter((f) => f.id !== id));
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-10">
      <SubPageHeader title="Favorites" />

      <div className="px-4 py-4">
        <p className="text-sm text-gray-500 mb-4">{favorites.length} saved items</p>

        {favorites.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mb-4">
              <Heart className="w-10 h-10 text-red-400" />
            </div>
            <p className="text-gray-500 text-center">No favorites yet.<br />Start exploring products!</p>
            <button
              onClick={() => navigate("/products")}
              className="mt-4 px-6 py-3 bg-indigo-700 text-white rounded-full font-semibold hover:bg-indigo-800 transition-colors"
            >
              Browse Products
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3">
            {favorites.map((item) => (
              <div key={item.id} className="bg-white rounded-2xl shadow-sm overflow-hidden">
                <div className="relative">
                  <img
                    src={item.image}
                    alt={item.name}
                    className="w-full aspect-square object-cover"
                  />
                  <button
                    onClick={() => handleRemove(item.id)}
                    className="absolute top-2 right-2 w-8 h-8 bg-white rounded-full shadow-md flex items-center justify-center"
                  >
                    <Heart className="w-4 h-4 fill-red-500 text-red-500" />
                  </button>
                  <span className="absolute top-2 left-2 bg-black/40 text-white text-xs px-2 py-0.5 rounded-full backdrop-blur-sm">
                    {item.category}
                  </span>
                </div>
                <div className="p-3">
                  <p className="font-semibold text-gray-900 text-sm leading-tight mb-1 line-clamp-2">
                    {item.name}
                  </p>
                  <div className="flex items-center gap-1 mb-2">
                    <Star className="w-3 h-3 fill-yellow-400 text-yellow-400" />
                    <span className="text-xs text-gray-500">{item.rating}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <p className="font-bold text-indigo-700">${item.price}</p>
                    <button className="p-1.5 bg-indigo-700 rounded-lg hover:bg-indigo-800 transition-colors">
                      <ShoppingBag className="w-4 h-4 text-white" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. ORDER HISTORY
// ─────────────────────────────────────────────────────────────────────────────
const dummyOrders = [
  {
    id: "ORD-2024-001",
    date: "Mar 15, 2026",
    items: [
      { name: "Wireless Headphones Pro", qty: 1, price: 299.99 },
      { name: "USB-C Hub 7-in-1", qty: 1, price: 59.99 },
    ],
    total: 359.98,
    status: "Delivered",
    statusColor: "bg-green-100 text-green-700",
    image: "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?w=120&q=80",
  },
  {
    id: "ORD-2024-002",
    date: "Mar 8, 2026",
    items: [{ name: "Smart Watch Series 5", qty: 1, price: 399.99 }],
    total: 399.99,
    status: "In Transit",
    statusColor: "bg-blue-100 text-blue-700",
    image: "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?w=120&q=80",
  },
  {
    id: "ORD-2024-003",
    date: "Feb 28, 2026",
    items: [
      { name: "Mechanical Keyboard", qty: 1, price: 149.99 },
      { name: "Laptop Stand Pro", qty: 1, price: 79.99 },
    ],
    total: 229.98,
    status: "Delivered",
    statusColor: "bg-green-100 text-green-700",
    image: "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=120&q=80",
  },
  {
    id: "ORD-2024-004",
    date: "Feb 10, 2026",
    items: [{ name: "Portable SSD 1TB", qty: 2, price: 239.98 }],
    total: 239.98,
    status: "Cancelled",
    statusColor: "bg-red-100 text-red-700",
    image: "https://images.unsplash.com/photo-1627557726569-4cf5c6a76c2f?w=120&q=80",
  },
  {
    id: "ORD-2024-005",
    date: "Jan 22, 2026",
    items: [{ name: "Smartphone X12 Pro", qty: 1, price: 999.99 }],
    total: 999.99,
    status: "Delivered",
    statusColor: "bg-green-100 text-green-700",
    image: "https://images.unsplash.com/photo-1741061961703-0739f3454314?w=120&q=80",
  },
];

type FilterType = "All" | "Delivered" | "In Transit" | "Cancelled";

export function OrderHistory() {
  const [filter, setFilter] = useState<FilterType>("All");
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const filters: FilterType[] = ["All", "Delivered", "In Transit", "Cancelled"];
  const filtered = filter === "All" ? dummyOrders : dummyOrders.filter((o) => o.status === filter);

  return (
    <div className="bg-gray-50 min-h-screen pb-10">
      <SubPageHeader title="Order History" />

      {/* Summary */}
      <div className="px-4 pt-4 pb-2">
        <div className="grid grid-cols-3 gap-3 mb-4">
          {[
            { label: "Total Orders", value: 12, color: "text-indigo-700" },
            { label: "Delivered", value: 9, color: "text-green-600" },
            { label: "Cancelled", value: 1, color: "text-red-500" },
          ].map((s) => (
            <div key={s.label} className="bg-white rounded-2xl shadow-sm p-3 text-center">
              <p className={`text-2xl font-bold ${s.color}`}>{s.value}</p>
              <p className="text-xs text-gray-500 mt-0.5">{s.label}</p>
            </div>
          ))}
        </div>

        {/* Filter Tabs */}
        <div className="flex gap-2 overflow-x-auto pb-1 no-scrollbar">
          {filters.map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-1.5 rounded-full text-sm font-semibold whitespace-nowrap transition-colors ${
                filter === f
                  ? "bg-indigo-700 text-white shadow-md"
                  : "bg-white text-gray-600 border border-gray-200 hover:border-indigo-300"
              }`}
            >
              {f}
            </button>
          ))}
        </div>
      </div>

      {/* Orders List */}
      <div className="px-4 py-3 space-y-3">
        {filtered.map((order) => (
          <div key={order.id} className="bg-white rounded-2xl shadow-sm overflow-hidden">
            <button
              className="w-full text-left p-4"
              onClick={() => setExpandedId(expandedId === order.id ? null : order.id)}
            >
              <div className="flex items-center gap-3">
                <img
                  src={order.image}
                  alt=""
                  className="w-14 h-14 rounded-xl object-cover shrink-0"
                />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-1">
                    <p className="font-bold text-gray-900 text-sm">{order.id}</p>
                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${order.statusColor}`}>
                      {order.status}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400 mb-1">{order.date}</p>
                  <p className="text-sm text-gray-600">
                    {order.items.length} item{order.items.length > 1 ? "s" : ""} ·{" "}
                    <span className="font-bold text-indigo-700">${order.total.toFixed(2)}</span>
                  </p>
                </div>
                <ChevronRight
                  className={`w-5 h-5 text-gray-400 shrink-0 transition-transform ${
                    expandedId === order.id ? "rotate-90" : ""
                  }`}
                />
              </div>
            </button>

            {/* Expanded Details */}
            {expandedId === order.id && (
              <div className="border-t border-gray-100 px-4 pb-4">
                <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest mt-3 mb-2">
                  Items
                </p>
                <div className="space-y-2">
                  {order.items.map((item, idx) => (
                    <div key={idx} className="flex justify-between items-center">
                      <span className="text-sm text-gray-700">
                        {item.name} <span className="text-gray-400">×{item.qty}</span>
                      </span>
                      <span className="text-sm font-semibold text-gray-900">
                        ${item.price.toFixed(2)}
                      </span>
                    </div>
                  ))}
                </div>
                <div className="flex gap-2 mt-3 pt-3 border-t border-gray-100">
                  {order.status === "Delivered" && (
                    <button className="flex-1 py-2.5 rounded-xl bg-indigo-50 text-indigo-700 font-semibold text-sm hover:bg-indigo-100 transition-colors">
                      Leave Review
                    </button>
                  )}
                  {order.status !== "Cancelled" && (
                    <button className="flex-1 py-2.5 rounded-xl bg-gray-100 text-gray-700 font-semibold text-sm hover:bg-gray-200 transition-colors">
                      Track Order
                    </button>
                  )}
                  {order.status === "Cancelled" && (
                    <button className="flex-1 py-2.5 rounded-xl bg-indigo-50 text-indigo-700 font-semibold text-sm hover:bg-indigo-100 transition-colors">
                      Reorder
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
