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

    private TextInputEditText etName;
    private Spinner spinnerType;
    private TextInputEditText etAddress;
    private TextInputEditText etDescription;
    private TextView tvDialogTitle;

    private String selectedType = ResourcePoint.TYPE_POLE;

    public AddResourcePointDialog(androidx.fragment.app.Fragment fragment,
                                   OnResourcePointSaveListener saveListener,
                                   Runnable dismissCallback) {
        this.fragment = fragment;
        this.saveListener = saveListener;
        this.dismissCallback = dismissCallback;
    }

    public void show(double latitude, double longitude, String defaultType, String title) {
        View dialogView = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.dialog_resource_point, null);
        
        initViews(dialogView);
        setupSpinner(defaultType);
        setupButtons(latitude, longitude, title);

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
        tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
    }

    private void setupSpinner(String defaultType) {
        String[] types = {ResourcePoint.TYPE_POLE, ResourcePoint.TYPE_MANHOLE, 
                          ResourcePoint.TYPE_BUSINESS_HALL};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragment.requireContext(),
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(defaultType)) {
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

    private void setupButtons(double latitude, double longitude, String title) {
        tvDialogTitle.setText(title);

        Button btnCancel = ((View) etName.getParent().getParent()).findViewById(R.id.btnCancel);
        Button btnSave = ((View) etName.getParent().getParent()).findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> {
            if (dismissCallback != null) {
                dismissCallback.run();
            }
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                showToast("请输入名称");
                return;
            }

            ResourcePoint point = new ResourcePoint(name, selectedType, latitude, longitude);
            point.setAddress(address);
            point.setDescription(description);

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
