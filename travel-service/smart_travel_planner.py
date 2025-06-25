import os
import json
from datetime import datetime, timedelta
from dotenv import load_dotenv
from openai import OpenAI

# LangSmith imports for tracing
# from langsmith import traceable
# from langsmith.wrappers import wrap_openai

# Import weather agent only (city data now comes from Spring Boot)
from weather_agent import get_weather

# Load environment variables
load_dotenv()

# # Initialize LangSmith environment variables
# os.environ["LANGCHAIN_TRACING_V2"] = os.getenv("LANGCHAIN_TRACING_V2", "true")
# os.environ["LANGCHAIN_ENDPOINT"] = os.getenv("LANGCHAIN_ENDPOINT", "https://api.smith.langchain.com")
# os.environ["LANGCHAIN_API_KEY"] = os.getenv("LANGCHAIN_API_KEY", "")
# os.environ["LANGCHAIN_PROJECT"] = os.getenv("LANGCHAIN_PROJECT", "wanderwise")

# Initialize OpenAI client with LangSmith wrapper
key= os.getenv("OPENAI_API_KEY")
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

def generate_trip_plan(origin, destination, start_date, day_count, budget, weather_data, city_data):
    """
    Generate a trip plan using OpenAI LLM with weather and city database information.
    
    Args:
        origin (str): Origin city
        destination (str): Destination city  
        start_date (str): Start date in YYYY-MM-DD format
        day_count (int): Number of days for the trip
        budget (int): Total budget in Taka
        weather_data (list): Weather forecast for each day
        city_data (dict): Complete city database with spots, hotels, restaurants (from Spring Boot)
        
    Returns:
        str: Generated trip plan from the LLM
    """
      # Format weather data for the prompt
    print("🌤️The key...", key)
    weather_info = ""
    if isinstance(weather_data, list):
        for i, weather in enumerate(weather_data[:day_count]):
            date = weather.get('date', f'Day {i+1}')
            # Handle different weather data formats
            if 'morning' in weather:
                # Format from weather agent - show morning, afternoon, and night
                morning = weather.get('morning', {})
                afternoon = weather.get('afternoon', {})
                night = weather.get('night', {})
                
                # Morning info
                morning_conditions = morning.get('conditions', 'Unknown')
                morning_temp = morning.get('temperature', 'N/A')
                morning_precipitation = morning.get('precipitation_chance', 0)
                morning_humidity = morning.get('humidity', 0)
                
                # Afternoon info
                afternoon_conditions = afternoon.get('conditions', 'Unknown')
                afternoon_temp = afternoon.get('temperature', 'N/A')
                afternoon_precipitation = afternoon.get('precipitation_chance', 0)
                
                # Night info
                night_conditions = night.get('conditions', 'Unknown')
                night_temp = night.get('temperature', 'N/A')
                
                weather_info += f"Day {i+1} ({date}):\n"
                weather_info += f"  Morning: {morning_conditions}, {morning_temp}°C, Rain: {morning_precipitation}%, Humidity: {morning_humidity}%\n"
                weather_info += f"  Afternoon: {afternoon_conditions}, {afternoon_temp}°C, Rain: {afternoon_precipitation}%\n"
                weather_info += f"  Night: {night_conditions}, {night_temp}°C\n"
            else:
                # Handle simple format or fallback
                conditions = weather.get('condition', weather.get('conditions', 'Unknown'))
                temp_max = weather.get('temp_max', weather.get('temperature', 'N/A'))
                temp_min = weather.get('temp_min', 'N/A')
                rain_chance = weather.get('rain_chance', weather.get('precipitation_chance', 0))
                humidity = weather.get('humidity', 0)
                weather_info += f"Day {i+1} ({date}): {conditions}, High: {temp_max}°C, Low: {temp_min}°C, Rain: {rain_chance}%, Humidity: {humidity}%\n"
    else:
        weather_info = "Weather data unavailable\n"      # Format city data efficiently for the prompt (don't include all raw data)
    spots_summary = []
    hotels_summary = []
    restaurants_summary = []
    
    if city_data.get('spots'):
        for spot in city_data['spots']:
            spots_summary.append({
                "name": spot.get('spot_name', 'Unknown'),
                "description": spot.get('description', 'Tourist attraction'),
                "entry_fee": spot.get('entry_fee', 0)
            })
    
    if city_data.get('hotels'):
        for hotel in city_data['hotels']:
            hotels_summary.append({
                "name": hotel.get('hotel_name', 'Unknown'),
                "price_range": f"{hotel.get('price_min', 0)}-{hotel.get('price_max', 0)}",
                "rating": hotel.get('rating', 0),
                "amenities": hotel.get('amenities', 'Standard amenities')
            })
    
    if city_data.get('restaurants'):
        for restaurant in city_data['restaurants']:
            restaurants_summary.append({
                "name": restaurant.get('restaurant_name', 'Unknown'),
                "cuisine": restaurant.get('cuisine_type', 'Local'),
                "avg_cost": restaurant.get('avg_cost', 500),
                "rating": restaurant.get('rating', 4.0)
            })
    
    city_summary = {
        "spots": spots_summary,
        "hotels": hotels_summary,
        "restaurants": restaurants_summary
    }
    
    city_info = json.dumps(city_summary, indent=2, ensure_ascii=False)
      # Print debug information about the data being used
    print("📊 Data Summary:")
    # Weather data is a list of daily forecasts, not a dict
    if isinstance(weather_data, list) and weather_data:
        print(f"   Weather data: Available ({len(weather_data)} days)")
    elif isinstance(weather_data, dict) and weather_data.get('error'):
        print(f"   Weather data: Error - {weather_data.get('error')}")
    else:
        print("   Weather data: Not available")
    
    if city_data.get('success'):
        print(f"   Spots: {len(city_data.get('spots', []))}")
        print(f"   Hotels: {len(city_data.get('hotels', []))}")
        print(f"   Restaurants: {len(city_data.get('restaurants', []))}")
    print(f"   City data source: {city_data.get('data_source', 'Unknown')}")
    print()
    
    # Show sample of city data being used
    if city_data.get('spots'):
        print("🏛️ Sample spots being used:")
        for spot in city_data['spots'][:3]:
            print(f"   • {spot.get('spot_name', 'Unknown spot')}")
    
    if city_data.get('hotels'):
        print("🏨 Sample hotels being used:")
        for hotel in city_data['hotels'][:3]:
            print(f"   • {hotel.get('hotel_name', 'Unknown hotel')} - ৳{hotel.get('price_min', 'N/A')}")
    
    if city_data.get('restaurants'):
        print("🍽️ Sample restaurants being used:")
        for restaurant in city_data['restaurants'][:3]:
            print(f"   • {restaurant.get('restaurant_name', 'Unknown restaurant')} - ৳{restaurant.get('avg_cost', 'N/A')}")
    print()# Create a clean, focused prompt for the LLM
    prompt = f"""Create a detailed travel itinerary using the provided data.

## Trip Parameters:
- From: {origin} → To: {destination}
- Start: {start_date} | Duration: {day_count} days
- Budget: ৳{budget}

## Weather Forecast:
{weather_info}

## Available Data:
{city_info}

## Task:
Generate a comprehensive JSON travel plan using ALL the provided spots, hotels, and restaurants.

## Required JSON Structure:
{{
  "trip_summary": {{
    "origin": "{origin}",
    "destination": "{destination}",
    "start_date": "{start_date}",
    "duration": {day_count},
    "total_budget": {budget}
  }},
  "daily_itinerary": [
    {{
      "day": 1,
      "date": "YYYY-MM-DD",
      "weather": "weather description",
      "morning_activity": {{
        "spot_name": "use spot from data",
        "time": "9:00 AM - 11:00 AM",
        "description": "activity details",
        "entry_fee": 0,
        "image_url": ""
      }},
      "lunch_options": [
        {{
          "restaurant_name": "from data",
          "cuisine": "type",
          "cost_per_person": 400,
          "rating": 4.0
        }}
      ],
      "afternoon_activities": [
        {{
          "spot_name": "from data",
          "time": "2:00 PM - 5:00 PM",
          "description": "what to do",
          "entry_fee": 0
        }}
      ],
      "dinner_options": [
        {{
          "restaurant_name": "from data",
          "cuisine": "type",
          "cost_per_person": 500,
          "rating": 4.2
        }}
      ],
      "accommodation_options": [
        {{
          "hotel_name": "from data",
          "rating": 4.0,
          "cost_per_night": 3000,
          "amenities": "features"
        }}
      ],
      "day_budget": {{
        "accommodation": 3000,
        "meals": 900,
        "activities": 50,
        "transport": 200,
        "misc": 100,
        "total": 4250
      }}
    }}
  ],
  "budget_summary": {{
    "total_accommodation": 9000,
    "total_meals": 2700,
    "total_activities": 150,
    "total_transport": 600,
    "total_misc": 300,
    "grand_total": 12750,
    "remaining": 2250
  }}
}}

Instructions:
- Use EVERY spot, hotel, restaurant from the provided data
- Multiple options for meals and hotels
- Weather-appropriate activities
- Realistic timing and costs
- Include image URLs using the format: "/trip-images/[image_name].jpg"
- Match image names to location names (e.g., "Jaflong" -> "/trip-images/jaflong.jpg")
- Available images: jaflong.jpg, ratargul.jpg, lalakhal.jpg, sajek_valley.jpg, kaptai_lake.jpg, hanging_bridge.jpg, rajban_vihara.jpg, shahjalal_dargah.jpg, hotel_metro.jpg, garden_inn.jpg, sajek_resort.jpg, lalakhal_resort.jpg, paharika_inn.jpg, hotel_swamp_view.jpg, tribal_food.jpg, valley_cafe.jpg, woondal.jpg, blue_water.jpg, star_pacific.jpg, vihara_view.jpg, kutum_bari.jpg
- Return ONLY valid JSON"""

    try:
        # Call OpenAI API with increased token limit for comprehensive plans
        print("🤖 Calling OpenAI API...")
        response = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": "You are a professional travel planning assistant that creates detailed itineraries in valid JSON format. Always return properly formatted JSON without any markdown or explanations."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=3000,  # Increased for comprehensive plans
            temperature=0.7,
            response_format={ "type": "json_object" }  # Ensures JSON output
        )
        
        llm_response = response.choices[0].message.content
        
        # Print the LLM response for debugging
        print("🎯 LLM Response:")
        print("=" * 80)
        print(llm_response)
        print("=" * 80)
        
        return llm_response
        
    except Exception as e:
        print(f"❌ Error generating trip plan: {str(e)}")
        # Return a fallback JSON structure
        fallback_plan = {
            "trip_summary": {
                "origin": origin,
                "destination": destination,
                "start_date": start_date,
                "duration": day_count,
                "total_budget": budget,
                "error": "Failed to generate detailed plan"
            },
            "daily_itinerary": [],
            "total_budget_summary": {
                "grand_total": 0,
                "budget_remaining": budget,
                "error": f"Plan generation failed: {str(e)}"
            }
        }
        return json.dumps(fallback_plan, indent=2)

def get_image_url(item_name, item_type="spot"):
    """
    Map location names to image URLs from the trip-images folder
    
    Args:
        item_name (str): Name of the spot, hotel, or restaurant
        item_type (str): Type - 'spot', 'hotel', or 'restaurant'
    
    Returns:
        str: Image URL path
    """
    base_url = "/trip-images/"
    
    # Normalize name for matching (lowercase, remove spaces/special chars)
    normalized_name = item_name.lower().replace(" ", "_").replace("'", "").replace("-", "_")
    
    # Image mapping dictionary based on available images
    image_map = {
        # Tourist Spots
        "jaflong": "jaflong.jpg",
        "jaflong_tea_garden": "jaflong_view.jpg", 
        "hanging_bridge": "hanging_bridge.jpg",
        "ratargul_swamp_forest": "ratargul.jpg",
        "ratargul": "ratargul.jpg",
        "lalakhal": "lalakhal.jpg",
        "kaptai_lake": "kaptai_lake.jpg",
        "sajek_valley": "sajek_valley.jpg",
        "rajban_vihara": "rajban_vihara.jpg",
        "shahjalal_mazar": "shahjalal_dargah.jpg",
        "shahjalal_dargah": "shahjalal_dargah.jpg",
        "blue_water_restaurant": "blue_water.jpg",
        
        # Hotels
        "hotel_metro_international": "hotel_metro.jpg",
        "hotel_metro": "hotel_metro.jpg",
        "swamp_view_resort": "hotel_swamp_view.jpg",
        "garden_inn": "garden_inn.jpg",
        "sajek_resort": "sajek_resort.jpg",
        "lalakhal_resort": "lalakhal_resort.jpg",
        "paharika_inn": "paharika_inn.jpg",
        "lake_view_resort": "lake_view_rangamati.jpg",
        
        # Restaurants
        "tribal_cultural_restaurant": "tribal_food.jpg",
        "tribal_food_corner": "tribal_food.jpg",
        "star_pacific_hotel": "star_pacific.jpg",
        "valley_cafe": "valley_cafe.jpg",
        "woondal_restaurant": "woondal.jpg",
        "kutum_bari": "kutum_bari.jpg",
        "blue_water": "blue_water.jpg",
        "vihara_view_restaurant": "vihara_view.jpg"
    }
    
    # Try exact match first
    if normalized_name in image_map:
        return base_url + image_map[normalized_name]
    
    # Try partial matches for flexibility
    for key, image in image_map.items():
        if key in normalized_name or normalized_name in key:
            return base_url + image
    
    # Default images by type
    defaults = {
        "spot": "sajek_valley.jpg",  # Default scenic image
        "hotel": "garden_inn.jpg",   # Default hotel image  
        "restaurant": "valley_cafe.jpg"  # Default restaurant image
    }
    return base_url + defaults.get(item_type, "sajek_valley.jpg")
