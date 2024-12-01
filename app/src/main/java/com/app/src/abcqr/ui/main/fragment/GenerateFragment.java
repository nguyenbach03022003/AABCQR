package com.app.src.abcqr.ui.main.fragment;

import static com.app.src.abcqr.utils.VietQRCodeGenerator.generateVietQRCode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.app.src.abcqr.ui.main.fragment.QRCodeDialogFragment;
import com.app.src.abcqr.R;
import com.app.src.abcqr.databinding.FragmentGenerateBinding;
import com.app.src.abcqr.utils.generate.QRVersion;
import com.app.src.abcqr.utils.VietQRCodeGenerator;
import java.io.File;
import java.io.FileOutputStream;


public class GenerateFragment extends Fragment {
    private GenerateViewModel generateViewModel;
    private FragmentGenerateBinding binding;
    private Spinner spinnerCodeAction;
    private Spinner spinnerErrorCorrection;
    private Spinner spinnerBlockSize;
    private LinearLayout uniqueFieldsContainer;
    private Button buttonGenerate, saveButton, shareButton;
    private ImageView qrCodeImage;
    private Bitmap qrCodeBitmap;
    private int qrFormatMode;

    private String[] codeActions = {"Browse to a website", "Make a phone call", "Send an SMS", "Send an E-mail", "Free Formatted Text", "meCard", "Location", "VietQR"};
    private String[] errorCorrectionLevels = {"L", "M", "Q", "H"};
    private String[] blockSizes = {"5", "10", "15", "20", "30", "40", "50"};
    private String[] beneficiary_bank = {
            "Agribank",
            "VietinBank",
            "DongABank",
            "SaigonBank",
            "BIDV",
            "SeABank",
            "GPBank",
            "Vietcombank",
            "Techcombank",
            "MBBank",
            "VPBank",
            "HDBank",
            "SCB",
            "Eximbank",
            "SHB",
            "Oceanbank",
            "PVcomBank",
            "VIB",
            "NCB",
            "ABBANK",
            "NamABank",
            "VietABank",
            "VietCapitalBank",
            "BaoVietBank",
            "COOPBANK",
            "KienLongBank",
            "LPBank",
            "StandardChartered",
            "UnitedOverseas",
            "ShinhanBank",
            "Citibank",
            "KookminHN",
            "KEBHanaHN",
            "Timo",
            "VietBank",
            "DongABank",
            "DBSBank",
            "CIMB",
            "MAFC",
            "HSBC"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGenerateBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        generateViewModel = new ViewModelProvider(this).get(GenerateViewModel.class);

        spinnerCodeAction = binding.spinnerCodeAction;
        spinnerErrorCorrection = binding.spinnerErrorCorrection;
        spinnerBlockSize = binding.spinnerBlockSize;
        uniqueFieldsContainer = binding.uniqueFieldsContainer;
        buttonGenerate = binding.buttonGenerate;

        ArrayAdapter<String> codeActionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, codeActions);
        codeActionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCodeAction.setAdapter(codeActionAdapter);

        ArrayAdapter<String> errorCorrectionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, errorCorrectionLevels);
        errorCorrectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerErrorCorrection.setAdapter(errorCorrectionAdapter);

        ArrayAdapter<String> blockSizeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, blockSizes);
        blockSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBlockSize.setAdapter(blockSizeAdapter);

        spinnerCodeAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                qrFormatMode = position;
                updateUniqueFields();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        qrCodeImage = binding.qrCodeImage;
        saveButton = binding.saveButton;
        shareButton = binding.shareButton;
        shareButton.setOnClickListener(v -> shareQRCode());

        buttonGenerate.setOnClickListener(v -> {
            String errorCorrectionLevel = (String) spinnerErrorCorrection.getSelectedItem();
            String blockSize = (String) spinnerBlockSize.getSelectedItem();
            String data = getCurrentDataString();
            QRVersion.ErrorCorrectionLevel ec = QRVersion.ErrorCorrectionLevel.valueOf(errorCorrectionLevel);

            qrCodeBitmap = generateViewModel.generateQRCode(data, ec, Integer.parseInt(blockSize)).getValue();

            // Hiển thị QR Code trong cửa sổ nổi
            QRCodeDialogFragment qrCodeDialogFragment = new QRCodeDialogFragment(qrCodeBitmap);
            qrCodeDialogFragment.show(getFragmentManager(), "QRCodeDialog");
        });

//        saveButton.setOnClickListener(v -> {
//            boolean success = generateViewModel.saveQRCodeToLibrary(getContext(), qrCodeImage.getDrawable());
//            if (!success) {
//                Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(getContext(), "Image saved successfully", Toast.LENGTH_LONG).show();
//            }
//        });
        return view;
    }
    private void addSpinnerField(String label, String[] options) {
        TextView textView = new TextView(getContext());
        textView.setText(label);
        uniqueFieldsContainer.addView(textView);

        Spinner spinner = new Spinner(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        uniqueFieldsContainer.addView(spinner);
    }
    private void updateUniqueFields() {
        uniqueFieldsContainer.removeAllViews();

        switch (qrFormatMode) {
            case 0: // Browse to a website
                addEditTextField("Web Site URL");
                break;
            case 1: // Make a phone call
                addEditTextField("Phone Number");
                break;
            case 2: // Send an SMS
                addEditTextField("Phone Number");
                addEditTextField("SMS Message");
                break;
            case 3: // Send an E-mail
                addEditTextField("Mail Recipient");
                addEditTextField("Subject");
                addEditTextField("Body");
                break;
            case 4: // Free Formatted Text
                addEditTextField("Text");
                break;
            case 5:
                addEditTextField("Name");
                addEditTextField("Phone Number");
                addEditTextField("Email");
                addEditTextField("Address");
                addEditTextField("URL");
                addEditTextField("Nickname");
                break;
            case 6:
                addEditTextField("Address");
                break;
            case 7:
                addSpinnerField("Beneficiary Bank", beneficiary_bank);
                addEditTextField("Beneficiary Account Number");
                addEditTextField("Amount VND");
                break;

        }
    }

    private void addEditTextField(String hint) {
        EditText editText = new EditText(getContext());
        editText.setHint(hint);
        uniqueFieldsContainer.addView(editText);
    }

    private String getCurrentDataString() {
        StringBuilder data = new StringBuilder();
        switch (qrFormatMode) {
            case 0: // Browse to a website
                for (int i = 0; i < uniqueFieldsContainer.getChildCount(); i++) {
                    View view = uniqueFieldsContainer.getChildAt(i);
                    if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        if ("Web Site URL".equals(editText.getHint().toString())) {
                            data.append("http://").append(editText.getText().toString());
                            break;
                        }
                    }
                }
                break;
            case 1: // Make a phone call
                for (int i = 0; i < uniqueFieldsContainer.getChildCount(); i++) {
                    View view = uniqueFieldsContainer.getChildAt(i);
                    if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        if ("Phone Number".equals(editText.getHint().toString())) {
                            data.append("tel:").append(editText.getText().toString());
                            break;
                        }
                    }
                }
                break;
            case 2: // Send an SMS
                String phoneNumber = "";
                String smsMessage = "";
                for (int i = 0; i < uniqueFieldsContainer.getChildCount(); i++) {
                    View view = uniqueFieldsContainer.getChildAt(i);
                    if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        if ("Phone Number".equals(editText.getHint().toString())) {
                            phoneNumber = editText.getText().toString();
                        } else if ("SMS Message".equals(editText.getHint().toString())) {
                            smsMessage = editText.getText().toString();
                        }
                    }
                }
                data.append("smsto:").append(phoneNumber).append(":").append(smsMessage);
                break;
            case 3: // Send an E-mail
                String mailRecipient = "";
                String subject = "";
                String body = "";
                for (int i = 0; i < uniqueFieldsContainer.getChildCount(); i++) {
                    View view = uniqueFieldsContainer.getChildAt(i);
                    if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        if ("Mail Recipient".equals(editText.getHint().toString())) {
                            mailRecipient = editText.getText().toString();
                        } else if ("Subject".equals(editText.getHint().toString())) {
                            subject = editText.getText().toString();
                        } else if ("Body".equals(editText.getHint().toString())) {
                            body = editText.getText().toString();
                        }
                    }
                }
                data.append("mailto:").append(mailRecipient).append("?subject=").append(subject).append("&body=").append(body);
                break;
            case 4: // Free Formatted Text
                for (int i = 0; i < uniqueFieldsContainer.getChildCount(); i++) {
                    View view = uniqueFieldsContainer.getChildAt(i);
                    if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        if ("Text".equals(editText.getHint().toString())) {
                            data.append(editText.getText().toString());
                            break;
                        }
                    }
                }
                break;
            case 5: //mecard
                String name = "";
                String telephone = "";
                String email = "";
                String address = "";
                String url = "";
                String nickname = "";
                for (int i = 0; i < uniqueFieldsContainer.getChildCount(); i++) {
                    View view = uniqueFieldsContainer.getChildAt(i);
                    if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        if ("Name".equals(editText.getHint().toString())) {
                            name = editText.getText().toString();
                        } else if ("Phone Number".equals(editText.getHint().toString())) {
                            telephone = editText.getText().toString();
                        } else if ("Email".equals(editText.getHint().toString())) {
                            email = editText.getText().toString();
                        } else if ("Address".equals(editText.getHint().toString())) {
                            address = editText.getText().toString();
                        } else if ("URL".equals(editText.getHint().toString())) {
                            url = editText.getText().toString();
                        } else if ("Nickname".equals(editText.getHint().toString())) {
                            nickname = editText.getText().toString();
                        }
                    }
                }
                data.append("MECARD:N:").append(name).append(";TEL:").append(telephone).append(";EMAIL:").append(email).append(";ADR:")
                        .append(address).append(";URL:").append(url).append(";NICKNAME:").append(nickname);
                break;
            case 6: //location
                String addr = "";
                for (int i = 0; i < uniqueFieldsContainer.getChildCount(); i++) {
                    View view = uniqueFieldsContainer.getChildAt(i);
                    if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        if ("Address".equals(editText.getHint().toString())) {
                            addr = editText.getText().toString();
                        }
                    }
                }
                data.append("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(addr));
                break;
            case 7: // Beneficiary Bank
                String selectedBank = "";
                String accountNumber = "";
                String Amount_VND = "";

                for (int i = 0; i < uniqueFieldsContainer.getChildCount(); i++) {
                    View view = uniqueFieldsContainer.getChildAt(i);

                    if (view instanceof Spinner) {
                        Spinner spinner = (Spinner) view;
                        selectedBank = spinner.getSelectedItem().toString();
                    } else if (view instanceof EditText) {
                        EditText editText = (EditText) view;
                        if ("Beneficiary Account Number".equals(editText.getHint().toString())) {
                            accountNumber = editText.getText().toString();
                        } else if ("Amount VND".equals(editText.getHint().toString())) {
                            Amount_VND = editText.getText().toString();
                        }
                    }
                }
                data.append(generateVietQRCode(selectedBank, accountNumber, Amount_VND));
                break;
        }
        return data.toString();
    }

    private void shareQRCode() {
        if (qrCodeBitmap != null) {
            try {
                File file = new File(getContext().getExternalCacheDir(), "qr_code.png");
                FileOutputStream fOut = new FileOutputStream(file);
                qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
                file.setReadable(true, false);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = FileProvider.getUriForFile(getContext(), "ahqr.provider", file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setType("image/png");
                startActivity(Intent.createChooser(intent, "Share QR Code"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}