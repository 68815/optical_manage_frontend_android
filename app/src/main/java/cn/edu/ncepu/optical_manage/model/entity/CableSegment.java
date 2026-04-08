package cn.edu.ncepu.optical_manage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class CableSegment {
    @JsonProperty("segment_id")
    private Long id;

    @JsonProperty("id")
    private Long segmentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("routing_id")
    private Long routingId;

    @JsonProperty("cable_level")
    private String cableLevel;

    @JsonProperty("length")
    private BigDecimal length;

    @JsonProperty("fiber_count")
    private Integer fiberCount;

    @JsonProperty("tube_count")
    private Integer tubeCount;

    @JsonProperty("fibers_per_tube")
    private Integer fibersPerTube;

    @JsonProperty("laying_style")
    private String layingStyle;

    @JsonProperty("start_point_id")
    private Long startPointId;

    @JsonProperty("end_point_id")
    private Long endPointId;

    @JsonProperty("geom")
    private String geom;

    @JsonProperty("props")
    private String props;

    @JsonProperty("points")
    private List<Point> points;

    @JsonProperty("created_at")
    private String createTime;

    @JsonProperty("updated_at")
    private String updateTime;

    public CableSegment() {
    }

    public Long getId() {
        if (id != null) return id;
        return segmentId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(Long segmentId) {
        this.segmentId = segmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRoutingId() {
        return routingId;
    }

    public void setRoutingId(Long routingId) {
        this.routingId = routingId;
    }

    public String getCableLevel() {
        return cableLevel;
    }

    public void setCableLevel(String cableLevel) {
        this.cableLevel = cableLevel;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public Integer getFiberCount() {
        return fiberCount;
    }

    public void setFiberCount(Integer fiberCount) {
        this.fiberCount = fiberCount;
    }

    public Integer getTubeCount() {
        return tubeCount;
    }

    public void setTubeCount(Integer tubeCount) {
        this.tubeCount = tubeCount;
    }

    public Integer getFibersPerTube() {
        return fibersPerTube;
    }

    public void setFibersPerTube(Integer fibersPerTube) {
        this.fibersPerTube = fibersPerTube;
    }

    public String getLayingStyle() {
        return layingStyle;
    }

    public void setLayingStyle(String layingStyle) {
        this.layingStyle = layingStyle;
    }

    public Long getStartPointId() {
        return startPointId;
    }

    public void setStartPointId(Long startPointId) {
        this.startPointId = startPointId;
    }

    public Long getEndPointId() {
        return endPointId;
    }

    public void setEndPointId(Long endPointId) {
        this.endPointId = endPointId;
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

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public static class Point {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("lat")
        private double lat;

        @JsonProperty("lng")
        private double lng;

        @JsonProperty("latitude")
        private double latitude;

        @JsonProperty("longitude")
        private double longitude;

        public Point() {
        }

        public Point(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
            this.latitude = lat;
            this.longitude = lng;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
            this.latitude = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
            this.longitude = lng;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
            this.lat = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
            this.lng = longitude;
        }
    }
}
