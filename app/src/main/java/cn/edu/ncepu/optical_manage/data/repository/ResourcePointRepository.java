package cn.edu.ncepu.optical_manage.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.model.ApiResponse;
import cn.edu.ncepu.optical_manage.model.MapResponse;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;
import cn.edu.ncepu.optical_manage.model.ResourceRequest;
import cn.edu.ncepu.optical_manage.model.request.MapQueryRequest;
import cn.edu.ncepu.optical_manage.utils.CoordTransformUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ResourcePointRepository {

    private final ApiService apiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MutableLiveData<List<ResourcePoint>> resourcePoints = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<ResourcePoint> createdPoint = new MutableLiveData<>();
    private final MutableLiveData<ResourcePoint> updatedPoint = new MutableLiveData<>();
    private final MutableLiveData<Long> deletedPointId = new MutableLiveData<>();

    public ResourcePointRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public LiveData<List<ResourcePoint>> getResourcePoints() {
        return resourcePoints;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<ResourcePoint> getCreatedPoint() {
        return createdPoint;
    }

    public LiveData<ResourcePoint> getUpdatedPoint() {
        return updatedPoint;
    }

    public LiveData<Long> getDeletedPointId() {
        return deletedPointId;
    }

    public void loadAllResourcePoints() {
        isLoading.postValue(true);
        Timber.d("开始加载资源点列表");
        
        MapQueryRequest request = new MapQueryRequest();
        request.setType("resource");
        request.setLimit(1000);
        
        apiService.queryResources(request).enqueue(new Callback<MapResponse>() {
            @Override
            public void onResponse(Call<MapResponse> call, Response<MapResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ResourcePoint> points = parseResourcePoints(response.body());
                    Timber.d("加载到 %d 个资源点", points != null ? points.size() : 0);
                    if (points != null) {
                        for (ResourcePoint point : points) {
                            convertToGCJ02(point);
                        }
                    }
                    resourcePoints.postValue(points);
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
    
    private List<ResourcePoint> parseResourcePoints(MapResponse mapResponse) {
        try {
            if (mapResponse.getResources() != null) {
                List<ResourcePoint> points = new ArrayList<>();
                for (MapResponse.ResourceInfo info : mapResponse.getResources()) {
                    ResourcePoint point = new ResourcePoint();
                    point.setId(info.getId());
                    point.setType(info.getType());
                    point.setName(info.getName());
                    point.setLatitude(info.getLat());
                    point.setLongitude(info.getLng());
                    point.setGeom(info.getGeom());
                    point.setProps(info.getProps());
                    points.add(point);
                }
                return points;
            }
        } catch (Exception e) {
            Timber.e(e, "解析资源点数据失败");
        }
        return null;
    }

    public void createResourcePoint(ResourcePoint point) {
        isLoading.postValue(true);
        Timber.d("创建资源点: %s", point.getName());
        ResourceRequest request = convertToRequest(point);

        apiService.createResourcePoint(request).enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Long id = response.body().getData();
                    Timber.d("资源点创建成功, id=%d", id);
                    if (id != null) {
                        point.setId(id);
                        createdPoint.postValue(point);
                    }
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "添加失败";
                    Timber.e("创建资源点失败: %s", message);
                    errorMessage.postValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "创建资源点失败");
                errorMessage.postValue("添加失败：" + t.getMessage());
            }
        });
    }

    public void updateResourcePoint(ResourcePoint point) {
        isLoading.postValue(true);
        Timber.d("更新资源点: id=%d, name=%s", point.getId(), point.getName());
        ResourceRequest request = convertToRequest(point);

        apiService.updateResourcePoint(point.getId(), request).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updatedPoint.postValue(point);
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "更新失败";
                    errorMessage.postValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "更新资源点失败");
                errorMessage.postValue("更新失败：" + t.getMessage());
            }
        });
    }

    public void deleteResourcePoint(ResourcePoint point) {
        isLoading.postValue(true);
        Timber.d("删除资源点: id=%d, name=%s", point.getId(), point.getName());
        apiService.deleteResourcePoint(point.getId()).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Timber.d("资源点删除成功, id=%d", point.getId());
                    deletedPointId.postValue(point.getId());
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "删除失败";
                    Timber.e("删除资源点失败: %s", message);
                    errorMessage.postValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "删除资源点失败");
                errorMessage.postValue("删除失败：" + t.getMessage());
            }
        });
    }

    private ResourceRequest convertToRequest(ResourcePoint point) {
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put("name", point.getName() != null ? point.getName() : "");
        propsMap.put("status", point.getStatus() != null ? point.getStatus() : 0);
        propsMap.put("address", point.getAddress() != null ? point.getAddress() : "");

        String propsJson;
        try {
            propsJson = objectMapper.writeValueAsString(propsMap);
        } catch (JsonProcessingException e) {
            Timber.e(e, "转换props为JSON失败");
            propsJson = "{}";
        }

        String typeValue = "pole";
        if (point.getType() != null) {
            typeValue = point.getType();
        }

        return new ResourceRequest(
                point.getName(),
                typeValue,
                point.getAddress(),
                point.getStatus(),
                point.getLatitude(),
                point.getLongitude(),
                propsJson
        );
    }

    private void convertToGCJ02(ResourcePoint point) {
        double[] gcj02 = CoordTransformUtil.wgs84ToGcj02(point.getLatitude(), point.getLongitude());
        point.setLatitude(gcj02[0]);
        point.setLongitude(gcj02[1]);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}
