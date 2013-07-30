
package gov.in.bloomington.georeporter.fragments;

import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.customviews.EnhancedSupportMapFragment;
import gov.in.bloomington.georeporter.util.LocationUtils;
import gov.in.bloomington.georeporter.util.Util;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ChooseLocationFragment extends SherlockFragment implements LocationListener,
        ConnectionCallbacks, OnConnectionFailedListener, OnClickListener {

    private View layout;
    private GoogleMap mMap;
    // private RadioGroup mapRadio;

    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;

    private Button setAddress, removeAddress, snapToAddress;
    private Marker addressMarker;
    private LatLng setPosition;
    protected String addressVal;

    public static final int UPDATE_GOOGLE_MAPS_REQUEST = 0;

    public static final int DEFAULT_ZOOM = 17;
    
    private OnMapPositionClicked mapPositionClickedListener;
    public interface OnMapPositionClicked
    {
        public void positionClicked(String address,double latitude,double longitude);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_report_mapchooser, container, false);
        mLocationClient = new LocationClient(getActivity(), this, this);
        setAddress = (Button) layout.findViewById(R.id.buttonAddLocAddr);
        removeAddress = (Button) layout.findViewById(R.id.buttonRemoveLocAddr);
        snapToAddress = (Button) layout.findViewById(R.id.buttonSnapToAddress);
        setAddress.setOnClickListener(this);
        removeAddress.setOnClickListener(this);
        snapToAddress.setOnClickListener(this);
        mapPositionClickedListener = (ReportFragment)getFragmentManager().findFragmentByTag("Report");
        setUpMapIfNeeded();
        setRetainInstance(true);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play
     * services APK is correctly installed) and the map has not already been
     * instantiated.. This will ensure that we only ever call
     * {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt
     * for the user to install/update the Google Play services APK on their
     * device.
     * <p>
     * A user can return to this Activity after following the prompt and
     * correctly installing/updating/enabling the Google Play services. Since
     * the Activity may not have been completely destroyed during this process
     * (it is likely that it would only be stopped or paused),
     * {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            EnhancedSupportMapFragment mapFragment = (EnhancedSupportMapFragment) getFragmentManager()
                    .findFragmentById(R.id.map_fragment);
            mMap = mapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the
     * camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap}
     * is not null.
     */
    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

        // mapRadio = (RadioGroup) layout.findViewById(R.id.map_radio);

        /*
         * mapRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
         * @Override public void onCheckedChanged(RadioGroup group, int
         * checkedId) { switch (checkedId) { case R.id.rb_satellite:
         * mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); break; case
         * R.id.rb_hybrid: mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); break;
         * default: mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); } } });
         */
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener. The current Activity is
             * the listener, so the argument is "this".
             */
            mLocationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is considered "dead".
         */
        mLocationClient.disconnect();

        super.onStop();
    }

    public boolean isLocationGoodEnough(Location location)
    {
        if (location.getAccuracy() < LocationUtils.locationAccuraceThreshold)
            return true;
        else
            return false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects. If the error
         * has a resolution, try sending an Intent to start a Google Play
         * services activity that can resolve error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(),
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the user with
             * the error.
             */
            Util.displayCrashDialog(getActivity(),
                    "Sorry an error " + connectionResult.getErrorCode() + " occured.");
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(LocationUtils.FASTEST_INTERVAL);
        mLocationClient.requestLocationUpdates(mLocationRequest, this);

        // Now that the client has connected set the map to the last known
        // location to start off with
        Location location = mLocationClient.getLastLocation();

        if (location != null)
        {
            Log.d("Orignal Loc", location.getLatitude() + " " + location.getLongitude());
            LatLng latlong = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlong));
        }
    }

    @Override
    public void onDisconnected() {
        // Do nothing
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("New Loc", location.getLatitude() + " " + location.getLongitude() + " Accuracy "
                + location.getAccuracy());
        if (isLocationGoodEnough(location))
        {
            mLocationClient.removeLocationUpdates(this);
        }

        LatLng p = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(p));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.buttonAddLocAddr:
                if (addressMarker != null)
                    addressMarker.remove();

                setPosition = mMap.getCameraPosition().target;
                if (setPosition == null)
                {
                    setPosition = new LatLng(0, 0);
                }

                addressMarker = mMap
                        .addMarker(new MarkerOptions()
                                .position(setPosition)
                                .title("Address")
                                .snippet(
                                        "Lat: " + setPosition.latitude + " Long: "
                                                + setPosition.longitude));
                addressMarker.showInfoWindow();
                mapPositionClickedListener.positionClicked(addressVal,setPosition.latitude,setPosition.longitude);
                new ReverseGeocodingTask().execute(setPosition);
                break;
            case R.id.buttonRemoveLocAddr:
                addressMarker.remove();
                break;
            case R.id.buttonSnapToAddress:
                if (setPosition != null)
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(setPosition));
                break;
        }
    }

    /**
     * Task for using Google's Geocoder Queries Google's geocode, updates the
     * address in ServiceRequest, then refreshes the view so the user can see
     * the change
     */
    private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
        @Override
        protected String doInBackground(LatLng... params) {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            LatLng point = params[0];
            double latitude = point.latitude;
            double longitude = point.longitude;

            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                return String.format("%s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String address) {
            addressMarker.setSnippet(address);
            addressMarker.showInfoWindow();
            addressVal = address;
            mapPositionClickedListener.positionClicked(address,setPosition.latitude,setPosition.longitude);
        }
    }

}
