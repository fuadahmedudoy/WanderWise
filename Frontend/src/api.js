import axios from 'axios';

// Create a dedicated Axios instance
const api = axios.create({
    
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

// API Functions for Trip Management
export const tripApi = {
    // Accept a trip plan
    acceptTrip: async (tripPlan) => {
        try {
            const response = await api.post('/api/accepted-trips/accept', {
                tripPlan: tripPlan
            });
            return response.data;
        } catch (error) {
            console.error('Error accepting trip:', error);
            throw error;
        }
    },

    // Customize an existing trip plan
    customizeTrip: async (originalPlan, userPrompt) => {
        try {
            const response = await api.post('/api/trip/customize', {
                originalPlan: originalPlan,
                userPrompt: userPrompt
            });
            return response.data;
        } catch (error) {
            console.error('Error customizing trip:', error);
            throw error;
        }
    },

    // Get all accepted trips for current user
    getMyAcceptedTrips: async () => {
        try {
            const response = await api.get('/api/accepted-trips/my-trips');
            return response.data;
        } catch (error) {
            console.error('Error fetching my trips:', error);
            throw error;
        }
    },

    // Get categorized trips (ongoing, past, upcoming)
    getCategorizedTrips: async () => {
        try {
            const response = await api.get('/api/accepted-trips/categorized');
            return response.data;
        } catch (error) {
            console.error('Error fetching categorized trips:', error);
            throw error;
        }
    },

    // Get a specific accepted trip by ID
    getAcceptedTripById: async (tripId) => {
        try {
            const response = await api.get(`/api/accepted-trips/${tripId}`);
            return response.data;
        } catch (error) {
            console.error('Error fetching trip:', error);
            throw error;
        }
    },

    // Get recent accepted trips
    getRecentAcceptedTrips: async () => {
        try {
            const response = await api.get('/api/accepted-trips/recent');
            return response.data;
        } catch (error) {
            console.error('Error fetching recent trips:', error);
            throw error;
        }
    },

    // Get trip statistics
    getTripStats: async () => {
        try {
            const response = await api.get('/api/accepted-trips/stats');
            return response.data;
        } catch (error) {
            console.error('Error fetching trip stats:', error);
            throw error;
        }
    },

    // Delete an accepted trip
    deleteAcceptedTrip: async (tripId) => {
        try {
            const response = await api.delete(`/api/accepted-trips/${tripId}`);
            return response.data;
        } catch (error) {
            console.error('Error deleting trip:', error);
            throw error;
        }
    }
};

export default api;