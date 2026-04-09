package cn.edu.ncepu.optical_manage.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.model.User;
import cn.edu.ncepu.optical_manage.model.request.UserRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class UserRepository {

    private final ApiService apiService;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public UserRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public void login(String username, String password) {
        isLoading.postValue(true);
        Timber.d("开始登录: username=%s", username);

        Map<String, String> request = new HashMap<>();
        request.put("name", username);
        request.put("password", password);

        apiService.validatePassword(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Boolean ok = (Boolean) response.body().get("ok");
                    if (Boolean.TRUE.equals(ok)) {
                        Timber.d("登录成功，开始获取用户信息");
                        fetchUserInfo(username);
                    } else {
                        String message = (String) response.body().get("message");
                        Timber.e("登录失败: %s", message);
                        errorMessage.postValue(message != null ? message : "用户名或密码错误");
                        loginSuccess.postValue(false);
                    }
                } else {
                    Timber.e("登录失败: HTTP %d", response.code());
                    errorMessage.postValue("登录失败：" + response.code());
                    loginSuccess.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "登录请求失败");
                errorMessage.postValue("网络错误：" + t.getMessage());
                loginSuccess.postValue(false);
            }
        });
    }

    private void fetchUserInfo(String username) {
        apiService.getUserByName(username).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    user.setPassword(null);
                    currentUser.postValue(user);
                    loginSuccess.postValue(true);
                    Timber.d("用户信息获取成功: %s", user.getName());
                } else {
                    Timber.e("获取用户信息失败: HTTP %d", response.code());
                    errorMessage.postValue("获取用户信息失败");
                    loginSuccess.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Timber.e(t, "获取用户信息失败");
                errorMessage.postValue("网络错误：" + t.getMessage());
                loginSuccess.postValue(false);
            }
        });
    }

    public void register(String username, String password, String phone, String email) {
        isLoading.postValue(true);
        Timber.d("开始注册: username=%s", username);

        UserRequest request = new UserRequest(username, password);
        request.setPhone(phone);
        request.setEmail(email);

        Call<Map<String, Object>> call = apiService.createUser(request);
        Timber.d("注册请求 URL: %s", call.request().url());

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                isLoading.postValue(false);
                Timber.d("注册响应: HTTP %d, isSuccessful=%b", response.code(), response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Boolean ok = (Boolean) response.body().get("ok");
                    if (Boolean.TRUE.equals(ok)) {
                        Timber.d("注册成功");
                        registerSuccess.postValue(true);
                    } else {
                        String message = (String) response.body().get("message");
                        Timber.e("注册失败: %s", message);
                        errorMessage.postValue(message != null ? message : "注册失败");
                        registerSuccess.postValue(false);
                    }
                } else {
                    String errorBody = null;
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (Exception e) {
                            Timber.e(e, "读取错误响应失败");
                        }
                    }
                    Timber.e("注册失败: HTTP %d, errorBody=%s", response.code(), errorBody);
                    errorMessage.postValue("注册失败：" + response.code());
                    registerSuccess.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "注册请求失败: %s", t.getMessage());
                errorMessage.postValue("网络错误：" + t.getMessage());
                registerSuccess.postValue(false);
            }
        });
    }

    public void logout() {
        currentUser.postValue(null);
        loginSuccess.postValue(false);
        Timber.d("用户已退出登录");
    }

    public void updateUser(Long userId, String phone, String email) {
        isLoading.postValue(true);
        Timber.d("开始更新用户信息: userId=%d", userId);

        UserRequest request = new UserRequest();
        request.setPhone(phone);
        request.setEmail(email);

        apiService.updateUser(userId, request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Boolean ok = (Boolean) response.body().get("ok");
                    if (Boolean.TRUE.equals(ok)) {
                        Timber.d("用户信息更新成功");
                        User user = currentUser.getValue();
                        if (user != null) {
                            user.setPhone(phone);
                            user.setEmail(email);
                            currentUser.postValue(user);
                        }
                        updateSuccess.postValue(true);
                    } else {
                        Timber.e("用户信息更新失败");
                        errorMessage.postValue("更新失败");
                        updateSuccess.postValue(false);
                    }
                } else {
                    Timber.e("用户信息更新失败: HTTP %d", response.code());
                    errorMessage.postValue("更新失败：" + response.code());
                    updateSuccess.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                isLoading.postValue(false);
                Timber.e(t, "用户信息更新请求失败");
                errorMessage.postValue("网络错误：" + t.getMessage());
                updateSuccess.postValue(false);
            }
        });
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void clearUpdateSuccess() {
        updateSuccess.setValue(null);
    }
}