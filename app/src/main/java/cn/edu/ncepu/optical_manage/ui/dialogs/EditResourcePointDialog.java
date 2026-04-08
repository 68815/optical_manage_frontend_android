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

        View dialogView = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.dialog_resource_point, null);

        initViews(dialogView);
        populateData(point);
        setupButtons();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.requireContext())
                .setView(dialogView);
        dialog = builder.create();
        dialog.show();
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

    private void setupButtons() {
        Button btnCancel = ((View) etName.getParent().getParent()).findViewById(R.id.btnCancel);
        Button btnSave = ((View) etName.getParent().getParent()).findViewById(R.id.btnSave);

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
