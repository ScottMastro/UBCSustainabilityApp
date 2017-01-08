package ubc.cs.cpsc210.sustainabilityapp;

import java.util.ArrayList;
import java.util.List;

import ubc.cs.cpsc210.sustainabilityapp.model.POIRegistry;
import ubc.cs.cpsc210.sustainabilityapp.model.PointOfInterest;
import ubc.cs.cpsc210.sustainabilityapp.model.SharedPreferencesKeyValueStore;
import ubc.cs.cpsc210.sustainabilityapp.model.TourState;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class POIFragment extends ListFragment {
	/**
	 * All of the available points of interest.
	 */
	private PointOfInterest[] points;

	/**
	 * Manages and stores selected features and POI's.
	 */
	private TourState tourState;
	
	/**
	 * Has an item been clicked since the activity was resumed?
	 */
	private boolean itemClicked;
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Points are displayed in alphabetical order in the list view.
		points = POIRegistry.getDefault().getPointsAlphabetical().toArray(new PointOfInterest[]{});
		
		tourState = new TourState(POIRegistry.getDefault(), new SharedPreferencesKeyValueStore(getActivity(), TourState.STORE_NAME));
		
		setListAdapter(new ArrayAdapter<PointOfInterest>(getActivity(), android.R.layout.simple_list_item_multiple_choice, 
				points));
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

    
	/**
	 * Update the list of selected points, based on what is found in storage.
	 */
	@Override
	public void onResume() {
		itemClicked = false;
		
		List<PointOfInterest> selectedPoints = tourState.getSelectedPOIs();
		for (int i = 0; i < points.length; i++) {
			if (selectedPoints.contains(points[i])) {
				getListView().setItemChecked(i, true);
			} else {
				getListView().setItemChecked(i, false);
			}
		}
		
		super.onResume();
	}

	/**
	 * Update selected POI's and features, based on what the user has selected in the UI.
	 */
	@Override
	public void onPause() {
		updateTour();
		super.onPause();
	}

	private void updateTour() {
		if (itemClicked) {
			SparseBooleanArray checkedPositions = getListView().getCheckedItemPositions();
			
			List<PointOfInterest> selectedPOIs = new ArrayList<PointOfInterest>();
			
			for (int i = 0; i < checkedPositions.size(); i++) {
				if (checkedPositions.valueAt(i)) {
					selectedPOIs.add(points[checkedPositions.keyAt(i)]);
				}
			}
			
			tourState.setSelectedPOIs(selectedPOIs);
		}
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		itemClicked = true;
		updateTour();
		((ITourUpdate) getActivity()).updateMap();
	}
}
