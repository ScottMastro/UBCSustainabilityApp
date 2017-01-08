package ubc.cs.cpsc210.sustainabilityapp.routing;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

//import android.util.Log;

import ubc.cs.cpsc210.sustainabilityapp.model.LatLong;

/**
 * Wrapper around a service which calculates routes between geographic locations.  This class may 
 * be called concurrently from multiple threads -- it is thread-safe.
 * 
 * Currently, this class wraps the www.yournavigation.org API 
 * (<a href="http://wiki.openstreetmap.org/wiki/YOURS#Routing_API">http://wiki.openstreetmap.org/wiki/YOURS#Routing_API</a>).  
 */
public class RoutingService {
//	private final static String LOG_TAG = "RoutingService";
	private final static String URL_BASE = "http://yours.cs.ubc.ca/yours/api/1.0/gosmore.php?";
	
	/**
	 * Caches routes retrieved by their endpoints.  Access to this map must be synchronized on the
	 * map.
	 */
	private Map<RouteEndpoints, RouteInfo> routeCache = new HashMap<RouteEndpoints, RouteInfo>();
	
	/** 
	 * Client for making HTTP requests to the API of the service.
	 */
	private HttpClient client;
	
	public RoutingService() {
        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be accessing the HttpClient.
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        client = new DefaultHttpClient(cm, params);
	}
	
	public void shutdown() {
		if (client != null) {
			client.getConnectionManager().shutdown();
		}
	}
	
	/**
	 * Calculate route for given start point and end point.  An internet connection must be available.
	 * See {@link getRouteFromService} for further information on route generation.
	 * 
	 * @param start The start point of the route.
	 * @param end The end point of the route.
	 * @param useCache Indicates whether the service should return a cached route, if one exists.  
	 *                 If this flag is set to true, and a cached route is not available, then the new 
	 *                 route obtained from the server will be cached.
	 * @return Information on the route calculated, including the waypoints.
	 * @throws IOException If an error occurs while retrieving the route from the server.
	 */
	public RouteInfo getRoute(LatLong start, LatLong end, boolean useCache) throws IOException {
		
		RouteEndpoints endpoints = new RouteEndpoints(start, end);
		
		if(useCache){
			RouteInfo cachedRoute = getCachedRoute(endpoints);
			
			// if there is a route in the cache, return it and don't use the service
			if(cachedRoute != null)
				return cachedRoute;
		}
			
		// if there is no route in the cache or useCache is false
		RouteInfo routeFromService = getRouteFromService(endpoints);
		
		// if requested, store route from the service into the cache
		if(useCache)
			addRouteToCache(endpoints, routeFromService);
		
		return routeFromService;
	}
	
	/**
	 * Calculate route for given endpoints.  Currently, we use the www.yournavigation.org API
	 * (<a href="http://wiki.openstreetmap.org/wiki/YOURS#Routing_API">http://wiki.openstreetmap.org/wiki/YOURS#Routing_API</a>), 
	 * with result format set to geojson, vehicle set to foot and route type set to shortest (rather than fastest).  Using route 
	 * type of fastest can result in different routes between the same two points, depending on the 
	 * direction traveled.
	 * 
	 * Subclasses can override this method to connect to alternate routing services.
	 * 
	 * @param endpoints Endpoints of the route.
	 * @return Information on the route calculated, including waypoints.
	 * @throws IOException If an error occurs while retrieving the route from the server.
	 */
	private RouteInfo getRouteFromService(RouteEndpoints endpoints) throws IOException {

		//API no longer hosted at UBC
		//http://wiki.openstreetmap.org/wiki/YOURS
		String url = URL_BASE + "format=geojson" + "&flat=" + endpoints.getStart().getLatitude() + "&flon="
			+ endpoints.getStart().getLongitude() + "&tlat=" + endpoints.getEnd().getLatitude() +
			"&tlon=" + endpoints.getEnd().getLongitude() + "&v=foot&fast=0&layer=mapnik";
				
			// flat = latitude of the starting location.
			// flon = longitude of the starting location.
			// tlat = latitude of the end location.
			// tlon = longitude of the end location.
			// v = the type of transport, possible options are: motorcar, bicycle or foot. Default is: motorcar.
			// fast = 1 selects the fastest route, 0 the shortest route. Default is: 1. 
			// format = specifies the format (KML or geoJSON) in which the route result is being sent back to 
			// the client. This can either be kml or geojson. Default is: kml
			// http://wiki.openstreetmap.org/wiki/YOURS
		
		try {
			// set up to get response from service
			URI uri = new URI(url);
			HttpGet request = new HttpGet(uri);
			request.addHeader("X-Yours-client", "UBC CPSC 210");
			BasicResponseHandler handler = new BasicResponseHandler();
			
			// get response from service
			String responseString = client.execute(request, handler);
			
			// store response as JSONTokener
			JSONObject response = (JSONObject) new JSONTokener(responseString).nextValue();
						
			return jsonTokenerToRouteInfo(response);
			
		} catch (URISyntaxException e) {
			System.out.println("Malformed URI");
			throw new IOException();
		} catch (JSONException e) {
			System.out.println("Poor response");
			throw new IOException();
		}
	}

	/**
	 * Takes JSONTokener holding coordinates from service and extracts latitude and
	 * longitude for each point and returns as RouteInfo object
	 * 
	 * REQUIRES: coordinates string is formatted like "[latitude,longitude][latitude,longitude]..." 
	 * @param response The JSONTokener response from the service
	 */
	private RouteInfo jsonTokenerToRouteInfo(JSONObject response) throws JSONException{
		
		// gets all coordinates from JSONException
		String coordinates = response.getString("coordinates");
		// list to store LatLong points
		List<LatLong> points = new ArrayList<LatLong>();

		// helper stores a string of characters read from coordinates
		StringBuffer helper = new StringBuffer();
		// lat and lon set to 0.0; easy to notice error if value does not change
		Double lat = 0.0;
		Double lon = 0.0;
		
		// for every character in coordinates
		for(int i = 0; i <= coordinates.length() - 1; i++){
			
			// store current character temporarily  
			char currentChar = coordinates.charAt(i);
 
			// if the current character is a number, decimal place
			// or negative sign, add to helper
			if(Character.isDigit(currentChar) || currentChar == '.' || currentChar == '-')
				helper.append(currentChar);
			// if a previous character exists
			else if (i > 0){	
				// store previous character temporarily 
				char previousChar = coordinates.charAt(i - 1);
				
				// assuming latitude and longitude are separated by a comma
				if(currentChar == ',' && Character.isDigit(previousChar)){
					// store latitude in lat and reset helper
					lat = Double.valueOf(helper.toString());
					helper = new StringBuffer();
				}
				// if the character is neither a comma or a digit and comes after a digit,
				// the character is assumed to be a ']' that comes after the longitude
				else if (!Character.isDigit(currentChar) && Character.isDigit(previousChar)){
					//store latitude and longitude in LatLong list and reset variables
					lon = Double.valueOf(helper.toString());
					points.add(new LatLong(lon, lat));
					lat = 0.0;
					lon = 0.0;
					helper = new StringBuffer();
				}
			}					
		}
		
		return new RouteInfo(points);
	}
	
	private RouteInfo getCachedRoute(RouteEndpoints endpoints) {
		synchronized (routeCache) {
			return routeCache.get(endpoints);
		}
	}
	
	private void addRouteToCache(RouteEndpoints endpoints, RouteInfo routeInfo) {
		synchronized (routeCache) {
			routeCache.put(endpoints, routeInfo);
		}
	}
}