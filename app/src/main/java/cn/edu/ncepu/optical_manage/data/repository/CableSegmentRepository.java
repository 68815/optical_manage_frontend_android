package cn.edu.ncepu.optical_manage.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.model.ApiResponse;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import cn.edu.ncepu.optical_manage.model.MapResponse;
import cn.edu.ncepu.optical_manage.model.request.MapQueryRequest;
import cn.edu.ncepu.optical_manage.utils.CoordTransformUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class CableSegmentRepository {

    private final ApiService apiService;

    private final MutableLiveData<List<CableSegment>> cableSegments = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<CableSegment> createdSegment = new MutableLiveData<>();
    private final MutableLiveData<CableSegment> updatedSegment = new MutableLiveData<>();
    private final MutableLiveData<Long> deletedSegmentId = new MutableLiveData<>();

    public CableSegmentRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public LiveData<List<CableSegment>> getCableSegments() {
        return cableSegments;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<CableSegment> getCreatedSegment() {
        return createdSegment;
    }

    public LiveData<CableSegment> getUpdatedSegment() {
        return updatedSegment;
    }

    public LiveData<Long> getDeletedSegmentId() {
        return deletedSegmentId;
    }

    public void loadAllCableSegments() {
        isLoading.postValue(true);
        Timber.d("开始加载光缆段列表");
        
        MapQueryRequest request = new MapQueryRequest();
        request.setType("fiber");
        request.setLimit(1000);
        
        apiService.queryFiberSegments(request).enqueue(new Callback<MapResponse>() {
            @Override
            public void onResponse(Call<MapResponse> call, Response<MapResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CableSegment> segments = parseCableSegments(response.body());
                    Timber.d("加载到 %d 个光缆段", segments != null ? segments.size() : 0);
                    if (segments != null) {
                        for (CableSegment segment : segments) {
                            parseGeomToPoints(segment);
                            convertPointsToGCJ02(segment);
                        }
                    }
                    cableSegments.postValue(segments);
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "加载失败";
                    Timber.e("加载失败 - HTTP: %d, msg: %s", response.code(), message);
                    errorMessage.postValue(message);
                }
            }

            @Override
            public void onFailure(Call<MapResponse> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "网络请求失败 - URL: %s", call.request().url());
                errorMessage.postValue("网络错误：" + t.getMessage());
            }
        });
    }
    
    private List<CableSegment> parseCableSegments(MapResponse mapResponse) {
        try {
            if (mapResponse.getResources() != null) {
                List<CableSegment> segments = new ArrayList<>();
                for (MapResponse.ResourceInfo info : mapResponse.getResources()) {
                    CableSegment segment = new CableSegment();
                    segment.setId(info.getId());
                    segment.setName(info.getName());
                    segment.setGeom(info.getGeom());
                    segment.setProps(info.getProps());
                    segments.add(segment);
                }
                return segments;
            }
        } catch (Exception e) {
            Timber.e(e, "解析光缆段数据失败");
        }
        return null;
    }

    public void createCableSegment(CableSegment segment) {
        isLoading.postValue(true);
        Timber.d("创建光缆段: %s", segment.getName());

        Map<String, Object> request = convertToRequest(segment);
        apiService.createFiberSegment(request).enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Long id = response.body().getData();
                    if (id != null) {
                        segment.setId(id);
                    }
                    Timber.d("光缆段创建成功, id=%d", segment.getId());
                    createdSegment.postValue(segment);
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "添加失败";
                    Timber.e("创建光缆段失败: %s", message);
                    errorMessage.postValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "创建光缆段失败");
                errorMessage.postValue("光缆段添加失败：" + t.getMessage());
            }
        });
    }

    public void updateCableSegment(CableSegment segment) {
        isLoading.postValue(true);
        Timber.d("更新光缆段: id=%d, name=%s", segment.getId(), segment.getName());

        Map<String, Object> request = convertToRequest(segment);
        apiService.updateFiberSegment(segment.getId(), request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Object ok = response.body().get("ok");
                    if (Boolean.TRUE.equals(ok)) {
                        Timber.d("光缆段更新成功");
                        updatedSegment.postValue(segment);
                    } else {
                        Timber.e("更新光缆段失败");
                        errorMessage.postValue("更新失败");
                    }
                } else {
                    Timber.e("更新光缆段失败");
                    errorMessage.postValue("更新失败");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "更新光缆段失败");
                errorMessage.postValue("更新失败：" + t.getMessage());
            }
        });
    }

    public void deleteCableSegment(CableSegment segment) {
        isLoading.postValue(true);
        Timber.d("删除光缆段: id=%d, name=%s", segment.getId(), segment.getName());

        apiService.deleteFiberSegment(segment.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Object ok = response.body().get("ok");
                    if (Boolean.TRUE.equals(ok)) {
                        Timber.d("光缆段删除成功, id=%d", segment.getId());
                        deletedSegmentId.postValue(segment.getId());
                    } else {
                        Timber.e("删除光缆段失败");
                        errorMessage.postValue("删除失败");
                    }
                } else {
                    Timber.e("删除光缆段失败");
                    errorMessage.postValue("删除失败");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "删除光缆段失败");
                errorMessage.postValue("删除失败：" + t.getMessage());
            }
        });
    }

    private void parseGeomToPoints(CableSegment segment) {
        String geom = segment.getGeom();
        if (geom == null || !geom.startsWith("LINESTRING")) {
            return;
        }

        try {
            String coordsStr = geom.substring("LINESTRING(".length(), geom.length() - 1);
            String[] coordPairs = coordsStr.split(",");
            List<CableSegment.Point> points = new ArrayList<>();

            for (String pair : coordPairs) {
                String[] parts = pair.trim().split("\\s+");
                if (parts.length >= 2) {
                    double lng = Double.parseDouble(parts[0]);
                    double lat = Double.parseDouble(parts[1]);
                    points.add(new CableSegment.Point(lat, lng));
                }
            }

            if (!points.isEmpty()) {
                segment.setPoints(points);
                segment.setStartPointId(points.get(0).getId());
                segment.setEndPointId(points.get(points.size() - 1).getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void convertPointsToGCJ02(CableSegment segment) {
        List<CableSegment.Point> points = segment.getPoints();
        if (points == null || points.isEmpty()) {
            return;
        }

        for (CableSegment.Point point : points) {
            double[] gcj02 = CoordTransformUtil.wgs84ToGcj02(point.getLat(), point.getLng());
            point.setLat(gcj02[0]);
            point.setLng(gcj02[1]);
        }
    }

    private Map<String, Object> convertToRequest(CableSegment segment) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", segment.getName());
        request.put("routingId", segment.getRoutingId());
        request.put("cableLevel", segment.getCableLevel());
        request.put("length", segment.getLength());
        request.put("fiberCount", segment.getFiberCount());
        request.put("tubeCount", segment.getTubeCount());
        request.put("fibersPerTube", segment.getFibersPerTube());
        request.put("layingStyle", segment.getLayingStyle());
        request.put("props", segment.getProps());

        List<Map<String, Object>> pointsList = new ArrayList<>();
        if (segment.getPoints() != null) {
            for (CableSegment.Point point : segment.getPoints()) {
                Map<String, Object> pointMap = new HashMap<>();
                pointMap.put("id", point.getId());
                pointMap.put("lat", point.getLat());
                pointMap.put("lng", point.getLng());
                pointsList.add(pointMap);
            }
        }
        request.put("points", pointsList);

        return request;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}
