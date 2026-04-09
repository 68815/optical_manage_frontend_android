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

public class EditResourcePointDialog {

    public interface OnResourcePointUpdateListener {
        void onUpdate(ResourcePoint point);
    }

    private AlertDialog dialog;
    private final androidx.fragment.app.Fragment fragment;
    private final OnResourcePointUpdateListener updateListener;

    private TextInputEditText etName;
    private Spinner spinnerType;
    private Spinner spinnerStatus;
    private TextInputEditText etAddress;
    private ResourcePoint currentPoint;

    private String selectedType;
    private int selectedStatus;

    public EditResourcePointDialog(androidx.fragment.app.Fragment fragment,
                                    OnResourcePointUpdateListener updateListener) {
        this.fragment = fragment;
        this.updateListener = updateListener;
    }

    public void show(ResourcePoint point) {
        this.currentPoint = point;
        this.selectedType = point.getType();
        this.selectedStatus = point.getStatus() != null ? point.getStatus() : ResourcePoint.STATUS_NORMAL;

        dialogView = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.dialog_resource_point, null);

        initViews(dialogView);
        populateData(point);
        setupButtons();

        // 如果地址为空，自动根据坐标获取地址
        if (TextUtils.isEmpty(point.getAddress())) {
            loadAddressFromLocation(point.getLatitude(), point.getLongitude());
        }

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
                        // 只有当地址为空时才自动填充
                        if (TextUtils.isEmpty(etAddress.getText())) {
                            etAddress.setText(address);
                        }
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

    private void initViews(View dialogView) {
        etName = dialogView.findViewById(R.id.etName);
        spinnerType = dialogView.findViewById(R.id.spinnerType);
        spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        etAddress = dialogView.findViewById(R.id.etAddress);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvDialogTitle.setText("编辑资源点");
    }

    private void populateData(ResourcePoint point) {
        etName.setText(point.getName());
        etAddress.setText(point.getAddress());

        setupTypeSpinner(point);
        setupStatusSpinner(point.getStatus());
    }

    private void setupTypeSpinner(ResourcePoint currentPoint) {
        ResourcePoint.ResourceType[] types = ResourcePoint.ResourceType.values();
        String[] displayNames = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            displayNames[i] = types[i].getDisplayName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.requireContext(),
                android.R.layout.simple_spinner_item, displayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        String currentTypeValue = currentPoint.getType();
        if (currentTypeValue != null) {
            for (int i = 0; i < types.length; i++) {
                if (types[i].getValue().equals(currentTypeValue)) {
                    spinnerType.setSelection(i);
                    break;
                }
            }
        }

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = types[position].getValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupStatusSpinner(int currentStatus) {
        int[] statusValues = {ResourcePoint.STATUS_NORMAL, ResourcePoint.STATUS_FAULT, ResourcePoint.STATUS_MAINTENANCE};
        String[] statusDisplayNames = {"正常", "故障", "维护中"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.requireContext(),
                android.R.layout.simple_spinner_item, statusDisplayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i] == currentStatus) {
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

    private View dialogView;

    private void setupButtons() {
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                showToast("请输入名称");
                return;
            }

            currentPoint.setName(name);
            currentPoint.setType(selectedType);
            currentPoint.setAddress(address);
            currentPoint.setStatus(selectedStatus);

            if (updateListener != null) {
                updateListener.onUpdate(currentPoint);
            }
            dialog.dismiss();
        });
    }

    private void showToast(String message) {
        if (fragment.getContext() != null) {
            android.widget.Toast.makeText(fragment.getContext(), message,
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
