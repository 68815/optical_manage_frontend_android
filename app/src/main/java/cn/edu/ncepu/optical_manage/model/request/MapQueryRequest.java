package cn.edu.ncepu.optical_manage.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MapQueryRequest {
    @JsonProperty("type")
    private String type;

    @JsonProperty("bbox")
    private Bbox bbox;

    @JsonProperty("centerRadius")
    private CenterRadius centerRadius;

    @JsonProperty("filter")
    private String filter;

    @JsonProperty("limit")
    private int limit = 100;

    public static class Bbox {
        @JsonProperty("minLat")
        private double minLat;

        @JsonProperty("minLng")
        private double minLng;

        @JsonProperty("maxLat")
        private double maxLat;

        @JsonProperty("maxLng")
        private double maxLng;

        public Bbox() {}

        public Bbox(double minLat, double minLng, double maxLat, double maxLng) {
            this.minLat = minLat;
            this.minLng = minLng;
            this.maxLat = maxLat;
            this.maxLng = maxLng;
        }

        public double getMinLat() { return minLat; }
        public void setMinLat(double minLat) { this.minLat = minLat; }
        public double getMinLng() { return minLng; }
        public void setMinLng(double minLng) { this.minLng = minLng; }
        public double getMaxLat() { return maxLat; }
        public void setMaxLat(double maxLat) { this.maxLat = maxLat; }
        public double getMaxLng() { return maxLng; }
        public void setMaxLng(double maxLng) { this.maxLng = maxLng; }
    }

    public static class CenterRadius {
        @JsonProperty("lat")
        private double lat;

        @JsonProperty("lng")
        private double lng;

        @JsonProperty("radiusM")
        private double radiusM;

        public CenterRadius() {}

        public CenterRadius(double lat, double lng, double radiusM) {
            this.lat = lat;
            this.lng = lng;
            this.radiusM = radiusM;
        }

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public double getRadiusM() { return radiusM; }
        public void setRadiusM(double radiusM) { this.radiusM = radiusM; }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Bbox getBbox() { return bbox; }
    public void setBbox(Bbox bbox) { this.bbox = bbox; }
    public String getFilter() { return filter; }
    public void setFilter(String filter) { this.filter = filter; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
    public CenterRadius getCenterRadius() { return centerRadius; }
    public void setCenterRadius(CenterRadius centerRadius) { this.centerRadius = centerRadius; }
}
