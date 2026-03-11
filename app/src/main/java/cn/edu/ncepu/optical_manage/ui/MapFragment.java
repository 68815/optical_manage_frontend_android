package cn.edu.ncepu.optical_manage.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.ncepu.optical_manage.R;
import cn.edu.ncepu.optical_manage.api.ApiClient;
import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.model.ApiResponse;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements LocationSource, AMapLocationListener, AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private MapView mapView;
    private AMap aMap;
    private AMapLocationClient locationClient;
    private LocationSource.OnLocationChangedListener locationChangedListener;

    private EditText searchEditText;
    private ImageButton searchButton;
    private LinearLayout drawCablePanel;
    private TextView tvDrawHint;
    private Button btnCancelDraw;
    private Button btnFinishDraw;

    private FloatingActionButton fabAddPole;
    private FloatingActionButton fabAddManhole;
    private FloatingActionButton fabAddBusinessHall;
    private FloatingActionButton fabDrawCable;
    private FloatingActionButton fabRefresh;

    private ApiService apiService;
    private List<ResourcePoint> resourcePoints = new ArrayList<>();
    private List<CableSegment> cableSegments = new ArrayList<>();
    private Map<Long, Marker> markerMap = new HashMap<>();
    private Map<Long, Polyline> polylineMap = new HashMap<>();

    private boolean isDrawCableMode = false;
    private List<LatLng> cableDrawPoints = new ArrayList<>();
    private Polyline drawingPolyline;
    private ResourcePoint selectedStartPoint = null;
    private ResourcePoint selectedEndPoint = null;

    private enum AddMode {
        NONE, POLE, MANHOLE, BUSINESS_HALL
    }

    private AddMode currentAddMode = AddMode.NONE;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getApiService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        searchEditText = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);
        drawCablePanel = view.findViewById(R.id.drawCablePanel);
        tvDrawHint = view.findViewById(R.id.tvDrawHint);
        btnCancelDraw = view.findViewById(R.id.btnCancelDraw);
        btnFinishDraw = view.findViewById(R.id.btnFinishDraw);

        fabAddPole = view.findViewById(R.id.fabAddPole);
        fabAddManhole = view.findViewById(R.id.fabAddManhole);
        fabAddBusinessHall = view.findViewById(R.id.fabAddBusinessHall);
        fabDrawCable = view.findViewById(R.id.fabDrawCable);
        fabRefresh = view.findViewById(R.id.fabRefresh);

        initMap();
        initListeners();
        checkPermissions();

        return view;
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setLocationSource(this);
            aMap.getUiSettings().setMyLocationButtonEnabled(true);
            aMap.setMyLocationEnabled(true);
            aMap.setOnMapClickListener(this);
            aMap.setOnMarkerClickListener(this);
            aMap.setOnInfoWindowClickListener(this);
            aMap.setInfoWindowAdapter(new ResourcePointInfoWindowAdapter());
        }
    }

    private void initListeners() {
        searchButton.setOnClickListener(v -> performSearch());

        fabAddPole.setOnClickListener(v -> {
            currentAddMode = AddMode.POLE;
            showToast("点击地图添加电杆");
        });

        fabAddManhole.setOnClickListener(v -> {
            currentAddMode = AddMode.MANHOLE;
            showToast("点击地图添加人井");
        });

        fabAddBusinessHall.setOnClickListener(v -> {
            currentAddMode = AddMode.BUSINESS_HALL;
            showToast("点击地图添加营业厅");
        });

        fabDrawCable.setOnClickListener(v -> {
            startDrawCableMode();
        });

        fabRefresh.setOnClickListener(v -> {
            loadData();
        });

        btnCancelDraw.setOnClickListener(v -> {
            cancelDrawCableMode();
        });

        btnFinishDraw.setOnClickListener(v -> {
            finishDrawCable();
        });
    }

    private void checkPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.isEmpty()) {
            initLocation();
            loadData();
        } else {
            requestPermissions(missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {

            List<String> stillMissing = new ArrayList<>();
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    stillMissing.add(permission);
                }
            }
            if (stillMissing.isEmpty()) {
                initLocation();
                loadData();
            } else {
                showToast("需要\"精确定位\"权限才能使用完整功能");
            }
        }
    }

    private void initLocation() {
        try {
            locationClient = new AMapLocationClient(requireContext());
            locationClient.setLocationListener(this);
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(false);
            option.setInterval(2000);
            locationClient.setLocationOption(option);
            locationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        loadResourcePoints();
        loadCableSegments();
    }

    private void loadResourcePoints() {
        apiService.getAllResourcePoints().enqueue(new Callback<ApiResponse<List<ResourcePoint>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ResourcePoint>>> call, Response<ApiResponse<List<ResourcePoint>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    resourcePoints = response.body().getData();
                    updateMapMarkers();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ResourcePoint>>> call, Throwable t) {
                showToast("加载资源点失败: " + t.getMessage());
            }
        });
    }

    private void loadCableSegments() {
        apiService.getAllCableSegments().enqueue(new Callback<ApiResponse<List<CableSegment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CableSegment>>> call, Response<ApiResponse<List<CableSegment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cableSegments = response.body().getData();
                    updateMapPolylines();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CableSegment>>> call, Throwable t) {
                showToast("加载光缆段失败: " + t.getMessage());
            }
        });
    }

    private void updateMapMarkers() {
        for (Marker marker : markerMap.values()) {
            marker.remove();
        }
        markerMap.clear();

        for (ResourcePoint point : resourcePoints) {
            addMarkerForResourcePoint(point);
        }
    }

    private void addMarkerForResourcePoint(ResourcePoint point) {
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        float markerColor = getMarkerColor(point.getType());

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(point.getName())
                .snippet("类型: " + point.getType())
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

        Marker marker = aMap.addMarker(markerOptions);
        marker.setObject(point);
        markerMap.put(point.getId(), marker);
    }

    private float getMarkerColor(String type) {
        if (type == null) return BitmapDescriptorFactory.HUE_RED;

        switch (type) {
            case ResourcePoint.TYPE_POLE:
                return BitmapDescriptorFactory.HUE_BLUE;
            case ResourcePoint.TYPE_MANHOLE:
                return BitmapDescriptorFactory.HUE_GREEN;
            case ResourcePoint.TYPE_BUSINESS_HALL:
                return BitmapDescriptorFactory.HUE_ORANGE;
            default:
                return BitmapDescriptorFactory.HUE_RED;
        }
    }

    private void updateMapPolylines() {
        for (Polyline polyline : polylineMap.values()) {
            polyline.remove();
        }
        polylineMap.clear();

        for (CableSegment segment : cableSegments) {
            addPolylineForCableSegment(segment);
        }
    }

    private void addPolylineForCableSegment(CableSegment segment) {
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

    private void performSearch() {
        String keyword = searchEditText.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            showToast("请输入搜索关键词");
            return;
        }

        apiService.searchResourcePoints(keyword).enqueue(new Callback<ApiResponse<List<ResourcePoint>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ResourcePoint>>> call, Response<ApiResponse<List<ResourcePoint>>> response) {
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
            public void onFailure(Call<ApiResponse<List<ResourcePoint>>> call, Throwable t) {
                showToast("搜索失败: " + t.getMessage());
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (currentAddMode != AddMode.NONE) {
            showAddResourcePointDialog(latLng);
        } else if (isDrawCableMode) {
            handleDrawCableClick(latLng);
        }
    }

    private void showAddResourcePointDialog(LatLng latLng) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_resource_point, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etAddress);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);

        String[] types = {ResourcePoint.TYPE_POLE, ResourcePoint.TYPE_MANHOLE, ResourcePoint.TYPE_BUSINESS_HALL};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        switch (currentAddMode) {
            case POLE:
                spinnerType.setSelection(0);
                tvDialogTitle.setText("添加电杆");
                break;
            case MANHOLE:
                spinnerType.setSelection(1);
                tvDialogTitle.setText("添加人井");
                break;
            case BUSINESS_HALL:
                spinnerType.setSelection(2);
                tvDialogTitle.setText("添加营业厅");
                break;
        }

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> {
            currentAddMode = AddMode.NONE;
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String type = (String) spinnerType.getSelectedItem();
            String address = etAddress.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                showToast("请输入名称");
                return;
            }

            ResourcePoint point = new ResourcePoint(name, type, latLng.latitude, latLng.longitude);
            point.setAddress(address);
            point.setDescription(description);

            createResourcePoint(point);
            currentAddMode = AddMode.NONE;
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createResourcePoint(ResourcePoint point) {
        apiService.createResourcePoint(point).enqueue(new Callback<ApiResponse<ResourcePoint>>() {
            @Override
            public void onResponse(Call<ApiResponse<ResourcePoint>> call, Response<ApiResponse<ResourcePoint>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ResourcePoint created = response.body().getData();
                    resourcePoints.add(created);
                    addMarkerForResourcePoint(created);
                    showToast("添加成功");
                } else {
                    showToast("添加失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ResourcePoint>> call, Throwable t) {
                showToast("添加失败: " + t.getMessage());
            }
        });
    }

    private void startDrawCableMode() {
        isDrawCableMode = true;
        cableDrawPoints.clear();
        drawCablePanel.setVisibility(View.VISIBLE);
        tvDrawHint.setText("点击地图选择光缆段路径点");
        btnFinishDraw.setEnabled(false);
        showToast("开始绘制光缆段");
    }

    private void cancelDrawCableMode() {
        isDrawCableMode = false;
        cableDrawPoints.clear();
        drawCablePanel.setVisibility(View.GONE);
        if (drawingPolyline != null) {
            drawingPolyline.remove();
            drawingPolyline = null;
        }
    }

    private void handleDrawCableClick(LatLng latLng) {
        cableDrawPoints.add(latLng);

        if (drawingPolyline != null) {
            drawingPolyline.remove();
        }

        if (cableDrawPoints.size() >= 2) {
            PolylineOptions options = new PolylineOptions()
                    .addAll(cableDrawPoints)
                    .width(10)
                    .color(Color.RED);
            drawingPolyline = aMap.addPolyline(options);
            btnFinishDraw.setEnabled(true);
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        aMap.addMarker(markerOptions);

        tvDrawHint.setText("已选择 " + cableDrawPoints.size() + " 个点，继续点击或点击完成");
    }

    private void finishDrawCable() {
        if (cableDrawPoints.size() < 2) {
            showToast("至少需要选择2个点");
            return;
        }

        showCableSegmentDialog();
    }

    private void showCableSegmentDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cable_segment, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        Spinner spinnerStartPoint = dialogView.findViewById(R.id.spinnerStartPoint);
        Spinner spinnerEndPoint = dialogView.findViewById(R.id.spinnerEndPoint);
        TextInputEditText etFiberCount = dialogView.findViewById(R.id.etFiberCount);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);

        List<String> pointNames = new ArrayList<>();
        pointNames.add("请选择");
        for (ResourcePoint point : resourcePoints) {
            pointNames.add(point.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, pointNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartPoint.setAdapter(adapter);
        spinnerEndPoint.setAdapter(adapter);

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> {
            cancelDrawCableMode();
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            int startIndex = spinnerStartPoint.getSelectedItemPosition();
            int endIndex = spinnerEndPoint.getSelectedItemPosition();
            String fiberCountStr = etFiberCount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                showToast("请输入名称");
                return;
            }

            if (startIndex == 0 || endIndex == 0) {
                showToast("请选择起点和终点");
                return;
            }

            CableSegment segment = new CableSegment();
            segment.setName(name);
            segment.setStartPointId(resourcePoints.get(startIndex - 1).getId());
            segment.setEndPointId(resourcePoints.get(endIndex - 1).getId());
            segment.setFiberCount(TextUtils.isEmpty(fiberCountStr) ? 0 : Integer.parseInt(fiberCountStr));
            segment.setDescription(description);

            List<CableSegment.Point> points = new ArrayList<>();
            for (LatLng latLng : cableDrawPoints) {
                points.add(new CableSegment.Point(latLng.latitude, latLng.longitude));
            }
            segment.setPoints(points);

            createCableSegment(segment);
            cancelDrawCableMode();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createCableSegment(CableSegment segment) {
        apiService.createCableSegment(segment).enqueue(new Callback<ApiResponse<CableSegment>>() {
            @Override
            public void onResponse(Call<ApiResponse<CableSegment>> call, Response<ApiResponse<CableSegment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CableSegment created = response.body().getData();
                    cableSegments.add(created);
                    addPolylineForCableSegment(created);
                    showToast("光缆段添加成功");
                } else {
                    showToast("光缆段添加失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CableSegment>> call, Throwable t) {
                showToast("光缆段添加失败: " + t.getMessage());
            }
        });
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

        new MaterialAlertDialogBuilder(requireContext())
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
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_resource_point, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etAddress);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);

        tvDialogTitle.setText("编辑资源点");
        etName.setText(point.getName());
        etAddress.setText(point.getAddress());
        etDescription.setText(point.getDescription());

        String[] types = {ResourcePoint.TYPE_POLE, ResourcePoint.TYPE_MANHOLE, ResourcePoint.TYPE_BUSINESS_HALL};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(point.getType())) {
                spinnerType.setSelection(i);
                break;
            }
        }

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String type = (String) spinnerType.getSelectedItem();
            String address = etAddress.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                showToast("请输入名称");
                return;
            }

            point.setName(name);
            point.setType(type);
            point.setAddress(address);
            point.setDescription(description);

            updateResourcePoint(point);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateResourcePoint(ResourcePoint point) {
        apiService.updateResourcePoint(point.getId(), point).enqueue(new Callback<ApiResponse<ResourcePoint>>() {
            @Override
            public void onResponse(Call<ApiResponse<ResourcePoint>> call, Response<ApiResponse<ResourcePoint>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Marker marker = markerMap.get(point.getId());
                    if (marker != null) {
                        marker.remove();
                    }
                    addMarkerForResourcePoint(point);
                    showToast("更新成功");
                } else {
                    showToast("更新失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ResourcePoint>> call, Throwable t) {
                showToast("更新失败: " + t.getMessage());
            }
        });
    }

    private void showDeleteConfirmDialog(ResourcePoint point) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定要删除资源点 \"" + point.getName() + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteResourcePoint(point))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteResourcePoint(ResourcePoint point) {
        apiService.deleteResourcePoint(point.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    resourcePoints.remove(point);
                    Marker marker = markerMap.remove(point.getId());
                    if (marker != null) {
                        marker.remove();
                    }
                    showToast("删除成功");
                } else {
                    showToast("删除失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showToast("删除失败: " + t.getMessage());
            }
        });
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (locationChangedListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                locationChangedListener.onLocationChanged(aMapLocation);
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationChangedListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        locationChangedListener = null;
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationClient = null;
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
    }

    private class ResourcePointInfoWindowAdapter implements AMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            View view = LayoutInflater.from(requireContext()).inflate(R.layout.info_window_resource_point, null);
            TextView tvName = view.findViewById(R.id.tvName);
            TextView tvType = view.findViewById(R.id.tvType);
            TextView tvAddress = view.findViewById(R.id.tvAddress);

            Object object = marker.getObject();
            if (object instanceof ResourcePoint) {
                ResourcePoint point = (ResourcePoint) object;
                tvName.setText(point.getName());
                tvType.setText("类型: " + point.getType());
                tvAddress.setText("地址: " + (point.getAddress() != null ? point.getAddress() : "无"));
            }

            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
