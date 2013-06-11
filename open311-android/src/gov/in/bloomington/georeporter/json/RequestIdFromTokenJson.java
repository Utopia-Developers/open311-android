
package gov.in.bloomington.georeporter.json;

public class RequestIdFromTokenJson {
    private String service_request_id;
    private String token;

    public void setToken(String token)
    {
        this.token = token;
    }

    public void setServiceReuestId(String serviceRequestId)
    {
        this.service_request_id = serviceRequestId;
    }

    public String getToken()
    {
        return token;
    }

    public String getServiceReuestId()
    {
        return service_request_id;
    }
}
