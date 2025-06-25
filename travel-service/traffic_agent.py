import os
import requests
import json
from typing import Dict, Union
from dotenv import load_dotenv

# Load API key from .env
load_dotenv()
ORS_API_KEY = os.getenv("ORS_API_KEY")

# Mapping for simplified transport modes
MODE_MAP = {
    "car": "driving-car",
    "bus": "driving-car",        # ORS has no bus profile, approximated by car
    "bike": "cycling-regular"
}

def get_traffic(
    origin_lat: float,
    origin_lon: float,
    dest_lat: float,
    dest_lon: float,
    mode: str = "car"
) -> Union[Dict, Dict[str, str]]:
    """
    Fetch travel route and duration using OpenRouteService for car, bus, or bike.

    Parameters:
    ----------
    origin_lat : float
    origin_lon : float
    dest_lat : float
    dest_lon : float
    mode : str
        One of: 'car', 'bus', or 'bike'.

    Returns:
    -------
    Dict with travel details or error message.
    """
    if not ORS_API_KEY:
        return {"error": "ORS_API_KEY not set in environment."}

    # Translate user-friendly mode to ORS mode
    ors_mode = MODE_MAP.get(mode)
    if not ors_mode:
        return {"error": f"Invalid mode: {mode}. Use 'car', 'bus', or 'bike'."}

    url = f"https://api.openrouteservice.org/v2/directions/{ors_mode}"
    headers = {
        "Authorization": ORS_API_KEY,
        "Content-Type": "application/json"
    }
    body = {
        "coordinates": [
            [origin_lon, origin_lat],
            [dest_lon, dest_lat]
        ],
        "instructions": True
    }

    try:
        response = requests.post(url, json=body, headers=headers, timeout=10)
        response.raise_for_status()
        data = response.json()

        if "features" not in data or not data["features"]:
            return {
                "error": data.get("error", {}).get("message", "No route found."),
                "details": data
            }

        route = data["features"][0]["properties"]
        summary = route["summary"]
        segments = route.get("segments", [])
        steps = segments[0]["steps"] if segments and "steps" in segments[0] else []

        return {
            "mode": mode,
            "distance_km": round(summary["distance"] / 1000, 2),
            "estimated_time_min": round(summary["duration"] / 60, 1),
            "origin_waypoint": route.get("way_points", [None, None])[0],
            "destination_waypoint": route.get("way_points", [None, None])[1],
            "route_steps": [
                {
                    "instruction": step["instruction"],
                    "distance_m": step["distance"],
                    "duration_s": step["duration"]
                }
                for step in steps
            ],
            "note": "Powered by OpenRouteService"
        }

    except requests.RequestException as e:
        return {"error": f"OpenRouteService API error: {str(e)}"}

def print_route_steps(origin_lat, origin_lon, dest_lat, dest_lon, mode="car"):
    if not ORS_API_KEY:
        print("❌ ORS_API_KEY not set in .env")
        return

    ors_mode = MODE_MAP.get(mode)
    if not ors_mode:
        print("❌ Invalid mode. Use 'car', 'bus', or 'bike'.")
        return

    url = f"https://api.openrouteservice.org/v2/directions/{ors_mode}"
    headers = {
        "Authorization": ORS_API_KEY,
        "Content-Type": "application/json"
    }
    body = {
        "coordinates": [
            [origin_lon, origin_lat],
            [dest_lon, dest_lat]
        ],
        "instructions": True
    }

    try:
        response = requests.post(url, json=body, headers=headers, timeout=10)
        response.raise_for_status()
        data = response.json()

        features = data.get("features", [])
        if not features:
            print("❌ No route found.")
            return

        steps = features[0]["properties"]["segments"][0]["steps"]
        print(f"🧭 Route steps ({mode}):\n")
        for i, step in enumerate(steps, 1):
            print(f"{i}. {step['instruction']} — {round(step['distance'], 1)} m, {round(step['duration'], 1)} sec")

    except requests.RequestException as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    # Example usage
    result = get_traffic(
        origin_lat=24.3636, origin_lon=88.6241,  # Rajshahi
        dest_lat=24.8949, dest_lon=91.8687,      # Sylhet
        mode="car"  # Options: "car", "bus", "bike"
    )

    print(json.dumps(result, indent=2, ensure_ascii=False))

    # Also test the old function
    print("\n" + "="*50)
    print("Route steps:")
    print_route_steps(
        origin_lat=24.3636, origin_lon=88.6241,  # Rajshahi
        dest_lat=24.8949, dest_lon=91.8687,      # Sylhet
        mode="car"
    )
