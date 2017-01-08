package ubc.cs.cpsc210.sustainabilityapp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ubc.cs.cpsc210.sustainabilityapp.model.Feature;
import ubc.cs.cpsc210.sustainabilityapp.model.POIRegistry;
import ubc.cs.cpsc210.sustainabilityapp.model.SharedPreferencesKeyValueStore;
import ubc.cs.cpsc210.sustainabilityapp.model.TourState;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FeatureFragment extends ListFragment {
	/**
	 * Has an item been clicked since the activity was resumed?
	 */
	private boolean itemClicked;
	/**
	 * All of the available features.
	 */
	private Feature[] features;
	
	/**
	 * Manages and stores selected features and POI's.
	 */
	private TourState tourState;
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
		features = Feature.values();
		
		tourState = new TourState(POIRegistry.getDefault(), new SharedPreferencesKeyValueStore(getActivity(), TourState.STORE_NAME));
		
		setListAdapter(new ArrayAdapter<Feature>(getActivity(), android.R.layout.simple_list_item_multiple_choice, features));
    }
   

	/**
	 * Update the features selected in the list view, based on what is in storage.
	 */
	@Override
	public void onResume() {
		itemClicked = false;
		
		List<Feature> selectedFeatures = tourState.getSelectedFeatures();
		
		for (int i = 0; i < features.length; i++) {
			if (selectedFeatures.contains(features[i])) {
				getListView().setItemChecked(i, true);
			} else {
				getListView().setItemChecked(i, false);
			}
		}
		
		super.onResume();
	}
	
	/**
	 * Update storage based on the features selected in the list view.
	 */
	@Override
	public void onPause() {
		updateTour();
		super.onPause();
	}
	
	private void updateTour() {
		if (itemClicked){
			SparseBooleanArray checkedPositions = getListView().getCheckedItemPositions();
			
			Set<Feature> selectedFeatures = new HashSet<Feature>();
			
			for (int i = 0; i < checkedPositions.size(); i++) {
				if (checkedPositions.valueAt(i)) {
					selectedFeatures.add(features[checkedPositions.keyAt(i)]);
				}
			}
			
			tourState.setSelectedFeatures(new ArrayList<Feature>(selectedFeatures));
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		itemClicked = true;
		updateTour();
		((ITourUpdate) getActivity()).updateMap();
	}
}