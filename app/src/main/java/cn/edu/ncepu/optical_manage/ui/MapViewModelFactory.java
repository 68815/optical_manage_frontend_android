package cn.edu.ncepu.optical_manage.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.data.repository.CableSegmentRepository;
import cn.edu.ncepu.optical_manage.data.repository.ResourcePointRepository;

public class MapViewModelFactory implements ViewModelProvider.Factory {

    private final ApiService apiService;

    public MapViewModelFactory(ApiService apiService) {
        this.apiService = apiService;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MapViewModel.class)) {
            ResourcePointRepository resourcePointRepository = new ResourcePointRepository(apiService);
            CableSegmentRepository cableSegmentRepository = new CableSegmentRepository(apiService);
            return (T) new MapViewModel(resourcePointRepository, cableSegmentRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
