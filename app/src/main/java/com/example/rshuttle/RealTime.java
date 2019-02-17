package com.example.rshuttle;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class will contain all implementation of the methods real time data required for the
 * RShuttle App.
 *
 * NOTE:
 *      RIT's geo-area: 43.077091,-77.690163|43.093377,-77.652302
 *      RIT's agencyId: 643
 *
 * @author Justin Yau
 */
public class RealTime implements RealTimeInformation {

    // This is the api key we use to access openAPI
    public static final String openAPIKey = "7a9a8dd817mshe07f2c506c8d832p1ff971jsnb0ff5652a428";

    /**
     * This method makes a post request to the given url (has to be for rapidapi) and returns the
     * data object.
     *
     * @param url - The url of the request
     * @param agencyId - The agency id of the college that you want to find information about
     * @return - The data array
     * @return - NULL if error occurred
     * @author Justin Yau
     */
    public JSONArray makePostRequestOpenAPIObject(String url, String agencyId) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-RapidAPI-Key", openAPIKey);
            System.out.println("Response Code: " + con.getResponseCode());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONObject responseJson = new JSONObject(response.toString());
            JSONObject data = responseJson.getJSONObject("data");
            JSONArray set = data.getJSONArray(agencyId);
            return set;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    /**
     * This method makes a post request to the given url (has to be for rapidapi) and returns the
     * data array.
     *
     * @param url - The url of the request
     * @return - The data array
     * @return - NULL if error occurred
     * @author Justin Yau
     */
    public JSONArray makePostRequestOpenAPIArray(String url) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-RapidAPI-Key", openAPIKey);
            System.out.println("Response Code: " + con.getResponseCode());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONObject responseJson = new JSONObject(response.toString());
            JSONArray data = responseJson.getJSONArray("data");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method will return the bus locations associated with the agencyID (College) and routeID.
     *
     * @param agencyId - The id of the college that you want to find the bus locations for
     * @return - The bus locations associated with the agencyID and routeID.
     * @return - NULL if an error occurred
     * @author Justin Yau
     */
    public float[][] busLocations(String agencyId, String routeID) {
        try {
            JSONArray loc = makePostRequestOpenAPIObject("https://transloc-api-1-2.p.rapidapi.com/vehicles.json?routes=" + routeID + "&callback=call&agencies=" + agencyId, agencyId);
            if(loc == null) { return null; }
            float[][] locations = new float[loc.length()][2];
            for(int i = 0; i < loc.length(); i++) {
                JSONObject location = loc.getJSONObject(i);
                JSONObject coordinates = location.getJSONObject("location");
                locations[i][0] = Float.parseFloat(coordinates.getString("lat"));
                locations[i][1] = Float.parseFloat(coordinates.getString("lng"));
            }
            return locations;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * This method will return all the routes served by the inputted college
     * READ:
     *      Map has routeIDs as keys and a list of stop ids as values
     *      The first entity in the list will be the name of the route
     *      To get cords of stop ids, call the stop function and look it up
     *
     * @param agencyId - The id of the college that you want to find the routes for
     * @return - All the routes that are served by the college
     * @return - NULL if error occurred
     * @author Justin Yau
     */
    @Override
    public Map<String, List<String>> routes(String agencyId) {
        try {
            JSONArray r = makePostRequestOpenAPIObject("https://transloc-api-1-2.p.rapidapi.com/routes.json?callback=call&agencies=" + agencyId, agencyId);
            if(r == null) { return null; }
            Map<String, List<String>> routes = new HashMap<String, List<String>>();
            for(int i = 0; i < r.length(); i++) {
                JSONObject route = r.getJSONObject(i);
                List<String> s = new ArrayList<>();
                s.add(route.getString("long_name"));
                JSONArray stops = route.getJSONArray("stops");
                for(int j = 0; j < stops.length(); j++) {
                    String stopID = stops.getString(j);
                    s.add(stopID);
                }
                routes.put(route.getString("route_id"), s);
            }
            return routes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * This method will return all stops serves the agencyID and their respective coordinates
     *
     * READ:
     *      The map will be a collection of stopIds as the key and their coordinate as the value.
     *      USE in-cojunction with the routes method to make connections
     *
     * @param agencyId - The id of the college that you want to find the routes for
     * @return - All the routes that are served by the college and their coordinates
     * @return - NULL is an error occurred
     * @author Justin Yau
     */
    @Override
    public Map<String, String[]> stops(String agencyId) {
        try {
            JSONArray r = makePostRequestOpenAPIArray("https://transloc-api-1-2.p.rapidapi.com/stops.json?callback=call&agencies=" + agencyId);
            if(r == null) { return null; }
            Map<String, String[]> stops = new HashMap<String, String[]>();
            for(int i = 0; i < r.length(); i++) {
                JSONObject stop = r.getJSONObject(i);
                String[] info = new String[3];
                info[0] = stop.getString("name");
                JSONObject cord = stop.getJSONObject("location");
                info[1] = cord.getString("lat");
                info[2] = cord.getString("lng");
                stops.put(stop.getString("stop_id"), info);
            }
            return stops;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * This method will return all arrival times at the stopID that is served by agencyID
     *
     * READ:
     *      Hashmap will have routeIDs as the key values and their estimated arrival time as values
     *
     * @return - All the route ids and their respective arrival times
     * @return - Null if an error occurred
     * @author Justin Yau
     */
    public Map<String, String> timeAtStop(String agencyId, String stopId) {
        try {
            JSONArray data = makePostRequestOpenAPIArray("https://transloc-api-1-2.p.rapidapi.com/arrival-estimates.json?stops=" + stopId + "&callback=call&agencies=" + agencyId);
            if(data == null) { return null; }
            Map<String, String> times = new HashMap<String, String>();
            for(int i = 0; i < data.length(); i++) {
                JSONArray arrivals = data.getJSONObject(i).getJSONArray("arrivals");
                for(int j = 0; j < arrivals.length(); j++) {
                    JSONObject info = arrivals.getJSONObject(j);
                    times.put(info.getString("route_id"), info.getString("arrival_at"));
                }
            }
            return times;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * This method retrieves the list of all agencies within the specified coordinate rectangle
     *
     * READ:
     *      The map will have agency name as its key and its id as its value
     *
     * @param latitude - The latitude of the first point of the rectangle
     * @param longitude - The longitude of the first point of the rectangle
     * @param latitude1 - The latitude of the second point of the rectangle
     * @param longitude1 - The longitude of the second point of the rectangle
     * @return - List of all agencies within the specified coordinate rectangle
     * @return - NULL if an error occurred
     *
     * @author Justin Yau
     */
    public Map<String, String> getAgencyIds(String latitude, String longitude, String latitude1, String longitude1) {
        try {
            JSONArray data = makePostRequestOpenAPIArray("https://transloc-api-1-2.p.rapidapi.com/agencies.json?callback=call&geo_area=" + latitude + "%2C" + longitude + "%7C" + latitude1 + "%2C" + longitude1);
            if(data == null) { return null; }
            Map<String, String> agencies = new HashMap<String, String>();
            for(int i = 0; i < data.length(); i++) {
                JSONObject agency = data.getJSONObject(i);
                agencies.put(agency.getString("long_name"), agency.getString("agency_id"));
            }
            return agencies;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}