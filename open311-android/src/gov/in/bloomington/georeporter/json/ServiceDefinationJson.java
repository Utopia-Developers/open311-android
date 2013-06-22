
package gov.in.bloomington.georeporter.json;

import java.util.ArrayList;


/**
 * This is a java object representaion of the service defination response
 */
public class ServiceDefinationJson {
    private ArrayList<AttributesJson> attributes;
    private String service_code;

    public ArrayList<AttributesJson> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(ArrayList<AttributesJson> attributes) {
        this.attributes = attributes;
    }

    public String getService_code() {
        return this.service_code;
    }

    public void setService_code(String service_code) {
        this.service_code = service_code;
    }
}
