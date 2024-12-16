
package com.app.src.abcqr.utils.QR.scanner;

import static com.app.src.abcqr.utils.QR.scanner.QRTransform.calculateVersion;

import android.graphics.Bitmap;
import android.telephony.TelephonyCallback;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
//import org.opencv.highgui.Highgui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Scanner {

    private static int[] xAlign;
    private static int[] yAlign;
    private static int numAlign;
    private static int qrSize = 0;
    private static Point[] srcPoints;
    private static Point[] dstPoints;
    private static Bitmap output;
    private static Bitmap inputBitmap;
    private static Bitmap transformBitmap;
    private static int[][] outputQRModuleMatrix;
    public void setOutput(Bitmap in) {
        output = in;
    }

    public Bitmap getOutput() {
        return output;
    }

    public static void toBlockMatrixQR() {
        try {
            Bitmap grayImage = QRTransform.convertToGrayImage(transformBitmap);
            int[][] binaryMatrix = QRTransform.convertToBinaryMatrix(grayImage);
            int offset = 0;
            System.out.println("start find offset");
            for (int x = 0; x < binaryMatrix.length / 2; x++) {
                for (int y = 0; y < binaryMatrix[x].length / 2; y++) {
                    if (binaryMatrix[x][y] == 0) {
                        offset = x;
                        break;
                    }
                }
                if (offset != 0) {
                    break;
                }
            }
            QRTransform.convertToBlockMatrix(binaryMatrix, outputQRModuleMatrix, grayImage.getWidth(), grayImage.getHeight(), offset, QRTransform.getBlockSize(), QRTransform.getqrWidth(), qrSize);

        } catch (Exception e) {
            System.out.println("Something wrong processing");
            System.out.println(e.getMessage());
        }
    }




    public static void findAlignmentPattern() {
        int qrWidth = QRTransform.getqrWidth();
        int qrBlockSize = QRTransform.getBlockSize();
        System.out.println("Start Find Alignment Pattern");
        //QRTransform.startFindEye();
        try {
            Bitmap grayImage = QRTransform.convertToGrayImage(inputBitmap);
            System.out.println("Start Find Alignment Pattern here");
            int[][] binaryImage = QRTransform.convertToBinaryMatrix(grayImage);
            xAlign = new int[qrWidth + 1];
            yAlign = new int[qrWidth + 1];
            numAlign = QRTransform.findAlignPattern(binaryImage, grayImage.getWidth(), grayImage.getHeight(), xAlign, yAlign, qrBlockSize);
        } catch (Exception e) {
            System.out.println("Error Load Image");
            throw new RuntimeException(e);
        }
    }

    public static int uniqueAlignArray(int numAlign, int qrBlockSize) {
        int countUniquePoint = numAlign;
        for (int i = 0; i < numAlign - 1; i++) {
            if (xAlign[i] == -1 && yAlign[i] == -1) {
                continue;
            }
            for (int j = i + 1; j < numAlign; j++) {
                if (xAlign[j] == -1 && yAlign[j] == -1) {
                    continue;
                }
                //System.out.println("x:" + i + " ,y:" + j+","+QRTransform.calculateDistance(xAlign[i], yAlign[i], xAlign[j], yAlign[j]));
                if (QRTransform.calculateDistance(xAlign[i], yAlign[i], xAlign[j], yAlign[j]) < qrBlockSize) {
                    xAlign[j] = -1;
                    yAlign[j] = -1;
                    countUniquePoint--;
                }
            }
        }
        int[] xNewAlign = new int[countUniquePoint];
        int[] yNewAlign = new int[countUniquePoint];
        int index = 0;
        for (int i = 0; i < numAlign; i++) {
            if (xAlign[i] != -1 && yAlign[i] != -1) {
                xNewAlign[index] = xAlign[i];
                yNewAlign[index] = yAlign[i];
                index++;
            }
        }
        xAlign = xNewAlign;
        yAlign = yNewAlign;
        return countUniquePoint;
    }

    public static void addingAlignPattern(int qrSize, int qrVersion, int qrBlockSize, int numAlign) {
        int[][] alignPosition = {{0}, {0},
                {6, 18},
                {6, 22},
                {6, 26},
                {6, 30},
                {6, 34},
                {6, 22, 38}, {6, 24, 42}, {6, 26, 46}, {6, 28, 50}, {6, 30, 54}, {6, 32, 58}, {6, 34, 62}, {6, 26, 45, 66},
                {6, 26, 48, 70}, {6, 26, 50, 74}, {6, 30, 54, 78}, {6, 30, 56, 82}, {6, 30, 58, 86}, {6, 34, 62, 90},
                {6, 28, 50, 72, 94}, {6, 26, 50, 74, 98}, {6, 30, 54, 78, 102}, {6, 28, 54, 80, 106}, {6, 32, 58, 84, 110}, {6, 30, 58, 86, 114}, {6, 34, 62, 90, 118},
                {6, 26, 50, 74, 98, 122}, {6, 30, 54, 78, 102, 126}, {6, 26, 52, 78, 104, 130}, {6, 30, 56, 82, 108, 134}, {6, 34, 60, 86, 112, 138}, {6, 30, 58, 86, 114, 142}, {6, 34, 62, 90, 118, 146},
                {6, 30, 54, 78, 102, 126, 150}, {6, 24, 50, 76, 102, 128, 154}, {6, 28, 54, 80, 106, 132, 158}, {6, 32, 58, 84, 110, 136, 162}, {6, 26, 54, 82, 110, 138, 166}, {6, 30, 58, 86, 114, 142, 170},
        };
        int[] listAlignPos = alignPosition[qrVersion];
        //System.out.println("QRSize: " + qrSize);
        boolean isBlocked = false;
        if (qrSize - 7 * 2 < listAlignPos[listAlignPos.length - 1] + 1) {
            isBlocked = true;
        }

        for (int i = 0; i < listAlignPos.length; i++) {
            System.out.print(listAlignPos[i] + " ");
        }
        int indexPoint = 3;
        for (int i = 0; i < numAlign; i++) {
            int xExpected = xAlign[i] / qrBlockSize;
            int yExpected = yAlign[i] / qrBlockSize;
            double minPercentage = 100;
            int xMinValue = 0;
            for (int j = 0; j < listAlignPos.length; j++) {
                double percentage = QRTransform.percentageDif(xExpected, listAlignPos[j]);
                if (percentage < minPercentage) {
                    xMinValue = listAlignPos[j];
                    minPercentage = percentage;
                }
            }
            int yMinValue = 0;
            minPercentage = 100;
            for (int j = 0; j < listAlignPos.length; j++) {
                double percentage = QRTransform.percentageDif(yExpected, listAlignPos[j]);
                if (percentage < minPercentage) {
                    yMinValue = listAlignPos[j];
                    minPercentage = percentage;
                }
            }
            if (isBlocked) {
                if ((yMinValue == listAlignPos[0] && xMinValue == listAlignPos[listAlignPos.length - 1])
                        || (xMinValue == listAlignPos[0] && yMinValue == listAlignPos[listAlignPos.length - 1])) {
                    System.out.println("Skipped");
                    continue;
                }
            }
            System.out.println("xExpected: " + xExpected + " ,yExpected:" + yExpected);
            System.out.println("xMinvalue: " + xMinValue + " ,yMinValue:" + yMinValue);
            srcPoints[indexPoint + i] = new Point(xAlign[i], yAlign[i]);
            dstPoints[indexPoint + i] = new Point((xMinValue + 1) * qrBlockSize, (yMinValue + 1) * qrBlockSize);

        }
        //return null;
    }

    public static void initFile() {
        try {
            File myObj = new File("output.jpg");
            File myObj1 = new File("QRAffine.png");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File " + myObj.getName() + " already exists.");
            }
            if (myObj1.createNewFile()) {
                System.out.println("File created: " + myObj1.getName());
            } else {
                System.out.println("File " + myObj1.getName() + " already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    public static int[][] getQRMatrix(Bitmap bitmap) {
        try {
            //initFile();
            inputBitmap = bitmap;
            Mat srcImage = new Mat();
            Utils.bitmapToMat(inputBitmap, srcImage);
            QRTransform.startFindEye(inputBitmap);
            int xEye1 = QRTransform.getxEye1();
            int xEye2 = QRTransform.getxEye2();
            int xEye3 = QRTransform.getxEye3();
            int yEye1 = QRTransform.getyEye1();
            int yEye2 = QRTransform.getyEye2();
            int yEye3 = QRTransform.getyEye3();
            int qrBlockSize = QRTransform.getBlockSize();

            int[] qrSizeAndVersion = calculateVersion();
            qrSize = qrSizeAndVersion[0];
            int qrVersion = qrSizeAndVersion[1];
            if (qrVersion != 1) {
                findAlignmentPattern();
                numAlign = uniqueAlignArray(numAlign, qrBlockSize);

                int xNew1 = qrBlockSize * 4;
                int yNew1 = qrBlockSize * 4;
                int xNew2 = qrBlockSize * (qrSize - 4 + 1);
                int yNew2 = qrBlockSize * 4;
                int xNew3 = qrBlockSize * 4;
                int yNew3 = qrBlockSize * (qrSize - 4 + 1);

                srcPoints = new Point[numAlign + 3];
                dstPoints = new Point[numAlign + 3];
                srcPoints[0] = new Point(xEye1, yEye1);
                srcPoints[1] = new Point(xEye2, yEye2);
                srcPoints[2] = new Point(xEye3, yEye3);
                //srcPoints[3] = new Point(xAlignBefore, yAlignBefore);
                dstPoints[0] = new Point(xNew1, yNew1);
                dstPoints[1] = new Point(xNew2, yNew2);
                dstPoints[2] = new Point(xNew3, yNew3);

                addingAlignPattern(qrSize, qrVersion, qrBlockSize, numAlign);

                MatOfPoint2f srcMat = new MatOfPoint2f(srcPoints);
                MatOfPoint2f dstMat = new MatOfPoint2f(dstPoints);

                // Compute homography using RANSAC
                Mat homographyMatrix = Calib3d.findHomography(srcMat, dstMat, Calib3d.RANSAC, 3);
                Mat dstImage = new Mat();
                Imgproc.warpPerspective(srcImage, dstImage, homographyMatrix, srcImage.size());

                try {
                    transformBitmap = Bitmap.createBitmap(dstImage.cols(), dstImage.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(dstMat, transformBitmap);
                    int a = 0;
                }
                catch (CvException e){
                    Log.d("Exception",e.getMessage());}

            } else {
                  transformBitmap = QRTransform.getAffine();

            }
        } catch (Exception e) {
            System.out.println("Load Image Error");
            throw new RuntimeException(e);
        }
        outputQRModuleMatrix = new int[qrSize][qrSize];
        toBlockMatrixQR();
        return outputQRModuleMatrix;
    }

//    public static void main(String[] args) {
//
//        // Load an image and apply the homography
//        String imagePath = "QR7.png"; // Replace with your image path
//        int[][] qrMatrix = getQRMatrix(imagePath);
//        for (int i = 0; i < qrMatrix.length; i++) {
//            for (int j = 0; j < qrMatrix[i].length; j++) {
//                System.out.print(qrMatrix[i][j] + " ");
//            }
//            System.out.println();
//        }
//
//        //addingAlignPatternSrc(srcPoints,qrSize,qrVersion);
//    }
}
