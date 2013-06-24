/**
 * Model for working with all the information about a single Service Request
 * 
 * Includes the service information to query the endpoint for fresh data.
 * Includes service definition information.
 * Includes the raw data the user entered.
 * Includes a cache of data from endpoint.
 * 
 * Serialize this object by calling toString, which will return JSON.
 * Restore this object's state by passing a JSON String to the constructor. 
 * 
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */

package gov.in.bloomington.georeporter.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import gov.in.bloomington.georeporter.json.AttributesJson;
import gov.in.bloomington.georeporter.json.RequestsJson;
import gov.in.bloomington.georeporter.json.ServerAttributeJson;
import gov.in.bloomington.georeporter.json.ServiceDefinationJson;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;
import gov.in.bloomington.georeporter.json.ValuesJson;
import gov.in.bloomington.georeporter.util.Media;
import gov.in.bloomington.georeporter.util.json.JSONException;
import gov.in.bloomington.georeporter.util.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ServiceRequest {

    public static final String ENDPOINT = "endpoint";
    public static final String SERVICE = "service";
    public static final String SERVICE_DEFINITION = "service_definition";
    public static final String SERVICE_REQUEST = "service_request";
    public static final String POST_DATA = "post_data";
    // MetaData fields
    public static final String STATUS = "status";
    public static final String REQUESTED_DATETIME = "requested_datetime";
    public static final String UPDATED_DATETIME = "updated_datetime";

    
    private transient Gson gson;

    /**
     * The {@link ServerAttributeJson} definition from
     * raw/available_servers.json
     */
    public ServerAttributeJson endpoint;
    /**
     * The java object for a single service from GET Service List
     */
    public ServiceEntityJson service;
    /**
     * The java object response from GET Service Definition
     */
    public ServiceDefinationJson service_definition;
    /**
     * The JSON response from GET Service Request
     */
    public RequestsJson service_request;
    /**
     * The data that gets sent to POST Service Request JSON property names will
     * be the code from service_definition. Most JSON properties will just
     * contain single values entered by the user. Media will contain the URI to
     * the image file. MultiValueList attributes will an array of the chosen
     * values.
     */
    public JSONObject post_data;

    /**
     * Creates a new, empty ServiceRequest This does not load any user-submitted
     * data and should only be used for initial startup. Subsequent loads should
     * be done using the JSON String version
     * 
     * @param s A single service from GET Service List
     */
    public ServiceRequest(ServiceEntityJson s, Context c) {
        service = s;

        post_data = new JSONObject();
        
        endpoint = Open311.sEndpoint;

        if (service.getMetadata()) {

            service_definition = Open311.getServiceDefinition(
                    service.getService_code(), c);

        }

        // Read in the personal info fields from Preferences
        JSONObject personalInfo = Preferences.getPersonalInfo(c);
        Iterator<?> keys = personalInfo.keys();
        while (keys.hasNext()) {
            try {
                String key = (String) keys.next();
                String value = personalInfo.getString(key);
                if (value != "") {
                    post_data.put(key, value);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * Loads an existing ServiceRequest from a JSON String We will be
     * serializing to a file as JSON Strings. This will include any data a user
     * has already entered and any new information from the endpoint. All
     * createView() methods should use this constructor, since they might be
     * restoring from saveInstanceState()
     * 
     * @param serviceData
     */
    // TODO
    public ServiceRequest(Bundle serviceData) {
        gson = new Gson();
        if (serviceData.containsKey(ENDPOINT))
            endpoint = gson.fromJson(serviceData.getString(ENDPOINT),
                    ServerAttributeJson.class);
        if (serviceData.containsKey(SERVICE))
            service = gson.fromJson(serviceData.getString(SERVICE),
                    ServiceEntityJson.class);
        if (serviceData.containsKey(SERVICE_DEFINITION))
            service_definition = gson.fromJson(serviceData.getString(SERVICE_DEFINITION),
                    ServiceDefinationJson.class);
        if (serviceData.containsKey(POST_DATA))
            try {
                post_data = new JSONObject(serviceData.getString(POST_DATA));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (serviceData.containsKey(SERVICE_REQUEST))
            service_request = gson.fromJson(serviceData.getString(SERVICE_REQUEST),
                    RequestsJson.class);
    }

    /**
     * @return boolean
     */
    public boolean hasAttributes() {
        return service.getMetadata();
    }

    /**
     * @param code
     * @return AttributesJson
     */
    public AttributesJson getAttribute(String code) {
        AttributesJson attribute = null;

        ArrayList<AttributesJson> attributes = service_definition
                .getAttributes();
        int len = attributes.size();
        for (int i = 0; i < len; i++) {
            AttributesJson a = attributes.get(i);
            if (a.getCode().equals(code)) {
                attribute = a;
                break;
            }
        }
        return attribute;
    }

    /**
     * Returns the attribute description based on the attribute code Returns an
     * empty string if it cannot find the requested attribute
     * 
     * @param code
     * @return String
     */
    public String getAttributeDescription(String code) {
        String description = "";

        AttributesJson a = getAttribute(code);
        description = a.getDescription();

        return description;
    }

    /**
     * Returns the attribute datatype based on the attribute code If it cannot
     * determine the datatype, it returns "string" as the default
     * 
     * @param code
     * @return String
     */
    public String getAttributeDatatype(String code) {
        String type = Open311.STRING;
        AttributesJson a = getAttribute(code);
        type = a.getDatatype();
        return type;
    }

    /**
     * Returns the values for an attribute If it cannot determine the attribute,
     * it returns an empty JSONArray
     * 
     * @param code
     * @return ArrayList<ValuesJson>
     */
    public ArrayList<ValuesJson> getAttributeValues(String code) {
        ArrayList<ValuesJson> values;
        AttributesJson a = getAttribute(code);
        values = a.getValues();

        return values;
    }

    /**
     * Returns the name from a single value in an attribute
     * 
     * @param code The attribute code
     * @param key The value key
     * @return String
     */
    public String getAttributeValueName(String code, String key) {
        ArrayList<ValuesJson> values = getAttributeValues(code);
        int len = values.size();
        for (int i = 0; i < len; i++) {
            ValuesJson v = values.get(i);
            String k = v.getKey();
            if (k.equals(key)) {
                return v.getName();
            }
        }
        return null;
    }

    /**
     * @param code
     * @return boolean
     */
    public boolean isAttributeRequired(String code) {
        AttributesJson a = getAttribute(code);
        if (a.getRequired()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the URL for getting fresh information from the endpoint
     * 
     * @param request_id
     * @return String
     */
    public String getServiceRequestUrl(String request_id) {
        String baseUrl = endpoint.url;
        String jurisdiction = endpoint.jurisdiction_id;
        return String.format("%s/requests/%s.json?%s=%s", baseUrl, request_id,
                Open311.JURISDICTION, jurisdiction);
    }

    /**
     * Returns the URL for getting a service_request_id from a token
     * 
     * @param token
     * @return String
     * @throws JSONException
     */
    public String getServiceRequestIdFromTokenUrl(String token) throws JSONException {
        String baseUrl = endpoint.url;
        String jurisdiction = endpoint.jurisdiction_id;
        return String.format("%s/tokens/%s.json?%s=%s", baseUrl, token, Open311.JURISDICTION,
                jurisdiction);
    }

    /**
     * Returns a bitmap of the user's attached media It seems we cannot use
     * Uri's directly, without running out of memory. This will safely generate
     * a small bitmap ready to attach to an ImageView
     * 
     * @param width
     * @param height
     * @param context
     * @return Bitmap
     */
    public Bitmap getMediaBitmap(int width, int height, Context context) {
        String m = post_data.optString(Open311.MEDIA);
        if (!m.equals("")) {
            Uri imageUri = Uri.parse(m);
            if (imageUri != null) {
                String path = Media.getRealPathFromUri(imageUri, context);
                return Media.decodeSampledBitmap(path, width, height, context);
            }
        }
        return null;
    }
}
