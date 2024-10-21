package com.app.src.abcqr.utils.generate;

import static com.app.src.abcqr.utils.generate.QRErrorCorrectionEncoding.generateErrorCorrectionCodewords;
import static com.app.src.abcqr.utils.generate.QRErrorCorrectionEncoding.interleaveAndConvertToBinary;
import static com.app.src.abcqr.utils.generate.QRVersion.getVersionECInfo;

import java.util.ArrayList;
import java.util.Arrays;


public class QRCodeFacade {

    private QRCodeDataEncoding dataEncoding;
    private QRCodeModulePlacement modulePlacement;
    private int version;
    private QRVersion.ErrorCorrectionLevel errorCorrectionLevel;

    public QRCodeFacade(int version, QRVersion.ErrorCorrectionLevel errorCorrectionLevel) {
        this.version = version;
        this.errorCorrectionLevel = errorCorrectionLevel;
    }

    public int[][] generateQRCode(String data) {
        QRVersion.VersionECInfo vi = getVersionECInfo(version, errorCorrectionLevel);

        dataEncoding = new QRCodeDataEncoding(version, errorCorrectionLevel);
        String bitString = dataEncoding.dataEncode(data);

        System.out.println("QR Code Bit String: " + Arrays.toString(splitBinaryStringToIntArray(bitString)));

        int[] message = splitBinaryStringToIntArray(bitString);

        int[] ecCodewords = generateErrorCorrectionCodewords(message, vi.getEcCodewordsPerBlock());
        System.out.println("Error Correction Codewords: " + Arrays.toString(ecCodewords));

        String structuralFinalMessage = interleaveAndConvertToBinary(message, version, vi);
        System.out.println("structuralFinalMessage " + structuralFinalMessage);

        modulePlacement = new QRCodeModulePlacement(version, errorCorrectionLevel, structuralFinalMessage);
        return modulePlacement.getMatrix();
    }

    private static int[] splitBinaryStringToIntArray(String binaryString) {
        int length = binaryString.length();
        int remainder = length % 8;
        if (remainder != 0) {
            binaryString = "0".repeat(8 - remainder) + binaryString;
            length = binaryString.length();
        }
        ArrayList<Integer> intList = new ArrayList<>();

        for (int i = 0; i < length; i += 8) {
            String byteString = binaryString.substring(i, i + 8);
            int intValue = Integer.parseInt(byteString, 2);
            intList.add(intValue);
        }

        int[] intArray = new int[intList.size()];
        for (int i = 0; i < intList.size(); i++) {
            intArray[i] = intList.get(i);
        }

        return intArray;
    }

}
