package cn.edu.ncepu.optical_manage.ui.dialogs;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import cn.edu.ncepu.optical_manage.R;
import cn.edu.ncepu.optical_manage.model.ResourcePoint;
import cn.edu.ncepu.optical_manage.ui.helper.GeocodeHelper;
import timber.log.Timber;

public class AddResourcePointDialog {

    public interface OnResourcePointSaveListener {
        void onSave(ResourcePoint point);
    }

    private AlertDialog dialog;
    private final androidx.fragment.app.Fragment fragment;
    private final OnResourcePointSaveListener saveListener;
    private final Runnable dismissCallback;

    private View dialogView;
    private TextInputEditText etName;
    private Spinner spinnerType;
    private Spinner spinnerStatus;
    private TextView tvDialogTitle;
    private Button btnCancel;
    private Button btnSave;
    private TextInputEditText etAddress;

    private ResourcePoint.ResourceType selectedType = ResourcePoint.ResourceType.POLE;
    private int selectedStatus = ResourcePoint.STATUS_NORMAL;

    private double latitude;
    private double longitude;

    public AddResourcePointDialog(androidx.fragment.app.Fragment fragment,
                                   OnResourcePointSaveListener saveListener,
                                   Runnable dismissCallback) {
        this.fragment = fragment;
        this.saveListener = saveListener;
        this.dismissCallback = dismissCallback;
    }

    public void show(double latitude, double longitude, ResourcePoint.ResourceType defaultType, String title) {
        this.latitude = latitude;
        this.longitude = longitude;

        dialogView = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.dialog_resource_point, null);

        initViews(dialogView);
        setupTypeSpinner(defaultType);
        setupStatusSpinner();
        setupButtons(title);

        // 自动根据坐标获取地址
        loadAddressFromLocation(latitude, longitude);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.requireContext())
                .setView(dialogView);
        dialog = builder.create();
        dialog.show();
    }

    private void loadAddressFromLocation(double latitude, double longitude) {
        if (etAddress == null) return;

        // 检查坐标是否在中国大陆范围内（大致范围）
        if (latitude < 3.0 || latitude > 54.0 || longitude < 73.0 || longitude > 136.0) {
            Timber.w("坐标不在中国大陆范围内，跳过逆地理编码: lat=%f, lng=%f", latitude, longitude);
            etAddress.setHint("坐标不在中国境内，请手动输入地址");
            return;
        }

        etAddress.setHint("正在获取地址...");

        try {
            GeocodeHelper geocodeHelper = new GeocodeHelper(fragment.requireContext());
            geocodeHelper.getAddressFromLocation(latitude, longitude, new GeocodeHelper.OnAddressCallback() {
                @Override
                public void onAddressResult(String address) {
                    if (etAddress != null && fragment.isAdded()) {
                        etAddress.setText(address);
                    }
                }

                @Override
                public void onError(String error) {
                    Timber.e("获取地址失败: %s", error);
                    if (etAddress != null && fragment.isAdded()) {
                        etAddress.setHint("地址获取失败，请手动输入");
                    }
                }
            });
        } catch (Exception e) {
            Timber.e(e, "初始化GeocodeHelper失败");
            etAddress.setHint("地址服务不可用，请手动输入");
        }
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);
        etAddress = view.findViewById(R.id.etAddress);
    }

    private void setupTypeSpinner(ResourcePoint.ResourceType defaultType) {
        ResourcePoint.ResourceType[] types = ResourcePoint.ResourceType.values();
        String[] displayNames = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            displayNames[i] = types[i].getDisplayName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.requireContext(),
                android.R.layout.simple_spinner_item, displayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        for (int i = 0; i < types.length; i++) {
            if (types[i] == defaultType) {
                spinnerType.setSelection(i);
                break;
            }
        }

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = types[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupStatusSpinner() {
        int[] statusValues = {ResourcePoint.STATUS_NORMAL, ResourcePoint.STATUS_FAULT, ResourcePoint.STATUS_MAINTENANCE};
        String[] statusDisplayNames = {"正常", "故障", "维护中"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.requireContext(),
                android.R.layout.simple_spinner_item, statusDisplayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Set default selection to match initial selectedStatus value
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i] == selectedStatus) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = statusValues[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupButtons(String title) {
        tvDialogTitle.setText(title);

        btnCancel.setOnClickListener(v -> {
            if (dismissCallback != null) {
                dismissCallback.run();
            }
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                showToast("请输入名称");
                return;
            }

            ResourcePoint point = new ResourcePoint(name, selectedType.getValue(), latitude, longitude);
            point.setStatus(selectedStatus);
            point.setAddress(etAddress.getText().toString().trim());

            if (saveListener != null) {
                saveListener.onSave(point);
            }
            if (dismissCallback != null) {
                dismissCallback.run();
            }
            dialog.dismiss();
        });
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void showToast(String message) {
        if (fragment.getContext() != null) {
            android.widget.Toast.makeText(fragment.getContext(), message, 
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
