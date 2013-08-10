package gov.in.bloomington.georeporter.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.GoogleMap;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.customviews.EnhancedSupportMapFragment;

public class ReportsNearYouFragment extends SherlockFragment{
    private View layout;
    private EnhancedSupportMapFragment fragment;
    private GoogleMap map;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment =  (EnhancedSupportMapFragment) fm.findFragmentById(R.id.map_near_fragment);
        if (fragment == null) {
            fragment = new EnhancedSupportMapFragment();
            fm.beginTransaction().replace(R.id.map_near_fragment, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            map = fragment.getMap();            
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_reports_near, container, false);
        return layout;
    }
    
    

}
