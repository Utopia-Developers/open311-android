
package gov.in.bloomington.georeporter.json;


public class ServiceEntityJson {
    private String description;
    private String group;
    private String keywords;
    private boolean metadata;
    private String service_code;
    private String service_name;
    private String type;

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKeywords() {
        return this.keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public boolean getMetadata() {
        return this.metadata;
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    public String getService_code() {
        return this.service_code;
    }

    public void setService_code(String service_code) {
        this.service_code = service_code;
    }

    public String getService_name() {
        return this.service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
