package cn.edu.ncepu.optical_manage.api;

import cn.edu.ncepu.optical_manage.model.ApiResponse;
import cn.edu.ncepu.optical_manage.model.CableSegment;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;
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
    @GET("/api/resource-points")
    Call<ApiResponse<List<ResourcePoint>>> getAllResourcePoints();

    @GET("/api/resource-points/{id}")
    Call<ApiResponse<ResourcePoint>> getResourcePointById(@Path("id") Long id);

    @GET("/api/resource-points/search")
    Call<ApiResponse<List<ResourcePoint>>> searchResourcePoints(@Query("keyword") String keyword);

    @GET("/api/resource-points/type/{type}")
    Call<ApiResponse<List<ResourcePoint>>> getResourcePointsByType(@Path("type") String type);

    @POST("/api/resource-points")
    Call<ApiResponse<ResourcePoint>> createResourcePoint(@Body ResourcePoint resourcePoint);

    @PUT("/api/resource-points/{id}")
    Call<ApiResponse<ResourcePoint>> updateResourcePoint(@Path("id") Long id, @Body ResourcePoint resourcePoint);

    @DELETE("/api/resource-points/{id}")
    Call<ApiResponse<Void>> deleteResourcePoint(@Path("id") Long id);

    @GET("/api/cable-segments")
    Call<ApiResponse<List<CableSegment>>> getAllCableSegments();

    @GET("/api/cable-segments/{id}")
    Call<ApiResponse<CableSegment>> getCableSegmentById(@Path("id") Long id);

    @GET("/api/cable-segments/search")
    Call<ApiResponse<List<CableSegment>>> searchCableSegments(@Query("keyword") String keyword);

    @POST("/api/cable-segments")
    Call<ApiResponse<CableSegment>> createCableSegment(@Body CableSegment cableSegment);

    @PUT("/api/cable-segments/{id}")
    Call<ApiResponse<CableSegment>> updateCableSegment(@Path("id") Long id, @Body CableSegment cableSegment);

    @DELETE("/api/cable-segments/{id}")
    Call<ApiResponse<Void>> deleteCableSegment(@Path("id") Long id);
}
