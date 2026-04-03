import {
  ArrowLeft,
  Heart,
  Star,
  ShoppingCart,
  Share2,
  ChevronLeft,
  ChevronRight,
  Send,
  ThumbsUp,
  MoreVertical,
} from "lucide-react";
import { useState, useCallback } from "react";
import { Link, useParams } from "react-router";
import useEmblaCarousel from "embla-carousel-react";

// ─── Product Data ─────────────────────────────────────────────────────────────
const productData: Record<string, any> = {
  "1": {
    name: "Wireless Headphones Pro",
    basePrice: 299.99,
    images: [
      "https://images.unsplash.com/photo-1578517581165-61ec5ab27a19?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3aXJlbGVzcyUyMGhlYWRwaG9uZXMlMjBwcm9kdWN0fGVufDF8fHx8MTc3NDE5NDYwMHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      "https://images.unsplash.com/photo-1505314573890-fcc54e98fe44?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwcHJvZHVjdCUyMGFuZ2xlJTIwdmlld3xlbnwxfHx8fDE3NzQyMTEyOTV8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      "https://images.unsplash.com/photo-1619462729239-ca28ab216892?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwc2NyZWVuJTIwZGlzcGxheXxlbnwxfHx8fDE3NzQyMTEyOTZ8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    ],
    rating: 4.8,
    reviews: 324,
    description:
      "Premium wireless headphones with active noise cancellation, 30-hour battery life, and superior sound quality. Perfect for music lovers and professionals.",
    colors: [
      { name: "Midnight Black", hex: "#1a1a1a" },
      { name: "Silver", hex: "#c0c0c0" },
      { name: "Rose Gold", hex: "#b76e79" },
      { name: "Sky Blue", hex: "#87ceeb" },
    ],
    storage: [
      { size: "Standard", price: 0 },
      { size: "Premium", price: 50 },
    ],
  },
  "2": {
    name: "Smart Watch Series 5",
    basePrice: 399.99,
    images: [
      "https://images.unsplash.com/photo-1638095562082-449d8c5a47b4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHdhdGNoJTIwdGVjaG5vbG9neSUyMHByb2R1Y3R8ZW58MXx8fHwxNzc0MTk5MzQxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      "https://images.unsplash.com/photo-1505314573890-fcc54e98fe44?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwcHJvZHVjdCUyMGFuZ2xlJTIwdmlld3xlbnwxfHx8fDE3NzQyMTEyOTV8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      "https://images.unsplash.com/photo-1761906975728-2d8a600a0431?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwYmFjayUyMGNhbWVyYSUyMGNsb3NlfGVufDF8fHx8MTc3NDIxMTI5Nnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    ],
    rating: 4.9,
    reviews: 512,
    description:
      "Advanced fitness tracking, heart rate monitoring, and seamless connectivity. Stay connected and healthy with our latest smartwatch technology.",
    colors: [
      { name: "Space Gray", hex: "#5a5a5a" },
      { name: "Gold", hex: "#ffd700" },
      { name: "Silver", hex: "#c0c0c0" },
    ],
    storage: [
      { size: "GPS", price: 0 },
      { size: "GPS + Cellular", price: 100 },
    ],
  },
  "4": {
    name: "Smartphone X12 Pro",
    basePrice: 999.99,
    images: [
      "https://images.unsplash.com/photo-1741061961703-0739f3454314?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwbW9iaWxlJTIwcGhvbmV8ZW58MXx8fHwxNzc0MTg1MzcxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      "https://images.unsplash.com/photo-1505314573890-fcc54e98fe44?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwcHJvZHVjdCUyMGFuZ2xlJTIwdmlld3xlbnwxfHx8fDE3NzQyMTEyOTV8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      "https://images.unsplash.com/photo-1619462729239-ca28ab216892?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwc2NyZWVuJTIwZGlzcGxheXxlbnwxfHx8fDE3NzQyMTEyOTZ8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
      "https://images.unsplash.com/photo-1761906975728-2d8a600a0431?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzbWFydHBob25lJTIwYmFjayUyMGNhbWVyYSUyMGNsb3NlfGVufDF8fHx8MTc3NDIxMTI5Nnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    ],
    rating: 4.7,
    reviews: 856,
    description:
      "The most advanced smartphone yet. Featuring a stunning 6.7-inch display, professional-grade camera system, and all-day battery life. Experience innovation at its finest.",
    colors: [
      { name: "Midnight", hex: "#1a1a2e" },
      { name: "Starlight", hex: "#f5f5dc" },
      { name: "Deep Purple", hex: "#4b0082" },
      { name: "Product Red", hex: "#d32f2f" },
    ],
    storage: [
      { size: "128GB", price: 0 },
      { size: "256GB", price: 100 },
      { size: "512GB", price: 200 },
      { size: "1TB", price: 400 },
    ],
  },
};

// ─── Initial Reviews Data ─────────────────────────────────────────────────────
const initialReviews: Record<string, any[]> = {
  "1": [
    { id: 1, name: "Alex Martinez", avatar: "AM", rating: 5, date: "Mar 10, 2026", comment: "Absolutely amazing headphones! The noise cancellation is top-tier, and the battery life exceeded my expectations. Worth every penny.", helpful: 34, avatarColor: "from-blue-500 to-indigo-600" },
    { id: 2, name: "Sarah K.", avatar: "SK", rating: 4, date: "Feb 28, 2026", comment: "Great sound quality and comfortable to wear for long sessions. The only downside is the carrying case feels a bit cheap.", helpful: 21, avatarColor: "from-pink-500 to-rose-600" },
    { id: 3, name: "David Chen", avatar: "DC", rating: 5, date: "Feb 14, 2026", comment: "Best headphones I've ever owned. Crystal clear audio and the mic quality for calls is exceptional. Highly recommend!", helpful: 18, avatarColor: "from-green-500 to-teal-600" },
    { id: 4, name: "Jamie O.", avatar: "JO", rating: 3, date: "Jan 30, 2026", comment: "Good headphones overall, but I expected a bit more bass. The build quality is solid though.", helpful: 7, avatarColor: "from-orange-500 to-yellow-600" },
  ],
  "2": [
    { id: 1, name: "Lisa R.", avatar: "LR", rating: 5, date: "Mar 15, 2026", comment: "This watch has completely changed my fitness routine! The health tracking is incredibly accurate and the battery lasts all week.", helpful: 41, avatarColor: "from-purple-500 to-violet-600" },
    { id: 2, name: "Tom W.", avatar: "TW", rating: 5, date: "Mar 5, 2026", comment: "Sleek design, responsive touchscreen, and seamless phone integration. The GPS is very precise too.", helpful: 29, avatarColor: "from-cyan-500 to-blue-600" },
    { id: 3, name: "Nina P.", avatar: "NP", rating: 4, date: "Feb 20, 2026", comment: "Love the watch but the app could use some improvements. The watch itself is fantastic though.", helpful: 15, avatarColor: "from-red-500 to-rose-600" },
  ],
  "4": [
    { id: 1, name: "Ryan S.", avatar: "RS", rating: 5, date: "Mar 18, 2026", comment: "The camera system is unreal. Night mode photos look like they were taken with a professional DSLR. Display is gorgeous too.", helpful: 56, avatarColor: "from-emerald-500 to-green-600" },
    { id: 2, name: "Mia L.", avatar: "ML", rating: 4, date: "Mar 10, 2026", comment: "Very fast processor, great battery life. The software experience is smooth. The price is high but it's worth it.", helpful: 38, avatarColor: "from-violet-500 to-purple-600" },
    { id: 3, name: "Jake B.", avatar: "JB", rating: 5, date: "Feb 25, 2026", comment: "Switched from my old phone and the difference is night and day. Everything just works perfectly.", helpful: 27, avatarColor: "from-amber-500 to-orange-600" },
    { id: 4, name: "Chloe T.", avatar: "CT", rating: 4, date: "Feb 18, 2026", comment: "Beautiful phone with a stunning display. The Face ID is super fast and the haptic feedback feels premium.", helpful: 19, avatarColor: "from-teal-500 to-cyan-600" },
    { id: 5, name: "Omar H.", avatar: "OH", rating: 3, date: "Jan 28, 2026", comment: "Good phone but slightly overpriced. The base 128GB storage fills up quickly. Recommend getting at least 256GB.", helpful: 12, avatarColor: "from-indigo-500 to-blue-600" },
  ],
};

// ─── Star Rating Input ────────────────────────────────────────────────────────
function StarRatingInput({
  value,
  onChange,
}: {
  value: number;
  onChange: (v: number) => void;
}) {
  const [hovered, setHovered] = useState(0);
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          onClick={() => onChange(star)}
          onMouseEnter={() => setHovered(star)}
          onMouseLeave={() => setHovered(0)}
          className="transition-transform hover:scale-110"
        >
          <Star
            className={`w-7 h-7 transition-colors ${
              star <= (hovered || value)
                ? "fill-yellow-400 text-yellow-400"
                : "text-gray-300"
            }`}
          />
        </button>
      ))}
    </div>
  );
}

// ─── Rating Distribution Bar ──────────────────────────────────────────────────
function RatingBar({
  star,
  count,
  total,
}: {
  star: number;
  count: number;
  total: number;
}) {
  const pct = total > 0 ? (count / total) * 100 : 0;
  return (
    <div className="flex items-center gap-2">
      <span className="text-xs text-gray-500 w-3">{star}</span>
      <Star className="w-3 h-3 fill-yellow-400 text-yellow-400 shrink-0" />
      <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
        <div
          className="h-full bg-yellow-400 rounded-full transition-all"
          style={{ width: `${pct}%` }}
        />
      </div>
      <span className="text-xs text-gray-400 w-4 text-right">{count}</span>
    </div>
  );
}

// ─── Main Component ───────────────────────────────────────────────────────────
export function ProductDetail() {
  const { id } = useParams();
  const [selectedColor, setSelectedColor] = useState(0);
  const [selectedStorage, setSelectedStorage] = useState(0);
  const [isFavorite, setIsFavorite] = useState(false);
  const [emblaRef, emblaApi] = useEmblaCarousel({ loop: true });
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);

  // Reviews / Comments state
  const [reviews, setReviews] = useState(
    initialReviews[id || "1"] || initialReviews["1"]
  );
  const [newRating, setNewRating] = useState(0);
  const [newComment, setNewComment] = useState("");
  const [newName, setNewName] = useState("");
  const [helpfulIds, setHelpfulIds] = useState<Set<number>>(new Set());
  const [showReviewForm, setShowReviewForm] = useState(false);

  const product = productData[id || "1"];

  const scrollPrev = useCallback(() => {
    if (emblaApi) {
      emblaApi.scrollPrev();
      setSelectedImageIndex(emblaApi.selectedScrollSnap());
    }
  }, [emblaApi]);

  const scrollNext = useCallback(() => {
    if (emblaApi) {
      emblaApi.scrollNext();
      setSelectedImageIndex(emblaApi.selectedScrollSnap());
    }
  }, [emblaApi]);

  const scrollTo = useCallback(
    (index: number) => {
      if (emblaApi) {
        emblaApi.scrollTo(index);
        setSelectedImageIndex(index);
      }
    },
    [emblaApi]
  );

  const handleSubmitReview = () => {
    if (!newComment.trim() || newRating === 0) return;
    const initials = (newName.trim() || "AN")
      .split(" ")
      .map((w: string) => w[0])
      .join("")
      .slice(0, 2)
      .toUpperCase();
    const newReview = {
      id: Date.now(),
      name: newName.trim() || "Anonymous",
      avatar: initials,
      rating: newRating,
      date: "Mar 23, 2026",
      comment: newComment.trim(),
      helpful: 0,
      avatarColor: "from-indigo-500 to-purple-600",
    };
    setReviews((prev) => [newReview, ...prev]);
    setNewRating(0);
    setNewComment("");
    setNewName("");
    setShowReviewForm(false);
  };

  const toggleHelpful = (reviewId: number) => {
    setHelpfulIds((prev) => {
      const next = new Set(prev);
      if (next.has(reviewId)) next.delete(reviewId);
      else next.add(reviewId);
      return next;
    });
  };

  if (!product) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <h2 className="text-xl font-bold text-gray-900 mb-2">
            Product not found
          </h2>
          <Link to="/" className="text-indigo-700 font-semibold">
            Return to Home
          </Link>
        </div>
      </div>
    );
  }

  const totalPrice =
    product.basePrice + (product.storage?.[selectedStorage]?.price || 0);

  // Rating summary
  const ratingCounts = [5, 4, 3, 2, 1].map((star) => ({
    star,
    count: reviews.filter((r: any) => r.rating === star).length,
  }));
  const avgRating =
    reviews.length > 0
      ? (
          reviews.reduce((s: number, r: any) => s + r.rating, 0) /
          reviews.length
        ).toFixed(1)
      : String(product.rating);

  return (
    <div className="bg-white min-h-screen pb-24">
      {/* Header */}
      <header className="absolute top-0 left-0 right-0 z-10 px-6 py-4 flex items-center justify-between">
        <Link
          to="/"
          className="p-2 bg-white/90 backdrop-blur-sm rounded-full shadow-md hover:bg-white transition-colors"
        >
          <ArrowLeft className="w-6 h-6 text-gray-700" />
        </Link>
        <div className="flex gap-2">
          <button className="p-2 bg-white/90 backdrop-blur-sm rounded-full shadow-md hover:bg-white transition-colors">
            <Share2 className="w-6 h-6 text-gray-700" />
          </button>
          <button
            onClick={() => setIsFavorite(!isFavorite)}
            className="p-2 bg-white/90 backdrop-blur-sm rounded-full shadow-md hover:bg-white transition-colors"
          >
            <Heart
              className={`w-6 h-6 ${
                isFavorite ? "fill-red-500 text-red-500" : "text-gray-700"
              }`}
            />
          </button>
        </div>
      </header>

      {/* Image Carousel */}
      <div className="relative bg-gray-100">
        <div className="overflow-hidden" ref={emblaRef}>
          <div className="flex">
            {product.images.map((image: string, index: number) => (
              <div key={index} className="flex-[0_0_100%] min-w-0">
                <div className="aspect-square">
                  <img
                    src={image}
                    alt={`${product.name} - Image ${index + 1}`}
                    className="w-full h-full object-cover"
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Carousel Navigation */}
        {product.images.length > 1 && (
          <>
            <button
              onClick={scrollPrev}
              className="absolute left-4 top-1/2 -translate-y-1/2 p-2 bg-white/90 backdrop-blur-sm rounded-full shadow-md hover:bg-white transition-colors"
            >
              <ChevronLeft className="w-6 h-6 text-gray-700" />
            </button>
            <button
              onClick={scrollNext}
              className="absolute right-4 top-1/2 -translate-y-1/2 p-2 bg-white/90 backdrop-blur-sm rounded-full shadow-md hover:bg-white transition-colors"
            >
              <ChevronRight className="w-6 h-6 text-gray-700" />
            </button>
          </>
        )}

        {/* Carousel Dots */}
        {product.images.length > 1 && (
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
            {product.images.map((_: any, index: number) => (
              <button
                key={index}
                onClick={() => scrollTo(index)}
                className={`w-2 h-2 rounded-full transition-all ${
                  selectedImageIndex === index
                    ? "bg-indigo-700 w-6"
                    : "bg-white/60 hover:bg-white/80"
                }`}
              />
            ))}
          </div>
        )}
      </div>

      {/* Product Info */}
      <div className="px-6 py-6">
        {/* Name & Price */}
        <div className="mb-4">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">
            {product.name}
          </h1>
          <p className="text-3xl font-bold text-indigo-700">
            ${totalPrice.toFixed(2)}
          </p>
        </div>

        {/* Rating */}
        <div className="flex items-center gap-2 mb-6 pb-6 border-b border-gray-200">
          <div className="flex items-center gap-1">
            {[...Array(5)].map((_, i) => (
              <Star
                key={i}
                className={`w-5 h-5 ${
                  i < Math.floor(product.rating)
                    ? "fill-yellow-400 text-yellow-400"
                    : "text-gray-300"
                }`}
              />
            ))}
          </div>
          <span className="font-bold text-gray-900">{product.rating}</span>
          <span className="text-gray-500">({product.reviews} reviews)</span>
        </div>

        {/* Color Selection */}
        {product.colors && (
          <div className="mb-6">
            <h3 className="font-bold text-gray-900 mb-3">
              Color: {product.colors[selectedColor].name}
            </h3>
            <div className="flex gap-3">
              {product.colors.map((color: any, index: number) => (
                <button
                  key={color.name}
                  onClick={() => setSelectedColor(index)}
                  className={`w-12 h-12 rounded-full border-2 transition-all ${
                    selectedColor === index
                      ? "border-indigo-700 scale-110 shadow-lg"
                      : "border-gray-300 hover:border-gray-400"
                  }`}
                  style={{ backgroundColor: color.hex }}
                  title={color.name}
                >
                  {selectedColor === index && (
                    <div className="w-full h-full rounded-full flex items-center justify-center">
                      <div className="w-4 h-4 bg-white rounded-full border-2 border-gray-700"></div>
                    </div>
                  )}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Storage / Variant Selection */}
        {product.storage && (
          <div className="mb-6 pb-6 border-b border-gray-200">
            <h3 className="font-bold text-gray-900 mb-3">
              {product.name.includes("Phone") ? "Storage" : "Variant"}
            </h3>
            <div className="grid grid-cols-2 gap-3">
              {product.storage.map((option: any, index: number) => (
                <button
                  key={option.size}
                  onClick={() => setSelectedStorage(index)}
                  className={`px-4 py-3 rounded-xl border-2 font-semibold transition-all ${
                    selectedStorage === index
                      ? "border-indigo-700 bg-indigo-50 text-indigo-700"
                      : "border-gray-200 bg-white text-gray-700 hover:border-gray-300"
                  }`}
                >
                  <div>{option.size}</div>
                  {option.price > 0 && (
                    <div className="text-xs mt-1">+${option.price}</div>
                  )}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Description */}
        <div className="mb-6">
          <h3 className="font-bold text-gray-900 mb-3">Description</h3>
          <p className="text-gray-600 leading-relaxed">{product.description}</p>
        </div>

        {/* Key Features */}
        <div className="mb-6 pb-6 border-b border-gray-200">
          <h3 className="font-bold text-gray-900 mb-3">Key Features</h3>
          <ul className="space-y-2">
            {[
              "Premium build quality with attention to detail",
              "Advanced technology for superior performance",
              "Industry-leading warranty and support",
              "Free shipping on all orders",
            ].map((feature) => (
              <li key={feature} className="flex items-start gap-2">
                <div className="w-1.5 h-1.5 rounded-full bg-indigo-700 mt-2 shrink-0"></div>
                <span className="text-gray-600 flex-1">{feature}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* ── REVIEWS & COMMENTS ─────────────────────────────────── */}
        <div>
          {/* Section Header */}
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-gray-900 text-lg">
              Reviews
              <span className="ml-2 text-sm font-normal text-gray-400">
                ({reviews.length})
              </span>
            </h3>
            <button
              onClick={() => setShowReviewForm(!showReviewForm)}
              className="px-4 py-2 bg-indigo-700 text-white rounded-full text-sm font-semibold hover:bg-indigo-800 active:scale-95 transition-all"
            >
              Write Review
            </button>
          </div>

          {/* Rating Summary */}
          <div className="bg-gray-50 rounded-2xl p-4 mb-5">
            <div className="flex items-center gap-4">
              <div className="text-center shrink-0">
                <p className="text-5xl font-bold text-gray-900">{avgRating}</p>
                <div className="flex justify-center mt-1 mb-0.5">
                  {[...Array(5)].map((_, i) => (
                    <Star
                      key={i}
                      className={`w-4 h-4 ${
                        i < Math.round(Number(avgRating))
                          ? "fill-yellow-400 text-yellow-400"
                          : "text-gray-300"
                      }`}
                    />
                  ))}
                </div>
                <p className="text-xs text-gray-400">{reviews.length} reviews</p>
              </div>
              <div className="flex-1 space-y-1.5">
                {ratingCounts.map(({ star, count }) => (
                  <RatingBar
                    key={star}
                    star={star}
                    count={count}
                    total={reviews.length}
                  />
                ))}
              </div>
            </div>
          </div>

          {/* Write Review Form */}
          {showReviewForm && (
            <div className="bg-indigo-50 rounded-2xl p-4 mb-5 border border-indigo-100">
              <h4 className="font-bold text-gray-900 mb-3">Your Review</h4>

              {/* Name */}
              <div className="mb-3">
                <label className="text-xs font-medium text-gray-500 mb-1.5 block">
                  Your Name
                </label>
                <input
                  type="text"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="Enter your name (optional)"
                  className="w-full border border-gray-200 rounded-xl px-3 py-2.5 bg-white text-gray-900 outline-none focus:border-indigo-400 transition-colors text-sm"
                />
              </div>

              {/* Star Rating */}
              <div className="mb-3">
                <label className="text-xs font-medium text-gray-500 mb-1.5 block">
                  Rating
                </label>
                <StarRatingInput value={newRating} onChange={setNewRating} />
                {newRating > 0 && (
                  <p className="text-xs text-indigo-600 mt-1">
                    {["", "Poor", "Fair", "Good", "Very Good", "Excellent"][newRating]}
                  </p>
                )}
              </div>

              {/* Comment */}
              <div className="mb-3">
                <label className="text-xs font-medium text-gray-500 mb-1.5 block">
                  Your Comment
                </label>
                <textarea
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  placeholder="Share your experience with this product..."
                  rows={4}
                  className="w-full border border-gray-200 rounded-xl px-3 py-2.5 bg-white text-gray-900 outline-none focus:border-indigo-400 transition-colors resize-none text-sm"
                />
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => setShowReviewForm(false)}
                  className="flex-1 py-3 rounded-xl border border-gray-200 bg-white text-gray-600 font-semibold text-sm hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSubmitReview}
                  disabled={!newComment.trim() || newRating === 0}
                  className="flex-1 py-3 rounded-xl bg-indigo-700 text-white font-semibold text-sm hover:bg-indigo-800 transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                  <Send className="w-4 h-4" /> Submit
                </button>
              </div>
            </div>
          )}

          {/* Reviews List */}
          <div className="space-y-4">
            {reviews.map((review: any) => (
              <div
                key={review.id}
                className="bg-white border border-gray-100 rounded-2xl p-4 shadow-sm"
              >
                {/* Reviewer Header */}
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center gap-2.5">
                    <div
                      className={`w-9 h-9 rounded-full bg-gradient-to-br ${review.avatarColor} flex items-center justify-center shrink-0`}
                    >
                      <span className="text-white text-xs font-bold">
                        {review.avatar}
                      </span>
                    </div>
                    <div>
                      <p className="font-semibold text-gray-900 text-sm">
                        {review.name}
                      </p>
                      <p className="text-xs text-gray-400">{review.date}</p>
                    </div>
                  </div>
                  <button className="p-1 rounded-full hover:bg-gray-100 transition-colors">
                    <MoreVertical className="w-4 h-4 text-gray-400" />
                  </button>
                </div>

                {/* Stars */}
                <div className="flex gap-0.5 mb-2">
                  {[...Array(5)].map((_, i) => (
                    <Star
                      key={i}
                      className={`w-4 h-4 ${
                        i < review.rating
                          ? "fill-yellow-400 text-yellow-400"
                          : "text-gray-200"
                      }`}
                    />
                  ))}
                </div>

                {/* Comment Text */}
                <p className="text-gray-700 text-sm leading-relaxed mb-3">
                  {review.comment}
                </p>

                {/* Helpful Button */}
                <div className="flex items-center gap-2 pt-2 border-t border-gray-50">
                  <button
                    onClick={() => toggleHelpful(review.id)}
                    className={`flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full transition-colors ${
                      helpfulIds.has(review.id)
                        ? "bg-indigo-100 text-indigo-700"
                        : "bg-gray-100 text-gray-500 hover:bg-gray-200"
                    }`}
                  >
                    <ThumbsUp
                      className={`w-3.5 h-3.5 ${
                        helpfulIds.has(review.id) ? "fill-indigo-600" : ""
                      }`}
                    />
                    Helpful (
                    {review.helpful + (helpfulIds.has(review.id) ? 1 : 0)})
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Fixed Bottom: Add to Cart */}
      <div className="fixed bottom-16 left-0 right-0 bg-white border-t border-gray-200 px-6 py-4 shadow-lg max-w-md mx-auto">
        <button className="w-full bg-indigo-700 text-white py-4 rounded-full font-bold hover:bg-indigo-800 transition-colors shadow-md flex items-center justify-center gap-2">
          <ShoppingCart className="w-5 h-5" />
          Add to Cart — ${totalPrice.toFixed(2)}
        </button>
      </div>
    </div>
  );
}
