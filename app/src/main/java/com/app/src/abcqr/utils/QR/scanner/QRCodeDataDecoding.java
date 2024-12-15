package com.app.src.abcqr.utils.QR.scanner;

import com.app.src.abcqr.utils.QR.QRVersion;
import java.nio.charset.StandardCharsets;

import com.app.src.abcqr.utils.QR.QRVersion;

public class QRCodeDataDecoding {

    private int version;
    private QRVersion.ErrorCorrectionLevel errorCorrectionLevel;
    private String bitString;
    private String decodedData;

    public QRCodeDataDecoding(int version, QRVersion.ErrorCorrectionLevel errorCorrectionLevel, String bitString) {
        this.version = version;
        this.errorCorrectionLevel = errorCorrectionLevel;
        this.bitString = bitString;
    }

    public String decode() {

        // extract Mode Indicator
        String modeIndicator = bitString.substring(0, 4);
        if (!modeIndicator.equals("0100")) {
            throw new IllegalArgumentException("Unsupported mode indicator: " + modeIndicator);
        }

        // extract Character Count Indicator
        int charCountIndicatorLength = version <= 9 ? 8 : (version <= 26 ? 16 : 16);;
        String charCountBits = bitString.substring(4, 4 + charCountIndicatorLength);
        int charCount = Integer.parseInt(charCountBits, 2);

        // extract Data Bits
        int dataStartIndex = 4 + charCountIndicatorLength;
        String dataBitsString = bitString.substring(dataStartIndex);

        // convert data bits to byte array
        byte[] dataBytes = new byte[charCount];
        for (int i = 0; i < charCount; i++) {
            int byteStart = i * 8;
            if (byteStart + 8 > dataBitsString.length()) {
                throw new IllegalArgumentException("Insufficient data bits for the expected character count.");
            }
            String byteString = dataBitsString.substring(byteStart, byteStart + 8);
            dataBytes[i] = (byte) Integer.parseInt(byteString, 2);
        }

        decodedData = new String(dataBytes, StandardCharsets.UTF_8);
        return decodedData;
    }
    public String getDecodedData() {
        return decodedData;
    }
}