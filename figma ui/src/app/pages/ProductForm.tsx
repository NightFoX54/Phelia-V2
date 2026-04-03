import { useState } from "react";
import { ChevronLeft, Plus, Trash2, Upload } from "lucide-react";
import { useNavigate } from "react-router";

interface ProductVariant {
  id: string;
  color: string;
  storage: string;
  price: string;
  stock: string;
}

export function ProductForm() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    category: "",
    brand: "",
  });

  const [variants, setVariants] = useState<ProductVariant[]>([
    { id: "1", color: "", storage: "", price: "", stock: "" },
  ]);

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const addVariant = () => {
    const newVariant: ProductVariant = {
      id: Date.now().toString(),
      color: "",
      storage: "",
      price: "",
      stock: "",
    };
    setVariants([...variants, newVariant]);
  };

  const removeVariant = (id: string) => {
    if (variants.length > 1) {
      setVariants(variants.filter((v) => v.id !== id));
    }
  };

  const updateVariant = (id: string, field: string, value: string) => {
    setVariants(
      variants.map((v) => (v.id === id ? { ...v, [field]: value } : v))
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Saving product:", { ...formData, variants });
    navigate("/store-dashboard");
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-32">
      {/* Header */}
      <header className="bg-white px-6 py-4 shadow-sm sticky top-0 z-10">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate(-1)}
            className="p-2 -ml-2 hover:bg-gray-100 rounded-xl transition-colors"
          >
            <ChevronLeft className="w-6 h-6" />
          </button>
          <div>
            <h1 className="font-bold text-gray-900">Add Product</h1>
            <p className="text-sm text-gray-500">Create a new product listing</p>
          </div>
        </div>
      </header>

      <form id="product-form" onSubmit={handleSubmit} className="space-y-6 py-6">
        {/* Image Upload */}
        <div className="px-6">
          <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
            <label className="block text-sm font-semibold text-gray-700 mb-3">
              Product Images
            </label>
            <div className="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center hover:border-indigo-400 transition-colors cursor-pointer">
              <div className="flex flex-col items-center gap-3">
                <div className="w-16 h-16 bg-indigo-50 rounded-full flex items-center justify-center">
                  <Upload className="w-8 h-8 text-indigo-600" />
                </div>
                <div>
                  <p className="font-semibold text-gray-900 mb-1">
                    Upload product images
                  </p>
                  <p className="text-sm text-gray-500">
                    PNG, JPG up to 10MB
                  </p>
                </div>
                <button
                  type="button"
                  className="px-4 py-2 bg-indigo-50 text-indigo-700 rounded-lg font-semibold text-sm hover:bg-indigo-100 transition-colors"
                >
                  Choose Files
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Basic Information */}
        <div className="px-6">
          <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 space-y-4">
            <h2 className="font-bold text-gray-900">Basic Information</h2>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Product Name *
              </label>
              <input
                type="text"
                placeholder="e.g., Wireless Headphones Pro"
                value={formData.name}
                onChange={(e) => handleInputChange("name", e.target.value)}
                className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Description
              </label>
              <textarea
                placeholder="Describe your product..."
                value={formData.description}
                onChange={(e) => handleInputChange("description", e.target.value)}
                rows={4}
                className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent resize-none"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  Category *
                </label>
                <select
                  value={formData.category}
                  onChange={(e) => handleInputChange("category", e.target.value)}
                  className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent"
                  required
                >
                  <option value="">Select</option>
                  <option value="electronics">Electronics</option>
                  <option value="fashion">Fashion</option>
                  <option value="home">Home & Garden</option>
                  <option value="sports">Sports</option>
                  <option value="books">Books</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  Brand
                </label>
                <input
                  type="text"
                  placeholder="e.g., Apple"
                  value={formData.brand}
                  onChange={(e) => handleInputChange("brand", e.target.value)}
                  className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent"
                />
              </div>
            </div>
          </div>
        </div>

        {/* Product Variants */}
        <div className="px-6">
          <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="font-bold text-gray-900">Product Variants</h2>
              <button
                type="button"
                onClick={addVariant}
                className="flex items-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-700 rounded-xl font-semibold text-sm hover:bg-indigo-100 transition-colors"
              >
                <Plus className="w-4 h-4" />
                Add Variant
              </button>
            </div>

            <div className="space-y-4">
              {variants.map((variant, index) => (
                <div
                  key={variant.id}
                  className="p-4 bg-gray-50 rounded-xl border border-gray-200"
                >
                  <div className="flex items-center justify-between mb-3">
                    <p className="text-sm font-semibold text-gray-700">
                      Variant {index + 1}
                    </p>
                    {variants.length > 1 && (
                      <button
                        type="button"
                        onClick={() => removeVariant(variant.id)}
                        className="p-1 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">
                        Color
                      </label>
                      <input
                        type="text"
                        placeholder="e.g., Black"
                        value={variant.color}
                        onChange={(e) =>
                          updateVariant(variant.id, "color", e.target.value)
                        }
                        className="w-full px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">
                        Storage/Size
                      </label>
                      <input
                        type="text"
                        placeholder="e.g., 256GB"
                        value={variant.storage}
                        onChange={(e) =>
                          updateVariant(variant.id, "storage", e.target.value)
                        }
                        className="w-full px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">
                        Price ($)
                      </label>
                      <input
                        type="number"
                        placeholder="299.99"
                        value={variant.price}
                        onChange={(e) =>
                          updateVariant(variant.id, "price", e.target.value)
                        }
                        className="w-full px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent"
                        step="0.01"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">
                        Stock
                      </label>
                      <input
                        type="number"
                        placeholder="50"
                        value={variant.stock}
                        onChange={(e) =>
                          updateVariant(variant.id, "stock", e.target.value)
                        }
                        className="w-full px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent"
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </form>

      {/* Submit Button */}
      <div className="fixed bottom-16 left-0 right-0 bg-white border-t border-gray-200 z-20 shadow-lg">
        <div className="px-6 py-4">
          <button
            type="submit"
            form="product-form"
            className="w-full bg-indigo-700 text-white py-4 rounded-xl font-semibold hover:bg-indigo-800 transition-all shadow-lg"
          >
            Save Product
          </button>
        </div>
      </div>
    </div>
  );
}
