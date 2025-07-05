import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';
import '../styles/weather-details.css';

const WeatherDetails = () => {
    const [weatherData, setWeatherData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const { tripId } = useParams();

    useEffect(() => {
        const fetchWeatherDetails = async () => {
            try {
                const response = await api.get(`/api/weather/details/${tripId}`);
                setWeatherData(response.data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchWeatherDetails();
    }, [tripId]);

    if (loading) {
        return <div className="weather-details-container">Loading...</div>;
    }

    if (error) {
        return (
            <div className="weather-details-container">
                <div className="weather-details-card">
                    <h2>Error</h2>
                    <p>{error}</p>
                    <button className="back-button" onClick={() => navigate(-1)}>
                        Go Back
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="weather-details-container">
            <div className="weather-details-card">
                <h2>Weather Details for {weatherData.location}</h2>
                
                {weatherData.alerts && weatherData.alerts.length > 0 && (
                    <div className="weather-alerts">
                        <h3>⚠️ Weather Alerts</h3>
                        {weatherData.alerts.map((alert, index) => (
                            <p key={index} className="alert">{alert}</p>
                        ))}
                    </div>
                )}

                <div className="forecast-container">
                    <h3>Forecast</h3>
                    {weatherData.forecast.map((day, index) => (
                        <div key={index} className="forecast-day">
                            <h4>{day.date}</h4>
                            <p>Condition: {day.condition}</p>
                            <p>Temperature: {day.avg_temp_c}°C</p>
                            <p>Precipitation: {day.precipitation}mm</p>
                            <p>Wind: {day.wind_kph} km/h</p>
                        </div>
                    ))}
                </div>

                <button className="back-button" onClick={() => navigate(-1)}>
                    Go Back
                </button>
            </div>
        </div>
    );
};

export default WeatherDetails; 