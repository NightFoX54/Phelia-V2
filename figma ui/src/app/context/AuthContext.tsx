import { createContext, useContext, useState, ReactNode } from 'react';

export type UserRole = 'customer' | 'store' | 'admin';

interface User {
  email: string;
  role: UserRole;
  name?: string;
  storeName?: string;
}

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => boolean;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Dummy login credentials
const DUMMY_USERS = [
  { email: 'user@test.com', password: '123456', role: 'customer' as UserRole, name: 'John Doe' },
  { email: 'store@test.com', password: '123456', role: 'store' as UserRole, name: 'Store Owner', storeName: 'TechStore Pro' },
  { email: 'admin@test.com', password: '123456', role: 'admin' as UserRole, name: 'Admin User' },
];

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const login = (email: string, password: string): boolean => {
    const foundUser = DUMMY_USERS.find(
      (u) => u.email === email && u.password === password
    );
    
    if (foundUser) {
      const userData = {
        email: foundUser.email,
        role: foundUser.role,
        name: foundUser.name,
        storeName: foundUser.storeName,
      };
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
      return true;
    }
    return false;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        login,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
