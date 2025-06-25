package com.example.demo.service;

import com.example.demo.Repository.*;
import com.example.demo.entity.FeaturedDestination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CityDataService {

    @Autowired
    private FeaturedDestinationRepository featuredDestinationRepository;

    @Autowired
    private TripSpotSuggestionRepository tripSpotSuggestionRepository;

    @Autowired
    private TripAccommodationRepository tripAccommodationRepository;

    @Autowired
    private TripFoodOptionRepository tripFoodOptionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getCityData(String destination, String userEmail) {
        try {
            System.out.println("🏛️ CityDataService.getCityData called for: " + destination + " by user: " + userEmail);

            // Fetch real data from database only
            Map<String, Object> cityData = fetchRealCityData(destination);

            // Only mark as success if we actually found city data
            boolean hasValidCity = cityData.get("city") != null;
            cityData.put("success", hasValidCity);
            cityData.put("data_source", hasValidCity ? "postgresql-database" : "no-data-found");
            if (hasValidCity) {
                System.out.println("✅ CityDataService returning data for city: " +
                        ((Map<?, ?>) cityData.get("city")).get("name") + " with " +
                        ((List<?>) cityData.get("spots")).size() + " spots, " +
                        ((List<?>) cityData.get("hotels")).size() + " hotels, " +
                        ((List<?>) cityData.get("restaurants")).size() + " restaurants");
            } else {
                System.out.println("❌ CityDataService found NO DATA for destination: " + destination);
            }

            return cityData;

        } catch (Exception e) {
            System.err.println("❌ CityDataService error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch city data: " + e.getMessage());
            errorResponse.put("success", false);
            return errorResponse;
        }
    }

    private Map<String, Object> fetchRealCityData(String destination) {
        Map<String, Object> cityData = new HashMap<>();
        Map<String, Object> city = new HashMap<>(); // You may want to fetch actual city info here
        List<Map<String, Object>> spots = new ArrayList<>();

        try {
            // Get all data for this specific destination
            List<Object[]> spotResults = tripSpotSuggestionRepository.findSpotsByDestination(destination);
            List<Object[]> hotelResults = tripAccommodationRepository.findAccommodationsByDestination(destination);
            List<Object[]> restaurantResults = tripFoodOptionRepository.findFoodOptionsByDestination(destination);

            // You may want to fetch actual city info from DB, here it's just a placeholder
            if (!spotResults.isEmpty()) {
                Object[] firstSpot = spotResults.get(0);
                city.put("id", null); // Fix: avoid index out of bounds, or fetch city id separately if needed
                city.put("name", destination);
            }

            for (int i = 0; i < spotResults.size(); i++) {
                Object[] spotRow = spotResults.get(i);
                Map<String, Object> spot = new HashMap<>();
                spot.put("id", i + 1);
                spot.put("city_id", city.get("id"));
                spot.put("name", spotRow[0] != null ? spotRow[0].toString() : null);
                spot.put("description", spotRow[1] != null ? spotRow[1].toString() : null);
                spot.put("entry_fee", 0); // Use 0 instead of hardcoded 50
                spot.put("best_time", spotRow[4] != null ? spotRow[4].toString() : null);

                // time_needed
                Object timeNeededObj = spotRow[5];
                int timeNeeded = 0;
                if (timeNeededObj instanceof BigDecimal) timeNeeded = ((BigDecimal) timeNeededObj).intValue();
                else if (timeNeededObj instanceof Integer) timeNeeded = (Integer) timeNeededObj;
                spot.put("time_needed", timeNeeded);

                // lat
                Object latObj = spotRow[2];
                Double lat = null;
                if (latObj instanceof BigDecimal) lat = ((BigDecimal) latObj).doubleValue();
                else if (latObj instanceof Double) lat = (Double) latObj;
                else if (latObj instanceof Integer) lat = ((Integer) latObj).doubleValue();
                spot.put("lat", lat);

                // lon
                Object lonObj = spotRow[3];
                Double lon = null;
                if (lonObj instanceof BigDecimal) lon = ((BigDecimal) lonObj).doubleValue();
                else if (lonObj instanceof Double) lon = (Double) lonObj;
                else if (lonObj instanceof Integer) lon = ((Integer) lonObj).doubleValue();
                spot.put("lon", lon);

                spot.put("image_url", "https://cdn.example.com/images/" +
                        (spotRow[0] != null ? spotRow[0].toString().toLowerCase().replace(" ", "_") : "default") + ".jpg");

                // Add hotels for this spot
                List<Map<String, Object>> spotHotels = new ArrayList<>();
                int hotelsPerSpot = Math.max(1, hotelResults.size() / Math.max(1, spotResults.size()));
                int hotelStartIdx = i * hotelsPerSpot;

                for (int h = 0; h < hotelsPerSpot && (hotelStartIdx + h) < hotelResults.size(); h++) {
                    Object[] hotelRow = hotelResults.get(hotelStartIdx + h);
                    Map<String, Object> hotel = new HashMap<>();
                    hotel.put("id", hotelStartIdx + h + 1);
                    hotel.put("spot_id", i + 1);
                    hotel.put("name", hotelRow[0] != null ? hotelRow[0].toString() : null);
                    hotel.put("price_min", 0); // Remove hardcoded prices
                    hotel.put("price_max", 0);

                    // rating
                    Object ratingObj = hotelRow[4];
                    Double rating = null;
                    if (ratingObj instanceof BigDecimal) rating = ((BigDecimal) ratingObj).doubleValue();
                    else if (ratingObj instanceof Double) rating = (Double) ratingObj;
                    else if (ratingObj instanceof Integer) rating = ((Integer) ratingObj).doubleValue();
                    hotel.put("rating", rating);

                    // lat
                    Object hotelLatObj = hotelRow[2];
                    Double hotelLat = null;
                    if (hotelLatObj instanceof BigDecimal) hotelLat = ((BigDecimal) hotelLatObj).doubleValue();
                    else if (hotelLatObj instanceof Double) hotelLat = (Double) hotelLatObj;
                    else if (hotelLatObj instanceof Integer) hotelLat = ((Integer) hotelLatObj).doubleValue();
                    hotel.put("lat", hotelLat);

                    // lon
                    Object hotelLonObj = hotelRow[3];
                    Double hotelLon = null;
                    if (hotelLonObj instanceof BigDecimal) hotelLon = ((BigDecimal) hotelLonObj).doubleValue();
                    else if (hotelLonObj instanceof Double) hotelLon = (Double) hotelLonObj;
                    else if (hotelLonObj instanceof Integer) hotelLon = ((Integer) hotelLonObj).doubleValue();
                    hotel.put("lon", hotelLon);

                    hotel.put("contact", hotelRow[7] != null ? hotelRow[7].toString() : null);
                    hotel.put("image_url", "https://cdn.example.com/images/" +
                            (hotelRow[0] != null ? hotelRow[0].toString().toLowerCase().replace(" ", "_") : "hotel") + ".jpg");

                    spotHotels.add(hotel);
                }
                spot.put("hotels", spotHotels);

                // Add restaurants for this spot
                List<Map<String, Object>> spotRestaurants = new ArrayList<>();
                int restaurantsPerSpot = Math.max(1, restaurantResults.size() / Math.max(1, spotResults.size()));
                int restaurantStartIdx = i * restaurantsPerSpot;

                for (int r = 0; r < restaurantsPerSpot && (restaurantStartIdx + r) < restaurantResults.size(); r++) {
                    Object[] restaurantRow = restaurantResults.get(restaurantStartIdx + r);
                    Map<String, Object> restaurant = new HashMap<>();
                    restaurant.put("id", restaurantStartIdx + r + 1);
                    restaurant.put("spot_id", i + 1);
                    restaurant.put("name", restaurantRow[0] != null ? restaurantRow[0].toString() : null);
                    restaurant.put("popular_dishes", null); // Remove hardcoded dishes
                    restaurant.put("avg_cost", restaurantRow[9] != null ?
                            Integer.parseInt(restaurantRow[9].toString().replaceAll("[^0-9]", "")) : 0);

                    // lat
                    Object restLatObj = restaurantRow[2];
                    Double restLat = null;
                    if (restLatObj instanceof BigDecimal) restLat = ((BigDecimal) restLatObj).doubleValue();
                    else if (restLatObj instanceof Double) restLat = (Double) restLatObj;
                    else if (restLatObj instanceof Integer) restLat = ((Integer) restLatObj).doubleValue();
                    restaurant.put("lat", restLat);

                    // lon
                    Object restLonObj = restaurantRow[3];
                    Double restLon = null;
                    if (restLonObj instanceof BigDecimal) restLon = ((BigDecimal) restLonObj).doubleValue();
                    else if (restLonObj instanceof Double) restLon = (Double) restLonObj;
                    else if (restLonObj instanceof Integer) restLon = ((Integer) restLonObj).doubleValue();
                    restaurant.put("lon", restLon);

                    restaurant.put("image_url", "https://cdn.example.com/images/" +
                            (restaurantRow[0] != null ? restaurantRow[0].toString().toLowerCase().replace(" ", "_") : "restaurant") + ".jpg");

                    spotRestaurants.add(restaurant);
                }
                spot.put("restaurants", spotRestaurants);

                spots.add(spot);
            }

            // Structure data for the specific requested city only
            cityData.put("city", city);  // Single city object, not array
            cityData.put("spots", spots);

            int totalHotels = spots.stream()
                    .mapToInt(spot -> {
                        Object hotelsObj = ((Map<?, ?>) spot).get("hotels");
                        return hotelsObj instanceof List ? ((List<?>) hotelsObj).size() : 0;
                    }).sum();

            int totalRestaurants = spots.stream()
                    .mapToInt(spot -> {
                        Object restObj = ((Map<?, ?>) spot).get("restaurants");
                        return restObj instanceof List ? ((List<?>) restObj).size() : 0;
                    }).sum();

            System.out.println("📊 Fetched data for city: " + city.get("name") + " with " + spots.size() + " spots, " +
                    totalHotels + " hotels, " + totalRestaurants + " restaurants");

        } catch (Exception e) {
            System.err.println("❌ Error fetching real data: " + e.getMessage());
            e.printStackTrace();
            // Return empty data on error
            cityData.put("city", new HashMap<>());
            cityData.put("spots", new ArrayList<>());
        }

        // Flatten all hotels and restaurants for top-level keys
        List<Map<String, Object>> allHotels = new ArrayList<>();
        List<Map<String, Object>> allRestaurants = new ArrayList<>();
        for (Map<String, Object> spot : spots) {
            Object hotelsObj = spot.get("hotels");
            if (hotelsObj instanceof List) allHotels.addAll((List<Map<String, Object>>) hotelsObj);
            Object restObj = spot.get("restaurants");
            if (restObj instanceof List) allRestaurants.addAll((List<Map<String, Object>>) restObj);
        }
        cityData.put("hotels", allHotels);
        cityData.put("restaurants", allRestaurants);

        return cityData;
    }
}