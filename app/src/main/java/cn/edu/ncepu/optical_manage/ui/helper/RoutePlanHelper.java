package cn.edu.ncepu.optical_manage.ui.helper;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 路径规划帮助类，用于获取两点之间的实际路径（用于光缆路由）
 */
public class RoutePlanHelper implements RouteSearch.OnRouteSearchListener {

    public interface OnRoutePlanListener {
        /**
         * 路径规划成功
         * @param pathPoints 路径上的所有点（沿道路）
         * @param distance 总距离（米）
         * @param duration 预计时间（秒）
         */
        void onRoutePlanSuccess(List<LatLng> pathPoints, float distance, long duration);

        /**
         * 路径规划失败
         * @param errorCode 错误码
         * @param errorMessage 错误信息
         */
        void onRoutePlanFailed(int errorCode, String errorMessage);
    }

    private RouteSearch routeSearch;
    private OnRoutePlanListener listener;

    public RoutePlanHelper() {
        try {
            routeSearch = new RouteSearch(null);
            routeSearch.setRouteSearchListener(this);
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置路径规划回调监听
     */
    public void setOnRoutePlanListener(OnRoutePlanListener listener) {
        this.listener = listener;
    }

    /**
     * 规划驾车路径（适用于沿道路敷设的光缆）
     * @param start 起点
     * @param end 终点
     */
    public void planDriveRoute(LatLng start, LatLng end) {
        if (routeSearch == null) {
            if (listener != null) {
                listener.onRoutePlanFailed(-1, "RouteSearch 初始化失败");
            }
            return;
        }

        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                new LatLonPoint(start.latitude, start.longitude),
                new LatLonPoint(end.latitude, end.longitude)
        );

        // 驾车路径规划，不带策略（默认）
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(
                fromAndTo,
                RouteSearch.DrivingDefault,
                null,
                null,
                ""
        );

        routeSearch.calculateDriveRouteAsyn(query);
    }

    /**
     * 规划步行路径（适用于人行道/小巷敷设的光缆）
     * @param start 起点
     * @param end 终点
     */
    public void planWalkRoute(LatLng start, LatLng end) {
        if (routeSearch == null) {
            if (listener != null) {
                listener.onRoutePlanFailed(-1, "RouteSearch 初始化失败");
            }
            return;
        }

        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                new LatLonPoint(start.latitude, start.longitude),
                new LatLonPoint(end.latitude, end.longitude)
        );

        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo);
        routeSearch.calculateWalkRouteAsyn(query);
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int errorCode) {
        // 不需要公交路径
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        if (errorCode != AMapException.CODE_AMAP_SUCCESS || result == null) {
            if (listener != null) {
                listener.onRoutePlanFailed(errorCode, "驾车路径规划失败");
            }
            return;
        }

        if (result.getPaths() == null || result.getPaths().isEmpty()) {
            if (listener != null) {
                listener.onRoutePlanFailed(-1, "未找到可用路径");
            }
            return;
        }

        // 获取第一条路径
        DrivePath drivePath = result.getPaths().get(0);
        List<LatLng> pathPoints = new ArrayList<>();

        // 提取路径上的所有点
        for (DriveStep step : drivePath.getSteps()) {
            List<LatLonPoint> polyline = step.getPolyline();
            for (LatLonPoint point : polyline) {
                pathPoints.add(new LatLng(point.getLatitude(), point.getLongitude()));
            }
        }

        if (listener != null) {
            listener.onRoutePlanSuccess(
                    pathPoints,
                    drivePath.getDistance(),
                    drivePath.getDuration()
            );
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        if (errorCode != AMapException.CODE_AMAP_SUCCESS || result == null) {
            if (listener != null) {
                listener.onRoutePlanFailed(errorCode, "步行路径规划失败");
            }
            return;
        }

        if (result.getPaths() == null || result.getPaths().isEmpty()) {
            if (listener != null) {
                listener.onRoutePlanFailed(-1, "未找到可用路径");
            }
            return;
        }

        // 获取第一条步行路径
        var walkPath = result.getPaths().get(0);
        List<LatLng> pathPoints = new ArrayList<>();

        for (var step : walkPath.getSteps()) {
            List<LatLonPoint> polyline = step.getPolyline();
            for (LatLonPoint point : polyline) {
                pathPoints.add(new LatLng(point.getLatitude(), point.getLongitude()));
            }
        }

        if (listener != null) {
            listener.onRoutePlanSuccess(
                    pathPoints,
                    walkPath.getDistance(),
                    walkPath.getDuration()
            );
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        // 不需要骑行路径
    }
}
