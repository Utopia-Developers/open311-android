package gov.in.bloomington.georeporter.activities;

import gov.in.bloomington.georeporter.R;
import android.app.Activity;
// import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class DashboardActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // TODO create the dashboard_layout
	    setContentView(R.layout.activity_dashboard);
	}
	
	public void onClickAbout (View v){
		
	    startActivity (new Intent(getApplicationContext(), AboutActivity.class));
	}
	
	public void onClickReport (View v){
		
	    startActivity (new Intent(getApplicationContext(), ReportActivity.class));
	}
	
	public void onClickArchive (View v){
		
	    startActivity (new Intent(getApplicationContext(), SavedReportsActivity.class));
	}
	
	public void onClickSettings (View v){
		
	    startActivity (new Intent(getApplicationContext(), SettingsActivity.class));
	}
	
}
