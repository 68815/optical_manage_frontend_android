package cn.edu.ncepu.optical_manage.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

public class ResourcePointRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

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
        isLoading.setValue(true);
        apiService.getAllResourcePoints().enqueue(new Callback<ApiResponse<List<ResourcePoint>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ResourcePoint>>> call, Response<ApiResponse<List<ResourcePoint>>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    resourcePoints.setValue(response.body().getData());
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "加载失败";
                    errorMessage.setValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ResourcePoint>>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("加载资源点失败：" + t.getMessage());
            }
        });
    }

    public void createResourcePoint(ResourcePoint point) {
        isLoading.setValue(true);
        ResourceRequest request = convertToRequest(point);

        apiService.createResourcePoint(request).enqueue(new Callback<ApiResponse<ResourcePoint>>() {
            @Override
            public void onResponse(Call<ApiResponse<ResourcePoint>> call, Response<ApiResponse<ResourcePoint>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    createdPoint.setValue(response.body().getData());
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "添加失败";
                    errorMessage.setValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ResourcePoint>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("添加失败：" + t.getMessage());
            }
        });
    }

    public void updateResourcePoint(ResourcePoint point) {
        isLoading.setValue(true);
        ResourceRequest request = convertToRequest(point);

        apiService.updateResourcePoint(point.getId(), request).enqueue(new Callback<ApiResponse<ResourcePoint>>() {
            @Override
            public void onResponse(Call<ApiResponse<ResourcePoint>> call, Response<ApiResponse<ResourcePoint>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updatedPoint.setValue(point);
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "更新失败";
                    errorMessage.setValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ResourcePoint>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("更新失败：" + t.getMessage());
            }
        });
    }

    public void deleteResourcePoint(ResourcePoint point) {
        isLoading.setValue(true);
        apiService.deleteResourcePoint(point.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    deletedPointId.setValue(point.getId());
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "删除失败";
                    errorMessage.setValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("删除失败：" + t.getMessage());
            }
        });
    }

    private ResourceRequest convertToRequest(ResourcePoint point) {
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put("name", point.getName() != null ? point.getName() : "");
        propsMap.put("status", point.getStatus() != null ? point.getStatus() : "normal");
        propsMap.put("address", point.getAddress() != null ? point.getAddress() : "");

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

    public void clearError() {
        errorMessage.setValue(null);
    }
}
