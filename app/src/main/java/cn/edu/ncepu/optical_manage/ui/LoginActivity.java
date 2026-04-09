package cn.edu.ncepu.optical_manage.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import cn.edu.ncepu.optical_manage.BuildConfig;
import cn.edu.ncepu.optical_manage.R;
import cn.edu.ncepu.optical_manage.api.ApiClient;
import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.data.repository.UserRepository;
import cn.edu.ncepu.optical_manage.databinding.ActivityLoginBinding;
import cn.edu.ncepu.optical_manage.MainActivity;
import cn.edu.ncepu.optical_manage.model.User;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {

    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_ID = "user_id";

    private ActivityLoginBinding binding;
    private UserRepository userRepository;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if (isLoggedIn()) {
            navigateToMain();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        userRepository = new UserRepository(apiService);

        setupListeners();
        observeRepository();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> performLogin());

        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void observeRepository() {
        userRepository.getLoginSuccess().observe(this, success -> {
            if (success != null && success) {
                hideLoading();
                User user = userRepository.getCurrentUser().getValue();
                if (user != null) {
                    saveUserSession(user);
                    navigateToMain();
                }
            }
        });

        userRepository.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                hideLoading();
                showToast(error);
                userRepository.clearError();
            }
        });

        userRepository.getIsLoading().observe(this, loading -> {
            if (loading != null && loading) {
                showLoading();
            }
        });
    }

    private void performLogin() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.setError("请输入用户名");
            return;
        }
        binding.tilUsername.setError(null);

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("请输入密码");
            return;
        }
        binding.tilPassword.setError(null);

        userRepository.login(username, password);
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnLogin.setEnabled(true);
    }

    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, user.getName());
        editor.putLong(KEY_USER_ID, user.getId() != null ? user.getId() : -1);
        editor.apply();
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}