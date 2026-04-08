package cn.edu.ncepu.optical_manage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ApiResponse<T> {
    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("resources")
    private List<T> resources;

    @JsonProperty("id")
    private Long id;

    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public List<T> getResources() {
        return resources;
    }

    public void setResources(List<T> resources) {
        this.resources = resources;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isSuccess() {
        return code == 200 || ok;
    }

    public T getEffectiveData() {
        if (data != null) {
            return data;
        }
        if (resources != null && !resources.isEmpty()) {
            if (resources.size() == 1) {
                return resources.get(0);
            }
            return (T) resources;
        }
        return null;
    }
}
