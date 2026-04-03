import { Heart } from "lucide-react";
import { useState } from "react";
import { Link } from "react-router";

interface ProductCardProps {
  id: string;
  name: string;
  price: number;
  image: string;
  rating: number;
}

export function ProductCard({ id, name, price, image, rating }: ProductCardProps) {
  const [isFavorite, setIsFavorite] = useState(false);

  return (
    <Link to={`/product/${id}`}>
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
        <div className="relative aspect-square bg-gray-50">
          <img
            src={image}
            alt={name}
            className="w-full h-full object-cover"
          />
          <button
            onClick={(e) => {
              e.preventDefault();
              setIsFavorite(!isFavorite);
            }}
            className="absolute top-3 right-3 p-2 bg-white rounded-full shadow-md hover:scale-110 transition-transform"
          >
            <Heart
              className={`w-5 h-5 ${
                isFavorite ? "fill-red-500 text-red-500" : "text-gray-400"
              }`}
            />
          </button>
        </div>
        <div className="p-4">
          <h3 className="font-semibold text-gray-900 mb-1 line-clamp-2">{name}</h3>
          <div className="flex items-center gap-1 mb-2">
            <span className="text-yellow-400">★</span>
            <span className="text-sm text-gray-600">{rating.toFixed(1)}</span>
          </div>
          <p className="text-lg font-bold text-indigo-700">${price.toFixed(2)}</p>
        </div>
      </div>
    </Link>
  );
}
