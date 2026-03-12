package cn.edu.ncepu.optical_manage.model;

import com.google.gson.annotations.SerializedName;

public class ResourceRequest {
    @SerializedName("type")
    private String type;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    @SerializedName("props")
    private String props;

    public ResourceRequest() {
    }

    public ResourceRequest(String type, double lat, double lng, String props) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.props = props;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
    }
}
