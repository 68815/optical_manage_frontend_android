package cn.edu.ncepu.optical_manage.ui.helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import cn.edu.ncepu.optical_manage.R;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;

public class CableDrawHelper implements AMap.OnMarkerDragListener {

    public enum DrawMode {
        SELECT_START,
        SELECT_END,
        ADD_WAYPOINTS
    }

    public interface OnCableDrawListener {
        void onDrawModeChanged(boolean isDrawing, DrawMode mode);
        void onDrawCancelled();
        void onDrawFinished();
        void onCableSegmentCreated(CableSegment segment);
    }

    private final AMap aMap;
    private final androidx.fragment.app.Fragment fragment;
    private final OnCableDrawListener listener;

    private boolean isDrawCableMode = false;
    private DrawMode currentDrawMode = DrawMode.SELECT_START;

    private LatLng startPoint = null;
    private LatLng endPoint = null;
    private final List<LatLng> waypoints = new ArrayList<>();
    private Polyline previewPolyline = null;
    private final List<Marker> waypointMarkers = new ArrayList<>();

    private List<ResourcePoint> resourcePoints = new ArrayList<>();

    public CableDrawHelper(AMap aMap, 
                           androidx.fragment.app.Fragment fragment,
                           OnCableDrawListener listener) {
        this.aMap = aMap;
        this.fragment = fragment;
        this.listener = listener;
    }

    public void setResourcePoints(List<ResourcePoint> points) {
        this.resourcePoints = points;
    }

    public boolean isDrawCableMode() {
        return isDrawCableMode;
    }

    public DrawMode getCurrentDrawMode() {
        return currentDrawMode;
    }

    public void startDrawCableMode() {
        isDrawCableMode = true;
        currentDrawMode = DrawMode.SELECT_START;
        startPoint = null;
        endPoint = null;
        waypoints.clear();
        
        clearDrawElements();
        
        if (listener != null) {
            listener.onDrawModeChanged(true, currentDrawMode);
        }
    }

    public void cancelDrawCableMode() {
        isDrawCableMode = false;
        waypoints.clear();
        clearDrawElements();
        
        if (listener != null) {
            listener.onDrawCancelled();
        }
    }

    public void handleMapClick(LatLng latLng) {
        if (!isDrawCableMode) return;

        switch (currentDrawMode) {
            case SELECT_START:
                handleSelectStart(latLng);
                break;
            case SELECT_END:
                handleSelectEnd(latLng);
                break;
            case ADD_WAYPOINTS:
                handleAddWaypoint(latLng);
                break;
        }
    }

    private void handleSelectStart(LatLng latLng) {
        startPoint = latLng;
        
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("起点")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .draggable(false)
                .zIndex(10.0f);
        
        Marker marker = aMap.addMarker(options);
        waypointMarkers.add(marker);
        
        currentDrawMode = DrawMode.SELECT_END;
        if (listener != null) {
            listener.onDrawModeChanged(true, currentDrawMode);
        }
    }

    private void handleSelectEnd(LatLng latLng) {
        endPoint = latLng;
        
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("终点")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(false)
                .zIndex(10.0f);
        
        Marker marker = aMap.addMarker(options);
        waypointMarkers.add(marker);
        
        drawPreviewLine();
        
        currentDrawMode = DrawMode.ADD_WAYPOINTS;
        if (listener != null) {
            listener.onDrawModeChanged(true, currentDrawMode);
        }
    }

    private void handleAddWaypoint(LatLng latLng) {
        waypoints.add(latLng);
        
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("中间点 " + waypoints.size())
                .icon(createWaypointIcon(waypoints.size()))
                .draggable(true)
                .zIndex(15.0f);
        
        Marker marker = aMap.addMarker(options);
        marker.setObject(waypoints.size() - 1);
        waypointMarkers.add(marker);
        
        drawPreviewLine();
    }

    private void drawPreviewLine() {
        if (startPoint == null || endPoint == null) {
            return;
        }
        
        if (previewPolyline != null) {
            previewPolyline.remove();
        }
        
        List<LatLng> pathPoints = new ArrayList<>();
        pathPoints.add(startPoint);
        pathPoints.addAll(waypoints);
        pathPoints.add(endPoint);
        
        PolylineOptions options = new PolylineOptions()
                .addAll(pathPoints)
                .width(8)
                .color(Color.parseColor("#999999"))
                .setDottedLine(true)
                .zIndex(5.0f);
        
        previewPolyline = aMap.addPolyline(options);
    }

    private BitmapDescriptor createWaypointIcon(int index) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.BLUE);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
        
        String text = String.valueOf(index + 1);
        android.graphics.Rect bounds = new android.graphics.Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        
        int size = 60;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        canvas.drawCircle(size / 2, size / 2, size / 2, bgPaint);
        canvas.drawText(text, size / 2, size / 2 + bounds.height() / 2, paint);
        
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void finishDrawCable() {
        if (startPoint == null || endPoint == null) {
            showToast("请先选择起点和终点");
            return;
        }
        
        showCableSegmentDialog();
    }

    private void showCableSegmentDialog() {
        View dialogView = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.dialog_cable_segment, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        Spinner spinnerStartPoint = dialogView.findViewById(R.id.spinnerStartPoint);
        Spinner spinnerEndPoint = dialogView.findViewById(R.id.spinnerEndPoint);
        TextInputEditText etFiberCount = dialogView.findViewById(R.id.etFiberCount);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        
        TextView tvPathInfo = new TextView(fragment.requireContext());
        tvPathInfo.setText("路径包含 " + (waypoints.size() + 2) + " 个点，" + waypoints.size() + " 个中间点");
        tvPathInfo.setPadding(16, 8, 16, 8);
        tvPathInfo.setTextSize(14);
        tvPathInfo.setTextColor(Color.parseColor("#666666"));

        List<String> pointNames = new ArrayList<>();
        pointNames.add("请选择");
        for (ResourcePoint point : resourcePoints) {
            pointNames.add(point.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.requireContext(),
                android.R.layout.simple_spinner_item, pointNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartPoint.setAdapter(adapter);
        spinnerEndPoint.setAdapter(adapter);

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        LinearLayout dialogLayout = (LinearLayout) dialogView;
        dialogLayout.addView(tvPathInfo, 1);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.requireContext())
                .setView(dialogView);
        AlertDialog dialog = builder.create();

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
            segment.setProps(description);

            List<CableSegment.Point> points = new ArrayList<>();
            points.add(new CableSegment.Point(startPoint.latitude, startPoint.longitude));
            for (LatLng waypoint : waypoints) {
                points.add(new CableSegment.Point(waypoint.latitude, waypoint.longitude));
            }
            points.add(new CableSegment.Point(endPoint.latitude, endPoint.longitude));
            segment.setPoints(points);

            // Notify listener to create cable segment through ViewModel
            if (listener != null) {
                listener.onCableSegmentCreated(segment);
            }
            cancelDrawCableMode();
            dialog.dismiss();
            
            if (listener != null) {
                listener.onDrawFinished();
            }
        });

        dialog.show();
    }

    private void clearDrawElements() {
        if (previewPolyline != null) {
            previewPolyline.remove();
            previewPolyline = null;
        }
        
        for (Marker marker : waypointMarkers) {
            marker.remove();
        }
        waypointMarkers.clear();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Object object = marker.getObject();
        if (object instanceof Integer) {
            int index = (Integer) object;
            showToast("开始拖拽中间点 " + (index + 1));
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Object object = marker.getObject();
        if (object instanceof Integer) {
            int index = (Integer) object;
            waypoints.set(index, marker.getPosition());
            drawPreviewLine();
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Object object = marker.getObject();
        if (object instanceof Integer) {
            int index = (Integer) object;
            waypoints.set(index, marker.getPosition());
            drawPreviewLine();
            showToast("中间点 " + (index + 1) + " 已移动");
        }
    }

    private void showToast(String message) {
        if (fragment.getContext() != null) {
            android.widget.Toast.makeText(fragment.getContext(), message,
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
