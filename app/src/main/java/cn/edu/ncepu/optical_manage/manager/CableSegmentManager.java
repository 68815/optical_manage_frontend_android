package cn.edu.ncepu.optical_manage.manager;

import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.ncepu.optical_manage.model.CableSegment;

public class CableSegmentManager {

    private final AMap aMap;
    private final Map<Long, Polyline> polylineMap = new HashMap<>();

    public CableSegmentManager(AMap aMap) {
        this.aMap = aMap;
    }

    public void updatePolylines(List<CableSegment> segments) {
        clearAllPolylines();
        if (segments != null) {
            for (CableSegment segment : segments) {
                addPolyline(segment);
            }
        }
    }

    public void clearAllPolylines() {
        for (Polyline polyline : polylineMap.values()) {
            polyline.remove();
        }
        polylineMap.clear();
    }

    public void addPolyline(CableSegment segment) {
        if (segment == null || segment.getPoints() == null || segment.getPoints().isEmpty()) {
            return;
        }

        List<LatLng> latLngs = new ArrayList<>();
        for (CableSegment.Point point : segment.getPoints()) {
            latLngs.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(latLngs)
                .width(10)
                .color(Color.BLUE)
                .setDottedLine(false);

        Polyline polyline = aMap.addPolyline(polylineOptions);
        if (segment.getId() != null) {
            polylineMap.put(segment.getId(), polyline);
        }
    }

    public void updatePolyline(CableSegment segment) {
        if (segment == null || segment.getId() == null) return;
        
        Polyline existingPolyline = polylineMap.get(segment.getId());
        if (existingPolyline != null) {
            existingPolyline.remove();
        }
        addPolyline(segment);
    }

    public void removePolyline(Long segmentId) {
        if (segmentId == null) return;
        
        Polyline polyline = polylineMap.remove(segmentId);
        if (polyline != null) {
            polyline.remove();
        }
    }

    public Polyline getPolyline(CableSegment segment) {
        if (segment == null || segment.getId() == null) return null;
        return polylineMap.get(segment.getId());
    }

    public Map<Long, Polyline> getPolylineMap() {
        return polylineMap;
    }
}
