package gov.in.bloomington.georeporter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import gov.in.bloomington.georeporter.R;

public class ReportsNearYouFragment extends SherlockFragment{
    private View layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_reports_near, container, false);
        return layout;
    }
    
    

}
