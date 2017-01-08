package ubc.cs.cpsc210.sustainabilityapp;

import java.util.ArrayList;

import org.osmdroid.views.MapView;

import ubc.cs.cpsc210.sustainabilityapp.model.Feature;
import ubc.cs.cpsc210.sustainabilityapp.model.LatLong;
import ubc.cs.cpsc210.sustainabilityapp.model.PointOfInterest;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

public class MyMapView extends MapView{

	/**
	 * Log tag for LogCat messages
	 */
	private final static String LOG_TAG = "MyMapView";
	
	/**
	 * Reference to map interface
	 */
	private MapDisplayFragment map;
	
	/**
	 * ID for next custom POI
	 */
	private int currentId;
	
	/**
	 * Maximum number of pixels between down event and up event
	 */
	private int BUFFER = 3;
	
	/**
	 * Coordinates where down event occurs
	 */
	private float downX;
	private float downY;
	
	/**
	 * Time when down is pressed
	 */
	private long startDownTime;
	
	public MyMapView(Context context, MapDisplayFragment mapFrag) {
		super(context, null);
		map = mapFrag;
		currentId = 0;	
		
		downX = -1;
		downY = -1;
		startDownTime = 0;
	}
	
	/**
	 * Method is called when a touch event occurs on this MapView
	 */
	@Override 
	public boolean onTouchEvent(MotionEvent e){
		int action = e.getAction(); 
		
		// if screen is released
	    if(action == MotionEvent.ACTION_UP){
	    	long holdTime =  e.getEventTime() - startDownTime;
	    	startDownTime = 0;
	    	
	    	// hold for between 1 and 3 seconds -> custom POI added
	    	if(holdTime > 1000 && holdTime < 3000){
	            float x = e.getX();
	            float y = e.getY();
	            
	            // do not add POI if there is much movement between down and up
	            if(Math.abs(x - downX) < BUFFER && Math.abs(y - downY) < BUFFER){
	        	    Log.i(LOG_TAG, "Making new poi");
	
	            	PointOfInterest customPOI = makeNewPOIFromLatLong(map.getLatLongFromMap(x, y));
	            	
	            	map.addPOIToTour(customPOI);
	            }
            }
	    	// hold for more than 3 seconds -> custom POIs removed
	    	else if(holdTime > 3000)
	    		map.clearCustomPoints();
        }
	    // if screen is pressed down
        else if(action == MotionEvent.ACTION_DOWN){
        	// get time and location when pressed
        	startDownTime = e.getEventTime();
            downX = e.getX();
            downY = e.getY();
        }
        return super.onTouchEvent(e);
	}
	
	/**
	 * Creates a new custom POI at given LatLong
	 */
	public PointOfInterest makeNewPOIFromLatLong(LatLong position){
		PointOfInterest poi = new PointOfInterest(currentId + "", "Custom point");
		
		currentId++;
		
		poi.setLatLong(position);
		poi.setDescription("Custom point");
		poi.setAddress(null);
		ArrayList<Feature> emptyList = null;
		poi.setFeatures(emptyList);
		
		return poi;
	}	

}
