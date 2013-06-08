
package gov.in.bloomington.georeporter.json;

import java.util.List;

public class ServiceDefinationJson{
   	private List attributes;
   	private String service_code;

 	public List getAttributes(){
		return this.attributes;
	}
	public void setAttributes(List attributes){
		this.attributes = attributes;
	}
 	public String getService_code(){
		return this.service_code;
	}
	public void setService_code(String service_code){
		this.service_code = service_code;
	}
}
