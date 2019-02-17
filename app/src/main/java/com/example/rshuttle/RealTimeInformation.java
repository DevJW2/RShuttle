package com.example.rshuttle;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This interface will contain all the methods real time data required for the RShuttle App
 *
 * NOTE:
 *      RIT's geo-area: 43.077091,-77.690163|43.093377,-77.652302
 *      RIT's agencyId: 643
 *
 * @author Justin Yau
 */
public interface RealTimeInformation {

    /**
     * This method makes a post request to the given url (has to be for rapidapi) and returns the
     * data array.
     *
     * @param url - The url of the request
     * @param agencyId - The agency id of the college that you want to find information about
     * @return - The data array
     * @return - NULL if error occurred
     * @author Justin Yau
     */
    public JSONArray makePostRequestOpenAPIObject(String url, String agencyId);

    /**
     * This method makes a post request to the given url (has to be for rapidapi) and returns the
     * data array.
     *
     * @param url - The url of the request
     * @return - The data array
     * @return - NULL if error occurred
     * @author Justin Yau
     */
    public JSONArray makePostRequestOpenAPIArray(String url);

    /**
     * This method will return the bus locations associated with the agencyID (College) and routeID.
     *
     * @param agencyId - The id of the college that you want to find the bus locations for
     * @return - The bus locations associated with the agencyID and routeID.
     * @return - NULL if an error occurred
     * @author Justin Yau
     */
    public float[][] busLocations(String agencyId, String routeID);

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
    public Map<String, List<String>> routes(String agencyId);

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
    public Map<String, String[]> stops(String agencyId);

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
    public Map<String, List<String>> timeAtStop(String agencyId, String stopId);

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
    public Map<String, String> getAgencyIds(String latitude, String longitude, String latitude1, String longitude1);

}