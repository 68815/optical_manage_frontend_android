package cn.edu.ncepu.optical_manage.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import cn.edu.ncepu.optical_manage.data.repository.CableSegmentRepository;
import cn.edu.ncepu.optical_manage.data.repository.ResourcePointRepository;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;

public class MapViewModel extends ViewModel {

    public enum AddMode {
        NONE, RESOURCE_POINT, CABLE
    }

    private final ResourcePointRepository resourcePointRepository;
    private final CableSegmentRepository cableSegmentRepository;

    private final MutableLiveData<AddMode> currentAddMode = new MutableLiveData<>(AddMode.NONE);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public MapViewModel(ResourcePointRepository resourcePointRepository, 
                       CableSegmentRepository cableSegmentRepository) {
        this.resourcePointRepository = resourcePointRepository;
        this.cableSegmentRepository = cableSegmentRepository;
    }

    public LiveData<AddMode> getCurrentAddMode() {
        return currentAddMode;
    }

    public void setCurrentAddMode(AddMode mode) {
        currentAddMode.setValue(mode);
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void clearToastMessage() {
        toastMessage.setValue(null);
    }

    // ResourcePoint operations
    public LiveData<List<ResourcePoint>> getResourcePoints() {
        return resourcePointRepository.getResourcePoints();
    }

    public LiveData<String> getResourcePointError() {
        return resourcePointRepository.getErrorMessage();
    }

    public LiveData<Boolean> getResourcePointLoading() {
        return resourcePointRepository.getIsLoading();
    }

    public LiveData<ResourcePoint> getCreatedResourcePoint() {
        return resourcePointRepository.getCreatedPoint();
    }

    public LiveData<ResourcePoint> getUpdatedResourcePoint() {
        return resourcePointRepository.getUpdatedPoint();
    }

    public LiveData<Long> getDeletedResourcePointId() {
        return resourcePointRepository.getDeletedPointId();
    }

    public void loadResourcePoints() {
        resourcePointRepository.loadAllResourcePoints();
    }

    public void createResourcePoint(ResourcePoint point) {
        resourcePointRepository.createResourcePoint(point);
    }

    public void updateResourcePoint(ResourcePoint point) {
        resourcePointRepository.updateResourcePoint(point);
    }

    public void deleteResourcePoint(ResourcePoint point) {
        resourcePointRepository.deleteResourcePoint(point);
    }

    public void clearResourcePointError() {
        resourcePointRepository.clearError();
    }

    // CableSegment operations
    public LiveData<List<CableSegment>> getCableSegments() {
        return cableSegmentRepository.getCableSegments();
    }

    public LiveData<String> getCableSegmentError() {
        return cableSegmentRepository.getErrorMessage();
    }

    public LiveData<Boolean> getCableSegmentLoading() {
        return cableSegmentRepository.getIsLoading();
    }

    public LiveData<CableSegment> getCreatedCableSegment() {
        return cableSegmentRepository.getCreatedSegment();
    }

    public LiveData<CableSegment> getUpdatedCableSegment() {
        return cableSegmentRepository.getUpdatedSegment();
    }

    public LiveData<Long> getDeletedCableSegmentId() {
        return cableSegmentRepository.getDeletedSegmentId();
    }

    public void loadCableSegments() {
        cableSegmentRepository.loadAllCableSegments();
    }

    public void createCableSegment(CableSegment segment) {
        cableSegmentRepository.createCableSegment(segment);
    }

    public void updateCableSegment(CableSegment segment) {
        cableSegmentRepository.updateCableSegment(segment);
    }

    public void deleteCableSegment(CableSegment segment) {
        cableSegmentRepository.deleteCableSegment(segment);
    }

    public void clearCableSegmentError() {
        cableSegmentRepository.clearError();
    }

    // Combined loading
    public void loadAllData() {
        loadResourcePoints();
        loadCableSegments();
    }
}
