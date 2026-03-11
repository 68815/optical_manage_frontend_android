package cn.edu.ncepu.optical_manage.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;

import cn.edu.ncepu.optical_manage.R;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;

public class ResourcePointInfoWindowAdapter implements AMap.InfoWindowAdapter {

    private final LayoutInflater inflater;

    public ResourcePointInfoWindowAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = inflater.inflate(R.layout.info_window_resource_point, null);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvType = view.findViewById(R.id.tvType);
        TextView tvAddress = view.findViewById(R.id.tvAddress);

        Object object = marker.getObject();
        if (object instanceof ResourcePoint) {
            ResourcePoint point = (ResourcePoint) object;
            tvName.setText(point.getName());
            tvType.setText("类型：" + point.getType());
            tvAddress.setText("地址：" + (point.getAddress() != null ? point.getAddress() : "无"));
        }

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
