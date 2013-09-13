/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.models;

import android.content.Context;
import android.util.Log;

import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import com.android.volley.RequestQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.json.ServiceDefinationJson;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;
import gov.in.bloomington.georeporter.volleyrequests.GsonGetRequest;
import gov.in.bloomington.georeporter.volleyrequests.GsonPostServiceRequest;
import gov.in.bloomington.georeporter.volleyrequests.Open311XmlRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

public class Open311 {
    /**
     * Constants for Open311 keys I'm tired of making typos in key names
     */
    // Global required fields
    public static final String JURISDICTION = "jurisdiction_id";
    public static final String API_KEY = "api_key";
    public static final String FORMAT = "format";
    public static final String SERVICE_CODE = "service_code";
    public static final String SERVICE_NAME = "service_name";
    public static final String GROUP = "group";
    // Global basic fields
    public static final String MEDIA = "media";
    public static final String MEDIA_URL = "media_url";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "long";
    public static final String ADDRESS = "address";
    public static final String ADDRESS_STRING = "address_string";
    public static final String DESCRIPTION = "description";
    public static final String ANONOMOUSLY = "anonomously";
    public static final String SERVICE_NOTICE = "service_notice";
    public static final String ACCOUNT_ID = "account_id";
    public static final String STATUS = "status";
    public static final String STATUS_NOTES = "status_notes";
    public static final String AGENCY_RESPONSIBLE = "agency_responsible";
    public static final String REQUESTED_DATETIME = "requested_datetime";
    public static final String UPDATED_DATETIME = "updated_datetime";
    public static final String EXPECTED_DATETIME = "expected_datetime";
    // Personal Information fields
    public static final String EMAIL = "email";
    public static final String DEVICE_ID = "device_id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String PHONE = "phone";
    // Custom field definition in service_definition
    public static final String METADATA = "metadata";
    public static final String ATTRIBUTES = "attributes";
    public static final String VARIABLE = "variable";
    public static final String CODE = "code";
    public static final String ORDER = "order";
    public static final String VALUES = "values";
    public static final String VALUE = "value";
    public static final String KEY = "key";
    public static final String NAME = "name";
    public static final String REQUIRED = "required";
    public static final String DATATYPE = "datatype";
    public static final String STRING = "string";
    public static final String NUMBER = "number";
    public static final String DATETIME = "datetime";
    public static final String TEXT = "text";
    public static final String TRUE = "true";
    public static final String SINGLEVALUELIST = "singlevaluelist";
    public static final String MULTIVALUELIST = "multivaluelist";
    // Key names from /res/raw/available_servers.json
    public static final String URL = "url";
    public static final String SUPPORTS_MEDIA = "supports_media";
    // Key names for the saved reports file
    private static final String SAVED_REPORTS_FILE = "service_requests";
    public static final String SERVICE_REQUEST_ID = "service_request_id";
    public static final String TOKEN = "token";
    // Key names for formats
    public static final String JSON = "json";
    public static final String XML = "xml";
    // Key name for encoding
    public static final String UTF_8 = "UTF-8";

    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static ServerAttributeJson sEndpoint;
    public static boolean isLatestServiceListLoaded = false;
    public static boolean isDataLoading = false;
    public static String prevEndpoint;
    public static HashMap<String, ServiceDefinationJson> sServiceDefinitions;
    public static ArrayList<String> sGroups;
    public static ArrayList<ServiceRequest> mServiceRequests;
    public static JSONObject mPersonalInfo;

    public static RequestQueue requestQueue = null;
    public static GsonGetRequest<ArrayList<ServiceEntityJson>> sServiceRequestGson = null;
    public static Open311XmlRequest<ArrayList<ServiceEntityJson>> sServiceRequestXML = null;
    public static GsonPostServiceRequest sPostServiceRequest = null;

    public static HashMap<String, ArrayList<ServiceEntityJson>> sServiceGroups = null;

    public static String mBaseUrl;
    public static String mJurisdiction;
    public static String mApiKey;
    public static String mFormat = "json";

    private static Open311 mInstance;

    public static int selectedActionPosition = -1;

    public static synchronized Open311 getInstance() {
        if (mInstance == null) {
            mInstance = new Open311();
        }
        return mInstance;
    }

    // TODO Doc
    public static void setCurrentServerDetails(ServerAttributeJson cuurentServer)
    {
        mBaseUrl = cuurentServer.url;
        mJurisdiction = cuurentServer.jurisdiction_id;
        mApiKey = cuurentServer.api_key;
        mFormat = cuurentServer.format;
    }

    /**
     * Returns the services for a given group
     * 
     * @param group
     * @return ArrayList<ServiceEntityJson>
     */
    public static ArrayList<ServiceEntityJson> getServices(String group) {
        return Open311.sServiceGroups.get(group);
    }

    /**
     * @param service_code
     * @return ServiceDefinationJson
     */
    public static ServiceDefinationJson getServiceDefinition(String service_code, Context context) {

        if (sServiceDefinitions.containsKey(service_code)) {
            return sServiceDefinitions.get(service_code);
        }
        return null;
    }

    /**
     * POST new service request data to the endpoint The JSONObject should come
     * from ServiceRequest.post_data In the JSON data: All the keys should
     * already be named correctly. Attribute keys will already be in the form of
     * "attribute[code]". Most attributes will just contain single values
     * entered by the user; however, MultiValueList attributes will be an array
     * of the chosen values We will need to iterate over MultiValueList values
     * and add a seperate pair to the POST for each value. Media attributes will
     * contain the URI to the image file.
     * 
     * @param data JSON representation of user input
     * @return JSONObject
     * @throws JSONException
     * @throws IOException
     * @throws ClientProtocolException
     * @throws Open311Exception
     */

    public static String getServiceRequestId(String token) {
        return "";
    }

    /**
     * Prepares a POST that does not contain a media attachment
     * 
     * @param data
     * @return
     * @throws UnsupportedEncodingException UrlEncodedFormEntity
     * @throws JSONException
     */

    /**
     * Prepares a POST that includes a media attachment
     * 
     * @param data
     * @param context
     * @param mediaPath
     * @return
     * @throws UnsupportedEncodingException MultipartEntity
     * @throws JSONException
     */

    /**
     * Reads the saved reports file into a JSONArray Reports are stored as a
     * file on the device internal storage The file is a serialized JSONArray of
     * reports.
     * 
     * @return JSONArray
     */
    public static ArrayList<ServiceRequest> loadServiceRequests(Context c) {
        ArrayList<ServiceRequest> service_requests = null;

        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT);
        Gson gson = builder.create();

        byte[] bytes = new byte[1024];
        Log.d("Service Request Trying Loading", "Lets See");
        @SuppressWarnings("unused")
        int length;
        try {

            FileInputStream in = c.openFileInput(SAVED_REPORTS_FILE);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            Log.d("Service Request Loading", sb.toString());
            service_requests = gson.fromJson(sb.toString(),
                    new TypeToken<ArrayList<ServiceRequest>>() {
                    }.getType());
            in.close();
        } catch (FileNotFoundException e) {
            Log.w("Open311.loadServiceRequests",
                    "Saved Reports File does not exist");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return service_requests;
    }

    private static void closeQuietly(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the stored reports back out the file
     * 
     * @param c
     * @param requests An array of JSON-serialized ServiceRequest objects void
     */
    public static boolean saveServiceRequests(Context c, ArrayList<ServiceRequest> requests) {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT);
        Gson gson = builder.create();
        Log.d("Service Request Save trying", "Lets See");
        String json = gson.toJson(requests);
        Log.d("Service Request Save", json);
        FileOutputStream out;
        try {
            out = c.openFileOutput(SAVED_REPORTS_FILE, Context.MODE_PRIVATE);
            out.write(json.getBytes());
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds a ServiceRequest to the collection of saved reports Reports are
     * stored as a file on the device internal storage The file is a serialized
     * JSONArray of ServiceRequest objects.
     * 
     * @param report
     * @return Boolean
     */
    public static boolean saveServiceRequest(Context c, ServiceRequest sr) {
        sr.endpoint = sEndpoint;

        ArrayList<ServiceRequest> saved_requests = loadServiceRequests(c);

        if (saved_requests == null)
            saved_requests = new ArrayList<ServiceRequest>();

        // Push the new report onto the start of the array
        saved_requests.add(0, sr);

        return saveServiceRequests(c, saved_requests);

    }

    /**
     * http://endpoint/services.format?jurisdiction_id=jurisdiction
     * 
     * @return String
     */
    public static String getServiceListUrl() {
        String url = mBaseUrl + "/services." + mFormat;
        if (mJurisdiction != null && mJurisdiction.length() > 0) {
            url = url + "?" + JURISDICTION + "=" + mJurisdiction;
        }
        return url;
    }

    /**
     * http://endpoint/services/service_code.format?jurisdiction_id=jurisdiction
     * 
     * @param service_code
     * @return String
     */
    public static String getServiceDefinitionUrl(String service_code) {
        String url = mBaseUrl + "/services/" + service_code + "." + mFormat;
        if (mJurisdiction.length() > 0) {
            url = url + "?" + JURISDICTION + "=" + mJurisdiction;
        }
        return url;
    }

    /**
     * http://endpoint/services/requests.format?bbox=minLat,minLong,maxLat,
     * maxLong
     * 
     * @param service_code
     * @return String
     */
    public static String getServiceRequestUrl(double minLat, double minLong, double maxLat,
            double maxLong) {

        String url = mBaseUrl + "/requests" + "." + mFormat + "?bbox=" + minLat + "," + minLong
                + "," + maxLat + "," + maxLong;
        if (mJurisdiction.length() > 0) {
            url = url + "&" + JURISDICTION + "=" + mJurisdiction;
        }
        return url;
    }

    /**
     * http://endpoint/services/requests.format?start_date=ISO Format&
     * end_date=ISO Format&status={open|close}&jurisdiction_id=jid
     * 
     * @param service_code
     * @return String
     */
    public static String getServiceRequestUrl(String startDate, String endDate, String status) {

        String url = mBaseUrl + "/requests" + "." + mFormat + "?start_date=" + startDate
                + "&end_date=" + endDate + "&status=" + status;
        if (mJurisdiction.length() > 0) {
            url = url + "&" + JURISDICTION + "=" + mJurisdiction;
        }
        return url;
    }

}
