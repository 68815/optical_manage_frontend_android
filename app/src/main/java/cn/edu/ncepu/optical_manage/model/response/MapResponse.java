package cn.edu.ncepu.optical_manage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MapResponse {
    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("message")
    private String message;

    @JsonProperty("resources")
    private List<ResourceInfo> resources;

    public MapResponse() {
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResourceInfo> getResources() {
        return resources;
    }

    public void setResources(List<ResourceInfo> resources) {
        this.resources = resources;
    }

    public boolean isSuccess() {
        return ok;
    }

    public static class ResourceInfo {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("type")
        private String type;

        @JsonProperty("name")
        private String name;

        @JsonProperty("lat")
        private double lat;

        @JsonProperty("lng")
        private double lng;

        @JsonProperty("geom")
        private String geom;

        @JsonProperty("props")
        private String props;

        public ResourceInfo() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getGeom() {
            return geom;
        }

        public void setGeom(String geom) {
            this.geom = geom;
        }

        public String getProps() {
            return props;
        }

        public void setProps(String props) {
            this.props = props;
        }
    }
}
