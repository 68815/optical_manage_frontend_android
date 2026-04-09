package cn.edu.ncepu.optical_manage.ui.helper;

import android.content.Context;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import timber.log.Timber;

public class GeocodeHelper {

    private final GeocodeSearch geocodeSearch;

    public interface OnAddressCallback {
        void onAddressResult(String address);
        void onError(String error);
    }

    public GeocodeHelper(Context context) {
        try {
            geocodeSearch = new GeocodeSearch(context);
        } catch (AMapException e) {
            Timber.e(e, "初始化GeocodeSearch失败");
            throw new RuntimeException("初始化地理编码服务失败", e);
        }
    }

    /**
     * 根据坐标获取地址（逆地理编码）
     * @param latitude 纬度
     * @param longitude 经度
     * @param callback 回调
     */
    public void getAddressFromLocation(double latitude, double longitude, OnAddressCallback callback) {
        Timber.d("开始逆地理编码，坐标: lat=%f, lng=%f", latitude, longitude);

        LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
        // radius: 逆地理编码区域半径，单位：米
        // latLonType: 坐标类型，GeocodeSearch.AMAP 表示高德坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);

        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
                Timber.d("逆地理编码返回，错误码: %d", rCode);
                if (rCode == AMapException.CODE_AMAP_SUCCESS && result != null && result.getRegeocodeAddress() != null) {
                    String address = result.getRegeocodeAddress().getFormatAddress();
                    Timber.d("逆地理编码成功: %s", address);
                    callback.onAddressResult(address);
                } else {
                    String error = "逆地理编码失败，错误码: " + rCode;
                    Timber.e(error);
                    callback.onError(error);
                }
            }

            @Override
            public void onGeocodeSearched(com.amap.api.services.geocoder.GeocodeResult geocodeResult, int i) {
                // 正向地理编码，这里不需要
            }
        });

        geocodeSearch.getFromLocationAsyn(query);
    }
}
