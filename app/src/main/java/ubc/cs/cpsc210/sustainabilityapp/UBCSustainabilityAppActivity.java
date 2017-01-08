package ubc.cs.cpsc210.sustainabilityapp;

import java.util.HashMap;

import ubc.cs.cpsc210.sustainabilityapp.routing.RoutingService;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
/**
 * This is the main activity for the application, and hosts the three sub-activities which represent the
 * three tabs in the UI.
 */


/**
 * This demonstrates how you can implement switching between the tabs of a
 * TabHost through fragments.  It uses a trick (see the code below) to allow
 * the tabs to switch between fragments instead of simple views.
 */
public class UBCSustainabilityAppActivity extends FragmentActivity implements ITourUpdate {
	
	/**
	 * Log tag for LogCat messages
	 */
	private final static String LOG_TAG = "AppActivity";
	
	/**
	 * Tag labels for tabs/fragments
	 */
	private final static String MAP = "map";
	private final static String FEATURE = "feature";
	private final static String POI = "poi";
			
	
	/**
	 * The tab host.
	 */
    private TabHost mTabHost;
    
    /**
     * Tab manager responsible for swapping fragments when user changes a tab.
     */
    private TabManager mTabManager;
    
    /**
     * Routing service.
     */
    private RoutingService routingService;
    
    /**
     * The tag of the tab to be displayed when this activity is (re)created.
     */
    private String initialTabTag;
    
    /**
     * Constructor - initialize routing service
     */
    public UBCSustainabilityAppActivity() {
    	routingService = new RoutingService();
    	initialTabTag = null;
    }
    
    /**
     * Accessor for routing service
     * @return routing service
     */
    public RoutingService getRoutingService() {
    	return routingService;
    }
    
    @Override
    public void updateMap() {
    	Fragment mapFragment = getSupportFragmentManager().findFragmentByTag(MAP);
    	if (mapFragment != null) {
    		((MapDisplayFragment) mapFragment).update();
    		Log.i(LOG_TAG, "Updating map");
    	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
             
        Resources res = getResources();

        setContentView(R.layout.main);
        mTabHost = (TabHost) findViewById(R.id.tabhost);
        mTabHost.setup();

        setupTabs(res);
        setupSideMap();
        initialTabTag = findInitialTabTag(savedInstanceState, res);
        
        // Need to force a TabChange to handle change from portrait to landscape when
        // MapDisplayFragment is currently showing.
        if (initialTabTag.equals(POI))
        	mTabHost.setCurrentTabByTag(FEATURE);
        else
        	mTabHost.setCurrentTabByTag(POI);
    }

    /**
     * Determine tag for first tab to be displayed - restore from previous instance, if available.
     * 
     * @param savedInstanceState  the saved instance state
     * @param res   the application's resources
     * @return  the tag of the tab to be displayed when activity is (re)created.
     */
	private String findInitialTabTag(Bundle savedInstanceState, Resources res) {
		if (savedInstanceState != null) {
        	if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) 
        		initialTabTag = savedInstanceState.getString("tab");
        	else if (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
        			&& !savedInstanceState.getString("tab").equals(MAP)) 
        		initialTabTag = savedInstanceState.getString("tab");
        	else 
        		// we are in landscape mode and saved tab tag is "map", so need to switch to another tab 
        		// (let's arbitrarily choose POI).
        		initialTabTag = "poi";
        }
        else 
        	initialTabTag = "poi";
		
		return initialTabTag;
	}

    /**
     * Set up tabs
     * 
     * @param res   the application's resources
     */
	private void setupTabs(Resources res) {
		mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);

        mTabManager.addTab(mTabHost.newTabSpec(POI).setIndicator("POIs", res.getDrawable(R.drawable.ic_tab_poi)),
                POIFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec(FEATURE).setIndicator("Features", res.getDrawable(R.drawable.ic_tab_feature)),
                FeatureFragment.class, null);      
       
        // add MapDisplayFragment to tabs only in portrait mode
        if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        	Fragment mapFragment = getSupportFragmentManager().findFragmentByTag(MAP);
        	
        	if (mapFragment != null) {
        		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.remove(mapFragment);
                ft.commit();
                getSupportFragmentManager().executePendingTransactions();
                mTabManager.addTab(mTabHost.newTabSpec(MAP).setIndicator("Map", res.getDrawable(R.drawable.ic_tab_map)),
		                mapFragment, null);
        	}
        	else
		        mTabManager.addTab(mTabHost.newTabSpec(MAP).setIndicator("Map", res.getDrawable(R.drawable.ic_tab_map)),
		                MapDisplayFragment.class, null);
        }
	}
	
	/**
	 * Set up side map for landscape mode
	 */
	private void setupSideMap() {
        // if we're in landscape mode, view with id 'mapcontent' will be available, so add a MapDisplayFragment
        if (findViewById(R.id.mapcontent) != null) {
        	Fragment mapFragment = getSupportFragmentManager().findFragmentByTag(MAP);
        	
        	if (mapFragment == null) {
            	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        	MapDisplayFragment mapDisplayActivity = new MapDisplayFragment();
	        	ft.add(R.id.mapcontent, mapDisplayActivity, MAP);
	        	ft.commit();
        	}
        	else {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.remove(mapFragment);
                ft.commit();
                getSupportFragmentManager().executePendingTransactions();
                ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.mapcontent, mapFragment, MAP);
                ft.commit();	
        	}
        }
	}
    
    @Override
    protected void onResume() {
    	Log.d(LOG_TAG, "onResume");
    	
    	super.onResume();
    	
    	if (initialTabTag != null)
    		mTabHost.setCurrentTabByTag(initialTabTag);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Log.d(LOG_TAG, "onSaveInstanceState");
    	
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }


    /*
     * The TabManager class below is Copyright (C) 2011 The Android Open Source Project
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    /* 
     * Modified by Paul Carter (pcarter@cs.ubc.ca)
     * 		TabManager.addTab(TabHost.TabSpec, Fragment, Bundle) 
     * 		TabInfo.TabInfo(String, Class, Bundle, Fragment)
     * added.
     */
    /**
     * This is a helper class that implements a generic mechanism for
     * associating fragments with the tabs in a tab host.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between fragments.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabManager supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct fragment shown in a separate content area
     * whenever the selected tab changes.
     */
    public static class TabManager implements TabHost.OnTabChangeListener {
        private final FragmentActivity mActivity;
        private final TabHost mTabHost;
        private final int mContainerId;
        private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
        TabInfo mLastTab;

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                this(_tag, _class, _args, null);
            }
            
            TabInfo(String _tag, Class<?> _class, Bundle _args,  Fragment _fr) {
                tag = _tag;
                clss = _class;
                args = _args;
                fragment = _fr;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
            mActivity = activity;
            mTabHost = tabHost;
            mContainerId = containerId;
            mTabHost.setOnTabChangedListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mActivity));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }

            mTabs.put(tag, info);
            mTabHost.addTab(tabSpec);
        }
        
        public void addTab(TabHost.TabSpec tabSpec, Fragment fr, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mActivity));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, fr.getClass(), args, fr);

            // Add fragment to container then detach it as this tab might not currently be visible
            if (fr != null) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.add(mContainerId, fr, tag);
                ft.detach(fr);
                ft.commit();
            }

            mTabs.put(tag, info);
            mTabHost.addTab(tabSpec);
        }

        @Override
        public void onTabChanged(String tabId) {
        	Log.i(LOG_TAG, tabId);
            TabInfo newTab = mTabs.get(tabId);
            if (mLastTab != newTab) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                if (mLastTab != null) {
                    if (mLastTab.fragment != null) {
                        ft.detach(mLastTab.fragment);
                    }
                }
                if (newTab != null) {
                    if (newTab.fragment == null) {
                        newTab.fragment = Fragment.instantiate(mActivity,
                                newTab.clss.getName(), newTab.args);
                        ft.add(mContainerId, newTab.fragment, newTab.tag);
                    } else {
                        ft.attach(newTab.fragment);
                    }
                }

                mLastTab = newTab;
                ft.commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        }
    }
}



