/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.activities.MainActivity.OnDataRefreshListener;
import gov.in.bloomington.georeporter.adapters.GroupsFragmentStatePagerAdapter;
import gov.in.bloomington.georeporter.json.ServiceDefinationJson;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.util.Open311XmlParser;
import gov.in.bloomington.georeporter.volleyrequests.GsonGetRequest;
import gov.in.bloomington.georeporter.volleyrequests.OkHttpStack;
import gov.in.bloomington.georeporter.volleyrequests.Open311XmlRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChooseGroupFragment extends SherlockFragment implements OnDataRefreshListener,
        OnClickListener {
    private OnGroupSelectedListener mListener;
    private View layout;
    private GroupsFragmentStatePagerAdapter adapter;
    private ViewPager pager;
    private PagerSlidingTabStrip tabStrip;
    private ProgressBar progressBar;
    private TextView error;
    private Button retry;

    private AtomicInteger pendingRequests;
    private OnSetActionBarTitleListener titleSetCallback;
    private String serviceDefinationTag = "Defination";

    public interface OnSetActionBarTitleListener
    {
        public void setActionBarTitle(String title);
    }

    @Override
    public void onResume() {
        // Good to start task in onResume cause It garentees Activity is
        // created.
        super.onResume();

        if (Open311.sEndpoint != null && (Open311.prevEndpoint == null
                || !Open311.prevEndpoint.contentEquals(Open311.sEndpoint.url)
                || Open311.isLatestServiceListLoaded == false))
        {
            refresh();
        }
        else
        {
            setupFragment();
        }

    }

    public interface OnGroupSelectedListener {
        public void onGroupSelected(String group, boolean single);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnGroupSelectedListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_choose_group, container, false);
        adapter = new GroupsFragmentStatePagerAdapter(getFragmentManager(), getActivity());
        pager = (ViewPager) layout.findViewById(R.id.pager);
        tabStrip = (PagerSlidingTabStrip) layout.findViewById(R.id.tabs);
        progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);
        error = (TextView) layout.findViewById(R.id.textViewError);
        retry = (Button) layout.findViewById(R.id.buttonRetry);
        retry.setOnClickListener(this);
        progressBar.setVisibility(View.GONE);
        pager.setAdapter(adapter);
        tabStrip.setViewPager(pager);
        return layout;
    }

    public void setErrorMessage(boolean showMessage)
    {
        if (showMessage == false)
        {
            error.setVisibility(View.GONE);
            retry.setVisibility(View.GONE);
        }
        else
        {
            error.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
        }

    }

    public void postResponseError(VolleyError error)
    {
        if (error.networkResponse != null)
            Log.d("Status GET", error.networkResponse.statusCode + "");
        progressBar.setVisibility(View.GONE);
        Open311.isLatestServiceListLoaded = false;
        setErrorMessage(true);
    }

    public void postResponseSetup(ArrayList<ServiceEntityJson> response)
    {

        // If no metadata
        if (!loadServiceDefinations(response))
        {
            showLoader(false);
            pager.setVisibility(View.VISIBLE);
            tabStrip.setVisibility(View.VISIBLE);
            Open311.prevEndpoint = Open311.sEndpoint.url;
            Open311.isLatestServiceListLoaded = true;
            adapter.notifyDataSetChanged();
            tabStrip.notifyDataSetChanged();
        }

        setupFragment();
    }

    public void setupFragment()
    {
        titleSetCallback = (OnSetActionBarTitleListener) getActivity();
        if (Open311.sEndpoint != null)
        {
            titleSetCallback.setActionBarTitle(Open311.sEndpoint.name);
        }
    }

    public void refresh()
    {
        if (!Open311.isDataLoading)
        {
            Open311.isDataLoading = true;
            Open311.prevEndpoint = null;
            Open311.isLatestServiceListLoaded = false;

            pendingRequests = new AtomicInteger(0);

            showLoader(true);

            if (Open311.requestQueue == null)
                Open311.requestQueue = Volley.newRequestQueue(getActivity(), new OkHttpStack());

            if (Open311.sEndpoint.format.contentEquals(Open311.JSON))
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
                                Open311.isDataLoading = false;
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Open311.isDataLoading = false;
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
                                Open311.isDataLoading = false;
                            }
                        }, Open311XmlParser.SERVICE_REQUESTS, new ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Open311.isDataLoading = false;
                                postResponseError(error);
                            }
                        });
                Open311.requestQueue.add(Open311.sServiceRequestXML);
            }
        }
        else
        {
            showLoader(true);
        }

    }

    public void showLoader(boolean showLoader)
    {
        if (showLoader)
        {
            progressBar.setVisibility(View.VISIBLE);
            setErrorMessage(false);
            pager.setVisibility(View.GONE);
            tabStrip.setVisibility(View.GONE);
        }
        else
            progressBar.setVisibility(View.GONE);
    }

    public boolean loadServiceDefinations(ArrayList<ServiceEntityJson> response)
    {
        boolean isServiceDefinationPresent = false;
        Open311.sGroups = new ArrayList<String>();
        Open311.sServiceDefinitions = new HashMap<String, ServiceDefinationJson>();
        ArrayList<ServiceEntityJson> temp;
        // Go through all the services and pull out the seperate groups
        // Also, while we're running through, load any service_definitions
        GsonGetRequest<ServiceDefinationJson> serviceDefinationRequestGson;
        Open311XmlRequest<ServiceDefinationJson> serviceDefinationRequestXML;
        String group = null;
        if (Open311.sServiceGroups == null)
            Open311.sServiceGroups = new HashMap<String, ArrayList<ServiceEntityJson>>();
        int len = response.size();
        for (int i = 0; i < len; i++) {
            ServiceEntityJson s = response.get(i);
            // services may have an empty string for the group parameter
            group = s.getGroup();
            if (group == null) {
                group = getString(R.string.uncategorized);
            }

            if (!Open311.sGroups.contains(group)) {
                Open311.sGroups.add(group);
                temp = new ArrayList<ServiceEntityJson>();
                temp.add(s);
                Open311.sServiceGroups.put(group, temp);
            } else
            {
                Open311.sServiceGroups.get(group).add(s);
            }

            // Add Service Definitions to mServiceDefinitions
            if (s.getMetadata() == true) {
                isServiceDefinationPresent = true;
                final String code = s.getService_code();

                if (Open311.sEndpoint.format.contentEquals(Open311.JSON))
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
                                        Open311.isDataLoading = false;
                                        Open311.prevEndpoint = Open311.sEndpoint.url;
                                        Open311.isLatestServiceListLoaded = true;
                                        showLoader(false);
                                        pager.setVisibility(View.VISIBLE);
                                        tabStrip.setVisibility(View.VISIBLE);
                                        adapter.notifyDataSetChanged();
                                        tabStrip.notifyDataSetChanged();
                                    }
                                }
                            }, new ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Open311.isDataLoading = false;
                                    if (error.networkResponse != null)
                                        Log.d("Status GET", error.networkResponse.statusCode + "");
                                    showLoader(false);
                                    Open311.isLatestServiceListLoaded = false;
                                    setErrorMessage(true);
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
                                        Open311.isDataLoading = false;
                                        Open311.prevEndpoint = Open311.sEndpoint.url;
                                        Open311.isLatestServiceListLoaded = true;
                                        showLoader(false);
                                        pager.setVisibility(View.VISIBLE);
                                        tabStrip.setVisibility(View.VISIBLE);
                                        adapter.notifyDataSetChanged();
                                        tabStrip.notifyDataSetChanged();
                                    }
                                }
                            }, Open311XmlParser.SERVICE_DEFINITION, new ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse != null)
                                        Open311.isDataLoading = false;
                                    Log.d("Status GET", error.networkResponse.statusCode + "");
                                    showLoader(false);
                                    setErrorMessage(true);
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

    @Override
    public void onRefreshRequested() {
        refresh();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.buttonRetry:
                refresh();
        }
    }

}
