package cn.edu.ncepu.optical_manage.api;

import cn.edu.ncepu.optical_manage.model.ApiResponse;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import cn.edu.ncepu.optical_manage.model.MapResponse;
import cn.edu.ncepu.optical_manage.model.request.MapQueryRequest;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;
import cn.edu.ncepu.optical_manage.model.ResourceRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import java.util.List;
import java.util.Map;

public interface ApiService {

    @POST("/api/v1/map/query")
    Call<MapResponse> queryResources(@Body MapQueryRequest request);

    @GET("/api/v1/map/resource-point/{id}")
    Call<ResourcePoint> getResourcePointById(@Path("id") Long id);

    @POST("/api/v1/map/resource-point")
    Call<Long> createResourcePoint(@Body ResourceRequest resourceRequest);

    @PUT("/api/v1/map/resource-point/{id}")
    Call<Map<String, Object>> updateResourcePoint(@Path("id") Long id, @Body ResourceRequest resourceRequest);

    @DELETE("/api/v1/map/resource-point/{id}")
    Call<Map<String, Object>> deleteResourcePoint(@Path("id") Long id);

    @POST("/api/v1/map/fiber-segments/query")
    Call<MapResponse> queryFiberSegments(@Body MapQueryRequest request);

    @POST("/api/v1/map/fiber-segment")
    Call<ApiResponse<Long>> createFiberSegment(@Body Map<String, Object> request);

    @PUT("/api/v1/map/fiber-segments/{id}")
    Call<Map<String, Object>> updateFiberSegment(@Path("id") Long id, @Body Map<String, Object> request);

    @DELETE("/api/v1/map/fiber-segments/{id}")
    Call<Map<String, Object>> deleteFiberSegment(@Path("id") Long id);

    @POST("/api/v1/map/routings")
    Call<ApiResponse<Long>> createRouting(@Body Map<String, Object> routingRequest);

    @POST("/api/v1/map/routings/query")
    Call<ApiResponse<List<Map<String, Object>>>> queryRoutings(@Body MapQueryRequest request);

    @PUT("/api/v1/map/routings/{id}")
    Call<Map<String, Object>> updateRouting(@Path("id") Long id, @Body Map<String, Object> routingRequest);

    @DELETE("/api/v1/map/routings/{id}")
    Call<Map<String, Object>> deleteRouting(@Path("id") Long id);
}
