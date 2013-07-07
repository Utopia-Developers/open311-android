
package gov.in.bloomington.georeporter.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.activities.ReportActivity;
import gov.in.bloomington.georeporter.activities.SettingsActivity;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.json.ServiceDefinationJson;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.Preferences;
import gov.in.bloomington.georeporter.util.Open311XmlParser;
import gov.in.bloomington.georeporter.util.Util;
import gov.in.bloomington.georeporter.volleyrequests.GsonGetRequest;
import gov.in.bloomington.georeporter.volleyrequests.OkHttpStack;
import gov.in.bloomington.georeporter.volleyrequests.Open311XmlRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MainFragment extends SherlockFragment {
    private View layout;
    private static String SPLASH_IMAGE = "splash_image";
    private String serviceDefinationTag = "Defination";

    private ImageView splashImage;

    private AtomicInteger pendingRequests;
    private ProgressDialog progressDialog;

    private ServerAttributeJson current_server;
    
    private OnSetActionBarTitleListener titleSetCallback;
    
    public interface OnSetActionBarTitleListener
    {
        public void setActionBarTitle(String title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_main,
                container, false);

        splashImage = (ImageView) layout.findViewById(R.id.splash);
        splashImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onTouchImage(v);
            }
        });
        setRetainInstance(true);
        return layout;
    }

    @Override
    public void onResume() {
        // Good to start task in onResume cause It garentees Activity is
        // created.
        super.onResume();
        current_server = Preferences.getCurrentServer(getActivity());
        if (Open311.prevEndpoint == null
                || !Open311.prevEndpoint.contentEquals(current_server.url)
                || Open311.isLatestServiceListLoaded == false)
        {

            pendingRequests = new AtomicInteger(0);
            
            progressDialog = ProgressDialog.show(getActivity(),
                    getString(R.string.dialog_loading_services), "Please Wait", true);

            Open311.sEndpoint = current_server;

            // TODO
            if (Open311.requestQueue == null)
                Open311.requestQueue = Volley.newRequestQueue(getActivity(), new OkHttpStack());

            if (current_server == null) {
                progressDialog.dismiss();
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            } else {

                // Set the variable in the model from the server
                Open311.setCurrentServerDetails(current_server);

                if (current_server.format.contentEquals(Open311.JSON))
                {
                    String url = Open311.getServiceListUrl();
                    // Else we get a exception from volley
                    if (url.startsWith("www."))
                        url += "http://";
                    Open311.sServiceRequestGson = new GsonGetRequest<ArrayList<ServiceEntityJson>>(
                            url,
                            new TypeToken<ArrayList<ServiceEntityJson>>() {
                            }.getType(), null, new Listener<ArrayList<ServiceEntityJson>>() {

                                @Override
                                public void onResponse(ArrayList<ServiceEntityJson> response) {
                                    postResponseSetup(response);
                                }
                            }, new ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    postResponseError(error);
                                }
                            });
                    Open311.requestQueue.add(Open311.sServiceRequestGson);
                }
                else
                {
                    Open311.sServiceRequestXML = new Open311XmlRequest<ArrayList<ServiceEntityJson>>(
                            Open311.getServiceListUrl(),
                            new Listener<ArrayList<ServiceEntityJson>>() {

                                @Override
                                public void onResponse(ArrayList<ServiceEntityJson> response) {
                                    postResponseSetup(response);
                                }
                            }, Open311XmlParser.SERVICE_REQUESTS, new ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    postResponseError(error);
                                }
                            });
                    Open311.requestQueue.add(Open311.sServiceRequestXML);
                }

            }
        }
        else
        {
            setupFragment();
        }

    }

    /**
     * OnClick handler for activity_main layout
     * 
     * @param View v
     */
    public void onTouchImage(View v) {
        Intent intent = new Intent(getActivity(), ReportActivity.class);
        startActivity(intent);
    }

    public void postResponseError(VolleyError error)
    {
        progressDialog.dismiss();
        Open311.isLatestServiceListLoaded = false;
        Util.displayCrashDialog(
                getActivity(),
                getString(R.string.failure_loading_services));
    }

    public void postResponseSetup(ArrayList<ServiceEntityJson> response)
    {
        Open311.sServiceList = response;

        // If no metadata
        if (!loadServiceDefinations())
        {
            progressDialog.dismiss();
            Open311.prevEndpoint = Open311.sEndpoint.url;
            Open311.isLatestServiceListLoaded = true;
            Intent intent = new Intent(getActivity(), ReportActivity.class);
            startActivity(intent);
        }

        setupFragment();
    }

    public void setupFragment()
    {
        titleSetCallback = (OnSetActionBarTitleListener) getActivity();
        titleSetCallback.setActionBarTitle(current_server.name);

        String imageName = current_server.splash_image;
        Log.d("Image Name", imageName + "");
        if (imageName != null) {
            ImageView splash = (ImageView) layout.findViewById(R.id.splash);
            splash.setImageResource(getResources().getIdentifier(imageName,
                    "drawable", getActivity().getPackageName()));

            splash.setContentDescription(current_server.name);

        }
    }

    public boolean loadServiceDefinations()
    {
        boolean isServiceDefinationPresent = false;
        Open311.sGroups = new ArrayList<String>();
        Open311.sServiceDefinitions = new HashMap<String, ServiceDefinationJson>();

        // Go through all the services and pull out the seperate groups
        // Also, while we're running through, load any service_definitions
        GsonGetRequest<ServiceDefinationJson> serviceDefinationRequestGson;
        Open311XmlRequest<ServiceDefinationJson> serviceDefinationRequestXML;
        String group = null;
        int len = Open311.sServiceList.size();
        for (int i = 0; i < len; i++) {
            ServiceEntityJson s = Open311.sServiceList.get(i);
            // services may have an empty string for the group parameter
            group = s.getGroup();
            if (group == null) {
                group = getString(R.string.uncategorized);
            }
            if (!Open311.sGroups.contains(group)) {
                Open311.sGroups.add(group);
            }

            // Add Service Definitions to mServiceDefinitions
            if (s.getMetadata() == true) {
                isServiceDefinationPresent = true;
                final String code = s.getService_code();

                if (current_server.format.contentEquals(Open311.JSON))
                {
                    serviceDefinationRequestGson = new GsonGetRequest<ServiceDefinationJson>(
                            Open311.getServiceDefinitionUrl(code),
                            new TypeToken<ServiceDefinationJson>() {
                            }.getType(), null, new Listener<ServiceDefinationJson>() {

                                @Override
                                public void onResponse(ServiceDefinationJson response) {
                                    Open311.sServiceDefinitions.put(code, response);
                                    if (pendingRequests.decrementAndGet() == 0)
                                    {
                                        Open311.prevEndpoint = Open311.sEndpoint.url;
                                        Open311.isLatestServiceListLoaded = true;
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(getActivity(),
                                                ReportActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }, new ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.dismiss();
                                    Open311.isLatestServiceListLoaded = false;
                                    Util.displayCrashDialog(
                                            getActivity(),
                                            getString(R.string.failure_loading_services)
                                                    + error.getMessage());
                                    Open311.requestQueue.cancelAll(serviceDefinationTag);
                                }
                            });
                    serviceDefinationRequestGson.setTag(serviceDefinationTag);

                    Open311.requestQueue.add(serviceDefinationRequestGson);
                    pendingRequests.incrementAndGet();
                }
                else
                {
                    serviceDefinationRequestXML = new Open311XmlRequest<ServiceDefinationJson>(
                            Open311.getServiceDefinitionUrl(code),
                            new Listener<ServiceDefinationJson>() {

                                @Override
                                public void onResponse(ServiceDefinationJson response) {
                                    Open311.sServiceDefinitions.put(code, response);
                                    if (pendingRequests.decrementAndGet() == 0)
                                    {
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(getActivity(),
                                                ReportActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }, Open311XmlParser.SERVICE_DEFINITION, new ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    progressDialog.dismiss();
                                    Util.displayCrashDialog(
                                            getActivity(),
                                            getString(R.string.failure_loading_services)
                                                    + error.getMessage());
                                    Open311.requestQueue.cancelAll(serviceDefinationTag);
                                }
                            });

                    serviceDefinationRequestXML.setTag(serviceDefinationTag);

                    Open311.requestQueue.add(serviceDefinationRequestXML);
                    pendingRequests.incrementAndGet();
                }

            }
        }

        return isServiceDefinationPresent;
    }

}
