package com.app.src.abcqr.ui.main.fragment;

import android.app.Application;
import android.graphics.Bitmap;
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
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

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

            int[][] qrModuleMatrix = Scanner.getQRMatrix(bitmap);
            QRCodeDecoder decoder = new QRCodeDecoder(qrModuleMatrix);
            decoder.decode();
            qrCodeResult.setValue(decoder.getFinalMessage());
        } catch(Exception e){
            qrCodeResult.setValue("Loi");
        }
        return qrCodeResult;
    }

}