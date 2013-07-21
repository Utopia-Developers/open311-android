/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.activities.AttributeEntryActivity;
import gov.in.bloomington.georeporter.activities.ChooseLocationActivity;
import gov.in.bloomington.georeporter.activities.DataEntryActivity;
import gov.in.bloomington.georeporter.activities.MainActivity;
import gov.in.bloomington.georeporter.activities.SavedReportsActivity;
import gov.in.bloomington.georeporter.adapters.ServiceRequestAdapter;
import gov.in.bloomington.georeporter.json.AttributesJson;
import gov.in.bloomington.georeporter.json.RequestResponseJson;
import gov.in.bloomington.georeporter.json.RequestsJson;
import gov.in.bloomington.georeporter.json.ValuesJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.ServiceRequest;
import gov.in.bloomington.georeporter.util.Media;
import gov.in.bloomington.georeporter.util.Util;
import gov.in.bloomington.georeporter.util.json.JSONArray;
import gov.in.bloomington.georeporter.util.json.JSONException;
import gov.in.bloomington.georeporter.volleyrequests.GsonPostServiceRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends SherlockFragment implements OnItemClickListener,
        OnClickListener {
    /**
     * Request for handling Photo attachments to the Service Request
     */
    public static final int MEDIA_REQUEST = 0;
    /**
     * Request for handling lat, long, and address
     */
    public static final int LOCATION_REQUEST = 1;
    /**
     * Request to handle all the attributes
     */
    public static final int ATTRIBUTE_REQUEST = 2;
    /**
     * Request to handle all the rest of the basic parameters. ie. description,
     * firstname, lastname, email, etc.
     */
    public static final int DATA_ENTRY_REQUEST = 3;

    static Gson gson;

    private static final List<String> DATA_ENTRY_FIELDS = Arrays.asList(
            Open311.DESCRIPTION, Open311.FIRST_NAME, Open311.LAST_NAME, Open311.EMAIL,
            Open311.PHONE
            );

    private ServiceRequest mServiceRequest;
    private LinearLayout contentView;
    private TextView loadingFailedMessage;
    private Button loadingFailedRetry;
    private ProgressBar loadingProgress;
    private LayoutInflater layoutInflator;
    private ScrollView scrollView;
    private int currentViewCount = 0;
    private Uri mImageUri;

    /**
     * For Request Post
     */
    ProgressDialog mDialog;
    String mMediaPath = null;
    String errorMessage;
    String url = Open311.mBaseUrl + "/requests." + Open311.mFormat;
    boolean result = false;

    /**
     * @param sr
     * @return ReportFragment
     */
    public static ReportFragment newInstance(ServiceRequest sr) {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        gson = new Gson();
        args.putString(ServiceRequest.ENDPOINT, gson.toJson(sr.endpoint));
        args.putString(ServiceRequest.SERVICE, gson.toJson(sr.service));
        args.putString(ServiceRequest.SERVICE_DEFINITION, gson.toJson(sr.service_definition));
        if (sr.post_data != null)
            args.putString(ServiceRequest.POST_DATA, sr.post_data.toString());
        args.putString(ServiceRequest.SERVICE_REQUEST, gson.toJson(sr.service_request));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO
        mServiceRequest = new ServiceRequest(getArguments());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);
        scrollView = (ScrollView) v.findViewById(R.id.reportScrollView);
        contentView = (LinearLayout) v.findViewById(R.id.reportContentView);

        loadingProgress = (ProgressBar) v.findViewById(R.id.progressBarLoading);
        loadingFailedMessage = (TextView) v.findViewById(R.id.textViewMessage);
        loadingFailedRetry = (Button) v.findViewById(R.id.buttonRetry);
        loadingFailedRetry.setOnClickListener(this);
        v.findViewById(R.id.submit_button).setOnClickListener(this);
        v.findViewById(R.id.cancel_button).setOnClickListener(this);

        loadingProgress.setVisibility(View.GONE);
        layoutInflator = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        // Insert All Possible Views
        View temp;
        ((TextView) contentView.getChildAt(0)).setText(mServiceRequest.service.getService_name());
        ((TextView) contentView.getChildAt(1)).setText(mServiceRequest.service.getDescription());
        if (mServiceRequest.endpoint != null && mServiceRequest.endpoint.supports_media)
        {
            temp = layoutInflator.inflate(R.layout.report_item_media, null, false);
            temp.setTag(Open311.MEDIA);
            temp.setOnClickListener(this);
            contentView.addView(temp);

            Log.d("Media", "true" + contentView.getChildCount());
        }

        temp = layoutInflator.inflate(R.layout.report_item_map, null, false);
        temp.setTag(Open311.ADDRESS);
        temp.setOnClickListener(this);
        contentView.addView(temp);
        temp = layoutInflator.inflate(R.layout.report_item_description, contentView, false);
        contentView.addView(temp);

        addAttributes();

        return v;
    }

    private void addAttributes()
    {
        if (mServiceRequest.hasAttributes())
        {
            ArrayList<AttributesJson> attributes = (ArrayList<AttributesJson>) mServiceRequest.service_definition
                    .getAttributes();
            View temp;
            TextView viewHeader = (TextView) layoutInflator.inflate(R.layout.list_item_header,
                    contentView, false);
            viewHeader.setText(getString(R.string.report_attributes));
            contentView.addView(viewHeader);
            AttributesJson attribute;
            for (int i = 0; i < attributes.size(); i++)
            {
                attribute = attributes.get(i);
                // Attributes with variable=false should get displayed just with
                // description
                if (!attribute.getVariable())
                {
                    temp = layoutInflator.inflate(R.layout.report_item_text_entry, contentView,
                            false);
                    ((TextView) temp.findViewById(R.id.textViewDescription)).setText(attribute
                            .getDescription());
                    temp.findViewById(R.id.editTextEntry).setVisibility(View.GONE);
                    temp.setTag(attribute.getCode());
                    contentView.addView(temp);
                }
                else
                {
                    if (attribute.getDatatype().contentEquals(Open311.SINGLEVALUELIST))
                    {
                        temp = layoutInflator.inflate(R.layout.report_item_singlevalued,
                                contentView, false);
                        ((TextView) temp.findViewById(R.id.textViewDescription)).setText(attribute
                                .getDescription());
                        Spinner spinner = (Spinner) temp.findViewById(R.id.spinnerEntry);
                        ArrayList<String> adapterList = new ArrayList<String>();
                        ArrayList<ValuesJson> values = attribute.getValues();
                        for (int j = 0; j < values.size(); j++)
                        {
                            adapterList.add(values.get(j).getName());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                getActivity(), android.R.layout.simple_spinner_item, adapterList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                        temp.setTag(attribute.getCode());
                        contentView.addView(temp);
                    }
                    else if (attribute.getDatatype().contentEquals(Open311.MULTIVALUELIST))
                    {
                        temp = layoutInflator.inflate(R.layout.report_item_multivalued,
                                contentView, false);
                        ((TextView) temp.findViewById(R.id.textViewDescription)).setText(attribute
                                .getDescription());
                        LinearLayout content = (LinearLayout) temp.findViewById(R.id.multivaluedContent);
                        ArrayList<ValuesJson> values = attribute.getValues();
                        CheckBox checkBox;
                        for (int j = 0; j < values.size(); j++)
                        {
                            checkBox = new CheckBox(getActivity());
                            checkBox.setText(values.get(j).getName());
                            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                            checkBox.setLayoutParams(params);
                            content.addView(checkBox);
                        }
                        temp.setTag(attribute.getCode());
                        contentView.addView(temp);
                    }
                    else if(attribute.getDatatype().contentEquals(Open311.DATATYPE))
                    {
                        temp = layoutInflator.inflate(R.layout.report_item_text_entry, contentView,
                                false);
                        ((TextView) temp.findViewById(R.id.textViewDescription)).setText(getString(R.string.report_date));
                        temp.findViewById(R.id.editTextEntry).setVisibility(View.GONE);
                        temp.setOnClickListener(this);
                        temp.setTag(attribute.getCode());
                        contentView.addView(temp);
                    }
                    else
                    {

                        temp = layoutInflator.inflate(R.layout.report_item_text_entry, contentView,
                                false);
                        ((TextView) temp.findViewById(R.id.textViewDescription)).setText(attribute
                                .getDescription());
                        EditText input = ((EditText) temp.findViewById(R.id.editTextEntry));
                        String mDatatype = attribute.getDatatype();
                        if (mDatatype.equals(Open311.NUMBER)) {
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                            input.setHint(getString(R.string.answer_number_here));
                        }
                        else if (mDatatype.equals(Open311.TEXT)) {
                            input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                            input.setHint(getString(R.string.answer_here));
                        }
                        else
                        {
                            input.setHint(getString(R.string.answer_here));
                        }

                        temp.setTag(attribute.getCode());
                        contentView.addView(temp);

                    }
                }
            }

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ServiceRequest.SERVICE_REQUEST, mServiceRequest.toString());
    }

    /**
     * Starts a seperate activity for each report field Design background: We
     * cannot fit all the text and controls onto a single screen. In addition,
     * controls like the Camera and Map chooser must be in a seperate activity
     * anyway. This streamlines the process so each report field is handled the
     * same way.
     */
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        ServiceRequestAdapter adapter = (ServiceRequestAdapter) l.getAdapter();

        if (adapter.getItemViewType(position) != ServiceRequestAdapter.TYPE_HEADER) {
            // TODO Figure out which type of dialog to draw
            String labelKey = (String) adapter.getItem(position);

            if (labelKey.equals(Open311.MEDIA)) {

            }
            else if (labelKey.equals(Open311.ADDRESS_STRING)) {
                Intent i = new Intent(getActivity(), ChooseLocationActivity.class);
                startActivityForResult(i, LOCATION_REQUEST);
            }
            else if (DATA_ENTRY_FIELDS.contains(labelKey)) {
                TextView label = (TextView) v.findViewById(android.R.id.text1);

                Intent i = new Intent(getActivity(), DataEntryActivity.class);
                i.putExtra(DataEntryActivity.KEY, labelKey);
                i.putExtra(DataEntryActivity.VALUE, mServiceRequest.post_data.optString(labelKey));
                i.putExtra(DataEntryActivity.PROMPT, label.getText().toString());
                startActivityForResult(i, DATA_ENTRY_REQUEST);
            }
            else {
                AttributesJson attribute = mServiceRequest.getAttribute(labelKey);

                // For datetime attributes, we'll just pop open a date
                // picker dialog
                String datatype = attribute.getDatatype();
                if (datatype.equals(Open311.DATETIME)) {
                    DatePickerDialogFragment datePicker = new DatePickerDialogFragment(labelKey);
                    datePicker.show(getActivity().getSupportFragmentManager(), "datePicker");
                }
                // all other attribute types get a full seperate Activity
                else {
                    Intent i = new Intent(getActivity(), AttributeEntryActivity.class);
                    i.putExtra(AttributeEntryActivity.ATTRIBUTE, new Gson().toJson(attribute));
                    startActivityForResult(i, ATTRIBUTE_REQUEST);
                }
            }
        }
    }

    /**
     * Reads data returned from activities and updates mServiceRequest
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            try {
                switch (requestCode) {
                    case MEDIA_REQUEST:
                        // Determine if this is from the camera or gallery
                        Uri imageUri = (mImageUri != null) ? mImageUri : data.getData();
                        if (imageUri != null) {
                            mServiceRequest.post_data.put(Open311.MEDIA, imageUri.toString());
                            mImageUri = null; // Remember to wipe it out, so we
                                              // don't confuse camera and
                                              // gallery
                        }
                        break;

                    case LOCATION_REQUEST:
                        // The ChooseLocationActivity should put LATITUDE and
                        // LONGITUDE
                        // into the Intent data as type double
                        double latitude = data.getDoubleExtra(Open311.LATITUDE, 0);
                        double longitude = data.getDoubleExtra(Open311.LONGITUDE, 0);

                        mServiceRequest.post_data.put(Open311.LATITUDE, latitude);
                        mServiceRequest.post_data.put(Open311.LONGITUDE, longitude);
                        // Display the lat/long as text for now
                        // It will get replaced with the address when
                        // ReverseGeoCodingTask returns
                        new ReverseGeocodingTask().execute(new LatLng(latitude, longitude));
                        break;

                    /**
                     * Case to handle all the text-based parameters description,
                     * firstname, lastname, etc.
                     */
                    case DATA_ENTRY_REQUEST:
                        String labelKey = data.getStringExtra(DataEntryActivity.KEY);
                        String val = data.getStringExtra(DataEntryActivity.VALUE);
                        mServiceRequest.post_data.put(labelKey, val);
                        break;

                    /**
                     * Case to handle all possible attributes
                     */
                    case ATTRIBUTE_REQUEST:
                        String code = data.getStringExtra(Open311.CODE);
                        String datatype = data.getStringExtra(Open311.DATATYPE);
                        String value = data.getStringExtra(AttributeEntryActivity.VALUE);

                        String key = String
                                .format("%s[%s]", AttributeEntryActivity.ATTRIBUTE, code);

                        // Multivaluelist attributes will return a JSON string
                        // containg a JSONArray of values the user chose
                        if (datatype.equals(Open311.MULTIVALUELIST)) {
                            JSONArray array = new JSONArray(value);
                            mServiceRequest.post_data.put(key, array);
                        }
                        else {
                            mServiceRequest.post_data.put(key, value);
                        }
                        break;

                    default:
                        break;
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        refreshAdapter();
    }

    private void refreshAdapter() {
        // ServiceRequestAdapter a = (ServiceRequestAdapter)
        // mListView.getAdapter();
        // a.updateServiceRequest(mServiceRequest);
    }

    /**
     * A basic date picker used for DateTime attributes Pass in the attribute
     * code that you want the user to enter a date for
     */
    @SuppressLint("ValidFragment")
    private class DatePickerDialogFragment extends SherlockDialogFragment implements
            OnDateSetListener {
        private String mAttributeCode;

        /**
         * @param code The attribute code to update in mServiceRequest
         */
        public DatePickerDialogFragment(String code) {
            mAttributeCode = code;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            return new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        }

        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.set(year, monthOfYear, dayOfMonth);
            try {
                String code = String.format("%s[%s]", AttributeEntryActivity.ATTRIBUTE,
                        mAttributeCode);
                String date = DateFormat.getDateFormat(getActivity()).format(c.getTime());
                mServiceRequest.post_data.put(code, date);
                refreshAdapter();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
                // TODO Auto-generated catch block
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
            if (address != null) {
                try {
                    mServiceRequest.post_data.put(Open311.ADDRESS_STRING, address);
                    refreshAdapter();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            super.onPostExecute(address);
        }
    }

    @Override
    public void onClick(View v) {
        boolean clickConsumed = false;
        if (v.getTag() != null && v.getTag().toString().contentEquals(Open311.MEDIA))
        {
            clickConsumed = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.choose_media_source)
                    .setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
                        /**
                         * Start the camera activity To avoid differences in
                         * non-google-provided camera activities, we should
                         * always tell the camera activity to explicitly save
                         * the file in a Uri of our choosing. The camera
                         * activity may, or may not, also save an image file in
                         * the gallery. For now, I'm just not going to worry
                         * about creating duplicate files on people's phones.
                         * Users can clean those up themselves, if they want.
                         */
                        public void onClick(DialogInterface dialog, int id) {
                            mImageUri = Media.getOutputMediaFileUri(Media.MEDIA_TYPE_IMAGE);

                            Intent i = new Intent(
                                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            i.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                            startActivityForResult(i, MEDIA_REQUEST);
                        }
                    })
                    .setNeutralButton(R.string.gallery, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            i.setType("image/*");
                            startActivityForResult(i, MEDIA_REQUEST);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else if (v.getTag() != null && v.getTag().toString().contentEquals(Open311.ADDRESS))
        {
            Log.d("Map", "Click");
        }
        else if(v.getTag() != null && v.getTag().toString().contentEquals(Open311.DATETIME))
        {
            DatePickerDialogFragment datePicker = new DatePickerDialogFragment(v.getTag().toString());
            datePicker.show(getActivity().getSupportFragmentManager(), "datePicker");
        }

        if (clickConsumed)
            return;

        switch (v.getId())
        {
            case R.id.buttonRetry:
                break;
            case R.id.submit_button:
                mDialog = ProgressDialog.show(getActivity(),
                        getString(R.string.dialog_posting_service), "Please Wait", true);

                // Converting from a Uri to a real file path requires a database
                // cursor. Media.getRealPathFromUri must be done on the main UI
                // thread, since it makes its own loadInBackground call.
                if (mServiceRequest.post_data.has(Open311.MEDIA)) {
                    try {
                        mMediaPath = Media.getRealPathFromUri(
                                Uri.parse(mServiceRequest.post_data.getString(Open311.MEDIA)),
                                getActivity());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Open311.sPostServiceRequest = new GsonPostServiceRequest(getActivity(), url,
                        new ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mDialog.dismiss();
                                int statusCode = 0;
                                if (error.networkResponse != null)
                                {
                                    statusCode = error.networkResponse.statusCode;
                                }
                                switch (statusCode)
                                {
                                    case 403:
                                        errorMessage = getString(
                                                R.string.error_403);
                                        break;
                                    default:
                                        errorMessage = getString(
                                                R.string.failure_posting_service);
                                }
                                Util.displayCrashDialog(getActivity(), errorMessage);
                            }
                        }, new Listener<ArrayList<RequestsJson>>() {
                            @Override
                            public void onResponse(ArrayList<RequestsJson> responses) {
                                // TODO It needs to be in a background thread
                                RequestsJson response = responses.get(0);
                                if (responses.size() > 0) {
                                    SimpleDateFormat isoDate = new SimpleDateFormat(
                                            Open311.DATETIME_FORMAT);
                                    String requested_datetime = isoDate.format(new Date());

                                    mServiceRequest.endpoint = Open311.sEndpoint;
                                    mServiceRequest.service_request = response;

                                    try {
                                        mServiceRequest.post_data.put(
                                                ServiceRequest.REQUESTED_DATETIME,
                                                requested_datetime);
                                    } catch (JSONException e) {
                                        errorMessage = getResources().getString(
                                                R.string.failure_posting_service);
                                    }
                                    result = Open311.saveServiceRequest(getActivity(),
                                            mServiceRequest);
                                }
                                mDialog.dismiss();
                                if (!result) {
                                    if (errorMessage == null) {
                                        errorMessage = getString(R.string.failure_posting_service);
                                    }
                                    Util.displayCrashDialog(getActivity(), errorMessage);
                                }
                                else {
                                    Intent intent = new Intent(getActivity(),
                                            SavedReportsActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }, mServiceRequest, mMediaPath);
                Open311.requestQueue.add(Open311.sPostServiceRequest);
                break;
            case R.id.cancel_button:
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }

}
