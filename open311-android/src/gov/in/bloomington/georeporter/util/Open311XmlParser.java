/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Jaakko Rajaniemi <jaakko.rajaniemi@hel.fi>
 */

package gov.in.bloomington.georeporter.util;

import android.util.Xml;

import gov.in.bloomington.georeporter.json.AttributesJson;
import gov.in.bloomington.georeporter.json.RequestsJson;
import gov.in.bloomington.georeporter.json.ServiceDefinationJson;
import gov.in.bloomington.georeporter.json.ServiceEntityJson;
import gov.in.bloomington.georeporter.json.ValuesJson;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.util.json.JSONArray;
import gov.in.bloomington.georeporter.util.json.JSONException;
import gov.in.bloomington.georeporter.util.json.JSONObject;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Open311XmlParser {

    // XML tags
    public static final String SERVICES = "services";
    public static final String SERVICE = "service";
    public static final String REQUEST = "request";
    public static final String ATTRIBUTE = "attribute";
    public static final String SERVICE_REQUESTS = "service_requests";
    public static final String SERVICE_DEFINITION = "service_definition";
    public static final String ERRORS = "errors";
    public static final String ERROR = "error";

    private static final String ns = null;
    private XmlPullParser parser;

    public Open311XmlParser() {
        parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Converts an Open311 Service List from XML to Java Object
     * 
     * @param xml
     * @return ArrayList<{@link ServiceEntityJson}>
     * @throws XmlPullParserException
     * @throws IOException
     * 
     */
    public ArrayList<ServiceEntityJson> parseServices(String xml) throws XmlPullParserException, IOException {
        InputStream is;
        is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        parser.setInput(is, null);
        parser.nextTag();
        return parseServices(parser);
    }

    /**
     * Converts an Open311 Service Definition from XML to to Java Object
     * 
     * @param xml
     * @return {@link ServiceDefinationJson}
     * @throws XmlPullParserException
     * @throws IOException
     * 
     */
    public ServiceDefinationJson parseServiceDefinition(String xml) throws XmlPullParserException,
            IOException {
        InputStream is;
        is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        parser.setInput(is, null);
        parser.nextTag();
        return parseServiceDefinition(parser);
    }

    /**
     * Converts an Open311 Service Request List from XML to JSON
     * 
     * @param xml
     * @return JSONArray
     * @throws XmlPullParserException
     * @throws IOException
     * @throws JSONException
     */
    public ArrayList<RequestsJson> parseRequests(String xml) throws XmlPullParserException, IOException {
        InputStream is;
        is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        parser.setInput(is, null);
        parser.nextTag();
        return parseRequests(parser);
    }

    /**
     * Converts an Open311 error response from XML to JSON
     * 
     * @param xml
     * @return JSONArray
     * @throws XmlPullParserException
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray parseErrors(String xml) throws XmlPullParserException, IOException,
            JSONException {
        InputStream is;
        is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        parser.setInput(is, null);
        parser.nextTag();
        return parseErrors(parser);
    }

    /**
     * @param parser
     * @return JSONArray
     * @throws XmlPullParserException
     * @throws IOException
     * @throws ArrayList<ServiceEntityJson>
     */
    private ArrayList<ServiceEntityJson> parseServices(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        ArrayList<ServiceEntityJson> services = new ArrayList<ServiceEntityJson>();
        parser.require(XmlPullParser.START_TAG, ns, SERVICES);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(SERVICE)) {
                services.add(parseService(parser));
            } else {
                skip(parser);
            }
        }
        return services;
    }

    /**
     * @param parser
     * @return ServiceDefinationJson
     * @throws XmlPullParserException
     * @throws IOException
     * 
     */
    private ServiceDefinationJson parseServiceDefinition(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        ServiceDefinationJson serviceDefination = new ServiceDefinationJson();
        String service_code = null;
        ArrayList<AttributesJson> serviceAttributes = new ArrayList<AttributesJson>();

        parser.require(XmlPullParser.START_TAG, ns, SERVICE_DEFINITION);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(Open311.ATTRIBUTES)) {
                serviceAttributes = parseAttributes(parser);
            }
            else if (name.equals(Open311.SERVICE_CODE)) {
                serviceDefination.setService_code(readElement(parser, Open311.SERVICE_CODE));
            }
            else {
                skip(parser);
            }
        }

        
        serviceDefination.setAttributes(serviceAttributes);
        return serviceDefination;
    }

    /**
     * @param parser
     * @return JSONObject
     * @throws XmlPullParserException
     * @throws IOException
     * @throws JSONException
     */
    private ServiceEntityJson parseService(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, ns, SERVICE);
        ServiceEntityJson service = new  ServiceEntityJson();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(Open311.SERVICE_CODE)) {
                service.setService_code(readElement(parser, Open311.SERVICE_CODE));
            }
            else if (name.equals(Open311.SERVICE_NAME)) {
                service.setService_name(readElement(parser, Open311.SERVICE_NAME));
            }
            else if (name.equals(Open311.DESCRIPTION)) {
                service.setDescription(readElement(parser, Open311.DESCRIPTION));
            }
            else if (name.equals(Open311.GROUP)) {
                service.setGroup( readElement(parser, Open311.GROUP));
            }
            else if (name.equals(Open311.METADATA)) {
                service.setMetadata(Boolean.parseBoolean(readElement(parser, Open311.METADATA)));
            }
            else {
                skip(parser);
            }
        }
        return service;
    }

    /**
     * @param parser
     * @return ArrayList<AttributesJson>
     * @throws XmlPullParserException
     * @throws IOException
     * 
     */
    private ArrayList<AttributesJson> parseAttributes(XmlPullParser parser) throws XmlPullParserException,
            IOException{
        parser.require(XmlPullParser.START_TAG, ns, Open311.ATTRIBUTES);
        ArrayList<AttributesJson> attributes = new ArrayList<AttributesJson>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(ATTRIBUTE)) {
                AttributesJson attr = parseAttribute(parser);
                attributes.add(attr);
            }
        }
        return attributes;
    }

    /**
     * @param parser
     * @return AttributesJson
     * @throws XmlPullParserException
     * @throws IOException
     * 
     */
    private AttributesJson parseAttribute(XmlPullParser parser) throws XmlPullParserException,
            IOException{
        parser.require(XmlPullParser.START_TAG, ns, ATTRIBUTE);
        AttributesJson attribute = new AttributesJson();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(Open311.DATATYPE)) {
                attribute.setDatatype(readElement(parser, Open311.DATATYPE));
            }
            else if (name.equals(Open311.DESCRIPTION)) {
                attribute.setDescription(readElement(parser, Open311.DESCRIPTION));
            }
            else if (name.equals(Open311.CODE)) {
                attribute.setCode(readElement(parser, Open311.CODE));
            }
            else if (name.equals(Open311.ORDER)) {
                attribute.setOrder(Integer.parseInt(readElement(parser, Open311.ORDER)));
            }
            else if (name.equals(Open311.VARIABLE)) {
                attribute.setVariable(Boolean.parseBoolean(readElement(parser, Open311.VARIABLE)));
            }
            else if (name.equals(Open311.REQUIRED)) {
                attribute.setRequired(Boolean.parseBoolean(readElement(parser, Open311.REQUIRED)));
            }
            else if (name.equals(Open311.VALUES)) {
                attribute.setValues(parseValues(parser));
            }
            else {
                skip(parser);
            }
        }
        return attribute;
    }

    /**
     * @param parser
     * @return ArrayList<ValuesJson>
     * @throws XmlPullParserException
     * @throws IOException
     * 
     */
    private ArrayList<ValuesJson> parseValues(XmlPullParser parser) throws XmlPullParserException, IOException{
        ArrayList<ValuesJson> values = new ArrayList<ValuesJson>();
        parser.require(XmlPullParser.START_TAG, ns, Open311.VALUES);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(Open311.VALUE)) {
                values.add(parseValue(parser));
            }
            else {
                skip(parser);
            }
        }
        return values;
    }

    /**
     * @param parser
     * @return ValuesJson 
     * @throws XmlPullParserException
     * @throws IOException
     * 
     */
    private ValuesJson parseValue(XmlPullParser parser) throws XmlPullParserException, IOException{
        parser.require(XmlPullParser.START_TAG, ns, Open311.VALUE);
        ValuesJson  value = new ValuesJson ();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(Open311.NAME)) {
                value.setName(readElement(parser, Open311.NAME));
            }
            else if (name.equals(Open311.KEY)) {
                value.setKey(readElement(parser, Open311.KEY));
            }
            else {
                skip(parser);
            }
        }
        return value;
    }

    /**
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     * @throws JSONException
     */
    private ArrayList<RequestsJson> parseRequests(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        ArrayList<RequestsJson> requests = new ArrayList<RequestsJson>();
        parser.require(XmlPullParser.START_TAG, ns, SERVICE_REQUESTS);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(REQUEST)) {
                requests.add(parseRequest(parser));
            }
            else {
                skip(parser);
            }
        }
        return requests;
    }

    /**
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     * 
     */
    private RequestsJson parseRequest(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, ns, REQUEST);
        RequestsJson request = new RequestsJson();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(Open311.SERVICE_CODE)) {
                request.setService_code(readElement(parser, Open311.SERVICE_CODE));
            }
            else if (name.equals(Open311.SERVICE_REQUEST_ID)) {
                request.setService_request_id(readElement(parser, Open311.SERVICE_REQUEST_ID));
            }
            else if (name.equals(Open311.TOKEN)) {
                request.setToken(readElement(parser, Open311.TOKEN));
            }
            else if (name.equals(Open311.ACCOUNT_ID)) {
                request.setAccountId(readElement(parser, Open311.ACCOUNT_ID));
            }
            else if (name.equals(Open311.LATITUDE)) {
                request.setLat(Float.parseFloat(readElement(parser, Open311.LATITUDE)));
            }
            else if (name.equals(Open311.LONGITUDE)) {
                request.setLong(Float.parseFloat(readElement(parser, Open311.LONGITUDE)));
            }
            else if (name.equals(Open311.DESCRIPTION)) {
                request.setDescription(readElement(parser, Open311.DESCRIPTION));
            }
            else if (name.equals(Open311.SERVICE_NOTICE)) {
                request.setServiceNotice(readElement(parser, Open311.SERVICE_NOTICE));
            }
            else if (name.equals(Open311.STATUS_NOTES)) {
                request.status_notes = readElement(parser, Open311.STATUS_NOTES);
            }
            else if (name.equals(Open311.STATUS)) {
                request.setStatus(readElement(parser, Open311.STATUS));
            }
            else if (name.equals(Open311.REQUESTED_DATETIME)) {
                request.setRequested_datetime(readElement(parser, Open311.REQUESTED_DATETIME));
            }
            else if (name.equals(Open311.UPDATED_DATETIME)) {
                request.setUpdated_datetime(readElement(parser, Open311.UPDATED_DATETIME));
            }
            else if (name.equals(Open311.EXPECTED_DATETIME)) {
                request.expected_datetime =  readElement(parser, Open311.EXPECTED_DATETIME);
            }
            else if (name.equals(Open311.AGENCY_RESPONSIBLE)) {
                request.agency_responsible = readElement(parser, Open311.AGENCY_RESPONSIBLE);
            }
            else {
                skip(parser);
            }
        }
        return request;
    }

    /**
     * @param parser
     * @return JSONArray
     * @throws XmlPullParserException
     * @throws IOException
     * @throws JSONException
     */
    private JSONArray parseErrors(XmlPullParser parser) throws XmlPullParserException, IOException,
            JSONException {
        JSONArray ja = new JSONArray();
        parser.require(XmlPullParser.START_TAG, ns, ERRORS);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(ERROR)) {
                ja.put(parseError(parser));
            } else {
                skip(parser);
            }
        }
        return ja;
    }

    /**
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject parseError(XmlPullParser parser) throws XmlPullParserException, IOException,
            JSONException {
        parser.require(XmlPullParser.START_TAG, ns, ERROR);
        JSONObject jo = new JSONObject();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(Open311.CODE)) {
                jo.put(Open311.CODE, readElement(parser, Open311.CODE));
            }
            else if (name.equals(Open311.DESCRIPTION)) {
                jo.put(Open311.DESCRIPTION, readElement(parser, Open311.DESCRIPTION));
            }
            else {
                skip(parser);
            }
        }
        return jo;
    }

    /**
     * @param parser
     * @param element
     * @return String
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readElement(XmlPullParser parser, String element) throws IOException,
            XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, element);
        String service_code = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, element);
        return service_code;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
