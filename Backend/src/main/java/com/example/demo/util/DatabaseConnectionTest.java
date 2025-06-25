package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🔍 DATABASE CONNECTION TEST STARTING...\n");
        
        try {
            // Test 1: Basic connection
            testBasicConnection();
            
            // Test 2: Check database info
            checkDatabaseInfo();
            
            // Test 3: Check if tables exist
            checkTablesExist();
            
            // Test 4: Check data in travel_cities
            checkTravelCitiesData();
            
            // Test 5: Check data for Sylhet specifically
            checkSylhetData();
            
            System.out.println("\n✅ DATABASE CONNECTION TEST COMPLETED SUCCESSFULLY!\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ DATABASE CONNECTION TEST FAILED!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testBasicConnection() throws Exception {
        System.out.println("🔗 Testing basic database connection...");
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Database connection successful!");
                System.out.println("   Connection URL: " + connection.getMetaData().getURL());
                System.out.println("   Database Product: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("   Database Version: " + connection.getMetaData().getDatabaseProductVersion());
            } else {
                throw new Exception("Connection is null or closed");
            }
        }
    }

    private void checkDatabaseInfo() throws Exception {
        System.out.println("\n📊 Checking database info...");
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("   Database Name: " + connection.getCatalog());
            System.out.println("   Username: " + metaData.getUserName());
            System.out.println("   Driver Name: " + metaData.getDriverName());
        }
    }

    private void checkTablesExist() throws Exception {
        System.out.println("\n📋 Checking if required tables exist...");
        
        String[] requiredTables = {
            "travel_cities", 
            "travel_spots", 
            "travel_hotels", 
            "travel_restaurants"
        };
        
        for (String tableName : requiredTables) {
            try {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?", 
                    Integer.class, 
                    tableName
                );
                
                if (count != null && count > 0) {
                    System.out.println("   ✅ Table '" + tableName + "' exists");
                } else {
                    System.out.println("   ❌ Table '" + tableName + "' does NOT exist");
                }
            } catch (Exception e) {
                System.out.println("   ❌ Error checking table '" + tableName + "': " + e.getMessage());
            }
        }
    }

    private void checkTravelCitiesData() throws Exception {
        System.out.println("\n🏙️ Checking travel_cities table data...");
        
        try {
            // Count total cities
            Integer totalCities = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM travel_cities", Integer.class);
            System.out.println("   Total cities in database: " + totalCities);
            
            // List all cities
            if (totalCities != null && totalCities > 0) {
                List<Map<String, Object>> cities = jdbcTemplate.queryForList("SELECT id, name FROM travel_cities ORDER BY name");
                System.out.println("   Cities found:");
                for (Map<String, Object> city : cities) {
                    System.out.println("     - ID: " + city.get("id") + ", Name: " + city.get("name"));
                }
            } else {
                System.out.println("   ❌ No cities found in travel_cities table!");
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Error checking travel_cities: " + e.getMessage());
            throw e;
        }
    }

    private void checkSylhetData() throws Exception {
        System.out.println("\n🎯 Checking data specifically for 'Sylhet'...");
        
        try {
            // Check if Sylhet exists in travel_cities
            List<Map<String, Object>> sylhetCities = jdbcTemplate.queryForList(
                "SELECT * FROM travel_cities WHERE LOWER(name) LIKE LOWER(?)", 
                "%sylhet%"
            );
            
            if (!sylhetCities.isEmpty()) {
                System.out.println("   ✅ Found Sylhet in travel_cities:");
                for (Map<String, Object> city : sylhetCities) {
                    System.out.println("     - ID: " + city.get("id"));
                    System.out.println("     - Name: " + city.get("name"));
                    System.out.println("     - Description: " + city.get("description"));
                    
                    // Check related data
                    checkSylhetRelatedData(city.get("name").toString());
                }
            } else {
                System.out.println("   ❌ Sylhet NOT found in travel_cities table!");
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Error checking Sylhet data: " + e.getMessage());
            throw e;
        }
    }    private void checkSylhetRelatedData(String cityName) throws Exception {
        System.out.println("     Checking related data for " + cityName + ":");
        
        try {
            // First get the city ID
            Integer cityId = jdbcTemplate.queryForObject(
                "SELECT id FROM travel_cities WHERE LOWER(name) LIKE LOWER(?)", 
                Integer.class, 
                "%" + cityName + "%"
            );
            
            if (cityId != null) {
                System.out.println("       - City ID: " + cityId);
                
                // Check spots using city_id
                Integer spotCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM travel_spots WHERE city_id = ?", 
                    Integer.class, 
                    cityId
                );
                System.out.println("       - Spots: " + spotCount);
                
                // Check hotels using JOIN with spots
                Integer hotelCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM travel_hotels h " +
                    "JOIN travel_spots s ON h.spot_id = s.id " +
                    "WHERE s.city_id = ?", 
                    Integer.class, 
                    cityId
                );
                System.out.println("       - Hotels: " + hotelCount);
                
                // Check restaurants using JOIN with spots
                Integer restaurantCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM travel_restaurants r " +
                    "JOIN travel_spots s ON r.spot_id = s.id " +
                    "WHERE s.city_id = ?", 
                    Integer.class, 
                    cityId
                );
                System.out.println("       - Restaurants: " + restaurantCount);
                
            } else {
                System.out.println("       ❌ Could not find city ID for: " + cityName);
            }
            
        } catch (Exception e) {
            System.out.println("       ❌ Error checking related data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
