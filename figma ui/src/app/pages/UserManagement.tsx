import { useState } from "react";
import { ChevronLeft, Search, MoreVertical, Trash2, ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router";

interface User {
  id: string;
  name: string;
  email: string;
  role: "customer" | "store" | "admin";
  joinDate: string;
}

const initialUsers: User[] = [
  {
    id: "1",
    name: "John Doe",
    email: "user@test.com",
    role: "customer",
    joinDate: "2024-01-15",
  },
  {
    id: "2",
    name: "Store Owner",
    email: "store@test.com",
    role: "store",
    joinDate: "2024-02-10",
  },
  {
    id: "3",
    name: "Admin User",
    email: "admin@test.com",
    role: "admin",
    joinDate: "2024-01-01",
  },
  {
    id: "4",
    name: "Sarah Johnson",
    email: "sarah.j@email.com",
    role: "customer",
    joinDate: "2024-03-05",
  },
  {
    id: "5",
    name: "Mike's Electronics",
    email: "mike@store.com",
    role: "store",
    joinDate: "2024-02-20",
  },
  {
    id: "6",
    name: "Emily Chen",
    email: "emily.chen@email.com",
    role: "customer",
    joinDate: "2024-03-12",
  },
  {
    id: "7",
    name: "TechMart Store",
    email: "contact@techmart.com",
    role: "store",
    joinDate: "2024-01-25",
  },
];

export function UserManagement() {
  const navigate = useNavigate();
  const [users, setUsers] = useState<User[]>(initialUsers);
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState<string>("all");
  const [activeMenu, setActiveMenu] = useState<string | null>(null);

  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesRole = roleFilter === "all" || user.role === roleFilter;
    return matchesSearch && matchesRole;
  });

  const handleChangeRole = (userId: string, newRole: "customer" | "store" | "admin") => {
    setUsers(users.map((u) => (u.id === userId ? { ...u, role: newRole } : u)));
    setActiveMenu(null);
  };

  const handleDeleteUser = (userId: string) => {
    if (window.confirm("Are you sure you want to delete this user?")) {
      setUsers(users.filter((u) => u.id !== userId));
    }
    setActiveMenu(null);
  };

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case "admin":
        return "bg-purple-100 text-purple-700";
      case "store":
        return "bg-blue-100 text-blue-700";
      case "customer":
        return "bg-green-100 text-green-700";
      default:
        return "bg-gray-100 text-gray-700";
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
            <h1 className="font-bold text-gray-900">User Management</h1>
            <p className="text-sm text-gray-500">{filteredUsers.length} users</p>
          </div>
        </div>

        {/* Search Bar */}
        <div className="relative mb-4">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            placeholder="Search users by name or email..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-12 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:border-transparent"
          />
        </div>

        {/* Role Filter */}
        <div className="flex gap-2 overflow-x-auto pb-2">
          <button
            onClick={() => setRoleFilter("all")}
            className={`px-4 py-2 rounded-xl font-semibold text-sm whitespace-nowrap transition-colors ${
              roleFilter === "all"
                ? "bg-indigo-700 text-white"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
            }`}
          >
            All Users
          </button>
          <button
            onClick={() => setRoleFilter("customer")}
            className={`px-4 py-2 rounded-xl font-semibold text-sm whitespace-nowrap transition-colors ${
              roleFilter === "customer"
                ? "bg-green-600 text-white"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
            }`}
          >
            Customers
          </button>
          <button
            onClick={() => setRoleFilter("store")}
            className={`px-4 py-2 rounded-xl font-semibold text-sm whitespace-nowrap transition-colors ${
              roleFilter === "store"
                ? "bg-blue-600 text-white"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
            }`}
          >
            Store Owners
          </button>
          <button
            onClick={() => setRoleFilter("admin")}
            className={`px-4 py-2 rounded-xl font-semibold text-sm whitespace-nowrap transition-colors ${
              roleFilter === "admin"
                ? "bg-purple-600 text-white"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
            }`}
          >
            Admins
          </button>
        </div>
      </header>

      {/* User List */}
      <div className="px-6 py-6 space-y-3">
        {filteredUsers.map((user) => (
          <div
            key={user.id}
            className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100"
          >
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-start gap-3">
                <div className="w-12 h-12 bg-gradient-to-br from-indigo-400 to-purple-500 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="font-bold text-white text-lg">
                    {user.name.charAt(0).toUpperCase()}
                  </span>
                </div>
                <div>
                  <p className="font-semibold text-gray-900">{user.name}</p>
                  <p className="text-sm text-gray-500">{user.email}</p>
                  <p className="text-xs text-gray-400 mt-1">
                    Joined {new Date(user.joinDate).toLocaleDateString()}
                  </p>
                </div>
              </div>

              <div className="relative">
                <button
                  onClick={() =>
                    setActiveMenu(activeMenu === user.id ? null : user.id)
                  }
                  className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  <MoreVertical className="w-5 h-5 text-gray-400" />
                </button>

                {/* Dropdown Menu */}
                {activeMenu === user.id && (
                  <div className="absolute right-0 top-full mt-2 bg-white rounded-xl shadow-lg border border-gray-200 py-2 min-w-[180px] z-20">
                    <div className="px-3 py-2 text-xs font-semibold text-gray-500 border-b border-gray-100">
                      Change Role
                    </div>
                    <button
                      onClick={() => handleChangeRole(user.id, "customer")}
                      className="w-full px-4 py-2 text-left text-sm hover:bg-gray-50 transition-colors flex items-center gap-2"
                    >
                      <span className="w-2 h-2 bg-green-500 rounded-full"></span>
                      Customer
                    </button>
                    <button
                      onClick={() => handleChangeRole(user.id, "store")}
                      className="w-full px-4 py-2 text-left text-sm hover:bg-gray-50 transition-colors flex items-center gap-2"
                    >
                      <span className="w-2 h-2 bg-blue-500 rounded-full"></span>
                      Store Owner
                    </button>
                    <button
                      onClick={() => handleChangeRole(user.id, "admin")}
                      className="w-full px-4 py-2 text-left text-sm hover:bg-gray-50 transition-colors flex items-center gap-2"
                    >
                      <span className="w-2 h-2 bg-purple-500 rounded-full"></span>
                      Admin
                    </button>
                    <div className="border-t border-gray-100 mt-2 pt-2">
                      <button
                        onClick={() => handleDeleteUser(user.id)}
                        className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 transition-colors flex items-center gap-2"
                      >
                        <Trash2 className="w-4 h-4" />
                        Delete User
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>

            <div className="flex items-center justify-between">
              <span
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold ${getRoleBadgeColor(
                  user.role
                )}`}
              >
                {user.role === "admin" ? "Admin" : user.role === "store" ? "Store Owner" : "Customer"}
              </span>
            </div>
          </div>
        ))}

        {filteredUsers.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-500">No users found</p>
          </div>
        )}
      </div>

      {/* Click outside to close menu */}
      {activeMenu && (
        <div
          className="fixed inset-0 z-10"
          onClick={() => setActiveMenu(null)}
        ></div>
      )}
    </div>
  );
}
