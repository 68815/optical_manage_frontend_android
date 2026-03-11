package cn.edu.ncepu.optical_manage.ui.dialogs;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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
    private TextInputEditText etAddress;
    private TextInputEditText etDescription;
    private ResourcePoint currentPoint;

    public EditResourcePointDialog(androidx.fragment.app.Fragment fragment,
                                    OnResourcePointUpdateListener updateListener) {
        this.fragment = fragment;
        this.updateListener = updateListener;
    }

    public void show(ResourcePoint point) {
        this.currentPoint = point;
        
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
        etAddress = dialogView.findViewById(R.id.etAddress);
        etDescription = dialogView.findViewById(R.id.etDescription);
        
        com.google.android.material.textview.MaterialTextView tvDialogTitle = 
                dialogView.findViewById(R.id.tvDialogTitle);
        tvDialogTitle.setText("编辑资源点");
    }

    private void populateData(ResourcePoint point) {
        etName.setText(point.getName());
        etAddress.setText(point.getAddress());
        etDescription.setText(point.getDescription());

        String[] types = {ResourcePoint.TYPE_POLE, ResourcePoint.TYPE_MANHOLE, 
                          ResourcePoint.TYPE_BUSINESS_HALL};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.requireContext(),
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(point.getType())) {
                spinnerType.setSelection(i);
                break;
            }
        }
    }

    private void setupButtons() {
        Button btnCancel = ((View) etName.getParent().getParent()).findViewById(R.id.btnCancel);
        Button btnSave = ((View) etName.getParent().getParent()).findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String type = (String) spinnerType.getSelectedItem();
            String address = etAddress.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                showToast("请输入名称");
                return;
            }

            currentPoint.setName(name);
            currentPoint.setType(type);
            currentPoint.setAddress(address);
            currentPoint.setDescription(description);

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
