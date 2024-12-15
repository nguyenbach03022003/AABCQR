package com.app.src.abcqr.utils.QR.generate;

import static com.app.src.abcqr.utils.QR.QRVersion.getFormatInformationString;
import static com.app.src.abcqr.utils.QR.QRVersion.getVersionString;

import com.app.src.abcqr.utils.QR.QRVersion;

public class QRCodeModulePlacement {

    private static final int[][] FINDER_PATTERN = {
            {1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 0, 1},
            {1, 0, 1, 1, 1, 0, 1},
            {1, 0, 1, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1}
    };

    private static final int[][] ALIGNMENT_PATTERN = {
            {1, 1, 1, 1, 1},
            {1, 0, 0, 0, 1},
            {1, 0, 1, 0, 1},
            {1, 0, 0, 0, 1},
            {1, 1, 1, 1, 1}
    };
    private int version;
    private int size;
    private int bestMaskPattern;
    private QRVersion.ErrorCorrectionLevel ecLevel;
    private int[][] matrix;
    private int[][] operatedMatrix;
    private String finalMessage;

    public QRCodeModulePlacement(int version, QRVersion.ErrorCorrectionLevel ecLevel, String finalMessage) {
        this.version = version;
        this.ecLevel = ecLevel;
        this.size = 21 + (version - 1) * 4;  // Version size formula
        this.matrix = new int[size][size];
        operatedMatrix = new int[size][size];
        this.finalMessage = finalMessage;
        addFunctionPatterns();
    }

    private void addFunctionPatterns() {
        addFinderPatterns();
        addSeparators();
        addAlignmentPatterns();
        addTimingPatterns();
        addDarkModule();
        reserveFormatInformationArea();
        reserveVersionInformationArea();
        placeDataBits();
        applyMasking();
        placeFormatAndVersionInfo();
    }

    private void addFinderPatterns() {
        int[][] positions = {{0, 0}, {0, size - 7}, {size - 7, 0}};
        for (int[] pos : positions) {
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 7; j++) {
                    matrix[pos[0] + i][pos[1] + j] = FINDER_PATTERN[i][j];
                    operatedMatrix[pos[0] + i][pos[1] + j] = 1;
                }
            }
        }
    }

    private void addSeparators() {
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
                    matrix[y][x1] = 0;
                    operatedMatrix[y][x1] = 1;
                }
            } else if (y1 == y2) {
                for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                    matrix[y1][x] = 0;
                    operatedMatrix[y1][x] = 1;
                }
            }
        }
    }

    private void addAlignmentPatterns() {
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
                if (matrix[row][col] == 0) {
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 5; j++) {
                            matrix[row - 2 + i][col - 2 + j] = ALIGNMENT_PATTERN[i][j];
                            operatedMatrix[row - 2 + i][col - 2 + j] = 1;
                        }
                    }
                }
            }
        }
    }

    private void addTimingPatterns() {
        for (int i = 8; i < size - 8; i++) {
            matrix[i][6] = i % 2 == 0 ? 1 : 0;
            matrix[6][i] = i % 2 == 0 ? 1 : 0;
            operatedMatrix[i][6] = 1;
            operatedMatrix[6][i] = 1;
        }
    }

    private void addDarkModule() {
        matrix[4 * version + 9][8] = 1;
        operatedMatrix[4 * version + 9][8] = 1;
    }

    private void reserveFormatInformationArea() {
        for (int i = 0; i < 9; i++) {
            operatedMatrix[8][i] = 1;
            operatedMatrix[i][8] = 1;
        }
        for (int i = 0; i < 8; i++) {
            operatedMatrix[size - 1 - i][8] = 1;
            operatedMatrix[8][size - 1 - i] = 1;
        }
    }

    private void reserveVersionInformationArea() {
        if (version >= 7) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 3; j++) {
                    operatedMatrix[size - 11 + j][i] = 1;
                    operatedMatrix[i][size - 11 + j] = 1;
                }
            }
        }
    }

    private void placeDataBits() {
        int row = size - 1;
        int col = size - 1;
        boolean upwards = true;
        int dataIndex = 0;
        int dataLength = finalMessage.length();

        while (col > 0) {
            if (col == 6) col--; // Skip vertical timing pattern

            while ((upwards && row >= 0) || (!upwards && row < size)) {
                for (int i = 0; i < 2; i++) {
                    int currentCol = col - i;
                    if (operatedMatrix[row][currentCol] == 0) { // If the module is not already taken by a function pattern
                        if (dataIndex < dataLength) {
                            matrix[row][currentCol] = finalMessage.charAt(dataIndex) == '1' ? 1 : 0;
                            operatedMatrix[row][currentCol] = 2;
                            dataIndex++;
                        } else {
                            matrix[row][currentCol] = 0; // Add padding if no more data
                            operatedMatrix[row][currentCol] = 2;
                        }
                    }
                }
                row += upwards ? -1 : 1;
            }
            upwards = !upwards;
            row += upwards ? -1 : 1;
            col -= 2;
        }
    }

    private void applyMasking() {
        bestMaskPattern = chooseBestMaskPattern();
        System.out.println("Best mask: " + bestMaskPattern);
        matrix = applyMask(bestMaskPattern);
    }

    private void placeFormatAndVersionInfo() {
        // place format string
        String formatString = getFormatInformationString(ecLevel.ordinal(), bestMaskPattern);
        for (int i = 0; i <= 5; i++) {
            matrix[8][i] = formatString.charAt(i) == '1' ? 1 : 0;
            matrix[size - 1 - i][8] = formatString.charAt(i) == '1' ? 1 : 0;
        }
        // Skip timing pattern
        matrix[8][7] = formatString.charAt(6) == '1' ? 1 : 0;
        matrix[8][8] = formatString.charAt(7) == '1' ? 1 : 0;
        matrix[7][8] = formatString.charAt(8) == '1' ? 1 : 0;
        matrix[8][size - 8] = formatString.charAt(7) == '1' ? 1 : 0;
        matrix[8][size - 7] = formatString.charAt(8) == '1' ? 1 : 0;
        for (int i = 9; i < 15; i++) {
            matrix[8][size - 15 + i] = formatString.charAt(i) == '1' ? 1 : 0;
            matrix[14 - i][8] = formatString.charAt(i) == '1' ? 1 : 0;
        }
        //place version string
        if (version < 7) return; // No version information for versions < 7

        String versionString = getVersionString(version);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                int bit = versionString.charAt(17 - i * 3 - j) == '1' ? 1 : 0;
                matrix[size - 11 + j][i] = bit;
                matrix[i][size - 11 + j] = bit;
            }
        }
    }

    private int chooseBestMaskPattern() {
        int lowestPenalty = Integer.MAX_VALUE;
        int bestMaskPattern = 0;

        for (int i = 0; i < 8; i++) {
            int[][] maskedMatrix = applyMask(i);
            int penalty = calculatePenaltyScore(maskedMatrix);
            if (penalty < lowestPenalty) {
                lowestPenalty = penalty;
                bestMaskPattern = i;
            }
        }
        return bestMaskPattern;
    }

    private int[][] applyMask(int maskPattern) {
        int[][] maskedMatrix = new int[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                maskedMatrix[row][col] = matrix[row][col];
                if (operatedMatrix[row][col] == 2) {
                    if (shouldSwitchBit(row, col, maskPattern)) {
                        maskedMatrix[row][col] ^= 1;
                    }
                }
            }
        }
        return maskedMatrix;
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
                return (row / 2 + col / 3) % 2 == 0;
            case 5:
                return ((row * col) % 2 + (row * col) % 3) == 0;
            case 6:
                return (((row * col) % 2 + (row * col) % 3) % 2) == 0;
            case 7:
                return (((row + col) % 2 + (row * col) % 3) % 2) == 0;
            default:
                return false;
        }
    }

    private int calculatePenaltyScore(int[][] matrix) {
        int penalty = 0;
        penalty += evaluateCondition1(matrix);
        penalty += evaluateCondition2(matrix);
        penalty += evaluateCondition3(matrix);
        penalty += evaluateCondition4(matrix);
        return penalty;
    }

    private int evaluateCondition1(int[][] matrix) {
        int penalty = 0;
        // Check rows
        for (int row = 0; row < size; row++) {
            int consecutiveCount = 1;
            for (int col = 1; col < size; col++) {
                if (matrix[row][col] == matrix[row][col - 1]) {
                    consecutiveCount++;
                    if (consecutiveCount == 5) {
                        penalty += 3;
                    } else if (consecutiveCount > 5) {
                        penalty += 1;
                    }
                } else {
                    consecutiveCount = 1;
                }
            }
        }

        // Check columns
        for (int col = 0; col < size; col++) {
            int consecutiveCount = 1;
            for (int row = 1; row < size; row++) {
                if (matrix[row][col] == matrix[row - 1][col]) {
                    consecutiveCount++;
                    if (consecutiveCount == 5) {
                        penalty += 3;
                    } else if (consecutiveCount > 5) {
                        penalty += 1;
                    }
                } else {
                    consecutiveCount = 1;
                }
            }
        }

        return penalty;
    }

    private int evaluateCondition2(int[][] matrix) {
        int penalty = 0;

        for (int row = 0; row < size - 1; row++) {
            for (int col = 0; col < size - 1; col++) {
                if (matrix[row][col] == matrix[row][col + 1] &&
                        matrix[row][col] == matrix[row + 1][col] &&
                        matrix[row][col] == matrix[row + 1][col + 1]) {
                    penalty += 3;
                }
            }
        }

        return penalty;
    }

    private int evaluateCondition3(int[][] matrix) {
        int penalty = 0;

        // Pattern 1: 10111010000 (horizontal)
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size - 11; col++) {
                if (matrix[row][col] == 1 && matrix[row][col + 1] == 0 &&
                        matrix[row][col + 2] == 1 && matrix[row][col + 3] == 1 &&
                        matrix[row][col + 4] == 1 && matrix[row][col + 5] == 0 &&
                        matrix[row][col + 6] == 1 &&
                        matrix[row][col + 7] == 0 &&
                        matrix[row][col + 8] == 0 &&
                        matrix[row][col + 9] == 0 &&
                        matrix[row][col + 10] == 0) {
                    penalty += 40;
                }
            }
        }

        // Pattern 1: 10111010000 (vertical)
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size - 11; row++) {
                if (matrix[row][col] == 1 && matrix[row + 1][col] == 0 &&
                        matrix[row + 2][col] == 1 && matrix[row + 3][col] == 1 &&
                        matrix[row + 4][col] == 1 && matrix[row + 5][col] == 0 &&
                        matrix[row + 6][col] == 1 &&
                        matrix[row + 7][col] == 0 &&
                        matrix[row + 8][col] == 0 &&
                        matrix[row + 9][col] == 0 &&
                        matrix[row + 10][col] == 0) {
                    penalty += 40;
                }
            }
        }
        // Pattern 1: 00001011101 (horizontal)
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size - 11; col++) {
                if (matrix[row][col] == 0 && matrix[row][col + 1] == 0 &&
                        matrix[row][col + 2] == 0 && matrix[row][col + 3] == 0 &&
                        matrix[row][col + 4] == 1 && matrix[row][col + 5] == 0 &&
                        matrix[row][col + 6] == 1 &&
                        matrix[row][col + 7] == 1 &&
                        matrix[row][col + 8] == 1 &&
                        matrix[row][col + 9] == 0 &&
                        matrix[row][col + 10] == 1) {
                    penalty += 40;
                }
            }
        }

        // Pattern 1: 00001011101 (vertical)
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size - 11; row++) {
                if (matrix[row][col] == 0 && matrix[row + 1][col] == 0 &&
                        matrix[row + 2][col] == 0 && matrix[row + 3][col] == 0 &&
                        matrix[row + 4][col] == 1 && matrix[row + 5][col] == 0 &&
                        matrix[row + 6][col] == 1 &&
                        matrix[row + 7][col] == 1 &&
                        matrix[row + 8][col] == 1 &&
                        matrix[row + 9][col] == 0 &&
                        matrix[row + 10][col] == 1) {
                    penalty += 40;
                }
            }
        }
        return penalty;
    }

    private int evaluateCondition4(int[][] matrix) {
        int penalty = 0;
        int totalModules = size * size;
        int darkModules = 0;

        // Count dark modules
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col] == 1) {
                    darkModules++;
                }
            }
        }

        double darkPercentage = (double) darkModules / totalModules * 100;
        int previousMultipleOfFive = (int) (Math.floor(darkPercentage / 5) * 5);
        int nextMultipleOfFive = (int) (Math.ceil(darkPercentage / 5) * 5);

        int penalty1 = Math.abs(previousMultipleOfFive - 50) / 5;
        int penalty2 = Math.abs(nextMultipleOfFive - 50) / 5;

        penalty = Math.min(penalty1, penalty2) * 10;

        return penalty;
    }

    public void printMatrix() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(operatedMatrix[i][j] == 1 ? "1" : (operatedMatrix[i][j] == 0 ? "0" : "0"));
            }
            System.out.println();
        }
    }

    public int[][] getMatrix() {
        return matrix;
    }
}
