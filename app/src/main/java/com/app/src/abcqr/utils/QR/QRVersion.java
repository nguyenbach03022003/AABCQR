package com.app.src.abcqr.utils.QR;

import android.util.Pair;

public class QRVersion {
    public enum ErrorCorrectionLevel {
        L, M, Q, H
    }

    private static final int[] REQUIRED_REMAINDER_BITS = {
            0, 7, 7, 7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0
    };

    public static class VersionECInfo {
        private final int totalDataCodewords;
        private final int ecCodewordsPerBlock;
        private final int numBlocksGroup1;
        private final int numDataCodewordsInGroup1Block;
        private final int numBlocksGroup2;
        private final int numDataCodewordsInGroup2Block;

        public VersionECInfo(int totalDataCodewords, int ecCodewordsPerBlock, int numBlocksGroup1, int numDataCodewordsInGroup1Block, int numBlocksGroup2, int numDataCodewordsInGroup2Block) {
            this.totalDataCodewords = totalDataCodewords;
            this.ecCodewordsPerBlock = ecCodewordsPerBlock;
            this.numBlocksGroup1 = numBlocksGroup1;
            this.numDataCodewordsInGroup1Block = numDataCodewordsInGroup1Block;
            this.numBlocksGroup2 = numBlocksGroup2;
            this.numDataCodewordsInGroup2Block = numDataCodewordsInGroup2Block;
        }

        public int getTotalDataCodewords() {
            return totalDataCodewords;
        }

        public int getEcCodewordsPerBlock() {
            return ecCodewordsPerBlock;
        }

        public int getNumBlocksGroup1() {
            return numBlocksGroup1;
        }

        public int getNumDataCodewordsInGroup1Block() {
            return numDataCodewordsInGroup1Block;
        }

        public int getNumBlocksGroup2() {
            return numBlocksGroup2;
        }

        public int getNumDataCodewordsInGroup2Block() {
            return numDataCodewordsInGroup2Block;
        }

        @Override
        public String toString() {
            return "VersionECInfo{" +
                    "totalDataCodewords=" + totalDataCodewords +
                    ", ecCodewordsPerBlock=" + ecCodewordsPerBlock +
                    ", numBlocksGroup1=" + numBlocksGroup1 +
                    ", numDataCodewordsInGroup1Block=" + numDataCodewordsInGroup1Block +
                    ", numBlocksGroup2=" + numBlocksGroup2 +
                    ", numDataCodewordsInGroup2Block=" + numDataCodewordsInGroup2Block +
                    '}';
        }
    }

    private static final int[] BYTE_MODE_CAPACITY_L = {
            17, 32, 53, 78, 106, 134, 154, 192, 230, 271, 321, 367, 425, 458, 520, 586, 644, 718, 792, 858,
            929, 1003, 1091, 1171, 1273, 1367, 1465, 1528, 1628, 1732, 1840, 1952, 2068, 2188, 2303, 2431,
            2563, 2699, 2809, 2953
    };

    private static final int[] BYTE_MODE_CAPACITY_M = {
            14, 26, 42, 62, 84, 106, 122, 152, 180, 213, 251, 287, 331, 362, 412, 450, 504, 560, 624, 666,
            711, 779, 857, 911, 997, 1059, 1125, 1190, 1264, 1370, 1452, 1538, 1628, 1722, 1809, 1911, 1989,
            2099, 2213, 2331
    };

    private static final int[] BYTE_MODE_CAPACITY_Q = {
            11, 20, 32, 46, 60, 74, 86, 108, 130, 151, 177, 203, 241, 258, 292, 322, 364, 394, 442, 482,
            509, 565, 611, 661, 715, 751, 805, 868, 908, 982, 1030, 1112, 1168, 1228, 1283, 1351, 1423,
            1499, 1579, 1663
    };

    private static final int[] BYTE_MODE_CAPACITY_H = {
            7, 14, 24, 34, 44, 58, 64, 84, 98, 119, 137, 155, 177, 194, 220, 250, 280, 310, 338, 382,
            403, 439, 461, 511, 535, 593, 625, 658, 698, 742, 790, 842, 898, 958, 983, 1051, 1093, 1139,
            1219, 1273
    };
    private static final String[][] FORMAT_INFORMATION_STRINGS = {
            {"111011111000100", "111001011110011", "111110110101010", "111100010011101", "110011000101111", "110001100011000", "110110001000001", "110100101110110"},
            {"101010000010010", "101000100100101", "101111001111100", "101101101001011", "100010111111001", "100000011001110", "100111110010111", "100101010100000"},
            {"011010101011111", "011000001101000", "011111100110001", "011101000000110", "010010010110100", "010000110000011", "010111011011010", "010101111101101"},
            {"001011010001001", "001001110111110", "001110011100111", "001100111010000", "000011101100010", "000001001010101", "000110100001100", "000100000111011"}
    };

    private static final String[] VERSION_INFORMATION_STRINGS = {
            "000111110010010100", "001000010110111100", "001001101010011001", "001010010011010011", "001011101111110110",
            "001100011101100010", "001101100001000111", "001110011000001101", "001111100100101000", "010000101101111000",
            "010001010001011101", "010010101000010111", "010011010100110010", "010100100110100110", "010101011010000011",
            "010110100011001001", "010111011111101100", "011000111011000100", "011001000111100001", "011010111110101011",
            "011011000010001110", "011100110000011010", "011101001100111111", "011110110101110101", "011111001001010000",
            "100000100111010101", "100001011011110000", "100010100010111010", "100011011110011111", "100100101100001011",
            "100101010000101110", "100110101001100100", "100111010101000001", "101000110001101001"
    };
    // Add version information
    private static final VersionECInfo[][] VERSION_EC_INFO = new VersionECInfo[40][4];

    static {
        // Example data, should be filled with real QR version info
        VERSION_EC_INFO[0][0] = new VersionECInfo(19, 7, 1, 19, 0, 0); // Version 1, Level L
        VERSION_EC_INFO[0][1] = new VersionECInfo(16, 10, 1, 16, 0, 0); // Version 1, Level M
        VERSION_EC_INFO[0][2] = new VersionECInfo(13, 13, 1, 13, 0, 0); // Version 1, Level Q
        VERSION_EC_INFO[0][3] = new VersionECInfo(9, 17, 1, 9, 0, 0);  // Version 1, Level H

        VERSION_EC_INFO[1][0] = new VersionECInfo(34, 10, 1, 34, 0, 0); // Version 2, Level L
        VERSION_EC_INFO[1][1] = new VersionECInfo(28, 16, 1, 28, 0, 0); // Version 2, Level M
        VERSION_EC_INFO[1][2] = new VersionECInfo(22, 22, 1, 22, 0, 0); // Version 2, Level Q
        VERSION_EC_INFO[1][3] = new VersionECInfo(16, 28, 1, 16, 0, 0); // Version 2, Level H

        VERSION_EC_INFO[2][0] = new VersionECInfo(55, 15, 1, 55, 0, 0); // Version 3, Level L
        VERSION_EC_INFO[2][1] = new VersionECInfo(44, 26, 1, 44, 0, 0); // Version 3, Level M
        VERSION_EC_INFO[2][2] = new VersionECInfo(34, 18, 2, 17, 0, 0); // Version 3, Level Q
        VERSION_EC_INFO[2][3] = new VersionECInfo(26, 22, 2, 13, 0, 0); // Version 3, Level H

        VERSION_EC_INFO[3][0] = new VersionECInfo(80, 20, 1, 80, 0, 0); // Version 4, Level L
        VERSION_EC_INFO[3][1] = new VersionECInfo(64, 18, 2, 32, 0, 0); // Version 4, Level M
        VERSION_EC_INFO[3][2] = new VersionECInfo(48, 26, 2, 24, 0, 0); // Version 4, Level Q
        VERSION_EC_INFO[3][3] = new VersionECInfo(36, 16, 4, 9, 0, 0);  // Version 4, Level H

        VERSION_EC_INFO[4][0] = new VersionECInfo(108, 26, 1, 108, 0, 0); // Version 5, Level L
        VERSION_EC_INFO[4][1] = new VersionECInfo(86, 24, 2, 43, 0, 0);  // Version 5, Level M
        VERSION_EC_INFO[4][2] = new VersionECInfo(62, 18, 2, 15, 2, 16); // Version 5, Level Q
        VERSION_EC_INFO[4][3] = new VersionECInfo(46, 22, 2, 11, 2, 12); // Version 5, Level H

        VERSION_EC_INFO[5][0] = new VersionECInfo(136, 18, 2, 68, 0, 0); // Version 6, Level L
        VERSION_EC_INFO[5][1] = new VersionECInfo(108, 16, 4, 27, 0, 0); // Version 6, Level M
        VERSION_EC_INFO[5][2] = new VersionECInfo(76, 24, 4, 19, 0, 0);  // Version 6, Level Q
        VERSION_EC_INFO[5][3] = new VersionECInfo(60, 28, 4, 15, 0, 0);  // Version 6, Level H

        VERSION_EC_INFO[6][0] = new VersionECInfo(156, 20, 2, 78, 0, 0); // Version 7, Level L
        VERSION_EC_INFO[6][1] = new VersionECInfo(124, 18, 4, 31, 0, 0); // Version 7, Level M
        VERSION_EC_INFO[6][2] = new VersionECInfo(88, 18, 2, 14, 4, 15); // Version 7, Level Q
        VERSION_EC_INFO[6][3] = new VersionECInfo(66, 26, 4, 13, 1, 14); // Version 7, Level H

        VERSION_EC_INFO[7][0] = new VersionECInfo(194, 24, 2, 97, 0, 0); // Version 8, Level L
        VERSION_EC_INFO[7][1] = new VersionECInfo(154, 22, 2, 38, 2, 39); // Version 8, Level M
        VERSION_EC_INFO[7][2] = new VersionECInfo(110, 22, 4, 18, 2, 19); // Version 8, Level Q
        VERSION_EC_INFO[7][3] = new VersionECInfo(86, 26, 4, 14, 2, 15);  // Version 8, Level H

        VERSION_EC_INFO[8][0] = new VersionECInfo(232, 30, 2, 116, 0, 0); // Version 9, Level L
        VERSION_EC_INFO[8][1] = new VersionECInfo(182, 22, 3, 36, 2, 37); // Version 9, Level M
        VERSION_EC_INFO[8][2] = new VersionECInfo(132, 20, 4, 16, 4, 17); // Version 9, Level Q
        VERSION_EC_INFO[8][3] = new VersionECInfo(100, 24, 4, 12, 4, 13); // Version 9, Level H

        VERSION_EC_INFO[9][0] = new VersionECInfo(274, 18, 2, 68, 2, 69); // Version 10, Level L
        VERSION_EC_INFO[9][1] = new VersionECInfo(216, 26, 4, 43, 1, 44); // Version 10, Level M
        VERSION_EC_INFO[9][2] = new VersionECInfo(154, 24, 6, 19, 2, 20); // Version 10, Level Q
        VERSION_EC_INFO[9][3] = new VersionECInfo(122, 28, 6, 15, 2, 16); // Version 10, Level H

        VERSION_EC_INFO[10][0] = new VersionECInfo(324, 20, 4, 81, 0, 0); // Version 11, Level L
        VERSION_EC_INFO[10][1] = new VersionECInfo(254, 30, 1, 50, 4, 51); // Version 11, Level M
        VERSION_EC_INFO[10][2] = new VersionECInfo(180, 28, 4, 22, 4, 23); // Version 11, Level Q
        VERSION_EC_INFO[10][3] = new VersionECInfo(140, 24, 3, 12, 8, 13); // Version 11, Level H

        VERSION_EC_INFO[11][0] = new VersionECInfo(370, 24, 2, 92, 2, 93); // Version 12, Level L
        VERSION_EC_INFO[11][1] = new VersionECInfo(290, 22, 6, 36, 2, 37); // Version 12, Level M
        VERSION_EC_INFO[11][2] = new VersionECInfo(206, 26, 4, 20, 6, 21); // Version 12, Level Q
        VERSION_EC_INFO[11][3] = new VersionECInfo(158, 28, 7, 14, 4, 15); // Version 12, Level H

        VERSION_EC_INFO[12][0] = new VersionECInfo(428, 26, 4, 107, 0, 0); // Version 13, Level L
        VERSION_EC_INFO[12][1] = new VersionECInfo(334, 22, 8, 37, 1, 38); // Version 13, Level M
        VERSION_EC_INFO[12][2] = new VersionECInfo(244, 24, 8, 20, 4, 21); // Version 13, Level Q
        VERSION_EC_INFO[12][3] = new VersionECInfo(180, 22, 12, 11, 4, 12); // Version 13, Level H

        VERSION_EC_INFO[13][0] = new VersionECInfo(461, 30, 3, 115, 1, 116); // Version 14, Level L
        VERSION_EC_INFO[13][1] = new VersionECInfo(365, 24, 4, 40, 5, 41); // Version 14, Level M
        VERSION_EC_INFO[13][2] = new VersionECInfo(261, 20, 11, 16, 5, 17); // Version 14, Level Q
        VERSION_EC_INFO[13][3] = new VersionECInfo(197, 24, 11, 12, 5, 13); // Version 14, Level H

        VERSION_EC_INFO[14][0] = new VersionECInfo(523, 22, 5, 87, 1, 88); // Version 15, Level L
        VERSION_EC_INFO[14][1] = new VersionECInfo(415, 24, 5, 41, 5, 42); // Version 15, Level M
        VERSION_EC_INFO[14][2] = new VersionECInfo(295, 30, 5, 24, 7, 25); // Version 15, Level Q
        VERSION_EC_INFO[14][3] = new VersionECInfo(223, 24, 11, 12, 7, 13); // Version 15, Level H

        VERSION_EC_INFO[15][0] = new VersionECInfo(589, 24, 5, 98, 1, 99); // Version 16, Level L
        VERSION_EC_INFO[15][1] = new VersionECInfo(453, 28, 7, 45, 3, 46); // Version 16, Level M
        VERSION_EC_INFO[15][2] = new VersionECInfo(325, 24, 15, 19, 2, 20); // Version 16, Level Q
        VERSION_EC_INFO[15][3] = new VersionECInfo(253, 30, 3, 15, 13, 16); // Version 16, Level H

        VERSION_EC_INFO[16][0] = new VersionECInfo(647, 28, 1, 107, 5, 108); // Version 17, Level L
        VERSION_EC_INFO[16][1] = new VersionECInfo(507, 28, 10, 46, 1, 47); // Version 17, Level M
        VERSION_EC_INFO[16][2] = new VersionECInfo(367, 28, 1, 22, 15, 23); // Version 17, Level Q
        VERSION_EC_INFO[16][3] = new VersionECInfo(283, 28, 2, 14, 17, 15); // Version 17, Level H

        VERSION_EC_INFO[17][0] = new VersionECInfo(721, 30, 5, 120, 1, 121); // Version 18, Level L
        VERSION_EC_INFO[17][1] = new VersionECInfo(563, 26, 9, 43, 4, 44); // Version 18, Level M
        VERSION_EC_INFO[17][2] = new VersionECInfo(397, 28, 17, 22, 1, 23); // Version 18, Level Q
        VERSION_EC_INFO[17][3] = new VersionECInfo(313, 28, 2, 14, 19, 15); // Version 18, Level H

        VERSION_EC_INFO[18][0] = new VersionECInfo(795, 28, 3, 113, 4, 114); // Version 19, Level L
        VERSION_EC_INFO[18][1] = new VersionECInfo(627, 26, 3, 44, 11, 45); // Version 19, Level M
        VERSION_EC_INFO[18][2] = new VersionECInfo(445, 26, 17, 21, 4, 22); // Version 19, Level Q
        VERSION_EC_INFO[18][3] = new VersionECInfo(341, 26, 9, 13, 16, 14); // Version 19, Level H

        VERSION_EC_INFO[19][0] = new VersionECInfo(861, 28, 3, 107, 5, 108); // Version 20, Level L
        VERSION_EC_INFO[19][1] = new VersionECInfo(669, 26, 3, 41, 13, 42); // Version 20, Level M
        VERSION_EC_INFO[19][2] = new VersionECInfo(485, 30, 15, 24, 5, 25); // Version 20, Level Q
        VERSION_EC_INFO[19][3] = new VersionECInfo(385, 28, 15, 15, 10, 16); // Version 20, Level H

        VERSION_EC_INFO[20][0] = new VersionECInfo(932, 28, 4, 116, 4, 117); // Version 21, Level L
        VERSION_EC_INFO[20][1] = new VersionECInfo(714, 26, 17, 42, 0, 0);  // Version 21, Level M
        VERSION_EC_INFO[20][2] = new VersionECInfo(512, 28, 17, 22, 6, 23); // Version 21, Level Q
        VERSION_EC_INFO[20][3] = new VersionECInfo(406, 30, 19, 16, 6, 17); // Version 21, Level H

        VERSION_EC_INFO[21][0] = new VersionECInfo(1006, 28, 2, 111, 7, 112); // Version 22, Level L
        VERSION_EC_INFO[21][1] = new VersionECInfo(782, 28, 17, 46, 0, 0);   // Version 22, Level M
        VERSION_EC_INFO[21][2] = new VersionECInfo(568, 30, 7, 24, 16, 25);  // Version 22, Level Q
        VERSION_EC_INFO[21][3] = new VersionECInfo(442, 24, 34, 13, 0, 0);   // Version 22, Level H

        VERSION_EC_INFO[22][0] = new VersionECInfo(1094, 30, 4, 121, 5, 122); // Version 23, Level L
        VERSION_EC_INFO[22][1] = new VersionECInfo(860, 28, 4, 47, 14, 48);   // Version 23, Level M
        VERSION_EC_INFO[22][2] = new VersionECInfo(614, 30, 11, 24, 14, 25);  // Version 23, Level Q
        VERSION_EC_INFO[22][3] = new VersionECInfo(464, 30, 16, 15, 14, 16);  // Version 23, Level H

        VERSION_EC_INFO[23][0] = new VersionECInfo(1174, 30, 6, 117, 4, 118); // Version 24, Level L
        VERSION_EC_INFO[23][1] = new VersionECInfo(914, 28, 6, 45, 14, 46);   // Version 24, Level M
        VERSION_EC_INFO[23][2] = new VersionECInfo(664, 30, 11, 24, 16, 25);  // Version 24, Level Q
        VERSION_EC_INFO[23][3] = new VersionECInfo(514, 30, 30, 16, 2, 17);   // Version 24, Level H

        VERSION_EC_INFO[24][0] = new VersionECInfo(1276, 26, 8, 106, 4, 107); // Version 25, Level L
        VERSION_EC_INFO[24][1] = new VersionECInfo(1000, 28, 8, 47, 13, 48);  // Version 25, Level M
        VERSION_EC_INFO[24][2] = new VersionECInfo(718, 30, 7, 24, 22, 25);   // Version 25, Level Q
        VERSION_EC_INFO[24][3] = new VersionECInfo(538, 30, 22, 15, 13, 16);  // Version 25, Level H

        VERSION_EC_INFO[25][0] = new VersionECInfo(1370, 28, 10, 114, 2, 115); // Version 26, Level L
        VERSION_EC_INFO[25][1] = new VersionECInfo(1062, 28, 19, 46, 4, 47);   // Version 26, Level M
        VERSION_EC_INFO[25][2] = new VersionECInfo(754, 28, 28, 22, 6, 23);    // Version 26, Level Q
        VERSION_EC_INFO[25][3] = new VersionECInfo(596, 30, 33, 16, 4, 17);    // Version 26, Level H

        VERSION_EC_INFO[26][0] = new VersionECInfo(1468, 30, 8, 122, 4, 123); // Version 27, Level L
        VERSION_EC_INFO[26][1] = new VersionECInfo(1128, 28, 22, 45, 3, 46);  // Version 27, Level M
        VERSION_EC_INFO[26][2] = new VersionECInfo(808, 30, 8, 23, 26, 24);   // Version 27, Level Q
        VERSION_EC_INFO[26][3] = new VersionECInfo(628, 30, 12, 15, 28, 16);  // Version 27, Level H

        VERSION_EC_INFO[27][0] = new VersionECInfo(1531, 30, 3, 117, 10, 118); // Version 28, Level L
        VERSION_EC_INFO[27][1] = new VersionECInfo(1193, 28, 3, 45, 23, 46);   // Version 28, Level M
        VERSION_EC_INFO[27][2] = new VersionECInfo(871, 30, 4, 24, 31, 25);    // Version 28, Level Q
        VERSION_EC_INFO[27][3] = new VersionECInfo(661, 30, 11, 15, 31, 16);   // Version 28, Level H

        VERSION_EC_INFO[28][0] = new VersionECInfo(1631, 30, 7, 116, 7, 117); // Version 29, Level L
        VERSION_EC_INFO[28][1] = new VersionECInfo(1267, 28, 21, 45, 7, 46);  // Version 29, Level M
        VERSION_EC_INFO[28][2] = new VersionECInfo(911, 30, 1, 23, 37, 24);   // Version 29, Level Q
        VERSION_EC_INFO[28][3] = new VersionECInfo(701, 30, 19, 15, 26, 16);  // Version 29, Level H

        VERSION_EC_INFO[29][0] = new VersionECInfo(1735, 30, 5, 115, 10, 116); // Version 30, Level L
        VERSION_EC_INFO[29][1] = new VersionECInfo(1373, 28, 19, 47, 10, 48);  // Version 30, Level M
        VERSION_EC_INFO[29][2] = new VersionECInfo(985, 30, 15, 24, 25, 25);   // Version 30, Level Q
        VERSION_EC_INFO[29][3] = new VersionECInfo(745, 30, 23, 15, 25, 16);   // Version 30, Level H

        VERSION_EC_INFO[30][0] = new VersionECInfo(1843, 30, 13, 115, 3, 116); // Version 31, Level L
        VERSION_EC_INFO[30][1] = new VersionECInfo(1455, 28, 2, 46, 29, 47);   // Version 31, Level M
        VERSION_EC_INFO[30][2] = new VersionECInfo(1033, 30, 42, 24, 1, 25);   // Version 31, Level Q
        VERSION_EC_INFO[30][3] = new VersionECInfo(793, 30, 23, 15, 28, 16);   // Version 31, Level H

        VERSION_EC_INFO[31][0] = new VersionECInfo(1955, 30, 17, 115, 0, 0);   // Version 32, Level L
        VERSION_EC_INFO[31][1] = new VersionECInfo(1541, 28, 10, 46, 23, 47);  // Version 32, Level M
        VERSION_EC_INFO[31][2] = new VersionECInfo(1115, 30, 10, 24, 35, 25);  // Version 32, Level Q
        VERSION_EC_INFO[31][3] = new VersionECInfo(845, 30, 19, 15, 35, 16);   // Version 32, Level H

        VERSION_EC_INFO[32][0] = new VersionECInfo(2071, 30, 17, 115, 1, 116); // Version 33, Level L
        VERSION_EC_INFO[32][1] = new VersionECInfo(1631, 28, 14, 46, 21, 47);  // Version 33, Level M
        VERSION_EC_INFO[32][2] = new VersionECInfo(1171, 30, 29, 24, 19, 25);  // Version 33, Level Q
        VERSION_EC_INFO[32][3] = new VersionECInfo(901, 30, 11, 15, 46, 16);   // Version 33, Level H

        VERSION_EC_INFO[33][0] = new VersionECInfo(2191, 30, 13, 115, 6, 116); // Version 34, Level L
        VERSION_EC_INFO[33][1] = new VersionECInfo(1725, 28, 14, 46, 23, 47);  // Version 34, Level M
        VERSION_EC_INFO[33][2] = new VersionECInfo(1231, 30, 44, 24, 7, 25);   // Version 34, Level Q
        VERSION_EC_INFO[33][3] = new VersionECInfo(961, 30, 59, 16, 1, 17);    // Version 34, Level H

        VERSION_EC_INFO[34][0] = new VersionECInfo(2306, 30, 12, 121, 7, 122); // Version 35, Level L
        VERSION_EC_INFO[34][1] = new VersionECInfo(1812, 28, 12, 47, 26, 48);  // Version 35, Level M
        VERSION_EC_INFO[34][2] = new VersionECInfo(1286, 30, 39, 24, 14, 25);  // Version 35, Level Q
        VERSION_EC_INFO[34][3] = new VersionECInfo(986, 30, 22, 15, 41, 16);   // Version 35, Level H

        VERSION_EC_INFO[35][0] = new VersionECInfo(2434, 30, 6, 121, 14, 122); // Version 36, Level L
        VERSION_EC_INFO[35][1] = new VersionECInfo(1914, 28, 6, 47, 34, 48);   // Version 36, Level M
        VERSION_EC_INFO[35][2] = new VersionECInfo(1354, 30, 46, 24, 10, 25);  // Version 36, Level Q
        VERSION_EC_INFO[35][3] = new VersionECInfo(1054, 30, 2, 15, 64, 16);   // Version 36, Level H

        VERSION_EC_INFO[36][0] = new VersionECInfo(2566, 30, 17, 122, 4, 123); // Version 37, Level L
        VERSION_EC_INFO[36][1] = new VersionECInfo(1992, 28, 29, 46, 14, 47);  // Version 37, Level M
        VERSION_EC_INFO[36][2] = new VersionECInfo(1426, 30, 49, 24, 10, 25);  // Version 37, Level Q
        VERSION_EC_INFO[36][3] = new VersionECInfo(1096, 30, 24, 15, 46, 16);  // Version 37, Level H

        VERSION_EC_INFO[37][0] = new VersionECInfo(2702, 30, 4, 122, 18, 123); // Version 38, Level L
        VERSION_EC_INFO[37][1] = new VersionECInfo(2102, 28, 13, 46, 32, 47);  // Version 38, Level M
        VERSION_EC_INFO[37][2] = new VersionECInfo(1502, 30, 48, 24, 14, 25);  // Version 38, Level Q
        VERSION_EC_INFO[37][3] = new VersionECInfo(1142, 30, 42, 15, 32, 16);  // Version 38, Level H

        VERSION_EC_INFO[38][0] = new VersionECInfo(2812, 30, 20, 117, 4, 118); // Version 39, Level L
        VERSION_EC_INFO[38][1] = new VersionECInfo(2216, 28, 40, 47, 7, 48);   // Version 39, Level M
        VERSION_EC_INFO[38][2] = new VersionECInfo(1582, 30, 43, 24, 22, 25);  // Version 39, Level Q
        VERSION_EC_INFO[38][3] = new VersionECInfo(1222, 30, 10, 15, 67, 16);  // Version 39, Level H

        VERSION_EC_INFO[39][0] = new VersionECInfo(2956, 30, 19, 118, 6, 119); // Version 40, Level L
        VERSION_EC_INFO[39][1] = new VersionECInfo(2334, 28, 18, 47, 31, 48);  // Version 40, Level M
        VERSION_EC_INFO[39][2] = new VersionECInfo(1666, 30, 34, 24, 34, 25);  // Version 40, Level Q
        VERSION_EC_INFO[39][3] = new VersionECInfo(1276, 30, 20, 15, 61, 16);  // Version 40, Level H

    }

    public static int getVersionCapacity(int length, ErrorCorrectionLevel level) {
        int[] capacities;
        switch (level) {
            case L:
                capacities = BYTE_MODE_CAPACITY_L;
                break;
            case M:
                capacities = BYTE_MODE_CAPACITY_M;
                break;
            case Q:
                capacities = BYTE_MODE_CAPACITY_Q;
                break;
            case H:
                capacities = BYTE_MODE_CAPACITY_H;
                break;
            default:
                throw new IllegalArgumentException("Unknown error correction level: " + level);
        }

        for (int i = 0; i < capacities.length; i++) {
            if (length <= capacities[i]) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("Data too long for QR code version 40.");
    }

    public static VersionECInfo getVersionECInfo(int version, ErrorCorrectionLevel level) {
        return VERSION_EC_INFO[version - 1][level.ordinal()];
    }

    public static int getRequiredRemainderBits(int version) {
        if (version < 1 || version > 40) {
            throw new IllegalArgumentException("Invalid QR code version: " + version);
        }
        return REQUIRED_REMAINDER_BITS[version - 1];
    }

    public static String getFormatInformationString(int errorCorrectionLevel, int maskPattern) {
        return FORMAT_INFORMATION_STRINGS[errorCorrectionLevel][maskPattern];
    }

    public static String getVersionString(int version) {
        return version >= 7 ? VERSION_INFORMATION_STRINGS[version - 7] : "";
    }
    public static Pair<Boolean, int[]> decodeFormatInfo(String formatBits){
        int[] formatAndMask = new int[2];
        for(int i = 0; i < 4; i++){
            for(int j = 0 ; j < 8; j++){
                if(formatBits.equals(FORMAT_INFORMATION_STRINGS[i][j])){
                    formatAndMask[0] = i;
                    formatAndMask[1] = j;
                    return new Pair<>(true, formatAndMask);
                }
            }
        }
        return new Pair<>(false, formatAndMask);
    }
//    public static int decodeVersionInfo(String versionBits){
//    }
}