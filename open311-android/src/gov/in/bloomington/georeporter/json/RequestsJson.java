
package gov.in.bloomington.georeporter.json;

import com.google.gson.annotations.SerializedName;

public class RequestsJson {
    private String address;
    private String description;
    private String lat;
    // As long is a java keyword.
    @SerializedName("long")
    private String longitude;
    private String requested_datetime;
    private String service_code;
    private String service_name;
    private String service_request_id;
    private String status;
    private String token;
    private String updated_datetime;
    private String account_id;
    private String service_notice;

    public String status_notes;
    public String expected_datetime;
    public String agency_responsible;
    public String address_id;
    public String zipcode;

    public void setServiceNotice(String serviceNotice)
    {
        this.service_notice = serviceNotice;
    }

    public String getServiceNotice()
    {
        return this.service_notice;
    }

    public void setAccountId(String accountId)
    {
        this.account_id = accountId;
    }

    public String getAccountId()
    {
        return this.account_id;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLat() {
        return this.lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLong() {
        return this.longitude;
    }

    public void setLong(String longitude) {
        this.longitude = longitude;
    }

    public String getRequested_datetime() {
        return this.requested_datetime;
    }

    public void setRequested_datetime(String requested_datetime) {
        this.requested_datetime = requested_datetime;
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

    public String getService_request_id() {
        return this.service_request_id;
    }

    public void setService_request_id(String service_request_id) {
        this.service_request_id = service_request_id;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUpdated_datetime() {
        return this.updated_datetime;
    }

    public void setUpdated_datetime(String updated_datetime) {
        this.updated_datetime = updated_datetime;
    }
}
