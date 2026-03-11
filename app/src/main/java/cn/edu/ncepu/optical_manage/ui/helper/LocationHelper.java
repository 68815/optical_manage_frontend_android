package cn.edu.ncepu.optical_manage.ui.helper;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;

public class LocationHelper implements LocationSource, AMapLocationListener {

    public interface OnLocationReadyListener {
        void onLocationReady(AMapLocation location);
        void onLocationError(int errorCode, String errorMsg);
    }

    private AMapLocationClient locationClient;
    private OnLocationChangedListener locationChangedListener;
    private final Context context;
    private OnLocationReadyListener readyListener;

    public LocationHelper(Context context) {
        this.context = context;
    }

    public void setOnLocationReadyListener(OnLocationReadyListener listener) {
        this.readyListener = listener;
    }

    public void initLocation() {
        try {
            locationClient = new AMapLocationClient(context);
            locationClient.setLocationListener(this);
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(false);
            option.setInterval(2000);
            locationClient.setLocationOption(option);
            locationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
            if (readyListener != null) {
                readyListener.onLocationError(-1, e.getMessage());
            }
        }
    }

    public void stopLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
        }
    }

    public void destroyLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
            locationClient = null;
        }
        locationChangedListener = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (locationChangedListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                locationChangedListener.onLocationChanged(aMapLocation);
                if (readyListener != null) {
                    readyListener.onLocationReady(aMapLocation);
                }
            } else {
                if (readyListener != null) {
                    readyListener.onLocationError(aMapLocation.getErrorCode(), aMapLocation.getErrorInfo());
                }
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
        destroyLocation();
    }
}
