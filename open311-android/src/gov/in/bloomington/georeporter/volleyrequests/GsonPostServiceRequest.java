
package gov.in.bloomington.georeporter.volleyrequests;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.ByteArrayBody;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gov.in.bloomington.georeporter.json.RequestsJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.ServiceRequest;
import gov.in.bloomington.georeporter.util.Media;
import gov.in.bloomington.georeporter.util.Open311Parser;
import gov.in.bloomington.georeporter.util.Open311XmlParser;
import gov.in.bloomington.georeporter.util.json.JSONArray;
import gov.in.bloomington.georeporter.util.json.JSONException;
import gov.in.bloomington.georeporter.util.json.JSONObject;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GsonPostServiceRequest extends Request<ArrayList<RequestsJson>> {
    private MultipartEntity post;
    private final Listener<ArrayList<RequestsJson>> mListener;
    private ServiceRequest serviceRequest;
    private String mediaPath;
    private Context context;
    private UrlEncodedFormEntity postUrl;

    public GsonPostServiceRequest(Context context, String url, ErrorListener errorListener,
            Listener<ArrayList<RequestsJson>> listener, ServiceRequest serviceRequest,
            String mediaPath) {
        super(Method.POST, url, errorListener);
        post = new MultipartEntity();

        mListener = listener;

        this.mediaPath = mediaPath;
        this.serviceRequest = serviceRequest;
        this.context = context;

        try {
            if (this.mediaPath != null)
                buildMultipartEntity();
            else
                buildUrlEncodedEntitiy();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void buildUrlEncodedEntitiy() throws UnsupportedEncodingException {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        // This could cause a JSONException, but we let this one bubble up the
        // stack
        // If we don't have a service_code, we don't have a valid POST
        pairs.add(new BasicNameValuePair(Open311.SERVICE_CODE, serviceRequest.service
                .getService_code()));

        if (Open311.mJurisdiction.length() > 0) {
            pairs.add(new BasicNameValuePair(Open311.JURISDICTION, Open311.mJurisdiction));
        }
        if (Open311.mApiKey.length() > 0) {
            pairs.add(new BasicNameValuePair(Open311.API_KEY, Open311.mApiKey));
        }

        JSONObject data = serviceRequest.post_data;
        Iterator<?> keys = data.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object o;
            try {
                o = data.get(key);
                // Add MULTIVALUELIST values
                if (o instanceof JSONArray) {
                    String k = key + "[]"; // Key name to POST multiple values
                    JSONArray values = (JSONArray) o;
                    int len = values.length();
                    for (int i = 0; i < len; i++) {
                        try {
                            pairs.add(new BasicNameValuePair(k, values
                                    .getString(i)));
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                // All other fields can just be plain key-value pairs
                else {
                    // Lat and Long need to be converted to string
                    if (o instanceof Double) {
                        o = Double.toString((Double) o);
                    }
                    pairs.add(new BasicNameValuePair(key, (String) o));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        postUrl = new UrlEncodedFormEntity(pairs, Open311.UTF_8);
    }

    private void buildMultipartEntity() throws UnsupportedEncodingException {
        // This could cause a JSONException, but we let this one bubble up the
        // stack
        // If we don't have a service_code, we don't have a valid POST
        post.addPart(Open311.SERVICE_CODE,
                new StringBody(serviceRequest.service.getService_code()));

        if (Open311.mJurisdiction != null) {
            post.addPart(Open311.JURISDICTION, new StringBody(Open311.mJurisdiction));
        }
        if (Open311.mApiKey != null) {
            post.addPart(Open311.API_KEY, new StringBody(Open311.mApiKey));
        }
        JSONObject data = serviceRequest.post_data;
        Iterator<?> keys = data.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object o;
            try {
                o = data.get(key);
                // Attach media to the post
                // Do not read from the data object.
                // Instead, use the mediaPath that is passed in.
                // This relies on the fact that there can only be one media
                // attachment per ServiceRequest.
                if (key == Open311.MEDIA) {
                    final Bitmap media = Media.decodeSampledBitmap(mediaPath,
                            Media.UPLOAD_WIDTH, Media.UPLOAD_HEIGHT, context);
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    media.compress(CompressFormat.PNG, 100, stream);
                    final byte[] binaryData = stream.toByteArray();
                    post.addPart(Open311.MEDIA, new ByteArrayBody(binaryData,
                            Media.UPLOAD_FILENAME));
                }
                // Attach MULTIVALUELIST values
                else if (o instanceof JSONArray) {
                    String k = key + "[]"; // Key name to POST multiple values
                    JSONArray values = (JSONArray) o;
                    int len = values.length();
                    for (int i = 0; i < len; i++) {
                        try {
                            post.addPart(k, new StringBody(values.getString(i)));
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                // All other fields can be attached as plain key-value pairs
                else {
                    if (o instanceof Double) {
                        o = Double.toString((Double) o);
                    }
                    Charset charset = Charset.forName(Open311.UTF_8);
                    post.addPart(key, new StringBody((String) o, charset));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getBodyContentType() {
        if (mediaPath != null)
            return post.getContentType().getValue();
        else
            return postUrl.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if (mediaPath != null)
                post.writeTo(bos);
            else
                postUrl.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<ArrayList<RequestsJson>> parseNetworkResponse(NetworkResponse response) {
        ArrayList<RequestsJson> serviceRequests = null;
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        Log.d("Server Response", parsed);

        int status = response.statusCode;
        // The spec does not declare what exact status codes to use
        // Bloomington uses 200 Okay
        // Chicago uses 201 Created
        if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED
                || status == HttpStatus.SC_ACCEPTED) {
            
            if(Open311.mFormat.contentEquals(Open311.XML))
            {
                Open311XmlParser mParser = new Open311XmlParser();
                try {
                    serviceRequests = mParser.parseRequests(parsed);
                    Log.d("Server Response Parsed", "Yes - XML");
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else
            {
                serviceRequests = new Gson().fromJson(parsed,
                        new TypeToken<ArrayList<RequestsJson>>() {
                        }.getType());
                Log.d("Server Response Parsed", "Yes - JSON");
            }
            

            
        } else {
            // The server indicated some error. See if they returned the
            // error description as JSON
            String dialogMessage;
            try {
                Open311Parser mParser = new Open311Parser(Open311.mFormat);
                JSONArray errors = mParser.parseErrors(parsed);
                dialogMessage = errors.getJSONObject(0).getString(
                        Open311.DESCRIPTION);
            } catch (JSONException e) {
                Response.error(new ParseError(response));
            }
            
             
            
        }
        return Response.success(serviceRequests, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(ArrayList<RequestsJson> response) {

        mListener.onResponse(response);
    }
}
