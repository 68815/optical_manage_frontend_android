package cn.edu.ncepu.optical_manage.manager;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.ncepu.optical_manage.model.ResourcePoint;

public class ResourcePointManager {

    private final AMap aMap;
    private final Map<Long, Marker> markerMap = new HashMap<>();

    public ResourcePointManager(AMap aMap) {
        this.aMap = aMap;
    }

    public void updateMarkers(List<ResourcePoint> points) {
        clearAllMarkers();
        if (points != null) {
            for (ResourcePoint point : points) {
                addMarker(point);
            }
        }
    }

    public void clearAllMarkers() {
        for (Marker marker : markerMap.values()) {
            marker.remove();
        }
        markerMap.clear();
    }

    public void addMarker(ResourcePoint point) {
        if (point == null) return;
        
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        float markerColor = getMarkerColor(point.getType());

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(point.getName())
                .snippet("类型：" + (point.getType() != null ? point.getType().getDisplayName() : "未知"))
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

        Marker marker = aMap.addMarker(markerOptions);
        marker.setObject(point);
        if (point.getId() != null) {
            markerMap.put(point.getId(), marker);
        }
    }

    public void updateMarker(ResourcePoint point) {
        if (point == null || point.getId() == null) return;
        
        Marker existingMarker = markerMap.get(point.getId());
        if (existingMarker != null) {
            existingMarker.remove();
        }
        addMarker(point);
    }

    public void removeMarker(Long pointId) {
        if (pointId == null) return;
        
        Marker marker = markerMap.remove(pointId);
        if (marker != null) {
            marker.remove();
        }
    }

    private float getMarkerColor(ResourcePoint.ResourceType type) {
        if (type == null) return BitmapDescriptorFactory.HUE_RED;
        switch (type) {
            case POLE:
                return BitmapDescriptorFactory.HUE_BLUE;
            case MANHOLE:
                return BitmapDescriptorFactory.HUE_GREEN;
            case OFFICE:
                return BitmapDescriptorFactory.HUE_ORANGE;
            case CABINET:
                return BitmapDescriptorFactory.HUE_YELLOW;
            case BASE_STATION:
                return BitmapDescriptorFactory.HUE_VIOLET;
            case DISTRIBUTION_BOX:
                return BitmapDescriptorFactory.HUE_CYAN;
            case USER_TERMINAL:
                return BitmapDescriptorFactory.HUE_ROSE;
            default:
                return BitmapDescriptorFactory.HUE_RED;
        }
    }

    public Marker getMarker(ResourcePoint point) {
        if (point == null || point.getId() == null) return null;
        return markerMap.get(point.getId());
    }

    public Map<Long, Marker> getMarkerMap() {
        return markerMap;
    }
}
