/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.customviews.EnhancedSupportMapFragment;
import gov.in.bloomington.georeporter.customviews.RoundedDrawable;
import gov.in.bloomington.georeporter.json.RequestsJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.ServiceRequest;

import org.json.JSONException;

import java.util.ArrayList;

public class SavedReportViewFragment extends SherlockFragment {
    private static final String POSITION = "position";
    private ServiceRequest mServiceRequest;
    private int mPosition;
    private GoogleMap mMap;
    private EnhancedSupportMapFragment mapFragment;
    private View layout;
    private RoundedDrawable defaultLogo;
    private int size;

    public static SavedReportViewFragment newInstance(int position) {
        SavedReportViewFragment fragment = new SavedReportViewFragment();
        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt(POSITION);
        if (Open311.mServiceRequests == null)
            Open311.mServiceRequests = Open311.loadServiceRequests(getActivity());
        mServiceRequest = Open311.mServiceRequests.get(mPosition);
        size = getActivity().getResources().getDimensionPixelSize(R.dimen.logo);
        defaultLogo = new RoundedDrawable(BitmapFactory.decodeResource(
                getActivity().getResources(), R.drawable.ic_launcher), size, size);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_report_saved, container, false);
        mapFragment = (EnhancedSupportMapFragment) getFragmentManager().findFragmentById(
                R.id.map_fragment);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        refreshViewData();
        refreshFromServer();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            // Try to obtain the map from the EnhancedSupportMapFragment.

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
        if (mServiceRequest.service_request.getLat() != null
                && mServiceRequest.service_request.getLong() != null)
        {
            LatLng addressLoc = new LatLng(Double.parseDouble(mServiceRequest.service_request
                    .getLat()), Double.parseDouble(mServiceRequest.service_request.getLong()));
            MarkerOptions options = new MarkerOptions();
            options.title(mServiceRequest.service_request.getService_name());
            if (mServiceRequest.service_request.getAddress() != null)
                options.snippet(mServiceRequest.service_request.getAddress());
            else
                options.snippet("Lat " + mServiceRequest.service_request.getLat() + " Long "
                        + mServiceRequest.service_request.getLong());
            options.position(addressLoc);
            Marker addr = mMap.addMarker(options);
            addr.showInfoWindow();

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(addressLoc, 17));
        }
        else
        {
            mapFragment.getView().setVisibility(View.GONE);
        }
    }

    private void refreshViewData() {
        View v = getView();
        TextView textView;

        textView = (TextView) v.findViewById(R.id.service_name);
        textView.setText(mServiceRequest.service.getService_name());

        ImageView media = (ImageView) v.findViewById(R.id.media);
        media.setImageBitmap(mServiceRequest.getMediaBitmap(100, 100, getActivity()));

        textView = (TextView) v.findViewById(R.id.endpoint);
        if (mServiceRequest.endpoint.name != null) {
            textView.setText(mServiceRequest.endpoint.name);
        }

        textView = (TextView) v.findViewById(R.id.description);
        if (mServiceRequest.service.getDescription() != null) {
            textView.setText(mServiceRequest.service.getDescription());
        }
        else if (mServiceRequest.post_data.has(Open311.DESCRIPTION)) {
            textView.setText(mServiceRequest.post_data.optString(Open311.DESCRIPTION));
        }

        textView = (TextView) v.findViewById(R.id.status);
        if (mServiceRequest.service_request.getStatus() != null) {
            textView.setText(mServiceRequest.service_request.getStatus());
        }

        ImageView status = (ImageView) v.findViewById(R.id.imageViewStatus);
        if (!mServiceRequest.service_request.getStatus().contentEquals("open"))
            status.setImageResource(R.drawable.closedissue);

        ImageView logo = (ImageView) v.findViewById(R.id.imageViewEndpoint);
        // TODO Depending on the endpoint show the endpoint logo else show
        // default logo
        logo.setImageDrawable(defaultLogo);
    }

    public void refreshFromServer()
    {

        final RequestsJson sr = mServiceRequest.service_request;

        if (sr.getService_request_id() == null) {

            StringRequest getRequestId = null;
            try {
                getRequestId = new StringRequest(mServiceRequest.getServiceRequestIdFromTokenUrl(sr
                        .getToken()), new Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("Server Response", response);
                        String id;
                        id = response;
                        if (id != null) {
                            sr.setService_request_id(id);
                        }
                        else {
                            String pending = getResources().getString(R.string.pending);
                            if (!sr.getStatus().equals(pending)) {
                                sr.setStatus(pending);
                            }
                        }
                        if (sr.getService_request_id() != null) {
                            String request_id = mServiceRequest.service_request
                                    .getService_request_id();
                            StringRequest updateServiceRequest = new StringRequest(
                                    mServiceRequest.getServiceRequestUrl(request_id),
                                    new Listener<String>() {

                                        @Override
                                        public void onResponse(String response) {
                                            Log.d("Server Response", response);
                                            if (response != null && response != "")
                                            {
                                                ArrayList<RequestsJson> results = new Gson()
                                                        .fromJson(
                                                                response,
                                                                new TypeToken<ArrayList<RequestsJson>>() {
                                                                }.getType());
                                                mServiceRequest.service_request = results.get(0);
                                                Open311.mServiceRequests.add(mServiceRequest);
                                                Open311.saveServiceRequests(getActivity(),
                                                        Open311.mServiceRequests);
                                                refreshViewData();
                                            }

                                        }
                                    }, new ErrorListener() {

                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                        }
                                    });

                            Open311.requestQueue.add(updateServiceRequest);

                        }
                    }
                }, new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
            } catch (JSONException e) {

                e.printStackTrace();
            }

            Open311.requestQueue.add(getRequestId);

        }

    }

}
