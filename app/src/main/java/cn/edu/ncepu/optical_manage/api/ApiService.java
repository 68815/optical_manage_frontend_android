package cn.edu.ncepu.optical_manage.api;

import cn.edu.ncepu.optical_manage.model.ApiResponse;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;
import cn.edu.ncepu.optical_manage.model.ResourceRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import java.util.List;

public interface ApiService {
    @GET("/api/v1/map/resource-point")
    Call<ApiResponse<List<ResourcePoint>>> getAllResourcePoints();

    @GET("/api/v1/map/resource-point/{id}")
    Call<ApiResponse<ResourcePoint>> getResourcePointById(@Path("id") Long id);

    @GET("/api/v1/map/resource-point/search")
    Call<ApiResponse<List<ResourcePoint>>> searchResourcePoints(@Query("keyword") String keyword);

    @GET("/api/v1/map/resource-point/type/{type}")
    Call<ApiResponse<List<ResourcePoint>>> getResourcePointsByType(@Path("type") String type);

    @POST("/api/v1/map/resource-point")
    Call<ApiResponse<ResourcePoint>> createResourcePoint(@Body ResourceRequest resourceRequest);

    @PUT("/api/v1/map/resource-point/{id}")
    Call<ApiResponse<ResourcePoint>> updateResourcePoint(@Path("id") Long id, @Body ResourceRequest resourceRequest);

    @DELETE("/api/v1/map/resource-point/{id}")
    Call<ApiResponse<Void>> deleteResourcePoint(@Path("id") Long id);

    @GET("/api/v1/map/cable-segments")
    Call<ApiResponse<List<CableSegment>>> getAllCableSegments();

    @GET("/api/v1/map/cable-segments/{id}")
    Call<ApiResponse<CableSegment>> getCableSegmentById(@Path("id") Long id);

    @GET("/api/v1/map/cable-segments/search")
    Call<ApiResponse<List<CableSegment>>> searchCableSegments(@Query("keyword") String keyword);

    @POST("/api/v1/map/cable-segments")
    Call<ApiResponse<CableSegment>> createCableSegment(@Body CableSegment cableSegment);

    @PUT("/api/v1/map/cable-segments/{id}")
    Call<ApiResponse<CableSegment>> updateCableSegment(@Path("id") Long id, @Body CableSegment cableSegment);

    @DELETE("/api/v1/map/cable-segments/{id}")
    Call<ApiResponse<Void>> deleteCableSegment(@Path("id") Long id);
}
