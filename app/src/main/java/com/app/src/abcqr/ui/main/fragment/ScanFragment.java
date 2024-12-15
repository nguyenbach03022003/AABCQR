package com.app.src.abcqr.ui.main.fragment;

import static com.app.src.abcqr.utils.DateTimeConverters.dateToTimestamp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.src.abcqr.R;
import com.app.src.abcqr.data.model.MyQR;
import com.app.src.abcqr.databinding.FragmentScanBinding;
import com.app.src.abcqr.ui.adapter.QRAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.List;

public class ScanFragment extends Fragment implements QRAdapter.OnItemClickListener{
    private RecyclerView recyclerViewQR;
    private QRAdapter qrAdapter;
    private List<MyQR> myQRList;
    private ScanViewModel scanViewModel;
    private FragmentScanBinding binding;
    private FloatingActionButton floatingActionButton;
    private  ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                try {
                    if (uri != null) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                        String message = scanViewModel.decodeQRCode(bitmap).getValue();
                        String type = handleQRCodeResult(message);
                        Date currTime = new Date();
                        MyQR newQR =new MyQR(type, dateToTimestamp(currTime), message);
                        scanViewModel.insert(newQR);
                        myQRList.add(0, newQR);
                        qrAdapter.notifyDataSetChanged();
                    }
                }
                catch(Exception e){
                    Toast.makeText(getContext(), "Error decoding QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    accessLibrary();
                } else {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Permission Required")
                            .setMessage("Permission needed for this action. Please enable it in app settings.")
                            .setPositiveButton("Go to settings", (dialog, which) -> goToAppSettings())
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                }
            });

    private void goToAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireActivity().getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentScanBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        scanViewModel = new ViewModelProvider(this).get(ScanViewModel.class);
        floatingActionButton = binding.floatingActionButton;
        floatingActionButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            } else {
                accessLibrary();
            }
        });

        recyclerViewQR = view.findViewById(R.id.list_qr_item);
        recyclerViewQR.setLayoutManager(new LinearLayoutManager(getContext()));
        myQRList = scanViewModel.getAll().getValue();
        qrAdapter = new QRAdapter(getContext(),myQRList,this);
        recyclerViewQR.setAdapter(qrAdapter);
        return view;
    }

    private void accessLibrary() {
        Toast.makeText(getContext(), "Accessing Library", Toast.LENGTH_SHORT).show();
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public String handleQRCodeResult(String result) {
        String type = "";
        String title = "QR Code Result";
        String message = result;
        String positiveButtonText = "OK";
        String action;
        Uri uri;

        if (result.startsWith("http://www.google.com/maps/search") || result.startsWith("https://www.google.com/maps/search")) {
            title = "Open location";
            message = "Location detected, would you like to open Google Maps? " + result;
            action = Intent.ACTION_VIEW;
            uri = Uri.parse(result);
            positiveButtonText = "Open";
            type = "Map";
        } else if (result.startsWith("http://") || result.startsWith("https://")) {
            title = "Open Web page";
            message = "QR Action detected, would you like to open this web page? " + result;
            action = Intent.ACTION_VIEW;
            uri = Uri.parse(result);
            positiveButtonText = "Open";
            type = "Web";
        } else if (result.startsWith("tel:")) {
            title = "Make a Call";
            message = "QR Action detected, would you like to call this number? ";
            action = Intent.ACTION_DIAL;
            uri = Uri.parse(result);
            positiveButtonText = "Call";
            type = "Call";
        } else if (result.startsWith("smsto:")) {
            title = "Send SMS";
            message = "QR Action detected, would you like to send an SMS?";
            action = Intent.ACTION_SENDTO;
            uri = Uri.parse(result);
            positiveButtonText = "Send";
            type = "SMS";
        } else if (result.startsWith("mailto:")) {
            title = "Send Email";
            message = "QR Action detected, would you like to send an email?";
            action = Intent.ACTION_SENDTO;
            uri = Uri.parse(result);
            positiveButtonText = "Send";
            type = "Email";
        } else {
            uri = null;
            action = null;
            if (result.startsWith("MECARD:") || result.startsWith("BEGIN:VCARD")) {
                title = "Add Contact";
                message = "QR Action detected, would you like to add this contact?";
                positiveButtonText = "Add";
                type = "MeCard";
            } else {
                // Handle plain text
                title = "Text Detected";
                message = "QR Code contains the following text: " + result;
                positiveButtonText = "OK";
                type = "Text";
            }
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    if (action != null && uri != null) {
                        Intent intent = new Intent(action, uri);
                        startActivity(intent);
                    } else if (result.startsWith("MECARD:") || result.startsWith("BEGIN:VCARD")) {
                        addContact(result);
                    }
                });

        if (!type.equals("Text")) {
            dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        }

        dialogBuilder.create().show();
        return type;
    }


    private void addContact(String vCard) {
        String[] meCardParts = vCard.substring(7).split(";");
        String name = "";
        String phone = "";
        String email = "";
        for (String part : meCardParts) {
            if (part.startsWith("N:")) {
                name = part.substring(2);
            } else if (part.startsWith("TEL:")) {
                phone = part.substring(4);
            } else if (part.startsWith("EMAIL:")) {
                email = part.substring(6);
            }
        }
        Intent contactIntent = new Intent(Intent.ACTION_INSERT);
        contactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        contactIntent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        contactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        contactIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
        startActivity(contactIntent);
    }

    @Override
    public void onDButtonClick(int position) {
        scanViewModel.delete(myQRList.get(position));
        myQRList.remove(position);
        qrAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPButtonClick(int position) {
        MyQR qr = myQRList.get(position);
        handleQRCodeResult(qr.getMessage());
    }
}