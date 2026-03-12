package cn.edu.ncepu.optical_manage.model;

import com.google.gson.annotations.SerializedName;

public class ResourcePoint {
    @SerializedName("point_id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private ResourceType type;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("address")
    private String address;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createTime;

    @SerializedName("updated_at")
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

    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_FAULT = "fault";
    public static final String STATUS_MAINTENANCE = "maintenance";

    public ResourcePoint() {
        this.status = STATUS_NORMAL;
    }

    public ResourcePoint(String name, ResourceType type, double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = STATUS_NORMAL;
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

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
}
