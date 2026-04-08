package cn.edu.ncepu.optical_manage.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourcePoint {
    @JsonProperty("resource_point_id")
    private Long id;

    @JsonProperty("id")
    private Long resourcePointId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("address")
    private String address;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lng")
    private double longitude;

    @JsonProperty("geom")
    private String geom;

    @JsonProperty("props")
    private String props;

    @JsonProperty("created_at")
    private String createTime;

    @JsonProperty("updated_at")
    private String updateTime;

    public enum ResourceType {
        POLE("pole", "电杆"),
        MANHOLE("manhole", "人井"),
        OFFICE("office", "营业厅"),
        CABINET("cabinet", "光交箱"),
        BASE_STATION("base_station", "基站"),
        DISTRIBUTION_BOX("distribution_box", "分纤箱"),
        USER_TERMINAL("user_terminal", "用户终端");

        private final String value;
        private final String displayName;

        ResourceType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ResourceType fromValue(String value) {
            if (value == null) return POLE;
            for (ResourceType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return POLE;
        }
    }

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_FAULT = 1;
    public static final int STATUS_MAINTENANCE = 2;

    public ResourcePoint() {
        this.status = STATUS_NORMAL;
    }

    public ResourcePoint(String name, String type, double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = STATUS_NORMAL;
    }

    public Long getId() {
        if (id != null) return id;
        return resourcePointId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourcePointId() {
        return resourcePointId;
    }

    public void setResourcePointId(Long resourcePointId) {
        this.resourcePointId = resourcePointId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ResourceType getResourceType() {
        return ResourceType.fromValue(type);
    }
}
