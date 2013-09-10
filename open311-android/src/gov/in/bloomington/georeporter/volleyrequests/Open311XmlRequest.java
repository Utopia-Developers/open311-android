
package gov.in.bloomington.georeporter.volleyrequests;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import gov.in.bloomington.georeporter.json.ServiceEntityJson;
import gov.in.bloomington.georeporter.util.Open311XmlParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Open311XmlRequest<T> extends Request<T> {

    private final Listener<T> listener;
    private final String requestType;

    private String CHARSET = "UTF-8";

    public Open311XmlRequest(String url, Listener<T> listener,
            String requestType, ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = listener;
        this.requestType = requestType;

    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String xml = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            Open311XmlParser xmlParser = new Open311XmlParser();
            if (requestType.contentEquals(Open311XmlParser.SERVICE_REQUESTS))
            {
                return (Response<T>) Response.success(xmlParser.parseServices(xml),
                        HttpHeaderParser.parseCacheHeaders(response));
            }
            else if (requestType.contentEquals(Open311XmlParser.SERVICE_DEFINITION))
            {
                return (Response<T>) Response.success(xmlParser.parseServiceDefinition(xml),
                        HttpHeaderParser.parseCacheHeaders(response));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    public T parseServices(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        ArrayList<ServiceEntityJson> servicesList = new ArrayList<ServiceEntityJson>();
        return (T) servicesList;

    }

}
