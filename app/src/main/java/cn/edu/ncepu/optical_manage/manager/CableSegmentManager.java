package cn.edu.ncepu.optical_manage.manager;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.model.ApiResponse;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CableSegmentManager {

    private ApiService apiService;
    private AMap aMap;
    private Context context;
    private Map<Long, Polyline> polylineMap = new HashMap<>();
    private OnCableSegmentChangedListener listener;

    public interface OnCableSegmentChangedListener {
        void onCableSegmentsUpdated();
    }

    public CableSegmentManager(ApiService apiService, AMap aMap, Context context) {
        this.apiService = apiService;
        this.aMap = aMap;
        this.context = context;
    }

    public void setOnCableSegmentChangedListener(OnCableSegmentChangedListener listener) {
        this.listener = listener;
    }

    public void loadAllCableSegments() {
        apiService.getAllCableSegments().enqueue(new Callback<ApiResponse<List<CableSegment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CableSegment>>> call, Response<ApiResponse<List<CableSegment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CableSegment> segments = response.body().getData();
                    updatePolylines(segments);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CableSegment>>> call, Throwable t) {
                showToast("加载光缆段失败：" + t.getMessage());
            }
        });
    }

    private void updatePolylines(List<CableSegment> segments) {
        clearAllPolylines();
        for (CableSegment segment : segments) {
            addPolyline(segment);
        }
    }

    public void clearAllPolylines() {
        for (Polyline polyline : polylineMap.values()) {
            polyline.remove();
        }
        polylineMap.clear();
    }

    public void addPolyline(CableSegment segment) {
        if (segment.getPoints() == null || segment.getPoints().isEmpty()) {
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
        polylineMap.put(segment.getId(), polyline);
    }

    public void createCableSegment(CableSegment segment) {
        apiService.createCableSegment(segment).enqueue(new Callback<ApiResponse<CableSegment>>() {
            @Override
            public void onResponse(Call<ApiResponse<CableSegment>> call, Response<ApiResponse<CableSegment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CableSegment created = response.body().getData();
                    addPolyline(created);
                    showToast("光缆段添加成功");
                    if (listener != null) {
                        listener.onCableSegmentsUpdated();
                    }
                } else {
                    showToast("光缆段添加失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CableSegment>> call, Throwable t) {
                showToast("光缆段添加失败：" + t.getMessage());
            }
        });
    }

    public void updateCableSegment(CableSegment segment) {
        apiService.updateCableSegment(segment.getId(), segment).enqueue(new Callback<ApiResponse<CableSegment>>() {
            @Override
            public void onResponse(Call<ApiResponse<CableSegment>> call, Response<ApiResponse<CableSegment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Polyline polyline = polylineMap.get(segment.getId());
                    if (polyline != null) {
                        polyline.remove();
                    }
                    addPolyline(segment);
                    showToast("更新成功");
                    if (listener != null) {
                        listener.onCableSegmentsUpdated();
                    }
                } else {
                    showToast("更新失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CableSegment>> call, Throwable t) {
                showToast("更新失败：" + t.getMessage());
            }
        });
    }

    public void deleteCableSegment(CableSegment segment) {
        apiService.deleteCableSegment(segment.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    polylineMap.remove(segment.getId()).remove();
                    showToast("删除成功");
                    if (listener != null) {
                        listener.onCableSegmentsUpdated();
                    }
                } else {
                    showToast("删除失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showToast("删除失败：" + t.getMessage());
            }
        });
    }

    public Polyline getPolyline(CableSegment segment) {
        return polylineMap.get(segment.getId());
    }

    public Map<Long, Polyline> getPolylineMap() {
        return polylineMap;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
