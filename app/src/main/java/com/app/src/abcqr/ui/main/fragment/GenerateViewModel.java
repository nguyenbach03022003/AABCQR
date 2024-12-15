package com.app.src.abcqr.ui.main.fragment;



import static com.app.src.abcqr.utils.QR.QRVersion.getVersionCapacity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.app.src.abcqr.utils.QR.generate.QRCodeEncodingFacade;
import com.app.src.abcqr.utils.QR.QRVersion;


public class GenerateViewModel extends ViewModel {

    private MutableLiveData<Bitmap> qrCodeBitmap = new MutableLiveData<>();

    public GenerateViewModel() {
        qrCodeBitmap = new MutableLiveData<>();
    }

    public LiveData<Bitmap> generateQRCode(String data, QRVersion.ErrorCorrectionLevel errorCorrectionLevel, int blockSize) {

        int version = getVersionCapacity(data.length(), errorCorrectionLevel);
        QRCodeEncodingFacade qrCodeEncodingFacade = new QRCodeEncodingFacade(version, errorCorrectionLevel);
        int[][] matrix = qrCodeEncodingFacade.generateQRCode(data);
        int size = matrix.length;
        int imageSize = size * blockSize;
        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.RGB_565);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int color = matrix[y][x] == 1 ? 0xFF000000 : 0xFFFFFFFF; // black or white
                for (int dy = 0; dy < blockSize; dy++) {
                    for (int dx = 0; dx < blockSize; dx++) {
                        bitmap.setPixel(x * blockSize + dx, y * blockSize + dy, color);
                    }
                }
            }
        }
        qrCodeBitmap.setValue(bitmap);
        return qrCodeBitmap;
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