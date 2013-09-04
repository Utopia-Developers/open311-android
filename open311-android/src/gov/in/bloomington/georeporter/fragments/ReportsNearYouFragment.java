package gov.in.bloomington.georeporter.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.customviews.EnhancedSupportMapFragment;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.volleyrequests.OkHttpStack;

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
            /*map.setInfoWindowAdapter(new InfoWindowAdapter() {
                
                //We will keep the default bubble
                @Override
                public View getInfoWindow(Marker arg0) {                
                    return null;
                }
                
                @Override
                public View getInfoContents(Marker marker) {
                    LayoutInflater inflator = LayoutInflater.from(getActivity());
                    View v = inflator.inflate(R.layout.infowindow_layout, null, false);                
                    renderMarker(marker,v);
                    return v;
                }
            });*/
        }
        
        if(Open311.requestQueue == null)
        {
            Open311.requestQueue = Volley.newRequestQueue(getActivity(), new OkHttpStack());
        }
    }
    
    public void renderMarker(Marker marker,View v)
    {
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_reports_near, container, false);
        return layout;
    }
    
    

}
