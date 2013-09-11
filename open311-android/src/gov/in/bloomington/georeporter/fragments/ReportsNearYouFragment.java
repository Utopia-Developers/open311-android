
package gov.in.bloomington.georeporter.fragments;

import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.customviews.EnhancedSupportMapFragment;
import gov.in.bloomington.georeporter.json.RequestsJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.util.LocationUtils;
import gov.in.bloomington.georeporter.util.Util;
import gov.in.bloomington.georeporter.volleyrequests.GsonGetRequest;
import gov.in.bloomington.georeporter.volleyrequests.OkHttpStack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReportsNearYouFragment extends SherlockFragment implements LocationListener,
        ConnectionCallbacks, OnConnectionFailedListener, OnCameraChangeListener {
    private View layout;
    private EnhancedSupportMapFragment fragment;
    private GoogleMap mMap;

    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;

    private VisibleRegion visibleRegion;

    // My bound values
    double left;
    double top;
    double right;
    double bottom;
    LatLng topLeft, prevTopLeft;
    private float currentZoom;
    private float threshold;

    private GsonGetRequest<ArrayList<RequestsJson>> request;
    private HashMap<String, Marker> markerHash;
    private HashMap<Marker, RequestsJson> markerDataHash;
    private boolean initialDataFetched;
    
    private String startDate,stopDate;
    private SimpleDateFormat sdf;
    final String isodateformat = "yyyy-MM-dd'T'HH:mm:ssZ";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (EnhancedSupportMapFragment) fm.findFragmentById(R.id.map_near_fragment);
        if (fragment == null) {
            fragment = new EnhancedSupportMapFragment();
            fm.beginTransaction().replace(R.id.map_near_fragment, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap == null) {
            mMap = fragment.getMap();
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setMyLocationEnabled(false);
            mMap.moveCamera(CameraUpdateFactory.zoomTo(17));

            mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
                // We will keep the default bubble
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    LayoutInflater inflator = LayoutInflater.from(getActivity());
                    View v = inflator.inflate(R.layout.infowindow_layout, null,
                            false);
                    renderMarker(marker, v);
                    return v;
                }
            });

        }

        if (Open311.requestQueue == null)
        {
            Open311.requestQueue = Volley.newRequestQueue(getActivity(), new OkHttpStack());
        }
        if (Open311.sEndpoint.bbox)
        {
            visibleRegion = mMap.getProjection().getVisibleRegion();
            mMap.setOnCameraChangeListener(this);
            currentZoom = mMap.getCameraPosition().zoom;
            LatLng currCenter = mMap.getCameraPosition().target;
            setVisibleRegionBounds();
            float result[] = new float[1];
            Location.distanceBetween(currCenter.latitude, currCenter.longitude,
                    currCenter.latitude, left, result);
            threshold = result[0];
            Log.d("Threshold", threshold + " ");
        }
        else
        {
            Calendar c = Calendar.getInstance();
            sdf = new SimpleDateFormat(isodateformat,Locale.US);
            c.setTime(new Date()); // Now use today date.
            stopDate = sdf.format(c.getTime());
            c.add(Calendar.DATE, -30); // last month
            startDate = sdf.format(c.getTime());
        }
    }

    public void setVisibleRegionBounds()
    {
        prevTopLeft = topLeft;
        visibleRegion = mMap.getProjection().getVisibleRegion();
        left = visibleRegion.latLngBounds.southwest.longitude;
        top = visibleRegion.latLngBounds.northeast.latitude;
        right = visibleRegion.latLngBounds.northeast.longitude;
        bottom = visibleRegion.latLngBounds.southwest.latitude;
        topLeft = new LatLng(top, left);
        if (prevTopLeft != null && topLeft != null)
            Log.d("LatLng", topLeft.latitude + " " + topLeft.longitude + " " + prevTopLeft.latitude
                    + " " + prevTopLeft.longitude);
    }

    public void fetchNewReports()
    {
        float distance;
        float result[] = new float[1];
        setVisibleRegionBounds();
        if (prevTopLeft != null)
        {
            Location.distanceBetween(prevTopLeft.latitude, prevTopLeft.longitude, topLeft.latitude,
                    topLeft.longitude, result);
            distance = result[0];
            Log.d("Threshold", threshold + " " + distance);
            if (distance >= threshold)
                fetchData();
        }
    }

    public void renderMarker(Marker marker, View v)
    {
        RequestsJson data = markerDataHash.get(marker);
        TextView title, description, agency, date, address;
        title = (TextView) v.findViewById(R.id.textViewTitle);
        description = (TextView) v.findViewById(R.id.textViewDescription);
        agency = (TextView) v.findViewById(R.id.textViewAgency);
        date = (TextView) v.findViewById(R.id.textViewDate);
        address = (TextView) v.findViewById(R.id.textViewAddress);
        title.setText(data.getService_name());

        if (data.getDescription() != null)
            description.setText(data.getDescription());
        if (data.agency_responsible != null)
            agency.setText(data.agency_responsible);

        if (data.getUpdated_datetime() != null)
        {
            sdf= new SimpleDateFormat(isodateformat,Locale.US);            
            Date d = null;
            try {
                d = sdf.parse(data.getUpdated_datetime());
                sdf.applyPattern("MMMM dd, yyyy");
                date.setText(sdf.format(d));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (data.getAddress() != null)
            address.setText(data.getAddress());

    }

    public void fetchData()
    {
        String url = Open311.sEndpoint.bbox ? Open311
                .getServiceRequestUrl(bottom, left, top, right) : Open311.getServiceRequestUrl(startDate,
                stopDate, "open");
        Log.d("URL", url);
        request = new GsonGetRequest<ArrayList<RequestsJson>>(url,
                new TypeToken<ArrayList<RequestsJson>>() {
                }.getType(), null, new Listener<ArrayList<RequestsJson>>() {
                    @Override
                    public void onResponse(ArrayList<RequestsJson> response) {
                        RequestsJson data;
                        Log.d("Response", response.size() + " ");
                        int count = 0;
                        for (int i = 0; i < response.size(); i++)
                        {
                            data = response.get(i);
                            //We need Lat and Long
                            if (!markerHash.containsKey(data.getService_request_id()) && data.getLat()!=null && data.getLong()!=null)
                            {
                                count++;
                                Marker temp = createMarker(data);
                                markerHash.put(data.getService_request_id(), temp);
                                markerDataHash.put(temp, data);
                            }
                        }
                        Log.d("New Markers", count + " ");
                    }
                }, new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        Open311.requestQueue.add(request);
    }

    public Marker createMarker(RequestsJson data)
    {
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(Double.parseDouble(data.getLat()), Double.parseDouble(data
                .getLong())));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = mMap.addMarker(options);
        return marker;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_reports_near, container, false);
        mLocationClient = new LocationClient(getActivity(), this, this);
        markerHash = new HashMap<String, Marker>();
        markerDataHash = new HashMap<Marker, RequestsJson>();
        initialDataFetched = false;
        return layout;
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

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
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
            fetchNewReports();
            initialDataFetched = true;
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
        if (!initialDataFetched)
            fetchNewReports();
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        fetchNewReports();
        if (position.zoom != currentZoom)
        {
            LatLng currCenter = position.target;
            Log.d("Camera", "Zoom Changed");
            currentZoom = position.zoom;
            float result[] = new float[1];
            Location.distanceBetween(currCenter.latitude, currCenter.longitude,
                    currCenter.latitude, left, result);
            threshold = result[0];
            Log.d("Threshold", threshold + " ");
        }
    }

}
