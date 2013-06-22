
package gov.in.bloomington.georeporter.json;

import java.util.ArrayList;
import java.util.List;

public class AttributesJson {
    private String code;
    private String datatype;
    private String datatype_description;
    private String description;
    private Number order;
    private boolean required;
    private ArrayList<ValuesJson> values;
    private boolean variable;

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDatatype() {
        return this.datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getDatatype_description() {
        return this.datatype_description;
    }

    public void setDatatype_description(String datatype_description) {
        this.datatype_description = datatype_description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Number getOrder() {
        return this.order;
    }

    public void setOrder(Number order) {
        this.order = order;
    }

    public boolean getRequired() {
        return this.required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public ArrayList<ValuesJson> getValues() {
        return this.values;
    }

    public void setValues(ArrayList<ValuesJson> values) {
        this.values = values;
    }

    public boolean getVariable() {
        return this.variable;
    }

    public void setVariable(boolean variable) {
        this.variable = variable;
    }
}
