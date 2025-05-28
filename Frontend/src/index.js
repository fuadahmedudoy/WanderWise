import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './styles/global.css';

console.log("🚀 Trip Planner App starting up...");
console.log("🚀 Current URL:", window.location.href);
console.log("🚀 Checking localStorage for existing auth data...");
console.log("   - Token:", localStorage.getItem('token') ? "✅ Found" : "❌ Not found");
console.log("   - User:", localStorage.getItem('currentUser') ? "✅ Found" : "❌ Not found");

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

console.log("✅ Trip Planner App rendered successfully");