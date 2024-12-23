package com.app.src.abcqr.ui.main.fragment;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.app.src.abcqr.data.model.MyQR;
import com.app.src.abcqr.data.model.MyQRDao;
import com.app.src.abcqr.data.repository.MyQRDatabase;
import com.app.src.abcqr.data.repository.MyQRRepository;
import com.app.src.abcqr.utils.QR.scanner.QRCodeDecoder;
import com.app.src.abcqr.utils.QR.scanner.Scanner;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScanViewModel extends AndroidViewModel {
    private MyQRRepository myQRRepository;
    private MutableLiveData<List<MyQR>> myQRList;
    private final MutableLiveData<String> qrCodeResult;
    private static final int MAX_WIDTH = 1000;
    private static final int MAX_HEIGHT = 1000;
    public ScanViewModel(
            @NonNull Application application
    ) {
        super(application);
        qrCodeResult = new MutableLiveData<>();
        myQRList = new MutableLiveData<>();
        MyQRDao dao = MyQRDatabase.getInstance(getApplication()).myQRDao();
        myQRRepository = new MyQRRepository(dao);
        myQRList.setValue(myQRRepository.getAll());
    }
    public LiveData<List<MyQR>> getAll(){
        myQRList.setValue(myQRRepository.getAll());
        return myQRList;
    }
    public void insert(MyQR item){
        myQRRepository.insert(item);
    }
    public void delete(MyQR item) {
        myQRRepository.delete(item);
    }
    public LiveData<String> decodeQRCode(Bitmap bitmap) {
        try {
            if (bitmap.getWidth() > MAX_WIDTH || bitmap.getHeight() > MAX_HEIGHT) {
                bitmap = resizeBitmapUsingOpenCV(bitmap, MAX_WIDTH, MAX_HEIGHT);
            }
            int padding = 4;
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();

            // Create a new bitmap with padding
            Bitmap paddedBitmap = Bitmap.createBitmap(
                    originalWidth + padding * 2,
                    originalHeight + padding * 2,
                    bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888
            );

            // Initialize a canvas with the new bitmap
            Canvas canvas = new Canvas(paddedBitmap);

            // Optional: Fill the padding area with a solid color (e.g., white)
            canvas.drawColor(Color.WHITE);

            // Draw the original bitmap onto the canvas with the specified padding
            canvas.drawBitmap(bitmap, padding, padding, null);

            int[][] qrModuleMatrix = Scanner.getQRMatrix(paddedBitmap);
            QRCodeDecoder decoder = new QRCodeDecoder(qrModuleMatrix);
            String result = decoder.decode();
            qrCodeResult.setValue(result);
        } catch(Exception e){
            qrCodeResult.setValue("Loi");
        }
        return qrCodeResult;
    }
    private Bitmap resizeBitmapUsingOpenCV(Bitmap bitmap, int maxWidth, int maxHeight) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);

        // Determine the scaling factor while maintaining aspect ratio
        double widthRatio = (double) maxWidth / mat.width();
        double heightRatio = (double) maxHeight / mat.height();
        double scalingFactor = Math.min(widthRatio, heightRatio);

        // If the image is already within the desired size, return original
        if (scalingFactor >= 1.0) {
            return bitmap;
        }

        // Calculate new dimensions
        int newWidth = (int) (mat.width() * scalingFactor);
        int newHeight = (int) (mat.height() * scalingFactor);

        // Resize the image
        Mat resizedMat = new Mat();
        Size newSize = new Size(newWidth, newHeight);
        Imgproc.resize(mat, resizedMat, newSize, 0, 0, Imgproc.INTER_AREA);

        Bitmap resizedBitmap = Bitmap.createBitmap(
                newWidth,
                newHeight,
                bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888
        );
        Utils.matToBitmap(resizedMat, resizedBitmap);

        // Release Mats to free memory
        mat.release();
        resizedMat.release();

        return resizedBitmap;
    }

}