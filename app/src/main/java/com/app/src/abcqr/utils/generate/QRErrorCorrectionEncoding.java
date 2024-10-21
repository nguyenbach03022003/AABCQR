package com.app.src.abcqr.utils.generate;

import static com.app.src.abcqr.utils.generate.QRVersion.getRequiredRemainderBits;

import java.util.Arrays;

public class QRErrorCorrectionEncoding {
    private static final int[] LOG_TABLE = new int[256];
    private static final int[] ALOG_TABLE = new int[256];
    private static final int POLYNOMIAL = 0x11d;

    static {
        int x = 1;
        LOG_TABLE[0] = 1;
        for (int i = 1; i < 256; i++) {
            int a = LOG_TABLE[i - 1] * 2 > 255 ? (LOG_TABLE[i - 1] << 1) ^ POLYNOMIAL : LOG_TABLE[i - 1] << 1;
            LOG_TABLE[i] = a;
            ALOG_TABLE[a] = i;
        }
        ALOG_TABLE[1] = 0;
        ALOG_TABLE[2] = 1;
    }

    private static int[] reverseIntArr(int[] inputArr) {
        for (int i = 0; i < inputArr.length / 2; i++) {
            int temp = inputArr[i];
            inputArr[i] = inputArr[inputArr.length - i - 1];
            inputArr[inputArr.length - i - 1] = temp;
        }
        return inputArr;

    }

    private static int[] generateGeneratorPolynomial(int degree) {
        int[] generator = new int[degree + 1];
        generator[0] = 1;
        for (int i = 1; i <= degree; i++) {
            generator[i] = 1;
            for (int j = i - 1; j > 0; j--) {
                generator[j] = generator[j - 1] ^ LOG_TABLE[(ALOG_TABLE[generator[j]] + i - 1) % 255];
            }
            generator[0] = LOG_TABLE[(ALOG_TABLE[generator[0]] + i - 1) % 255];
        }
        return generator;
    }

    private static int[] dividePolynomials(int[] message, int[] generator) {
        int[] remainder = Arrays.copyOf(message, message.length);
        for (int i = 0; i < message.length - generator.length + 1; i++) { // so lan chia
            //  System.out.println("Loop number:"+ Integer.toString(i+1));
            //  System.out.println(Arrays.toString(remainder));
            //  System.out.println(Arrays.toString(message));
            //  System.out.println(Arrays.toString(generator));
            int[] scaledGenerator = new int[generator.length];
            int scaleFactor = remainder[i];
            for (int j = 0; j < generator.length; j++) {
                scaledGenerator[j] = LOG_TABLE[(ALOG_TABLE[generator[j]] + ALOG_TABLE[scaleFactor]) % 255];
                remainder[j + i] = remainder[j + i] ^ scaledGenerator[j];
            }
        }
        return Arrays.copyOfRange(remainder, message.length - generator.length + 1, remainder.length);
    }

    public static int[] generateErrorCorrectionCodewords(int[] message, int errorCorrectionCodewords) {
        int[] generator = generateGeneratorPolynomial(errorCorrectionCodewords);
        int[] messagePolynomial = new int[message.length + errorCorrectionCodewords];
        System.arraycopy(message, 0, messagePolynomial, 0, message.length);
        int[] remainder = dividePolynomials(messagePolynomial, reverseIntArr(generator));
        return remainder;
    }

    public static String interleaveAndConvertToBinary(int[] message, int version, QRVersion.VersionECInfo versionECInfo) {
        int totalDataCodewords = versionECInfo.getTotalDataCodewords();
        int ecCodewordsPerBlock = versionECInfo.getEcCodewordsPerBlock();
        int numBlocksGroup1 = versionECInfo.getNumBlocksGroup1();
        int numDataCodewordsGroup1 = versionECInfo.getNumDataCodewordsGroup1();
        int numBlocksGroup2 = versionECInfo.getNumBlocksGroup2();
        int numDataCodewordsGroup2 = versionECInfo.getNumDataCodewordsGroup2();

        int totalBlocks = numBlocksGroup1 + numBlocksGroup2;
        int[][] dataBlocks = new int[totalBlocks][];
        int dataIndex = 0;

        for (int i = 0; i < numBlocksGroup1; i++) {
            dataBlocks[i] = Arrays.copyOfRange(message, dataIndex, dataIndex + numDataCodewordsGroup1);
            dataIndex += numDataCodewordsGroup1;
        }
        for (int i = numBlocksGroup1; i < totalBlocks; i++) {
            dataBlocks[i] = Arrays.copyOfRange(message, dataIndex, dataIndex + numDataCodewordsGroup2);
            dataIndex += numDataCodewordsGroup2;
        }

        int[][] ecBlocks = new int[totalBlocks][];
        for (int i = 0; i < totalBlocks; i++) {
            ecBlocks[i] = generateErrorCorrectionCodewords(dataBlocks[i], ecCodewordsPerBlock);
        }

        StringBuilder binaryString = new StringBuilder();
        for (int i = 0; i < totalDataCodewords; i++) {
            for (int j = 0; j < totalBlocks; j++) {
                if (i < dataBlocks[j].length) {
                    binaryString.append(String.format("%8s", Integer.toBinaryString(dataBlocks[j][i])).replace(' ', '0'));
                }
            }
        }
        for (int i = 0; i < ecCodewordsPerBlock; i++) {
            for (int j = 0; j < totalBlocks; j++) {
                binaryString.append(String.format("%8s", Integer.toBinaryString(ecBlocks[j][i])).replace(' ', '0'));
            }
        }


        int remainderBitsCount = getRequiredRemainderBits(version);
        for (int i = 0; i < remainderBitsCount; i++) {
            binaryString.append('0');
        }

        return binaryString.toString();
    }
}
