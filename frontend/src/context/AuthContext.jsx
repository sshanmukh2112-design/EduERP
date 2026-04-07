import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('eduerp_user');
    const storedRole = localStorage.getItem('eduerp_role');
    const storedToken = localStorage.getItem('eduerp_token');

    if (storedUser && storedRole && storedToken) {
      setUser(JSON.parse(storedUser));
      setRole(storedRole);
      setToken(storedToken);
    }
    setLoading(false);
  }, []);

  const login = async (email, password, selectedRole) => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      const data = await response.json();

      if (response.ok && data.user) {
        if (data.user.role.toLowerCase() === selectedRole.toLowerCase()) {
          const userData = { email: data.user.email, name: data.user.name, role: selectedRole };
          const backendToken = 'mock-token-' + Date.now(); // Replace when JWT is implemented in backend

          setUser(userData);
          setRole(selectedRole);
          setToken(backendToken);

          localStorage.setItem('eduerp_user', JSON.stringify(userData));
          localStorage.setItem('eduerp_role', selectedRole);
          localStorage.setItem('eduerp_token', backendToken);

          return { success: true };
        } else {
          return { success: false, message: `Wrong credentials.\nThese credentials are registered in the ${data.user.role.toLowerCase()} role.` };
        }
      }
      return { success: false, message: data.message || 'Invalid credentials. Please try again.' };
    } catch (err) {
      console.error("Login request failed", err);
      return { success: false, message: 'Could not connect to the server.' };
    }
  };

  const register = async (name, email, password, confirmPassword, roleToRegister) => {
    try {
      const response = await fetch('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name,
          email,
          password,
          confirmPassword,
          role: roleToRegister.toUpperCase()
        })
      });
      const data = await response.json();

      if (response.ok) {
        return { success: true, message: data.message };
      } else {
        return { success: false, message: data.message || 'Registration failed.' };
      }
    } catch (err) {
      console.error("Signup request failed", err);
      return { success: false, message: 'Could not connect to the server.' };
    }
  };

  const logout = () => {
    setUser(null);
    setRole(null);
    setToken(null);
    localStorage.removeItem('eduerp_user');
    localStorage.removeItem('eduerp_role');
    localStorage.removeItem('eduerp_token');
  };

  const isAuthenticated = !!token;

  return (
    <AuthContext.Provider value={{ user, role, token, isAuthenticated, loading, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
