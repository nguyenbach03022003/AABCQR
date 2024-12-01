package com.app.src.abcqr.ui.main.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.src.abcqr.R;
import com.app.src.abcqr.ui.main.fragment.GenerateViewModel;

public class QRCodeDialogFragment extends DialogFragment {

    private Bitmap qrCodeBitmap;
    private GenerateViewModel generateViewModel;

    // Constructor nhận vào QR Code
    public QRCodeDialogFragment(Bitmap qrCodeBitmap) {
        this.qrCodeBitmap = qrCodeBitmap;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Tạo dialog
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.dialog_qr_code);

        // Tìm ImageView trong layout dialog và thiết lập hình ảnh QR code
        ImageView qrCodeImage = dialog.findViewById(R.id.qrCodeImage);
        qrCodeImage.setImageBitmap(qrCodeBitmap);

        // Điều chỉnh scaleType cho ImageView để phóng to ảnh
        qrCodeImage.setScaleType(ImageView.ScaleType.FIT_CENTER); // Hoặc sử dụng CENTER_CROP tùy vào yêu cầu của bạn

        // Cài đặt nút Close để đóng cửa sổ
        View closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());  // Đóng cửa sổ khi nhấn nút

        View saveButton = dialog.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            boolean success = saveQRCodeToLibrary(getContext(), qrCodeImage.getDrawable());
            if (!success) {
                Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Image saved successfully", Toast.LENGTH_LONG).show();
            }
        });
        return dialog;
    }
    public boolean saveQRCodeToLibrary(Context context, Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        String savedImageURL = MediaStore.Images.Media.insertImage(
                context.getContentResolver(),
                bitmap,
                "QR Code",
                "Generated QR Code"
        );

        if (savedImageURL == null) {
            return false;
        } else {
            return true;
        }
    }
}
