package gov.in.bloomington.georeporter.json;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class PostDataJson {
    public String jurisdriction_id;
    public String service_code;
    public Double lat;
    @SerializedName("long")
    public Double longitude;
    public HashMap<String, String> attribute;
    public String address_string;
    public String address_id;
    public String email;
    public String device_id;
    public String account_id;
    public String first_name;
    public String last_name;
    public String phone;
    public String media_url;
    public String description;
    public String requested_datetime;
    
    
    
}
