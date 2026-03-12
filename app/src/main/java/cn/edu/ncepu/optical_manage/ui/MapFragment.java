package cn.edu.ncepu.optical_manage.ui;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import cn.edu.ncepu.optical_manage.R;
import cn.edu.ncepu.optical_manage.api.ApiClient;
import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.manager.CableSegmentManager;
import cn.edu.ncepu.optical_manage.manager.ResourcePointManager;
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

    private ApiService apiService;
    private ResourcePointManager resourcePointManager;
    private CableSegmentManager cableSegmentManager;
    private LocationHelper locationHelper;
    private CableDrawHelper cableDrawHelper;

    private AddMode currentAddMode = AddMode.NONE;
    private List<ResourcePoint> resourcePoints = new ArrayList<>();

    private enum AddMode {
        NONE,
        RESOURCE_POINT,
        CABLE
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getApiService();
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

    private void initMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        
        if (aMap == null) {
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
        resourcePointManager = new ResourcePointManager(apiService, aMap, requireContext());
        cableSegmentManager = new CableSegmentManager(apiService, aMap, requireContext());

        resourcePointManager.setOnResourcePointChangedListener(() -> loadData());
        cableSegmentManager.setOnCableSegmentChangedListener(() -> {});
    }

    private void initLocationHelper() {
        locationHelper = new LocationHelper(requireContext());
    }

    private void initCableDrawHelper() {
        cableDrawHelper = new CableDrawHelper(aMap, this, cableSegmentManager, 
                new CableDrawHelper.OnCableDrawListener() {
            @Override
            public void onDrawModeChanged(boolean isDrawing, CableDrawHelper.DrawMode mode) {
                updateDrawPanel(isDrawing, mode);
            }

            @Override
            public void onDrawCancelled() {
                hideDrawPanel();
                currentAddMode = AddMode.NONE;
            }

            @Override
            public void onDrawFinished() {
                hideDrawPanel();
                loadData();
                currentAddMode = AddMode.NONE;
            }
        });

        aMap.setOnMarkerDragListener(cableDrawHelper);
    }

    private void initListeners() {
        searchButton.setOnClickListener(v -> performSearch());

        fabAddResourcePoint.setOnClickListener(v -> {
            currentAddMode = AddMode.RESOURCE_POINT;
            showToast("点击地图添加资源点");
        });

        fabDrawCable.setOnClickListener(v -> {
            currentAddMode = AddMode.CABLE;
            cableDrawHelper.setResourcePoints(resourcePoints);
            cableDrawHelper.startDrawCableMode();
            showDrawPanel();
        });

        fabRefresh.setOnClickListener(v -> loadData());

        btnCancelDraw.setOnClickListener(v -> {
            cableDrawHelper.cancelDrawCableMode();
            hideDrawPanel();
            currentAddMode = AddMode.NONE;
        });

        btnFinishDraw.setOnClickListener(v -> cableDrawHelper.finishDrawCable());
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

    private void checkPermissions() {
        List<String> missingPermissions = PermissionUtils.getMissingPermissions(requireContext(), REQUIRED_PERMISSIONS);

        if (missingPermissions.isEmpty()) {
            initLocation();
            loadData();
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
                loadData();
            } else {
                showToast("需要定位权限才能使用完整功能");
            }
        }
    }

    private void initLocation() {
        aMap.setLocationSource(locationHelper);
        locationHelper.initLocation();
    }

    private void loadData() {
        resourcePointManager.loadAllResourcePoints();
        cableSegmentManager.loadAllCableSegments();
        
        apiService.getAllResourcePoints().enqueue(
                new retrofit2.Callback<cn.edu.ncepu.optical_manage.model.ApiResponse<List<ResourcePoint>>>() {
            @Override
            public void onResponse(retrofit2.Call<cn.edu.ncepu.optical_manage.model.ApiResponse<List<ResourcePoint>>> call,
                                   retrofit2.Response<cn.edu.ncepu.optical_manage.model.ApiResponse<List<ResourcePoint>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    resourcePoints = response.body().getData();
                    cableDrawHelper.setResourcePoints(resourcePoints);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<cn.edu.ncepu.optical_manage.model.ApiResponse<List<ResourcePoint>>> call,
                                  Throwable t) {
            }
        });
    }

    private void performSearch() {
        String keyword = searchEditText.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            showToast("请输入搜索关键词");
            return;
        }

        apiService.searchResourcePoints(keyword).enqueue(
                new retrofit2.Callback<cn.edu.ncepu.optical_manage.model.ApiResponse<List<ResourcePoint>>>() {
            @Override
            public void onResponse(retrofit2.Call<cn.edu.ncepu.optical_manage.model.ApiResponse<List<ResourcePoint>>> call,
                                   retrofit2.Response<cn.edu.ncepu.optical_manage.model.ApiResponse<List<ResourcePoint>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ResourcePoint> results = response.body().getData();
                    if (results != null && !results.isEmpty()) {
                        ResourcePoint first = results.get(0);
                        LatLng latLng = new LatLng(first.getLatitude(), first.getLongitude());
                        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        showToast("找到 " + results.size() + " 个结果");
                    } else {
                        showToast("未找到匹配的资源点");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<cn.edu.ncepu.optical_manage.model.ApiResponse<List<ResourcePoint>>> call,
                                  Throwable t) {
                showToast("搜索失败：" + t.getMessage());
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        switch (currentAddMode) {
            case RESOURCE_POINT:
                showAddResourcePointDialog(latLng);
                break;
            case CABLE:
                cableDrawHelper.handleMapClick(latLng);
                updateDrawPanel(true, cableDrawHelper.getCurrentDrawMode());
                break;
            case NONE:
            default:
                break;
        }
    }

    private void showAddResourcePointDialog(LatLng latLng) {
        AddResourcePointDialog dialog = new AddResourcePointDialog(
                this,
                point -> {
                    resourcePointManager.createResourcePoint(point);
                    currentAddMode = AddMode.NONE;
                },
                () -> currentAddMode = AddMode.NONE
        );
        dialog.show(latLng.latitude, latLng.longitude, ResourcePoint.ResourceType.POLE, "添加资源点");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object object = marker.getObject();
        if (object instanceof ResourcePoint) {
            ResourcePoint point = (ResourcePoint) object;
            marker.showInfoWindow();
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
                updatedPoint -> resourcePointManager.updateResourcePoint(updatedPoint)
        );
        dialog.show(point);
    }

    private void showDeleteConfirmDialog(ResourcePoint point) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定要删除资源点 \"" + point.getName() + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> resourcePointManager.deleteResourcePoint(point))
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
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        if (locationHelper != null) {
            locationHelper.destroyLocation();
        }
    }
}
