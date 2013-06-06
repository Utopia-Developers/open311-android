package gov.in.bloomington.georeporter.vollleyrequests;

import gov.in.bloomington.georeporter.json.ServiceEntityJson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class Open311XmlRequest<T> extends Request<T> {

	private final Listener<T> listener;
	private final String requestType;

	// XML tags
	private final String SERVICES = "services";
	private final String SERVICE = "service";
	private final String REQUEST = "request";
	public final String ATTRIBUTE = "attribute";
	private final String SERVICE_REQUESTS = "service_requests";
	private final String SERVICE_DEFINITION = "service_definition";
	private final String ERRORS = "errors";
	private final String ERROR = "error";
	private String CHARSET="UTF-8";

	private final String ns = null;
	private XmlPullParser parser;

	public Open311XmlRequest(String url, Listener<T> listener,
			String requestType, ErrorListener errorListener) {
		super(Method.GET, url, errorListener);
		this.listener = listener;
		this.requestType = requestType;
		this.parser = Xml.newPullParser();
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String xml = new String(response.data,
					HttpHeaderParser.parseCharset(response.headers));
			InputStream is;
			is = new ByteArrayInputStream(xml.getBytes(CHARSET));
			parser.setInput(is, null);
			parser.nextTag();
			return Response.success(parseServices(parser),
					HttpHeaderParser.parseCacheHeaders(response));
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
