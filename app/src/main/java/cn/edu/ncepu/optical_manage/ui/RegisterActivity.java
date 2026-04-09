package cn.edu.ncepu.optical_manage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.edu.ncepu.optical_manage.BuildConfig;
import cn.edu.ncepu.optical_manage.R;
import cn.edu.ncepu.optical_manage.api.ApiClient;
import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.data.repository.UserRepository;
import cn.edu.ncepu.optical_manage.databinding.ActivityRegisterBinding;
import cn.edu.ncepu.optical_manage.model.User;
import timber.log.Timber;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ApiService apiService = ApiClient.getApiService();
        userRepository = new UserRepository(apiService);

        setupListeners();
        observeRepository();
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> performRegister());

        binding.tvLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void observeRepository() {
        userRepository.getRegisterSuccess().observe(this, success -> {
            if (success != null && success) {
                hideLoading();
                showToast("注册成功！请登录");
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
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

    private void performRegister() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

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

        if (password.length() < 6) {
            binding.tilPassword.setError("密码长度不能少于6位");
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("两次输入的密码不一致");
            return;
        }
        binding.tilConfirmPassword.setError(null);

        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("请输入有效的邮箱地址");
            return;
        }
        binding.tilEmail.setError(null);

        userRepository.register(username, password, phone, email);
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnRegister.setEnabled(true);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}