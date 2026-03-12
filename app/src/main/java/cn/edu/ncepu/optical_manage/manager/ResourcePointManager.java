package cn.edu.ncepu.optical_manage.manager;

import android.content.Context;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.model.ApiResponse;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;
import cn.edu.ncepu.optical_manage.model.ResourceRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResourcePointManager {

    private ApiService apiService;
    private AMap aMap;
    private Context context;
    private Map<Long, Marker> markerMap = new HashMap<>();
    private OnResourcePointChangedListener listener;
    private Gson gson = new Gson();

    public interface OnResourcePointChangedListener {
        void onResourcePointsUpdated();
    }

    public ResourcePointManager(ApiService apiService, AMap aMap, Context context) {
        this.apiService = apiService;
        this.aMap = aMap;
        this.context = context;
    }

    public void setOnResourcePointChangedListener(OnResourcePointChangedListener listener) {
        this.listener = listener;
    }

    public void loadAllResourcePoints() {
        apiService.getAllResourcePoints().enqueue(new Callback<ApiResponse<List<ResourcePoint>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ResourcePoint>>> call, Response<ApiResponse<List<ResourcePoint>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ResourcePoint> points = response.body().getData();
                    updateMarkers(points);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ResourcePoint>>> call, Throwable t) {
                showToast("加载资源点失败：" + t.getMessage());
            }
        });
    }

    private void updateMarkers(List<ResourcePoint> points) {
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

    public void createResourcePoint(ResourcePoint point) {
        ResourceRequest request = convertToRequest(point);
        
        apiService.createResourcePoint(request).enqueue(new Callback<ApiResponse<ResourcePoint>>() {
            @Override
            public void onResponse(Call<ApiResponse<ResourcePoint>> call, Response<ApiResponse<ResourcePoint>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ResourcePoint created = response.body().getData();
                    addMarker(created);
                    showToast("添加成功：" + created.getName());
                    if (listener != null) {
                        listener.onResourcePointsUpdated();
                    }
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "未知错误";
                    showToast("添加失败：" + message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ResourcePoint>> call, Throwable t) {
                showToast("添加失败：" + t.getMessage());
            }
        });
    }

    public void updateResourcePoint(ResourcePoint point) {
        ResourceRequest request = convertToRequest(point);
        
        apiService.updateResourcePoint(point.getId(), request).enqueue(new Callback<ApiResponse<ResourcePoint>>() {
            @Override
            public void onResponse(Call<ApiResponse<ResourcePoint>> call, Response<ApiResponse<ResourcePoint>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Marker marker = markerMap.get(point.getId());
                    if (marker != null) {
                        marker.remove();
                    }
                    addMarker(point);
                    showToast("更新成功");
                    if (listener != null) {
                        listener.onResourcePointsUpdated();
                    }
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "未知错误";
                    showToast("更新失败：" + message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ResourcePoint>> call, Throwable t) {
                showToast("更新失败：" + t.getMessage());
            }
        });
    }

    private ResourceRequest convertToRequest(ResourcePoint point) {
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put("name", point.getName() != null ? point.getName() : "");
        propsMap.put("address", point.getAddress() != null ? point.getAddress() : "");
        propsMap.put("status", point.getStatus() != null ? point.getStatus() : "normal");
        
        String propsJson = gson.toJson(propsMap);
        
        String typeValue = "pole";
        if (point.getType() != null) {
            typeValue = point.getType().getValue();
        }
        
        return new ResourceRequest(
                typeValue,
                point.getLatitude(),
                point.getLongitude(),
                propsJson
        );
    }

    public void deleteResourcePoint(ResourcePoint point) {
        apiService.deleteResourcePoint(point.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Marker marker = markerMap.remove(point.getId());
                    if (marker != null) {
                        marker.remove();
                    }
                    showToast("删除成功");
                    if (listener != null) {
                        listener.onResourcePointsUpdated();
                    }
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "未知错误";
                    showToast("删除失败：" + message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showToast("删除失败：" + t.getMessage());
            }
        });
    }

    public Marker getMarker(ResourcePoint point) {
        return markerMap.get(point.getId());
    }

    public Map<Long, Marker> getMarkerMap() {
        return markerMap;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
