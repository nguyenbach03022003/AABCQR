package com.app.src.abcqr.utils.QR.generate;

import static com.app.src.abcqr.utils.QR.QRVersion.getRequiredRemainderBits;

import com.app.src.abcqr.utils.QR.QRVersion;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class QRErrorCorrection {
    private static final int[] LOG_TABLE = new int[256];
    private static final int[] ALOG_TABLE = new int[256];
    private static final int POLYNOMIAL = 0x11d;

    static    {
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
    //------------------------------Helper funtions------------------------------------------------------
    private static int[] reverseIntArr(int[] inputArr) {
        for (int i = 0; i < inputArr.length / 2; i++) {
            int temp = inputArr[i];
            inputArr[i] = inputArr[inputArr.length - i - 1];
            inputArr[inputArr.length - i - 1] = temp;
        }
        return inputArr;

    }
    private static int[] binaryStringToCodewords(String binaryString, int remainderBits) {
        if (remainderBits > 0) { //remove remainder bits
            binaryString = binaryString.substring(0, binaryString.length() - remainderBits);
        }

        int codewordsCount = binaryString.length() / 8;
        int[] codewords = new int[codewordsCount];

        for (int i = 0; i < codewordsCount; i++) {
            String byteString = binaryString.substring(i * 8, (i + 1) * 8);
            codewords[i] = Integer.parseInt(byteString, 2);
        }

        return codewords;
    }
    //------------------------------GF(256) operator----------------------------------------------------
    private static int gfMultiply(int a, int b) {
        if (a == 0 || b == 0) return 0;
        int logSum = ALOG_TABLE[a] + ALOG_TABLE[b];
        if (logSum >= 255) logSum -= 255;
        return LOG_TABLE[logSum];
    }

    private static int gfDivide(int a, int b) {
        if (b == 0) throw new ArithmeticException("Division by zero in GF(2^8)");
        if (a == 0) return 0;
        int logDiff = ALOG_TABLE[a] - ALOG_TABLE[b];
        if (logDiff < 0) logDiff += 255;
        return LOG_TABLE[logDiff];
    }

    private static int gfAdd(int a, int b) {
        return a ^ b;
    }

    private static int gfSubtract(int a, int b) {
        return a ^ b;
    }
    //------------------------------GF(256) polynomial operator funtions----------------------------------------------------
    private static int[] dividePolynomials(int[] message, int[] generator) {
        int[] remainder = Arrays.copyOf(message, message.length);
        for (int i = 0; i < message.length - generator.length + 1; i++) { // so lan chia
            int[] scaledGenerator = new int[generator.length];
            int scaleFactor = remainder[i];
            for (int j = 0; j < generator.length; j++) {
                scaledGenerator[j] = gfMultiply(generator[j], scaleFactor);
                remainder[j + i] = remainder[j + i] ^ scaledGenerator[j];
            }
        }
        return Arrays.copyOfRange(remainder, message.length - generator.length + 1, remainder.length);
    }
    public static int[] multiplyPolynomials(int[] a, int[] b) {
        int[] result = new int[a.length + b.length - 1];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                result[i + j] = gfAdd(result[i + j], gfMultiply(a[i], b[j]));
            }
        }
        return result;
    }
    public static int evaluatePolynomial(int[] polynomial, int x) { //use horner method
        int result = 0;
        for (int coefficient : polynomial) {
            result = gfMultiply(result, x);
            result = gfAdd(result, coefficient);
        }
        return result;
    }
    //------------------------------------Encoding funtions--------------------------------------------------------------------
    public static int[] generateGeneratorPolynomial(int degree) {
        int[] generator = new int[degree + 1];
        generator[0] = 1;
        for (int i = 1; i <= degree; i++) {
            generator[i] = 1;
            for (int j = i - 1; j > 0; j--) {
                generator[j] = generator[j - 1] ^ LOG_TABLE[(ALOG_TABLE[generator[j]] + i - 1) % 255];
            }
            generator[0] = LOG_TABLE[(ALOG_TABLE[generator[0]] + i - 1) % 255];
        }
        return reverseIntArr(generator);
    }
    public static int[] generateErrorCorrectionCodewords(int[] message, int errorCorrectionCodewords) {
        int[] generator = generateGeneratorPolynomial(errorCorrectionCodewords);
        int[] messagePolynomial = new int[message.length + errorCorrectionCodewords];
        System.arraycopy(message, 0, messagePolynomial, 0, message.length);
        int[] remainder = dividePolynomials(messagePolynomial, generator);
        return remainder;
    }
    public static String interleaveAndConvertToBinary(int[] message, int version, QRVersion.VersionECInfo versionECInfo) {
        //Extract parameter for interleaving
        int totalDataCodewords = versionECInfo.getTotalDataCodewords();
        int ecCodewordsPerBlock = versionECInfo.getEcCodewordsPerBlock();
        int numBlocksGroup1 = versionECInfo.getNumBlocksGroup1();
        int numDataCodewordsInGroup1Block = versionECInfo.getNumDataCodewordsInGroup1Block();

        int numBlocksGroup2 = versionECInfo.getNumBlocksGroup2();

        int numDataCodewordsInGroup2Block = versionECInfo.getNumDataCodewordsInGroup2Block();
        // 1 codeword = 1 byte
        // 1 blocks = 1 encoding / decoding polynomial
        int totalBlocks = numBlocksGroup1 + numBlocksGroup2;
        int[][] dataBlocks = new int[totalBlocks][];
        int dataIndex = 0;
        //Add numDataCodewordsInGroup1Block for numBlocksGroup1 times
        for (int i = 0; i < numBlocksGroup1; i++) {
            dataBlocks[i] = Arrays.copyOfRange(message, dataIndex, dataIndex + numDataCodewordsInGroup1Block);
            dataIndex += numDataCodewordsInGroup1Block;
        }
        //Add numDataCodewordsInGroup2Block for numBlocksGroup2 times
        for (int i = numBlocksGroup1; i < totalBlocks; i++) {
            dataBlocks[i] = Arrays.copyOfRange(message, dataIndex, dataIndex + numDataCodewordsInGroup2Block);
            dataIndex += numDataCodewordsInGroup2Block;
        }
        // generate error correction codewords for each blocks
        int[][] ecBlocks = new int[totalBlocks][];
        for (int i = 0; i < totalBlocks; i++) {
            ecBlocks[i] = generateErrorCorrectionCodewords(dataBlocks[i], ecCodewordsPerBlock);
        }
        // interleave data blocks
        StringBuilder binaryString = new StringBuilder();
        for (int i = 0; i < totalDataCodewords; i++) {
            for (int j = 0; j < totalBlocks; j++) {
                if (i < dataBlocks[j].length) {
                    binaryString.append(String.format("%8s", Integer.toBinaryString(dataBlocks[j][i])).replace(' ', '0'));
                }
            }
        }
        // interleave error correction blocks
        for (int i = 0; i < ecCodewordsPerBlock; i++) {
            for (int j = 0; j < totalBlocks; j++) {
                binaryString.append(String.format("%8s", Integer.toBinaryString(ecBlocks[j][i])).replace(' ', '0'));
            }
        }
        //pad remainder bits
        int remainderBitsCount = getRequiredRemainderBits(version);
        for (int i = 0; i < remainderBitsCount; i++) {
            binaryString.append('0');
        }

        return binaryString.toString();
    }
    //---------------------------------------Decoding funtions----------------------------------------------------------------
    public static Pair<Boolean, int[]> errorCorrectionPolynomial(int[] receiveMessage, int[] generatorPolynomial){
        int errMaxLen = generatorPolynomial.length - 1;
        int[] syndromes = calculateSyndromes(receiveMessage, errMaxLen);
        boolean needCorrection = false;
        for(int i = 0; i < syndromes.length; i++){
            if (syndromes[i] != 0)
                needCorrection = true;
        }
        if(!needCorrection)
            return new Pair<>(true, receiveMessage);
        int[] errorLocatorPolynomial = berlekampMassey(syndromes);
        int[] pos = findErrorPositions(errorLocatorPolynomial, receiveMessage.length);
        if(pos.length == 0 ){
            int[] emptyArr = {};
            return new Pair<>(false, emptyArr);
        }
        int[] mag = QRErrorCorrection.calculateErrorMagnitudes(errorLocatorPolynomial, syndromes, pos);

        for(int i = 0 ; i < pos.length; i++){
            int position = pos[i];
            int errMagnitude = mag[i];
            receiveMessage[receiveMessage.length - position - 1] ^= errMagnitude;
        }
        return new Pair<>(true, receiveMessage);
    }
    public static int[] berlekampMassey(int[] syndromes) {
        int[] c = {};
        int[] oldC = {};
        int[] d = {};

        int f = -1;
        int bf = 0;
        int delta;

        for(int i = 0; i < syndromes.length; i++){
            delta = syndromes[i];
            int cNext = 0;
            for(int j = 1; j <= c.length; j++){
                cNext = gfAdd(cNext, gfMultiply(syndromes[i-j] , c[j - 1]));
            }
            delta = gfSubtract(delta, cNext);
            if(delta ==0)
                continue;
            if(f == -1){
                f = i;
                c = new int[i + 1];
            } else{
                int dfP1 = gfSubtract(syndromes[f] , bf);
                d = new int[i-f + oldC.length];
                int coef = gfDivide(delta , dfP1);
                d[i-f-1] = coef;
                for(int j = 0 ; j < oldC.length ; j++){
                    d[i-f + j] = gfMultiply(oldC[j] , coef);
                }
                if(c.length - i <= oldC.length - f){
                    f = i;
                    oldC = c;
                    bf = cNext;
                }
                int maxLength = Math.max(c.length, d.length);
                int[] newC = new int[maxLength];
                for (int j = 0; j < maxLength; j++) {
                    int val1 = (j < c.length) ? c[j] : 0;
                    int val2 = (j < d.length) ? d[j] : 0;

                    newC[j] = gfAdd(val1 , val2);
                }
                c = newC;
            }
        }
        int[] errorLocatorPoly = new int[c.length + 1];
        errorLocatorPoly[0] = 1;
        System.arraycopy(c, 0, errorLocatorPoly, 1, c.length);
        return reverseIntArr(errorLocatorPoly);
    }
    public static int[] findErrorPositions(int[] errorLocatorPolynomial, int rLen){
        List<Integer> errorPositions = new ArrayList<>();
        for (int i = 0; i < rLen; i++) {
            int eval = 0;
            for(int j = 0; j < errorLocatorPolynomial.length; j++){
                eval ^= gfDivide(errorLocatorPolynomial[j], LOG_TABLE[(i * (errorLocatorPolynomial.length - 1 - j)) % 255]);
            }
            if (eval == 0) {
                errorPositions.add(i);
            }
        }
        return errorPositions.stream().mapToInt(Integer::intValue).toArray();
    }
    public static int[] calculateSyndromes(int[] r, int generatorDegree){
        int[] s = new int[generatorDegree];
        for(int i = 0 ; i < generatorDegree; i++){
            for(int j = r.length - 1 ; j>=0; j--){
                s[i] ^= gfMultiply(
                        r[j] , LOG_TABLE[((r.length - 1 - j) * i) % 255] // Need to use GFMultiply to handle the case either a or b is zero
                );
            }
        }
        return s;
    }
    public static int[] calculateErrorMagnitudes(int[] errorLocatorPoly, int[] syndromes, int[] errorPositions) {
        // Compute Error Evaluator Polynomial Ω(x):
        int[] errorEvaluatorPoly = calculateErrorEvaluator(errorLocatorPoly, reverseIntArr(syndromes));
        // Compute the formal derivative of the error locator polynomial, Λ'(x)
        int[] errorLocatorDeriv = derivative(errorLocatorPoly);

        // Compute error magnitudes:
        int[] errorMagnitudes = new int[errorPositions.length];

        for (int i = 0; i < errorPositions.length; i++) {
            int position = errorPositions[i];

            // Forney’s formula requires evaluating at X_i^-1.
            // Usually, if X_i = α^(position), then X_i^-1 = α^(255 - position)
            int Xi_inv = LOG_TABLE[(255 - position) % 255];

            int numerator = evaluatePolynomial(errorEvaluatorPoly, Xi_inv);
            int denominator = evaluatePolynomial(errorLocatorDeriv, Xi_inv);

            errorMagnitudes[i] = gfMultiply(LOG_TABLE[position],gfDivide(numerator, denominator));
        }

        return errorMagnitudes;
    }
    private static int[] calculateErrorEvaluator(int[] errorLocatorPoly, int[] syndromes) {
        int[] omega = multiplyPolynomials(errorLocatorPoly, syndromes);
        int errEvaluatorLen = 2 * (errorLocatorPoly.length - 1);
        int[] errorEvaluator = new int[errEvaluatorLen];
        for(int i = 0; i < errEvaluatorLen; i++)
            errorEvaluator[errEvaluatorLen-  i- 1] = omega[omega.length -1 - i];
        return errorEvaluator;
    }
    private static int[] derivative(int[] poly) {
        if (poly.length <= 1) {
            return new int[0];
        }
        int[] deriv = new int[poly.length - 1];
        for(int i = poly.length - 2; i >= 0; i-=2){
            deriv[i] = poly[i];
        }
        return deriv;
    }
    public static int[] reverseInterleaveAndErrorCorretion(String binaryString, int version, QRVersion.VersionECInfo versionECInfo) {
        // Extract parameters
        int totalDataCodewords = versionECInfo.getTotalDataCodewords();
        int ecCodewordsPerBlock = versionECInfo.getEcCodewordsPerBlock();
        int numBlocksGroup1 = versionECInfo.getNumBlocksGroup1();
        int numDataCodewordsInGroup1Block = versionECInfo.getNumDataCodewordsInGroup1Block();
        int numBlocksGroup2 = versionECInfo.getNumBlocksGroup2();
        int numDataCodewordsInGroup2Block = versionECInfo.getNumDataCodewordsInGroup2Block();

        int totalBlocks = numBlocksGroup1 + numBlocksGroup2;
        int totalECCodewords = ecCodewordsPerBlock * totalBlocks;
        int remainderBitsCount = getRequiredRemainderBits(version);
        int totalBits = binaryString.length();

        // Calculate total bits
        int expectedDataBits = totalDataCodewords * 8;
        int expectedECBItterBits = totalECCodewords * 8;
        if (totalBits < expectedDataBits + expectedECBItterBits + remainderBitsCount) {
            return null;
        }

        // Remove remainder bits
        String dataWithECB = binaryString.substring(0, binaryString.length() - remainderBitsCount);

        // Split data and EC codewords
        int totalCodewords = totalDataCodewords + totalECCodewords;
        List<Integer> allCodewords = new ArrayList<>();
        for (int i = 0; i < dataWithECB.length(); i += 8) {
            String byteStr = dataWithECB.substring(i, Math.min(i + 8, dataWithECB.length()));
            int codeword = Integer.parseInt(byteStr, 2);
            allCodewords.add(codeword);
        }

        // Separate data and EC codewords
        int[] interleavedDataCodewords = new int[totalDataCodewords];
        int[] ecCodewords = new int[totalECCodewords];

        for (int i = 0; i < totalDataCodewords; i++) {
            interleavedDataCodewords[i] = allCodewords.get(i);
        }
        for (int i = 0; i < totalECCodewords; i++) {
            ecCodewords[i] = allCodewords.get(totalDataCodewords + i);
        }

        int[][] dataBlocks = new int[totalBlocks][];
        for(int i = 0 ; i < numBlocksGroup1;i ++){
            dataBlocks[i] = new int[numDataCodewordsInGroup1Block];
        }
        for(int i = numBlocksGroup1 ; i < totalBlocks; i ++){
            dataBlocks[i] = new int[numDataCodewordsInGroup2Block];
        }
        // Deinterleave data codewords into blocks
        int dataIndex = 0; //dataIndex: current index in each block
        for (int i = 0; i < totalDataCodewords; i+= totalBlocks) {
            int k = 0; // if some blocks in the first group does not have enough rooms, then don't increase the index of interleavedDataCodewords, k is used for this reason
            for(int j = 0; j < totalBlocks;j++){
                if (dataIndex < dataBlocks[j].length){ // check spot valid
                    dataBlocks[j][dataIndex] = interleavedDataCodewords[i+k++]; //only increase k if there is a valid spot
                }
            }
            dataIndex++;

        }

        // Deinterleave EC codewords into blocks
        int[][] ecBlocks = new int[totalBlocks][ecCodewordsPerBlock];
        int ecIndex = 0;
        for (int i = 0; i < ecCodewordsPerBlock; i++) {
            for (int j = 0; j < totalBlocks; j++) {
                if (ecIndex < ecCodewords.length) {
                    ecBlocks[j][i] = ecCodewords[ecIndex++];
                } else {
                    return null;
                }
            }
        }

        // perform error correction for each block
        int[][] correctedDataBlocks = new int[totalBlocks][];
        for (int i = 0; i < totalBlocks; i++) {
            int[] receivedMessage = new int[dataBlocks[i].length + ecCodewordsPerBlock];
            System.arraycopy(dataBlocks[i], 0, receivedMessage, 0, dataBlocks[i].length);
            System.arraycopy(ecBlocks[i], 0, receivedMessage, dataBlocks[i].length, ecCodewordsPerBlock);

            //generatorPolynomial can be read from original qr code, here I generate it instead :V
            int[] generatorPolynomial = generateGeneratorPolynomial(ecCodewordsPerBlock);

            Pair<Boolean, int[]> correctionResult = errorCorrectionPolynomial(receivedMessage, generatorPolynomial);
            if (!correctionResult.first) {
                // Error correction failed
                return null;
            }

            int[] correctedData = Arrays.copyOfRange(correctionResult.second, 0, dataBlocks[i].length);
            correctedDataBlocks[i] = correctedData;
        }

        int[] originalMessage = new int[totalDataCodewords];
        int originalMessageIndex = 0;
        //final message is just straightforward 2d to 1d map of correctedDataBlocks
        for(int i = 0;i < totalBlocks; i++){
            for(int j = 0; j < correctedDataBlocks[i].length; j++){
                originalMessage[originalMessageIndex++] = correctedDataBlocks[i][j];
            }
        }
        return originalMessage;
    }
}

