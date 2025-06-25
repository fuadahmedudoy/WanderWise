"""
Flask service for WanderWise Travel Planner
Integrates with backend database for travel data and uses intelligent trip planning
"""

from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS
import sys
import os
import json

# Add the current directory to path for local imports
sys.path.append(os.path.dirname(__file__))

try:
    from weather_agent import get_weather
    from smart_travel_planner import generate_trip_plan
except ImportError as e:
    print(f"Import error: {e}")
    print("Make sure all agent files are in the travel-service directory")

app = Flask(__name__)
CORS(app)

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({"status": "healthy", "service": "travel-planner"})

@app.route('/plan-trip', methods=['POST'])
def plan_trip():
    """
    Plan a complete trip with all details using the local travel planner graph
    
    Expected JSON payload:
    {
        "destination": "Sylhet",
        "start_date": "2025-06-25",
        "duration_days": 3,        "budget": 50000,
        "origin": "Dhaka"
    }
    """
    try:
        data = request.get_json()
          # Debug: Print incoming request data
        print("📨 INCOMING REQUEST DATA:")
        print("=" * 50)
        print(f"Destination: {data.get('destination')}")
        print(f"Start Date: {data.get('start_date')}")
        print(f"Duration Days: {data.get('duration_days')}")
        print(f"Budget: {data.get('budget')}")
        print(f"Origin: {data.get('origin')}")
        print(f"User Data: {data.get('user_data', {}).get('userEmail', 'Unknown')}")
        print(f"City Data Available: {data.get('city_data') is not None}")
        if data.get('city_data'):
            city_data_from_spring = data.get('city_data')
            print(f"City Data Keys: {list(city_data_from_spring.keys())}")
            print(f"Spots: {len(city_data_from_spring.get('spots', []))}")
            print(f"Hotels: {len(city_data_from_spring.get('hotels', []))}")
            print(f"Restaurants: {len(city_data_from_spring.get('restaurants', []))}")
        print("=" * 50)
        
        # Validate required fields
        if not data or not data.get('destination'):
            return jsonify({"error": "Destination is required", "success": False}), 400
          # Extract the 5 core input parameters from Spring Boot
        user_data = data.get('user_data', {})  # Added by Spring Boot
        city_data = data.get('city_data', {})  # Added by Spring Boot
        destination = data.get('destination', 'Sylhet')
        start_date = data.get('start_date', '2025-06-25')
        duration_days = data.get('duration_days', 3)
        budget = data.get('budget', 15000)
        origin = data.get('origin', 'Dhaka')
        
        # Validate and ensure proper types
        try:
            duration_days = int(duration_days) if duration_days is not None else 3
            budget = float(budget) if budget is not None else 15000
        except (ValueError, TypeError):
            duration_days = 3
            budget = 15000        
        print(f"🌟 Planning trip: {origin} → {destination} ({duration_days} days, ৳{budget})")
        print(f"👤 User: {user_data.get('userEmail', 'Unknown')}")
          # Check if city data is available from Spring Boot backend
        if not city_data or not city_data.get('success'):
            print("⚠️ No valid city data from Spring Boot backend - no real data found in database")
            print("❌ Cannot generate trip plan without real destination data")
            return jsonify({
                "success": False,
                "error": f"No destination data found for {destination}",
                "message": f"The destination '{destination}' was not found in our database. Please check the spelling or try a different destination.",
                "data_source": "no-data-found"
            }), 404
        else:
            print(f"📊 Using Spring Boot city data: {len(city_data.get('spots', []))} spots, {len(city_data.get('hotels', []))} hotels, {len(city_data.get('restaurants', []))} restaurants")
        
        # Get weather data
        print("🌤️ Fetching weather data...")
        weather_data = get_weather(destination, start_date, duration_days)
        
        if isinstance(weather_data, dict) and "error" in weather_data:
            print(f"⚠️ Weather service unavailable: {weather_data['error']}")
            # Continue without weather data
            weather_data = []        # Generate trip plan using AI with the 5 core fields + city data
        print("🤖 Generating intelligent trip plan...")
        trip_plan_response = generate_trip_plan(
            origin=origin,
            destination=destination,
            start_date=start_date,
            day_count=duration_days,
            budget=budget,
            weather_data=weather_data,
            city_data=city_data
        )
        # Print the raw AI response for debugging
        print("📝 RAW AI RESPONSE START ===============================")
        print(trip_plan_response)
        print("📝 RAW AI RESPONSE END   ===============================")
        # Parse the AI response
        try:
            # Clean the response by removing markdown code blocks if present
            cleaned_response = trip_plan_response.strip()
            if cleaned_response.startswith("```json"):
                cleaned_response = cleaned_response[7:]
            if cleaned_response.endswith("```"):
                cleaned_response = cleaned_response[:-3]
            cleaned_response = cleaned_response.strip()
            
            print(f"🧹 Cleaned AI Response Length: {len(cleaned_response)} characters")
            
            trip_plan = json.loads(cleaned_response)
            print(f"✅ Successfully parsed JSON with keys: {list(trip_plan.keys())}")
            
            # Enhance with proper image URLs
            trip_plan = enhance_trip_plan_with_images(trip_plan)
            print(f"🖼️ Enhanced trip plan with images")
            
        except json.JSONDecodeError as e:
            print(f"⚠️ JSON parsing failed: {str(e)}")
            print(f"🔍 Raw response preview: {trip_plan_response[:200]}...")
            # Return raw response for debugging
            trip_plan = {
                "error": "Failed to parse AI response",
                "raw_response": trip_plan_response[:500] + "..." if len(trip_plan_response) > 500 else trip_plan_response
            }        # Format response for Spring Boot backend
        response = {
            "success": True,
            "destination": destination,
            "origin": origin,
            "duration_days": duration_days,
            "start_date": start_date,
            "budget": budget,            "trip_plan": trip_plan,
            "user_data": user_data,  # Include user context
            "data_summary": {
                "weather_available": isinstance(weather_data, list) and len(weather_data) > 0,
                "city_data_source": city_data.get('data_source', 'spring_boot_backend'),
                "spots_count": len(city_data.get('spots', [])),
                "hotels_count": len(city_data.get('hotels', [])),
                "restaurants_count": len(city_data.get('restaurants', [])),
                "processed_by": "python-travel-service"
            }
        }
        
        # 🖨️ DEBUG: Print what's being sent to frontend
        print("\n" + "="*80)
        print("📤 SENDING TO FRONTEND:")
        print("="*80)
        print(f"✅ Success: {response['success']}")
        print(f"📍 Destination: {response['destination']}")
        print(f"🏠 Origin: {response['origin']}")
        print(f"📅 Duration: {response['duration_days']} days")
        print(f"💰 Budget: ৳{response['budget']}")
        
        if response['trip_plan']:
            print(f"📋 Trip Plan Keys: {list(response['trip_plan'].keys())}")
            if 'daily_itinerary' in response['trip_plan']:
                itinerary = response['trip_plan']['daily_itinerary']
                print(f"📆 Days in Itinerary: {len(itinerary)}")
                  # Show first day details
                if itinerary:
                    day1 = itinerary[0]
                    print(f"🌅 Day 1 Morning Activity: {day1.get('morning_activity', {}).get('spot_name', 'N/A')}")
                    if day1.get('morning_activity', {}).get('image_url'):
                        print(f"🖼️ Day 1 Image: {day1['morning_activity']['image_url']}")
                    print(f"🍽️ Day 1 Lunch Options: {len(day1.get('lunch_options', []))}")
                    print(f"🏨 Day 1 Hotel Options: {len(day1.get('accommodation_options', []))}")
            
            if 'budget_summary' in response['trip_plan']:
                budget_summary = response['trip_plan']['budget_summary']
                print(f"💵 Total Cost: ৳{budget_summary.get('grand_total', 'N/A')}")
                print(f"💰 Remaining: ৳{budget_summary.get('remaining', 'N/A')}")
        
        print(f"📊 Data Summary: {response['data_summary']}")
        print("="*80)
        print("🚀 Response sent to frontend successfully!")
        print("="*80)
        
        # 📋 Print the complete JSON being sent to frontend
        print("\n" + "🔍 COMPLETE JSON RESPONSE TO FRONTEND:")
        print("=" * 100)
        print(json.dumps(response, indent=2, ensure_ascii=False))
        print("=" * 100 + "\n")
        
        return jsonify(response)
        
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e),
            "message": "Failed to plan trip"
        }), 500

@app.route('/trip-images/<filename>')
def serve_trip_image(filename):
    """Serve trip images from the Frontend public directory"""
    try:
        # Path to the Frontend public trip-images directory
        images_path = os.path.join(os.path.dirname(__file__), '..', 'Frontend', 'public', 'trip-images')
        return send_from_directory(images_path, filename)
    except FileNotFoundError:
        return jsonify({"error": "Image not found"}), 404

def enhance_trip_plan_with_images(trip_plan):
    """
    Enhance the trip plan by adding proper image URLs for all locations
    """
    if not isinstance(trip_plan, dict) or 'daily_itinerary' not in trip_plan:
        return trip_plan
    
    # Image mapping
    image_map = {
        # Spots (case-insensitive matching)
        "jaflong": "/trip-images/jaflong.jpg",
        "jaflong tea garden": "/trip-images/jaflong_view.jpg",
        "ratargul": "/trip-images/ratargul.jpg", 
        "ratargul swamp forest": "/trip-images/ratargul.jpg",
        "lalakhal": "/trip-images/lalakhal.jpg",
        "sajek valley": "/trip-images/sajek_valley.jpg",
        "kaptai lake": "/trip-images/kaptai_lake.jpg",
        "hanging bridge": "/trip-images/hanging_bridge.jpg",
        "rajban vihara": "/trip-images/rajban_vihara.jpg",
        "shahjalal mazar": "/trip-images/shahjalal_dargah.jpg",
        "shahjalal dargah": "/trip-images/shahjalal_dargah.jpg",
        
        # Hotels
        "hotel metro": "/trip-images/hotel_metro.jpg",
        "metro international": "/trip-images/hotel_metro.jpg",
        "garden inn": "/trip-images/garden_inn.jpg",
        "sajek resort": "/trip-images/sajek_resort.jpg",
        "lalakhal resort": "/trip-images/lalakhal_resort.jpg",
        "paharika inn": "/trip-images/paharika_inn.jpg",
        "swamp view": "/trip-images/hotel_swamp_view.jpg",
        "lake view": "/trip-images/lake_view_rangamati.jpg",
        
        # Restaurants
        "tribal": "/trip-images/tribal_food.jpg",
        "valley cafe": "/trip-images/valley_cafe.jpg",
        "woondal": "/trip-images/woondal.jpg",
        "blue water": "/trip-images/blue_water.jpg",
        "star pacific": "/trip-images/star_pacific.jpg",
        "vihara view": "/trip-images/vihara_view.jpg",
        "kutum bari": "/trip-images/kutum_bari.jpg"
    }
    
    def get_image_for_name(name, default_type="spot"):
        if not name:
            return "/trip-images/sajek_valley.jpg"
            
        name_lower = name.lower()
        
        # Try exact match first
        for key, url in image_map.items():
            if key in name_lower or name_lower in key:
                return url
        
        # Default images by type
        defaults = {
            "spot": "/trip-images/sajek_valley.jpg",
            "hotel": "/trip-images/garden_inn.jpg", 
            "restaurant": "/trip-images/valley_cafe.jpg"
        }
        return defaults.get(default_type, "/trip-images/sajek_valley.jpg")
    
    # Enhance each day's itinerary
    for day in trip_plan.get('daily_itinerary', []):
        # Morning activity
        if 'morning_activity' in day:
            activity = day['morning_activity']
            if 'spot_name' in activity and not activity.get('image_url'):
                activity['image_url'] = get_image_for_name(activity['spot_name'], 'spot')
        
        # Afternoon activities
        if 'afternoon_activities' in day:
            for activity in day['afternoon_activities']:
                if 'spot_name' in activity and not activity.get('image_url'):
                    activity['image_url'] = get_image_for_name(activity['spot_name'], 'spot')
        
        # Lunch options
        if 'lunch_options' in day:
            for restaurant in day['lunch_options']:
                if 'restaurant_name' in restaurant and not restaurant.get('image_url'):
                    restaurant['image_url'] = get_image_for_name(restaurant['restaurant_name'], 'restaurant')
        
        # Dinner options  
        if 'dinner_options' in day:
            for restaurant in day['dinner_options']:
                if 'restaurant_name' in restaurant and not restaurant.get('image_url'):
                    restaurant['image_url'] = get_image_for_name(restaurant['restaurant_name'], 'restaurant')
        
        # Accommodation options
        if 'accommodation_options' in day:
            for hotel in day['accommodation_options']:
                if 'hotel_name' in hotel and not hotel.get('image_url'):
                    hotel['image_url'] = get_image_for_name(hotel['hotel_name'], 'hotel')
    
    return trip_plan

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5001)
