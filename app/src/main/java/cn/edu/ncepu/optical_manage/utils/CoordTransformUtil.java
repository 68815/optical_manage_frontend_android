package cn.edu.ncepu.optical_manage.utils;

import android.content.Context;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;

public class CoordTransformUtil {

    private static Context context;

    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
    }

    public static double[] wgs84ToGcj02(double wgsLat, double wgsLng) {
        double[] gcj02 = new double[2];
        if (context == null) {
            gcj02[0] = wgsLat;
            gcj02[1] = wgsLng;
            return gcj02;
        }
        try {
            CoordinateConverter converter = new CoordinateConverter(context);
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(new LatLng(wgsLat, wgsLng));
            LatLng result = converter.convert();
            if (result != null) {
                gcj02[0] = result.latitude;
                gcj02[1] = result.longitude;
            } else {
                gcj02[0] = wgsLat;
                gcj02[1] = wgsLng;
            }
        } catch (Exception e) {
            e.printStackTrace();
            gcj02[0] = wgsLat;
            gcj02[1] = wgsLng;
        }
        return gcj02;
    }

    public static LatLng wgs84ToGcj02LatLng(double wgsLat, double wgsLng) {
        if (context == null) {
            return new LatLng(wgsLat, wgsLng);
        }
        try {
            CoordinateConverter converter = new CoordinateConverter(context);
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(new LatLng(wgsLat, wgsLng));
            LatLng result = converter.convert();
            return result != null ? result : new LatLng(wgsLat, wgsLng);
        } catch (Exception e) {
            e.printStackTrace();
            return new LatLng(wgsLat, wgsLng);
        }
    }
}
