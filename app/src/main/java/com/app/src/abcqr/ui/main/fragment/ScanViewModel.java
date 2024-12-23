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

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScanViewModel extends AndroidViewModel {
    private MyQRRepository myQRRepository;
    private MutableLiveData<List<MyQR>> myQRList;
    private final MutableLiveData<String> qrCodeResult;

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
//            int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
//            bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//            LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
//            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
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

}