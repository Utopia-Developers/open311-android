package gov.in.bloomington.georeporter.json;


import java.util.List;

import com.google.gson.annotations.SerializedName;

public class RequestsJson{
   	private String address;
   	private String description;
   	private Number lat;
   	//As long is a java keyword.
   	@SerializedName("long") private Number longitude;
   	private String requested_datetime;
   	private String service_code;
   	private String service_name;
   	private String service_request_id;
   	private String status;
   	private String token;
   	private String updated_datetime;

 	public String getAddress(){
		return this.address;
	}
	public void setAddress(String address){
		this.address = address;
	}
 	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}
 	public Number getLat(){
		return this.lat;
	}
	public void setLat(Number lat){
		this.lat = lat;
	}
 	public Number getLong(){
		return this.longitude;
	}
	public void setLong(Number longitude){
		this.longitude = longitude;
	}
 	public String getRequested_datetime(){
		return this.requested_datetime;
	}
	public void setRequested_datetime(String requested_datetime){
		this.requested_datetime = requested_datetime;
	}
 	public String getService_code(){
		return this.service_code;
	}
	public void setService_code(String service_code){
		this.service_code = service_code;
	}
 	public String getService_name(){
		return this.service_name;
	}
	public void setService_name(String service_name){
		this.service_name = service_name;
	}
 	public String getService_request_id(){
		return this.service_request_id;
	}
	public void setService_request_id(String service_request_id){
		this.service_request_id = service_request_id;
	}
 	public String getStatus(){
		return this.status;
	}
	public void setStatus(String status){
		this.status = status;
	}
 	public String getToken(){
		return this.token;
	}
	public void setToken(String token){
		this.token = token;
	}
 	public String getUpdated_datetime(){
		return this.updated_datetime;
	}
	public void setUpdated_datetime(String updated_datetime){
		this.updated_datetime = updated_datetime;
	}
}
