
package gov.in.bloomington.georeporter.json;

public class RequestResponseJson {
    private String service_request_id;
    private String token;
    private String service_notice;
    private String account_id;
    public String status;

    public void setToken(String token)
    {
        this.token = token;
    }

    public void setServiceNotice(String serviceNotice)
    {
        this.service_notice = serviceNotice;
    }

    public void setAccountId(String accountId)
    {
        this.account_id = accountId;
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

    public String getServiceNotice()
    {
        return this.service_notice;
    }

    public String getAccountId()
    {
        return this.account_id;
    }
}
