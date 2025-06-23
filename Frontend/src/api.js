import axios from 'axios';

// Create a dedicated Axios instance
const api = axios.create({
    // You can set your base URL here if you want
    // baseURL: 'http://localhost:8080' 
});

// ---- Request Interceptor ----
// This function will be called before every request is sent.
api.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        console.log('INTERCEPTOR: Retrieved token from localStorage:', token);

        if (token) {
            console.log('INTERCEPTOR: Attaching token to Authorization header.');
            config.headers['Authorization'] = `Bearer ${token}`;
        } else {
            console.warn('INTERCEPTOR: No token found in localStorage.');
        }
        return config;
    },
    error => {
        // This function will be called if there's an error setting up the request
        console.error('INTERCEPTOR: Error in request setup:', error);
        return Promise.reject(error);
    }
);

// ---- Response Interceptor ----
// This function will be called for every response that comes back from the API.
api.interceptors.response.use(
    response => {
        // Any status code that lie within the range of 2xx cause this function to trigger
        return response;
    },
    error => {
        // Any status codes that falls outside the range of 2xx cause this function to trigger
        console.error('API Error Response:', error.response);
        
        // If we get a 401 Unauthorized, it means the token is bad.
        // We should log the user out and redirect them to the login page.
        if (error.response && error.response.status === 401) {
            console.error("GLOBAL ERROR HANDLER: 401 Unauthorized. Token is invalid or expired. Logging out.");
            localStorage.removeItem('token');
            localStorage.removeItem('currentUser');
            
            // Redirect to login page
            if (window.location.pathname !== '/auth/login') {
                window.location.href = '/auth/login';
            }
        }
        
        return Promise.reject(error);
    }
);

export default api;