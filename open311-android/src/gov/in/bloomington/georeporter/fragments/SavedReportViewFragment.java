/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.json.RequestsJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.ServiceRequest;
import gov.in.bloomington.georeporter.util.json.JSONArray;
import gov.in.bloomington.georeporter.util.json.JSONException;
import gov.in.bloomington.georeporter.util.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class SavedReportViewFragment extends SherlockFragment {
    private static final String POSITION = "position";
    private ArrayList<ServiceRequest> mServiceRequests;
    private ServiceRequest mServiceRequest;
    private int mPosition;

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

        mServiceRequests = Open311.loadServiceRequests(getActivity());
        mServiceRequest = mServiceRequests.get(mPosition);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_saved, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        refreshViewData();
        new RefreshFromServerTask().execute();
        super.onActivityCreated(savedInstanceState);
    }

    private void refreshViewData() {
        View v = getView();
        TextView textView;

        textView = (TextView) v.findViewById(R.id.service_name);
        textView.setText(mServiceRequest.service.getService_name());

        ImageView media = (ImageView) v.findViewById(R.id.media);
        media.setImageBitmap(mServiceRequest.getMediaBitmap(100, 100, getActivity()));

        textView = (TextView) v.findViewById(R.id.address);
        if (mServiceRequest.service_request.getAddress() != null) {
            textView.setText(mServiceRequest.service_request.getAddress());
        }
        else if (mServiceRequest.post_data.has(Open311.ADDRESS_STRING)) {
            textView.setText(mServiceRequest.post_data.optString(Open311.ADDRESS_STRING));
        }

        textView = (TextView) v.findViewById(R.id.description);
        if (mServiceRequest.service_request.getDescription() != null) {
            textView.setText(mServiceRequest.service_request.getDescription());
        }
        else if (mServiceRequest.post_data.has(Open311.DESCRIPTION)) {
            textView.setText(mServiceRequest.post_data.optString(Open311.DESCRIPTION));
        }

        textView = (TextView) v.findViewById(R.id.status);
        if (mServiceRequest.service_request.getStatus()!=null) {
            textView.setText(mServiceRequest.service_request.getStatus());
        }
    }

    private class RefreshFromServerTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean tokenUpdated = false;
            Boolean serviceRequestUpdated = false;
            RequestsJson sr = mServiceRequest.service_request;

            if (sr.getService_request_id() == null) {
                String id;
                id = getServiceRequestId(sr.getToken());
                if (id != null) {
                    sr.setService_request_id(id);
                    tokenUpdated = true;
                }
                else {
                    String pending = getResources().getString(R.string.pending);
                    if (!sr.getStatus().equals(pending)) {
                        sr.setStatus(pending);
                        serviceRequestUpdated = true;
                    }
                }
            }

            if (sr.getService_request_id()!=null) {
                serviceRequestUpdated = fetchServiceRequest();
            }
            return tokenUpdated || serviceRequestUpdated;
        }

        private Boolean fetchServiceRequest() {
            try {
                String request_id = mServiceRequest.service_request
                        .getService_request_id();
                return updateServiceRequest(Open311.loadStringFromUrl(
                        mServiceRequest.getServiceRequestUrl(request_id), getActivity()));
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return false;
        }

        private String getServiceRequestId(String token) {
            HttpGet request;
            try {
                request = new HttpGet(mServiceRequest.getServiceRequestIdFromTokenUrl(token));
                HttpResponse r = Open311.getClient(getActivity()).execute(request);
                String responseString = EntityUtils.toString(r.getEntity());

                int status = r.getStatusLine().getStatusCode();
                if (status == HttpStatus.SC_OK) {
                    JSONArray result = new JSONArray(responseString);
                    JSONObject o = result.getJSONObject(0);
                    if (o.has(Open311.SERVICE_REQUEST_ID)) {
                        return o.getString(Open311.SERVICE_REQUEST_ID);
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        private Boolean updateServiceRequest(String result) {
            if (result != null && result != "") {
                
                ArrayList<RequestsJson> results = new Gson().fromJson(result, new TypeToken<ArrayList<RequestsJson>>(){}.getType());
                mServiceRequest.service_request = results.get(0);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean dataUpdated) {
            super.onPostExecute(dataUpdated);
            if (dataUpdated) {
                try {
                    mServiceRequests.add(mServiceRequest);
                    Open311.saveServiceRequests(getActivity(), mServiceRequests);
                    refreshViewData();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
