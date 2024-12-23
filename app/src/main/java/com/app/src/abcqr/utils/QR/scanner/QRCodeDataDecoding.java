package com.app.src.abcqr.utils.QR.scanner;

import com.app.src.abcqr.utils.QR.QRVersion;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.app.src.abcqr.utils.QR.QRVersion;

public class QRCodeDataDecoding {

    private int version;
    private QRVersion.ErrorCorrectionLevel errorCorrectionLevel;
    private String bitString;
    private String decodedData;
    private static final Map<Integer, Character> ALPHANUMERIC_MAP = new HashMap<>();
    private static final Map<Character, Integer> CHAR_TO_VALUE_MAP = new HashMap<>();

    static {
        String[] chars = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z", " ", "$", "%", "*",
                "+", "-", ".", "/", ":"
        };
        for (int i = 0; i < chars.length; i++) {
            ALPHANUMERIC_MAP.put(i, chars[i].charAt(0));
            CHAR_TO_VALUE_MAP.put(chars[i].charAt(0), i);
        }
    }

    public QRCodeDataDecoding(int version, QRVersion.ErrorCorrectionLevel errorCorrectionLevel, String bitString) {
        this.version = version;
        this.errorCorrectionLevel = errorCorrectionLevel;
        this.bitString = bitString;
    }

    public String decode() {

        // extract Mode Indicator
        String modeIndicator = bitString.substring(0, 4);
        if (modeIndicator.equals("0100") || modeIndicator.equals("0111")) {
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
        } else if(modeIndicator.equals("0010")){
            int charCountIndicatorLength;
            if (version >= 1 && version <= 9) {
                charCountIndicatorLength = 9;
            } else if (version >= 10 && version <= 26) {
                charCountIndicatorLength = 11;
            } else if (version >= 27 && version <= 40) {
                charCountIndicatorLength = 13;
            } else {
                throw new IllegalArgumentException("Invalid QR code version.");
            }
            if (bitString.length() < 4 + charCountIndicatorLength) {
                throw new IllegalArgumentException("Bit string too short to contain Character Count Indicator.");
            }

            // Extract Character Count Indicator
            String charCountBits = bitString.substring(4, 4 + charCountIndicatorLength);
            int charCount = Integer.parseInt(charCountBits, 2);

            // Extract Data Bits
            int dataStartIndex = 4 + charCountIndicatorLength;
            String dataBitsString = bitString.substring(dataStartIndex);
            StringBuilder decodedBuilder = new StringBuilder();

            int i = 0;
            while (i + 11 <= dataBitsString.length() && decodedBuilder.length() + 2 <= charCount) {
                String pairBits = dataBitsString.substring(i, i + 11);
                int value = Integer.parseInt(pairBits, 2);
                int firstCharValue = value / 45;
                int secondCharValue = value % 45;

                if (ALPHANUMERIC_MAP.containsKey(firstCharValue) && ALPHANUMERIC_MAP.containsKey(secondCharValue)) {
                    decodedBuilder.append(ALPHANUMERIC_MAP.get(firstCharValue));
                    decodedBuilder.append(ALPHANUMERIC_MAP.get(secondCharValue));
                } else {
                    throw new IllegalArgumentException("Invalid alphanumeric value encountered.");
                }

                i += 11;
            }
            if (decodedBuilder.length() < charCount && i + 6 <= dataBitsString.length()) {
                String singleBits = dataBitsString.substring(i, i + 6);
                int singleValue = Integer.parseInt(singleBits, 2);

                if (ALPHANUMERIC_MAP.containsKey(singleValue)) {
                    decodedBuilder.append(ALPHANUMERIC_MAP.get(singleValue));
                } else {
                    throw new IllegalArgumentException("Invalid alphanumeric value encountered in single character.");
                }

                i += 6;
            }
            decodedData = decodedBuilder.toString();
            return decodedData;
        } else{
            throw new UnsupportedOperationException("Unsupported Mode Indicator: " + modeIndicator);
        }


    }
    public String getDecodedData() {
        return decodedData;
    }
}