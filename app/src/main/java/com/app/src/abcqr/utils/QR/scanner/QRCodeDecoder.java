package com.app.src.abcqr.utils.QR.scanner;

import static com.app.src.abcqr.utils.QR.QRVersion.decodeFormatInfo;
import static com.app.src.abcqr.utils.QR.QRVersion.getVersionECInfo;
import static com.app.src.abcqr.utils.QR.generate.QRErrorCorrection.errorCorrectionPolynomial;
import static com.app.src.abcqr.utils.QR.generate.QRErrorCorrection.reverseInterleaveAndErrorCorretion;

import android.util.Pair;
import com.app.src.abcqr.utils.QR.QRVersion;


public class QRCodeDecoder {
    private int[][] matrix;
    private int[][] operatedMatrix;
    private int size;
    private int version;
    private QRVersion.ErrorCorrectionLevel ecLevel;
    private int maskPattern;
    private String finalMessage; // the extracted binary data

    public QRCodeDecoder(int[][] matrix) {
        this.matrix = matrix;
        this.size = matrix.length;
        this.version = (this.size - 21) / 4 + 1;
        //operatedMatrix is used to mark function modules
        //1: function module
        //0: data and EC module
        operatedMatrix = new int[size][size];
        markFinderPatterns();
        markSeparators();
        markAlignmentPatterns();
        markTimingPatterns();
        markDarkModule();
        markFormatInformationArea();
        markVersionInformationArea();
    }
    private void markFinderPatterns() {
        int[][] positions = {{0, 0}, {0, size - 7}, {size - 7, 0}};
        for (int[] pos : positions) {
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 7; j++) {
                    operatedMatrix[pos[0] + i][pos[1] + j] = 1;
                }
            }
        }
    }
    private void markSeparators() {
        int[][] positions = {
                {0, 7, 7, 7},
                {7, 0, 7, 7},
                {size - 8, 0, size - 8, 7},
                {size - 1, 7, size - 8, 7},
                {0, size - 8, 7, size - 8},
                {7, size - 8, 7, size - 1}
        };

        for (int[] pos : positions) {
            int x1 = pos[0];
            int y1 = pos[1];
            int x2 = pos[2];
            int y2 = pos[3];

            if (x1 == x2) {
                for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                    operatedMatrix[y][x1] = 1;
                }
            } else if (y1 == y2) {
                for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                    operatedMatrix[y1][x] = 1;
                }
            }
        }
    }
    private void markAlignmentPatterns() {
        int[][] alignmentPatternLocations = {
                {}, // Version 1 (no alignment pattern)
                {6, 18}, // Version 2
                {6, 22}, // Version 3
                {6, 26}, // Version 4
                {6, 30}, // Version 5
                {6, 34}, // Version 6
                {6, 22, 38}, // Version 7
                {6, 24, 42}, // Version 8
                {6, 26, 46}, // Version 9
                {6, 28, 50}, // Version 10
                {6, 30, 54}, // Version 11
                {6, 32, 58}, // Version 12
                {6, 34, 62}, // Version 13
                {6, 26, 46, 66}, // Version 14
                {6, 26, 48, 70}, // Version 15
                {6, 26, 50, 74}, // Version 16
                {6, 30, 54, 78}, // Version 17
                {6, 30, 56, 82}, // Version 18
                {6, 30, 58, 86}, // Version 19
                {6, 34, 62, 90}, // Version 20
                {6, 28, 50, 72, 94}, // Version 21
                {6, 26, 50, 74, 98}, // Version 22
                {6, 30, 54, 78, 102}, // Version 23
                {6, 28, 54, 80, 106}, // Version 24
                {6, 32, 58, 84, 110}, // Version 25
                {6, 30, 58, 86, 114}, // Version 26
                {6, 34, 62, 90, 118}, // Version 27
                {6, 26, 50, 74, 98, 122}, // Version 28
                {6, 30, 54, 78, 102, 126}, // Version 29
                {6, 26, 52, 78, 104, 130}, // Version 30
                {6, 30, 56, 82, 108, 134}, // Version 31
                {6, 34, 60, 86, 112, 138}, // Version 32
                {6, 30, 58, 86, 114, 142}, // Version 33
                {6, 34, 62, 90, 118, 146}, // Version 34
                {6, 30, 54, 78, 102, 126, 150}, // Version 35
                {6, 24, 50, 76, 102, 128, 154}, // Version 36
                {6, 28, 54, 80, 106, 132, 158}, // Version 37
                {6, 32, 58, 84, 110, 136, 162}, // Version 38
                {6, 26, 54, 82, 110, 138, 166}, // Version 39
                {6, 30, 58, 86, 114, 142, 170}  // Version 40
        };

        int[] positions = alignmentPatternLocations[version - 1];

        for (int row : positions) {
            for (int col : positions) {
                if (operatedMatrix[row][col] == 0) { // check overlapped with finder pattern
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 5; j++) {
                            operatedMatrix[row - 2 + i][col - 2 + j] = 1;
                        }
                    }
                }
            }
        }
    }
    private void markTimingPatterns() {
        for (int i = 8; i < size - 8; i++) {
            operatedMatrix[i][6] = 1;
            operatedMatrix[6][i] = 1;
        }
    }
    private void markDarkModule() {
        operatedMatrix[4 * version + 9][8] = 1;
    }

    private void markFormatInformationArea() {
        for (int i = 0; i < 9; i++) {
            operatedMatrix[8][i] = 1;
            operatedMatrix[i][8] = 1;
        }
        for (int i = 0; i < 8; i++) {
            operatedMatrix[size - 1 - i][8] = 1;
            operatedMatrix[8][size - 1 - i] = 1;
        }
    }

    private void markVersionInformationArea() {
        if (version >= 7) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 3; j++) {
                    operatedMatrix[size - 11 + j][i] = 1;
                    operatedMatrix[i][size - 11 + j] = 1;
                }
            }
        }
    }

    public String decode() {
        // extract format information bits
        String formatBits = readVerticalFormatInformationBits();
        Pair<Boolean, int[]> formatResult= decodeFormatInfo(formatBits);
        if(!formatResult.first) { //if vertical fails, try horizontal
            formatBits = readHorizontalFormatInformationBits();
            formatResult = decodeFormatInfo(formatBits);
            if(!formatResult.first) return null;
        }
        // decode format info to get EC level and mask pattern
        int[] formatInfo = formatResult.second;
        // formatInfo[0] = ecLevel ordinal, formatInfo[1] = mask pattern
        this.ecLevel = QRVersion.ErrorCorrectionLevel.values()[formatInfo[0]];
        this.maskPattern = formatInfo[1];
        unmaskData();

        this.finalMessage = readDataBits();
        //perform error correction
        QRVersion.VersionECInfo vi = getVersionECInfo(this.version, this.ecLevel);
        int[] message = reverseInterleaveAndErrorCorretion(finalMessage, this.version, vi);
        String messageBitString = intArrayToBinaryString(message);
        QRCodeDataDecoding dataDecoding = new QRCodeDataDecoding(this.version, this.ecLevel, messageBitString);
        String finalMessage = dataDecoding.decode();
        return finalMessage;
    }

    private String readHorizontalFormatInformationBits() {
        StringBuilder bits = new StringBuilder();
        for (int i = 0; i <= 5; i++) {
            bits.append(matrix[8][i] == 1 ? '1': '0');
        }
        // Skip timing pattern
        bits.append(matrix[8][7] == 1 ? '1': '0');
        bits.append( matrix[8][size - 8] == 1 ? '1': '0');
        bits.append(matrix[8][size - 7] == 1 ? '1': '0');
        for (int i = 9; i < 15; i++) {
            bits.append(matrix[8][size - 15 + i] == 1 ? '1': '0');
        }
        return bits.toString();
    }
    private String readVerticalFormatInformationBits() {
        StringBuilder bits = new StringBuilder();
        for (int i = 0; i <= 5; i++) {
            bits.append(matrix[size - 1 - i][8] == 1 ? '1': '0');
        }
        // Skip timing pattern
        bits.append(matrix[8][7] == 1 ? '1': '0');
        bits.append(matrix[8][8] == 1 ? '1': '0');
        bits.append(matrix[7][8] == 1 ? '1': '0');

        for (int i = 9; i < 15; i++) {
            bits.append(matrix[14 - i][8] == 1 ? '1': '0');
        }
        return bits.toString();
    }
    private void unmaskData() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                // Skip function patterns:
                if (operatedMatrix[row][col] == 1) { // check function module
                    continue;
                }
                // If this module is a data module, apply mask rule:
                if (shouldSwitchBit(row, col, maskPattern)) {
                    matrix[row][col] ^= 1;
                }
            }
        }
    }

    private boolean shouldSwitchBit(int row, int col, int maskPattern) {
        switch (maskPattern) {
            case 0:
                return (row + col) % 2 == 0;
            case 1:
                return row % 2 == 0;
            case 2:
                return col % 3 == 0;
            case 3:
                return (row + col) % 3 == 0;
            case 4:
                return ((row / 2) + (col / 3)) % 2 == 0;
            case 5:
                return ((row * col) % 2 + (row * col) % 3) == 0;
            case 6:
                return (((row * col) % 2) + ((row * col) % 3)) % 2 == 0;
            case 7:
                return (((row + col) % 2) + ((row * col) % 3)) % 2 == 0;
            default:
                return false;
        }
    }

    private String readDataBits() {
        StringBuilder dataBits = new StringBuilder();
        int row = size - 1;
        int col = size - 1;
        boolean upwards = true;

        while (col > 0) {
            if (col == 6) col--; // skip timing column
            while ((upwards && row >= 0) || (!upwards && row < size)) {
                for (int i = 0; i < 2; i++) {
                    int currentCol = col - i;
                    // check if not a function pattern:
                    if (operatedMatrix[row][currentCol] == 0) {
                        // this is a data module:
                        dataBits.append(matrix[row][currentCol]);
                    }
                }
                row += upwards ? -1 : 1;
            }
            upwards = !upwards;
            row += upwards ? -1 : 1;
            col -= 2;
        }

        return dataBits.toString();
    }
    private static String intArrayToBinaryString(int[] intArray) {
        StringBuilder binaryString = new StringBuilder();
        for (int value : intArray) {
            String byteString = Integer.toBinaryString(value & 0xFF);
            while (byteString.length() < 8) {
                byteString = "0" + byteString;
            }
            binaryString.append(byteString);
        }
        return binaryString.toString();
    }
    public String getFinalMessage() {
        return finalMessage;
    }
}
