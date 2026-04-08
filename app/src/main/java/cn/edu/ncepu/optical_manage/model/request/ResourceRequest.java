package cn.edu.ncepu.optical_manage.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceRequest {
    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("address")
    private String address;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("lat")
    private double lat;

    @JsonProperty("lng")
    private double lng;

    @JsonProperty("props")
    private String props;

    public ResourceRequest() {
    }

    public ResourceRequest(String name, String type, String address, Integer status, double lat, double lng, String props) {
        this.name = name;
        this.type = type;
        this.address = address;
        this.status = status;
        this.lat = lat;
        this.lng = lng;
        this.props = props;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
