package cn.edu.ncepu.optical_manage.ui;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import cn.edu.ncepu.optical_manage.R;
import cn.edu.ncepu.optical_manage.api.ApiClient;
import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.manager.CableSegmentManager;
import cn.edu.ncepu.optical_manage.manager.ResourcePointManager;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;
import cn.edu.ncepu.optical_manage.ui.adapters.ResourcePointInfoWindowAdapter;
import cn.edu.ncepu.optical_manage.ui.dialogs.AddResourcePointDialog;
import cn.edu.ncepu.optical_manage.ui.dialogs.EditResourcePointDialog;
import cn.edu.ncepu.optical_manage.ui.helper.CableDrawHelper;
import cn.edu.ncepu.optical_manage.ui.helper.LocationHelper;
import cn.edu.ncepu.optical_manage.utils.PermissionUtils;

public class MapFragment extends Fragment implements 
        AMap.OnMapClickListener, 
        AMap.OnMarkerClickListener, 
        AMap.OnInfoWindowClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private MapView mapView;
    private AMap aMap;

    private EditText searchEditText;
    private ImageButton searchButton;
    private LinearLayout drawCablePanel;
    private TextView tvDrawHint;
    private Button btnCancelDraw;
    private Button btnFinishDraw;

    private FloatingActionButton fabAddResourcePoint;
    private FloatingActionButton fabDrawCable;
    private FloatingActionButton fabRefresh;

    private MapViewModel viewModel;
    private ResourcePointManager resourcePointManager;
    private CableSegmentManager cableSegmentManager;
    private LocationHelper locationHelper;
    private CableDrawHelper cableDrawHelper;
    private Marker currentInfoWindowMarker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ApiService apiService = ApiClient.getApiService();
        
        // Initialize ViewModel with Factory
        MapViewModelFactory factory = new MapViewModelFactory(apiService);
        viewModel = new ViewModelProvider(this, factory).get(MapViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        initViews(view);
        initLocationHelper();
        initMap(savedInstanceState);
        initManagers();
        initCableDrawHelper();
        initListeners();
        observeViewModel();
        checkPermissions();

        return view;
    }

    private void initViews(View view) {
        mapView = view.findViewById(R.id.mapView);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);
        drawCablePanel = view.findViewById(R.id.drawCablePanel);
        tvDrawHint = view.findViewById(R.id.tvDrawHint);
        btnCancelDraw = view.findViewById(R.id.btnCancelDraw);
        btnFinishDraw = view.findViewById(R.id.btnFinishDraw);

        fabAddResourcePoint = view.findViewById(R.id.fabAddResourcePoint);
        fabDrawCable = view.findViewById(R.id.fabDrawCable);
        fabRefresh = view.findViewById(R.id.fabRefresh);
    }
    
    private void initLocationHelper() {
        locationHelper = new LocationHelper(requireContext());
    }

    private void initMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        if (null == aMap) {
            aMap = mapView.getMap();
            aMap.setLocationSource(locationHelper);
            aMap.getUiSettings().setMyLocationButtonEnabled(true);
            aMap.setMyLocationEnabled(true);
            aMap.setOnMapClickListener(this);
            aMap.setOnMarkerClickListener(this);
            aMap.setOnInfoWindowClickListener(this);
            aMap.setInfoWindowAdapter(new ResourcePointInfoWindowAdapter(LayoutInflater.from(requireContext())));
        }
    }

    private void initManagers() {
        resourcePointManager = new ResourcePointManager(aMap);
        cableSegmentManager = new CableSegmentManager(aMap);
    }


    private void initCableDrawHelper() {
        cableDrawHelper = new CableDrawHelper(aMap, this, 
                new CableDrawHelper.OnCableDrawListener() {
            @Override
            public void onDrawModeChanged(boolean isDrawing, CableDrawHelper.DrawMode mode) {
                updateDrawPanel(isDrawing, mode);
            }

            @Override
            public void onDrawCancelled() {
                hideDrawPanel();
                viewModel.setCurrentAddMode(MapViewModel.AddMode.NONE);
            }

            @Override
            public void onDrawFinished() {
                hideDrawPanel();
                viewModel.setCurrentAddMode(MapViewModel.AddMode.NONE);
            }

            @Override
            public void onCableSegmentCreated(CableSegment segment) {
                viewModel.createCableSegment(segment);
            }
        });

        aMap.setOnMarkerDragListener(cableDrawHelper);
    }

    private void initListeners() {
        searchButton.setOnClickListener(v -> performSearch());

        fabAddResourcePoint.setOnClickListener(v -> {
            viewModel.setCurrentAddMode(MapViewModel.AddMode.RESOURCE_POINT);
            showToast("点击地图添加资源点");
        });

        fabDrawCable.setOnClickListener(v -> {
            viewModel.setCurrentAddMode(MapViewModel.AddMode.CABLE);
            cableDrawHelper.startDrawCableMode();
            showDrawPanel();
        });

        fabRefresh.setOnClickListener(v -> viewModel.loadAllData());

        btnCancelDraw.setOnClickListener(v -> {
            cableDrawHelper.cancelDrawCableMode();
            hideDrawPanel();
            viewModel.setCurrentAddMode(MapViewModel.AddMode.NONE);
        });

        btnFinishDraw.setOnClickListener(v -> cableDrawHelper.finishDrawCable());
    }

    private void observeViewModel() {
        // Observe ResourcePoints
        viewModel.getResourcePoints().observe(getViewLifecycleOwner(), points -> {
            if (points != null) {
                resourcePointManager.updateMarkers(points);
                cableDrawHelper.setResourcePoints(points);
            }
        });

        // Observe CableSegments
        viewModel.getCableSegments().observe(getViewLifecycleOwner(), segments -> {
            if (segments != null) {
                cableSegmentManager.updatePolylines(segments);
            }
        });

        // Observe newly created ResourcePoint
        viewModel.getCreatedResourcePoint().observe(getViewLifecycleOwner(), point -> {
            if (point != null) {
                resourcePointManager.addMarker(point);
                showToast("添加成功：" + point.getName());
                viewModel.loadResourcePoints(); // Refresh list
            }
        });

        // Observe updated ResourcePoint
        viewModel.getUpdatedResourcePoint().observe(getViewLifecycleOwner(), point -> {
            if (point != null) {
                resourcePointManager.updateMarker(point);
                showToast("更新成功");
                viewModel.loadResourcePoints(); // Refresh list
            }
        });

        // Observe deleted ResourcePoint
        viewModel.getDeletedResourcePointId().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                resourcePointManager.removeMarker(id);
                showToast("删除成功");
                viewModel.loadResourcePoints(); // Refresh list
            }
        });

        // Observe newly created CableSegment
        viewModel.getCreatedCableSegment().observe(getViewLifecycleOwner(), segment -> {
            if (segment != null) {
                cableSegmentManager.addPolyline(segment);
                showToast("光缆段添加成功：" + segment.getName());
                viewModel.loadCableSegments(); // Refresh list
            }
        });

        // Observe updated CableSegment
        viewModel.getUpdatedCableSegment().observe(getViewLifecycleOwner(), segment -> {
            if (segment != null) {
                cableSegmentManager.updatePolyline(segment);
                showToast("更新成功");
                viewModel.loadCableSegments(); // Refresh list
            }
        });

        // Observe deleted CableSegment
        viewModel.getDeletedCableSegmentId().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                cableSegmentManager.removePolyline(id);
                showToast("删除成功");
                viewModel.loadCableSegments(); // Refresh list
            }
        });

        // Observe errors
        viewModel.getResourcePointError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showToast(error);
                viewModel.clearResourcePointError();
            }
        });

        viewModel.getCableSegmentError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showToast(error);
                viewModel.clearCableSegmentError();
            }
        });

        // Observe AddMode
        viewModel.getCurrentAddMode().observe(getViewLifecycleOwner(), mode -> {
            // Mode changes are handled in onMapClick
        });
    }

    private void checkPermissions() {
        List<String> missingPermissions = PermissionUtils.getMissingPermissions(requireContext(), REQUIRED_PERMISSIONS);

        if (missingPermissions.isEmpty()) {
            initLocation();
            viewModel.loadAllData();
        } else {
            requestPermissions(missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.hasAllPermissions(requireContext(), REQUIRED_PERMISSIONS)) {
                initLocation();
                viewModel.loadAllData();
            } else {
                showToast("需要定位权限才能使用完整功能");
            }
        }
    }

    private void updateDrawPanel(boolean isDrawing, CableDrawHelper.DrawMode mode) {
        if (!isDrawing) {
            hideDrawPanel();
            return;
        }

        String hint = "";
        switch (mode) {
            case SELECT_START:
                hint = "请点击地图选择起点（资源点）";
                btnFinishDraw.setEnabled(false);
                break;
            case SELECT_END:
                hint = "起点已选择，请点击地图选择终点（资源点）";
                btnFinishDraw.setEnabled(false);
                break;
            case ADD_WAYPOINTS:
                hint = "终点已选择，点击地图添加中间点，或点击完成";
                btnFinishDraw.setEnabled(true);
                break;
        }
        tvDrawHint.setText(hint);
    }

    private void showDrawPanel() {
        drawCablePanel.setVisibility(View.VISIBLE);
        showToast("开始绘制光缆段");
    }

    private void hideDrawPanel() {
        drawCablePanel.setVisibility(View.GONE);
    }

    private void initLocation() {
        aMap.setLocationSource(locationHelper);
        locationHelper.initLocation();
    }

    private void performSearch() {
        String keyword = searchEditText.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            showToast("请输入搜索关键词");
            return;
        }

        cn.edu.ncepu.optical_manage.model.request.MapQueryRequest request = new cn.edu.ncepu.optical_manage.model.request.MapQueryRequest();
        request.setFilter("name=" + keyword);
        request.setLimit(10);

        ApiClient.getApiService().queryResources(request).enqueue(
                new retrofit2.Callback<cn.edu.ncepu.optical_manage.model.MapResponse>() {
            @Override
            public void onResponse(retrofit2.Call<cn.edu.ncepu.optical_manage.model.MapResponse> call,
                                   retrofit2.Response<cn.edu.ncepu.optical_manage.model.MapResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.List<cn.edu.ncepu.optical_manage.model.ResourcePoint> results = null;
                    try {
                        if (response.body().getResources() != null) {
                            results = new java.util.ArrayList<>();
                            for (cn.edu.ncepu.optical_manage.model.MapResponse.ResourceInfo info : response.body().getResources()) {
                                cn.edu.ncepu.optical_manage.model.ResourcePoint point = new cn.edu.ncepu.optical_manage.model.ResourcePoint();
                                point.setId(info.getId());
                                point.setType(info.getType());
                                point.setName(info.getName());
                                point.setLatitude(info.getLat());
                                point.setLongitude(info.getLng());
                                results.add(point);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (results != null && !results.isEmpty()) {
                        cn.edu.ncepu.optical_manage.model.ResourcePoint first = results.get(0);
                        LatLng latLng = new LatLng(first.getLatitude(), first.getLongitude());
                        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        showToast("找到 " + results.size() + " 个结果");
                    } else {
                        showToast("未找到匹配的资源点");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<cn.edu.ncepu.optical_manage.model.MapResponse> call,
                                  Throwable t) {
                showToast("搜索失败：" + t.getMessage());
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        MapViewModel.AddMode currentMode = viewModel.getCurrentAddMode().getValue();
        if (currentMode == null) currentMode = MapViewModel.AddMode.NONE;

        switch (currentMode) {
            case RESOURCE_POINT:
                showAddResourcePointDialog(latLng);
                break;
            case CABLE:
                cableDrawHelper.handleMapClick(latLng);
                updateDrawPanel(true, cableDrawHelper.getCurrentDrawMode());
                break;
            case NONE:
            default:
                // 点击地图空白处关闭信息框
                if (currentInfoWindowMarker != null && currentInfoWindowMarker.isInfoWindowShown()) {
                    currentInfoWindowMarker.hideInfoWindow();
                    currentInfoWindowMarker = null;
                }
                break;
        }
    }

    private void showAddResourcePointDialog(LatLng latLng) {
        AddResourcePointDialog dialog = new AddResourcePointDialog(
                this,
                point -> {
                    viewModel.createResourcePoint(point);
                    viewModel.setCurrentAddMode(MapViewModel.AddMode.NONE);
                },
                () -> viewModel.setCurrentAddMode(MapViewModel.AddMode.NONE)
        );
        dialog.show(latLng.latitude, latLng.longitude, ResourcePoint.ResourceType.POLE, "添加资源点");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object object = marker.getObject();
        if (object instanceof ResourcePoint) {
            ResourcePoint point = (ResourcePoint) object;
            // 如果点击的是同一个标记点，则关闭信息框
            if (currentInfoWindowMarker == marker && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                currentInfoWindowMarker = null;
            } else {
                // 关闭之前的信息框
                if (currentInfoWindowMarker != null && currentInfoWindowMarker.isInfoWindowShown()) {
                    currentInfoWindowMarker.hideInfoWindow();
                }
                // 显示新的信息框
                marker.showInfoWindow();
                currentInfoWindowMarker = marker;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Object object = marker.getObject();
        if (object instanceof ResourcePoint) {
            ResourcePoint point = (ResourcePoint) object;
            showResourcePointDetailDialog(point);
        }
    }

    private void showResourcePointDetailDialog(ResourcePoint point) {
        String[] options = {"编辑", "删除"};

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(point.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showEditResourcePointDialog(point);
                            break;
                        case 1:
                            showDeleteConfirmDialog(point);
                            break;
                    }
                })
                .show();
    }

    private void showEditResourcePointDialog(ResourcePoint point) {
        EditResourcePointDialog dialog = new EditResourcePointDialog(
                this,
                updatedPoint -> viewModel.updateResourcePoint(updatedPoint)
        );
        dialog.show(point);
    }

    private void showDeleteConfirmDialog(ResourcePoint point) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定要删除资源点 \"" + point.getName() + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> viewModel.deleteResourcePoint(point))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_map_type_normal) {
            if (aMap != null) {
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                showToast("已切换为标准地图");
            }
            return true;
        } else if (id == R.id.action_map_type_satellite) {
            if (aMap != null) {
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                showToast("已切换为卫星地图");
            }
            return true;
        } else if (id == R.id.action_map_type_night) {
            if (aMap != null) {
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);
                showToast("已切换为夜间模式");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        if (locationHelper != null) {
            locationHelper.destroyLocation();
        }
    }
}
