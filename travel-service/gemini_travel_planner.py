import os
import json
from datetime import datetime, timedelta
from dotenv import load_dotenv
import google.generativeai as genai

# Import weather agent only (city data now comes from Spring Boot)
from weather_agent import get_weather

# Load environment variables
load_dotenv()

# Initialize Google Gemini API
gemini_api_key = os.getenv("GEMINI_API_KEY")
if not gemini_api_key:
    raise ValueError("GEMINI_API_KEY not found in environment variables")

genai.configure(api_key=gemini_api_key)

# Initialize Gemini model
model = genai.GenerativeModel('gemini-1.5-pro')

print(f"🤖 Gemini API initialized with key: {gemini_api_key[:10]}...")

def generate_trip_plan(origin, destination, start_date, day_count, budget, weather_data, city_data):
    """
    Generate a trip plan using Google Gemini LLM with weather and city database information.
    
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
    print("🌤️ Gemini API Key:", gemini_api_key[:10] + "..." if gemini_api_key else "Not found")
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
                "spot_name": spot.get('spot_name'),
                "description": spot.get('description', 'Tourist attraction'),
                "entry_fee": spot.get('entry_fee', 0)
            })
    
    if city_data.get('hotels'):
        for hotel in city_data['hotels']:
            hotels_summary.append({
                "hotel_name": hotel.get('hotel_name'),
                "price_range": f"{hotel.get('price_min', 0)}-{hotel.get('price_max', 0)}",
                "rating": hotel.get('rating', 0),
                "amenities": hotel.get('amenities', 'Standard amenities')
            })
    
    if city_data.get('restaurants'):
        for restaurant in city_data['restaurants']:
            restaurants_summary.append({
                "restaurant_name": restaurant.get('restaurant_name'),
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
    
    # Show all available data that AI should use
    if city_data.get('spots'):
        print("🏛️ Available spots that AI should use:")
        for spot in city_data['spots']:
            print(f"   • \"{spot.get('spot_name')}\"")
    
    if city_data.get('hotels'):
        print("🏨 Available hotels that AI should use:")
        for hotel in city_data['hotels']:
            print(f"   • \"{hotel.get('hotel_name')}\"")
    
    if city_data.get('restaurants'):
        print("🍽️ Available restaurants that AI should use:")
        for restaurant in city_data['restaurants']:
            print(f"   • \"{restaurant.get('restaurant_name')}\"")
    print()# Create a clean, focused prompt for the LLM
    prompt = f"""You are creating a {day_count}-day travel itinerary for {destination}.

## AVAILABLE DATA - USE EXACT NAMES:
{city_info}

## CRITICAL INSTRUCTIONS:
1. Use the EXACT field names from the data above:
   - For spots: use "spot_name" values exactly as shown
   - For hotels: use "hotel_name" values exactly as shown
   - For restaurants: use "restaurant_name" values exactly as shown
2. DO NOT use "Unknown" - use the actual names from the data
3. Generate exactly {day_count} days

## Trip Details:
- From: {origin} → To: {destination}
- Start: {start_date} | Duration: {day_count} days  
- Budget: ৳{budget}
- Weather: {weather_info}

## Generate this JSON structure using exact names from data above:
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
      "date": "{start_date}",
      "morning_activity": {{
        "spot_name": "[USE EXACT spot_name FROM DATA]",
        "time": "9:00 AM - 11:00 AM",
        "description": "Activity description",
        "entry_fee": 0,
        "image_url": "/trip-images/spot.jpg"
      }},
      "lunch_options": [{{
        "restaurant_name": "[USE EXACT restaurant_name FROM DATA]",
        "cuisine": "Local",
        "cost_per_person": 300,
        "rating": 4.0,
        "image_url": "/trip-images/restaurant.jpg"
      }}],
      "afternoon_activities": [{{
        "spot_name": "[USE EXACT spot_name FROM DATA]",
        "time": "1:00 PM - 5:00 PM",
        "description": "Activity description",
        "entry_fee": 0,
        "image_url": "/trip-images/spot.jpg"
      }}],
      "dinner_options": [{{
        "restaurant_name": "[USE EXACT restaurant_name FROM DATA]",
        "cuisine": "Local",
        "cost_per_person": 400,
        "rating": 4.0,
        "image_url": "/trip-images/restaurant.jpg"
      }}],
      "accommodation_options": [{{
        "hotel_name": "[USE EXACT hotel_name FROM DATA]",
        "rating": 4.0,
        "cost_per_night": 3000,
        "amenities": "Standard amenities",
        "image_url": "/trip-images/hotel.jpg"
      }}],
      "day_budget": {{
        "accommodation": 3000,
        "meals": 700,
        "activities": 0,
        "transport": 500,
        "total": 4200
      }}
    }}
  ],
  "budget_summary": {{
    "grand_total": {budget},
    "remaining": 0
  }}
}}

IMPORTANT: Generate exactly {day_count} days using the exact names from the data above."""

    try:
        # Call Gemini API with increased token limit for comprehensive plans
        print("🤖 Calling Gemini API...")
        response = model.generate_content(
            prompt,
            generation_config=genai.types.GenerationConfig(
                temperature=0.7,
                max_output_tokens=4096,
                response_mime_type="application/json"
            )
        )
        
        llm_response = response.text
        
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
    Extract filename from AI response URL or generate from name
    This function now works with the dynamic approach
    
    Args:
        item_name (str): Name of the spot, hotel, or restaurant, or full URL
        item_type (str): Type - 'spot', 'hotel', or 'restaurant'
    
    Returns:
        str: Image URL path or None if no valid image
    """
    if not item_name:
        # Return None instead of default - let frontend handle text fallback
        return None
    
    # If it's already a URL, extract the filename
    if item_name.startswith('http'):
        try:
            filename = item_name.split('/')[-1]
            
            # Clean up the filename - remove any prefixes like 'trip_images'
            if filename.startswith('trip_images'):
                filename = filename.replace('trip_images', '', 1)
            
            # Ensure it has an extension
            if '.' not in filename:
                filename += '.jpg'
                
            # Ensure filename doesn't start with underscore
            filename = filename.lstrip('_')
            
            return "/trip-images/" + filename
        except:
            return None
    
    # If it's already a processed URL path, just return it
    if item_name.startswith('/trip-images/'):
        return item_name
    
    # If it's just a name, create a filename
    # Convert to lowercase, replace spaces with underscores
    filename = item_name.lower().replace(' ', '_').replace('-', '_')
    filename = ''.join(c for c in filename if c.isalnum() or c in ['_', '.'])
    
    # Add .jpg extension if not present
    if not filename.endswith(('.jpg', '.jpeg', '.png', '.webp')):
        filename += '.jpg'
        
    return "/trip-images/" + filename

def customize_trip_plan(original_plan, user_prompt):
    """
    Customize an existing trip plan based on user's modification request using OpenAI LLM.
    
    Args:
        original_plan (dict): The original trip plan JSON containing all needed data
        user_prompt (str): User's customization request
        
    Returns:
        str: Customized trip plan from the LLM
    """
    
    try:
        # Check if OpenAI API key is available
        if not os.getenv("OPENAI_API_KEY"):
            print("❌ OPENAI_API_KEY not found in environment variables")
            raise Exception("OpenAI API key not configured")
        
        print(f"🔧 CUSTOMIZE DEBUG INFO:")
        print(f"   Original plan type: {type(original_plan)}")
        print(f"   Original plan keys: {list(original_plan.keys()) if isinstance(original_plan, dict) else 'Not a dict'}")
        print(f"   User prompt: {user_prompt}")
        print(f"   OpenAI client initialized: {client is not None}")
        
        # Convert original plan to string for the prompt
        original_plan_str = json.dumps(original_plan, indent=2, ensure_ascii=False)
        print(f"   Original plan JSON length: {len(original_plan_str)} characters")
        
        # Create a focused and intelligent prompt for trip customization
        prompt = f"""You are an expert travel planner specializing in trip customization. A user has an existing complete trip plan and wants to make specific modifications to it.

## Original Complete Trip Plan:
{original_plan_str}

## User's Modification Request:
"{user_prompt}"

## Your Task:
Intelligently modify the original trip plan according to the user's request. The original plan contains all the necessary data about available spots, hotels, restaurants, weather, and budget information.

## Customization Guidelines:

### Core Principles:
1. **Preserve Structure**: Keep the exact same JSON structure as the original plan
2. **Smart Changes**: Only modify what the user specifically requested
3. **Use Original Data**: Use spots, hotels, restaurants from the original plan's data
4. **Budget Awareness**: Stay within the original budget constraints
5. **Logical Consistency**: Ensure changes make practical sense

### Required Output Format:
Return a complete JSON trip plan with the EXACT same structure as the original plan.

### Image URLs: 
Use format: "/trip-images/[descriptive_name].jpg" for all activities

### Key Points:
- Maintain all required fields from original structure
- Be creative but realistic with changes
- Ensure modified plan is immediately usable
- Keep explanations within activity descriptions
- Update trip_theme and highlights if relevant

Now customize the trip plan based on the user's request:

Return ONLY the modified trip plan as valid JSON without any explanations or markdown formatting."""

        # Call Gemini API
        print("🤖 Calling Gemini API for trip customization...")
        response = model.generate_content(
            prompt,
            generation_config=genai.types.GenerationConfig(
                temperature=0.7,
                max_output_tokens=4096,
                response_mime_type="application/json"
            )
        )
        
        llm_response = response.text
        
        # Print the LLM response for debugging
        print("🎯 Customization LLM Response:")
        print("=" * 80)
        print(llm_response)
        print("=" * 80)
        
        return llm_response
        
    except Exception as e:
        print(f"❌ Error customizing trip plan: {str(e)}")
        print(f"❌ Error type: {type(e).__name__}")
        print(f"❌ Error details: {e}")
        
        # Return a fallback response
        fallback_response = {
            "error": "Failed to customize trip plan",
            "error_type": type(e).__name__,
            "error_details": str(e),
            "original_plan": original_plan,
            "user_request": user_prompt,
            "message": "Please try again with a different request or check system configuration"
        }
        return json.dumps(fallback_response, indent=2)
