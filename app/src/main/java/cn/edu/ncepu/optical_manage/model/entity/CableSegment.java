package cn.edu.ncepu.optical_manage.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CableSegment {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("startPointId")
    private Long startPointId;

    @SerializedName("endPointId")
    private Long endPointId;

    @SerializedName("startPointName")
    private String startPointName;

    @SerializedName("endPointName")
    private String endPointName;

    @SerializedName("length")
    private double length;

    @SerializedName("fiberCount")
    private int fiberCount;

    @SerializedName("points")
    private List<Point> points;

    @SerializedName("description")
    private String description;

    @SerializedName("createTime")
    private String createTime;

    @SerializedName("updateTime")
    private String updateTime;

    public CableSegment() {
    }

    public CableSegment(String name, Long startPointId, Long endPointId) {
        this.name = name;
        this.startPointId = startPointId;
        this.endPointId = endPointId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getStartPointName() {
        return startPointName;
    }

    public void setStartPointName(String startPointName) {
        this.startPointName = startPointName;
    }

    public String getEndPointName() {
        return endPointName;
    }

    public void setEndPointName(String endPointName) {
        this.endPointName = endPointName;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getFiberCount() {
        return fiberCount;
    }

    public void setFiberCount(int fiberCount) {
        this.fiberCount = fiberCount;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        public Point() {
        }

        public Point(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
