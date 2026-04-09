package cn.edu.ncepu.optical_manage.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cn.edu.ncepu.optical_manage.R;
import cn.edu.ncepu.optical_manage.api.ApiClient;
import cn.edu.ncepu.optical_manage.api.ApiService;
import cn.edu.ncepu.optical_manage.data.repository.UserRepository;
import cn.edu.ncepu.optical_manage.model.User;

public class ProfileFragment extends Fragment {

    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_ID = "user_id";

    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvPhone;
    private MaterialButton btnLogout;
    private SharedPreferences sharedPreferences;
    private UserRepository userRepository;
    private Long currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        ApiService apiService = ApiClient.getApiService();
        userRepository = new UserRepository(apiService);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        btnLogout = view.findViewById(R.id.btnLogout);

        setupListeners();
        loadUserInfo();
        observeRepository();
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> performLogout());

        tvEmail.setOnClickListener(v -> showEditDialog("邮箱", tvEmail.getText().toString(), false, value -> {
            tvEmail.setText(value);
            updateUserInfo();
        }));

        tvPhone.setOnClickListener(v -> showEditDialog("手机号", tvPhone.getText().toString(), true, value -> {
            tvPhone.setText(value);
            updateUserInfo();
        }));
    }

    private void observeRepository() {
        userRepository.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "更新成功", Toast.LENGTH_SHORT).show();
                userRepository.clearUpdateSuccess();
            }
        });

        userRepository.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                userRepository.clearError();
            }
        });
    }

    private void loadUserInfo() {
        String username = sharedPreferences.getString(KEY_USERNAME, null);
        if (username != null) {
            tvUsername.setText(username);
            fetchUserInfo(username);
        }
    }

    private void fetchUserInfo(String username) {
        ApiService apiService = ApiClient.getApiService();
        apiService.getUserByName(username).enqueue(new retrofit2.Callback<User>() {
            @Override
            public void onResponse(retrofit2.Call<User> call, retrofit2.Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    currentUserId = user.getId();

                    String email = user.getEmail();
                    String phone = user.getPhone();

                    tvEmail.setText(email != null && !email.isEmpty() ? email : "点击添加邮箱");
                    tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "点击添加手机号");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<User> call, Throwable t) {
            }
        });
    }

    private void updateUserInfo() {
        if (currentUserId == null) return;

        String phone = tvPhone.getText().toString();
        String email = tvEmail.getText().toString();

        if (phone.equals("点击添加手机号")) phone = "";
        if (email.equals("点击添加邮箱")) email = "";

        userRepository.updateUser(currentUserId, phone, email);
    }

    private void showEditDialog(String title, String currentValue, boolean isPhone, OnValueConfirmedListener listener) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.etInput);

        if (isPhone) {
            editText.setInputType(InputType.TYPE_CLASS_PHONE);
        } else {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        if (currentValue != null && !currentValue.startsWith("点击")) {
            editText.setText(currentValue);
            editText.setSelection(currentValue.length());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("修改" + title)
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String value = editText.getText().toString().trim();
                    listener.onConfirmed(value);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    interface OnValueConfirmedListener {
        void onConfirmed(String value);
    }

    private void performLogout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}