
package gov.in.bloomington.georeporter.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.RequestFuture;
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
import gov.in.bloomington.georeporter.util.Util;
import gov.in.bloomington.georeporter.volleyrequests.GsonGetRequest;
import gov.in.bloomington.georeporter.volleyrequests.OkHttpStack;

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
        pendingRequests = new AtomicInteger(0);
        progressDialog = ProgressDialog.show(getActivity(),
                getString(R.string.dialog_loading_services), "", true);

        final ServerAttributeJson current_server = Preferences.getCurrentServer(getActivity());

        // TODO
        if (Open311.requestQueue == null)
            Open311.requestQueue = Volley.newRequestQueue(getActivity(), new OkHttpStack());

        if (current_server == null) {
            progressDialog.dismiss();
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        } else {

            // Set the variable in the model from the server
            Open311.setCurrentServerDetails(current_server);
            Open311.sServiceRequest = new GsonGetRequest<ArrayList<ServiceEntityJson>>(
                    Open311.getServiceListUrl(), new TypeToken<ArrayList<ServiceEntityJson>>() {
                    }.getType(), null, new Listener<ArrayList<ServiceEntityJson>>() {

                        @Override
                        public void onResponse(ArrayList<ServiceEntityJson> response) {
                            Open311.sServiceList = response;

                            // If no metadata
                            if (!loadServiceDefinations())
                            {
                                progressDialog.dismiss();
                                Intent intent = new Intent(getActivity(), ReportActivity.class);
                                startActivity(intent);
                            }

                            getSherlockActivity().getSupportActionBar().setTitle(
                                    current_server.name);

                            String imageName = current_server.splash_image;
                            Log.d("Image Name", imageName + "");
                            if (imageName != null) {
                                ImageView splash = (ImageView) layout.findViewById(R.id.splash);
                                splash.setImageResource(getResources().getIdentifier(imageName,
                                        "drawable", getActivity().getPackageName()));

                                splash.setContentDescription(current_server.name);

                            }
                        }
                    }, new ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Util.displayCrashDialog(
                                    getActivity(),
                                    getString(R.string.failure_loading_services)
                                            + error.getMessage());
                        }
                    });
            Open311.requestQueue.add(Open311.sServiceRequest);

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

    public boolean loadServiceDefinations()
    {
        boolean isServiceDefinationPresent = false;
        Open311.sGroups = new ArrayList<String>();
        Open311.sServiceDefinitions = new HashMap<String, ServiceDefinationJson>();

        // Go through all the services and pull out the seperate groups
        // Also, while we're running through, load any service_definitions
        GsonGetRequest<ServiceDefinationJson> serviceDefinationRequest;
        String group = "";
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

                // TODO
                serviceDefinationRequest = new GsonGetRequest<ServiceDefinationJson>(
                        Open311.getServiceDefinitionUrl(code),
                        new TypeToken<ServiceDefinationJson>() {
                        }.getType(), null, new Listener<ServiceDefinationJson>() {

                            @Override
                            public void onResponse(ServiceDefinationJson response) {
                                Open311.sServiceDefinitions.put(code, response);
                                if (pendingRequests.decrementAndGet() == 0)
                                {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(getActivity(), ReportActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }, new ErrorListener() {

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
                serviceDefinationRequest.setTag(serviceDefinationTag);

                Open311.requestQueue.add(serviceDefinationRequest);
                pendingRequests.incrementAndGet();
            }
        }

        return isServiceDefinationPresent;
    }

}
