package cn.edu.ncepu.optical_manage.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.model.ApiResponse;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        isLoading.setValue(true);
        apiService.getAllCableSegments().enqueue(new Callback<ApiResponse<List<CableSegment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CableSegment>>> call, Response<ApiResponse<List<CableSegment>>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cableSegments.setValue(response.body().getData());
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "加载失败";
                    errorMessage.setValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CableSegment>>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("加载光缆段失败：" + t.getMessage());
            }
        });
    }

    public void createCableSegment(CableSegment segment) {
        isLoading.setValue(true);
        apiService.createCableSegment(segment).enqueue(new Callback<ApiResponse<CableSegment>>() {
            @Override
            public void onResponse(Call<ApiResponse<CableSegment>> call, Response<ApiResponse<CableSegment>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    createdSegment.setValue(response.body().getData());
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "添加失败";
                    errorMessage.setValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CableSegment>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("光缆段添加失败：" + t.getMessage());
            }
        });
    }

    public void updateCableSegment(CableSegment segment) {
        isLoading.setValue(true);
        apiService.updateCableSegment(segment.getId(), segment).enqueue(new Callback<ApiResponse<CableSegment>>() {
            @Override
            public void onResponse(Call<ApiResponse<CableSegment>> call, Response<ApiResponse<CableSegment>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updatedSegment.setValue(segment);
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "更新失败";
                    errorMessage.setValue(message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CableSegment>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("更新失败：" + t.getMessage());
            }
        });
    }

    public void deleteCableSegment(CableSegment segment) {
        isLoading.setValue(true);
        apiService.deleteCableSegment(segment.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    deletedSegmentId.setValue(segment.getId());
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

    public void clearError() {
        errorMessage.setValue(null);
    }
}
