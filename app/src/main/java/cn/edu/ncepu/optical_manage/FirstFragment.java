package cn.edu.ncepu.optical_manage;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import cn.edu.ncepu.optical_manage.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private MapView mapView;
    private AMap aMap;

    private boolean isAddMode = false; // 是否为添加光缆模式
    private final List<LatLng> points = new ArrayList<>();
    private Polyline currentPolyline;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);

        // 恢复点（如果有的话）——保证在 mapView 创建前恢复数据以便 later draw
        if (savedInstanceState != null) {
            restorePoints(savedInstanceState);
        }

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = binding.map;
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        setupMapClickListener();
        setupButtonClickListeners();

        // 如果恢复了点，重绘一次（map 已经初始化）
        if (points.size() > 1) {
            drawFiberLine();
        }
    }

    private void setupButtonClickListeners() {
        // 跳转按钮
        binding.buttonFirst.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        // 添加光缆按钮
        binding.buttonAddFiber.setOnClickListener(v -> {
            isAddMode = !isAddMode;
            if (isAddMode) {
                binding.buttonAddFiber.setText("完成绘制");
                points.clear(); // 开始新的绘制前清空旧的点
                if (currentPolyline != null) {
                    currentPolyline.remove(); // 移除地图上旧的线
                }
            } else {
                binding.buttonAddFiber.setText("添加光缆");
                // 此处可以添加保存光缆逻辑
            }
        });
    }

    private void setupMapClickListener() {
        aMap.setOnMapClickListener(latLng -> {
            if (isAddMode) {
                points.add(latLng);
                drawFiberLine();
            }
        });
    }

    private void drawFiberLine() {
        if (points.size() > 1) {
            if (currentPolyline != null) {
                currentPolyline.remove();
            }
            // 使用 match-constraint（0dp）和 ConstraintLayout 保证地图区域自适应。这里让折线宽度随屏幕密度适配。
            float density = getResources().getDisplayMetrics().density;
            float widthPx = 10f * density; // 以 10dp 为基准

            PolylineOptions options = new PolylineOptions()
                    .addAll(points)
                    .width(widthPx)
                    .color(Color.RED);
            currentPolyline = aMap.addPolyline(options);
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
        // 保存绘制点，保证配置变更/进程恢复后能还原
        savePoints(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        binding = null;
    }

    // 将点列表序列化为 double[] (lat,lng,lat,lng...)
    private void savePoints(@NonNull Bundle outState) {
        if (points.isEmpty()) return;
        double[] arr = new double[points.size() * 2];
        for (int i = 0; i < points.size(); i++) {
            LatLng p = points.get(i);
            arr[i * 2] = p.latitude;
            arr[i * 2 + 1] = p.longitude;
        }
        outState.putDoubleArray(getString(R.string.key_points), arr);
    }

    private void restorePoints(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        // 从 Bundle 使用和保存时相同的字符串 key 恢复
        double[] arr = savedInstanceState.getDoubleArray(getString(R.string.key_points));
        if (arr == null || arr.length < 2) return;
        points.clear();
        for (int i = 0; i < arr.length; i += 2) {
            double lat = arr[i];
            double lng = (i + 1 < arr.length) ? arr[i + 1] : 0.0;
            points.add(new LatLng(lat, lng));
        }
    }
}
