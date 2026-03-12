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
    private TextInputEditText etAddress;
    private TextView tvDialogTitle;
    private Button btnCancel;
    private Button btnSave;

    private ResourcePoint.ResourceType selectedType = ResourcePoint.ResourceType.POLE;
    private String selectedStatus = ResourcePoint.STATUS_NORMAL;

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

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.requireContext())
                .setView(dialogView);
        dialog = builder.create();
        dialog.show();
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        etAddress = view.findViewById(R.id.etAddress);
        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);
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
        String[] statusValues = {ResourcePoint.STATUS_NORMAL, ResourcePoint.STATUS_FAULT, ResourcePoint.STATUS_MAINTENANCE};
        String[] statusDisplayNames = {"正常", "故障", "维护中"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.requireContext(),
                android.R.layout.simple_spinner_item, statusDisplayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

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
            String address = etAddress.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                showToast("请输入名称");
                return;
            }

            ResourcePoint point = new ResourcePoint(name, selectedType, latitude, longitude);
            point.setAddress(address);
            point.setStatus(selectedStatus);

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
