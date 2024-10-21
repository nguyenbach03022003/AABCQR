package com.app.src.abcqr.ui.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.src.abcqr.R;


public class SplashActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkForPermission(); // Check for permission immediately
    }

    private void checkForPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startSplashAnimation(); // Start splash animation if permission is granted
        } else {
            requestThePermission();
            //startSplashAnimation();
        }
    }

    private void startSplashAnimation() {
        ImageView logoImageView = findViewById(R.id.logoImageView);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logoImageView.startAnimation(fadeInAnimation);

        new Handler().postDelayed(this::goToMainActivity, 3000); // Delay for 3 seconds before going to MainActivity
    }

    private void requestThePermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSplashAnimation(); // Start splash animation if permission is granted
            } else if (!shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                showGotoAppSettingsDialog();
            } else {
                requestThePermission();
            }
        }
    }

    private void showGotoAppSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Camera permission is required to scan QR code")
                .setPositiveButton("Go to settings", (dialog, which) -> goToAppSettings())
                .setNegativeButton("Cancel", (dialog, which) ->
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show())
                .show();

    }

    private void goToAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}