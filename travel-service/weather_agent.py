import requests
from datetime import datetime, timedelta
from typing import List, Dict, Union, Optional
from collections import defaultdict


# --- Helpers ---

def weather_code_to_description(code: int) -> str:
    mapping = {
        0: "Clear sky", 1: "Mainly clear", 2: "Partly cloudy", 3: "Overcast",
        45: "Fog", 48: "Depositing rime fog", 51: "Light drizzle", 53: "Moderate drizzle",
        55: "Dense drizzle", 56: "Light freezing drizzle", 57: "Dense freezing drizzle",
        61: "Slight rain", 63: "Moderate rain", 65: "Heavy rain", 66: "Light freezing rain",
        67: "Heavy freezing rain", 71: "Slight snow fall", 73: "Moderate snow fall",
        75: "Heavy snow fall", 77: "Snow grains", 80: "Slight rain showers",
        81: "Moderate rain showers", 82: "Violent rain showers", 85: "Slight snow showers",
        86: "Heavy snow showers", 95: "Thunderstorm", 96: "Thunderstorm with slight hail",
        99: "Thunderstorm with heavy hail"
    }
    return mapping.get(code, "Unknown")


def geocode_location(location: str) -> Optional[tuple[float, float]]:
    url = "https://nominatim.openstreetmap.org/search"
    params = {
        "q": location,
        "format": "json",
        "limit": 1,
        "accept-language": "en"
    }
    headers = {"User-Agent": "WeatherApp/1.0 (contact@example.com)"}
    try:
        resp = requests.get(url, params=params, headers=headers, timeout=10)
        resp.raise_for_status()
        results = resp.json()
        if results:
            return float(results[0]["lat"]), float(results[0]["lon"])
    except requests.RequestException:
        return None
    return None


def validate_date(date_str: str) -> Optional[datetime]:
    try:
        return datetime.strptime(date_str, "%Y-%m-%d")
    except ValueError:
        return None


# --- Main Weather Function ---

def get_weather(destination: str, start_date: str, days: int) -> Union[List[Dict], Dict[str, str]]:
    start = validate_date(start_date)
    if not start:
        return {"error": "Invalid date format. Use YYYY-MM-DD."}

    coords = geocode_location(destination)
    if not coords:
        return {"error": f"Could not geocode location: '{destination}'"}

    lat, lon = coords
    print(f"Coordinates for {destination}: {lat}, {lon}")
    end_date = (start + timedelta(days=days - 1)).strftime("%Y-%m-%d")

    url = "https://api.open-meteo.com/v1/forecast"
    params = {
        "latitude": lat,
        "longitude": lon,
        "start_date": start_date,
        "end_date": end_date,
        "hourly": ",".join([
            "temperature_2m", "weathercode", "precipitation_probability",
            "relative_humidity_2m", "wind_speed_10m"
        ]),
        "daily": "sunrise,sunset",
        "timezone": "Asia/Dhaka"
    }

    try:
        response = requests.get(url, params=params, timeout=10)
        response.raise_for_status()
        data = response.json()
        hourly = data.get("hourly", {})
        daily = data.get("daily", {})
    except requests.RequestException as e:
        return {"error": f"Weather API error: {str(e)}"}

    forecast_by_day = defaultdict(dict)
    time_list = hourly["time"]
    time_slots = {"morning": "09:00", "afternoon": "15:00", "night": "21:00"}

    for slot, time_str in time_slots.items():
        for i, t in enumerate(time_list):
            if t.endswith(time_str):
                date_key = t.split("T")[0]
                forecast_by_day[date_key][slot] = {
                    "temperature": hourly["temperature_2m"][i],
                    "conditions": weather_code_to_description(hourly["weathercode"][i]),
                    "precipitation_chance": hourly["precipitation_probability"][i],
                    "humidity": hourly["relative_humidity_2m"][i],
                    "wind_speed": hourly["wind_speed_10m"][i]
                }

    result = []
    for i, date in enumerate(daily["time"]):
        daily_forecast = {
            "date": date,
            "sunrise": daily["sunrise"][i][-5:],
            "sunset": daily["sunset"][i][-5:]
        }
        daily_forecast.update(forecast_by_day.get(date, {}))
        result.append(daily_forecast)

    return result


if __name__ == "__main__":
    import json
    result = get_weather("Dhaka", "2025-05-27", 3)
    print(json.dumps(result, indent=2))