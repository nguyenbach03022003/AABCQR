
package com.app.src.abcqr.utils.QR.scanner;

import static com.app.src.abcqr.utils.QR.scanner.QRTransform.calculateDistance;
import static com.app.src.abcqr.utils.QR.scanner.QRTransform.calculateVersion;
import static com.app.src.abcqr.utils.QR.scanner.QRTransform.getBlockSize;
import static com.app.src.abcqr.utils.QR.scanner.QRTransform.getXEyeAff1;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
//import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private static int[] xAlign;
    private static int[] yAlign;
    private static List<Point> alignPoints;
    private static int qrSize = 0;
    private static int qrBlockSize = 0;
    private static Point[] srcPerspectiveTransformPoints;
    private static Point[] dstPerspectiveTransformPoints;
    private static Bitmap inputBitmap;
    private static Bitmap affineBitmap;
    private static Bitmap transformBitmap;
    private static int[][] outputQRModuleMatrix;

    public static void toBlockMatrixQR() {
        try {
            Mat binary = new Mat();
            Mat transformMat = new Mat();
            Utils.bitmapToMat(transformBitmap, transformMat);
            Mat gray = new Mat();
            Imgproc.cvtColor(transformMat, gray, Imgproc.COLOR_BGR2GRAY);

            Imgproc.threshold(gray, binary, 128, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
            Utils.matToBitmap(binary, transformBitmap);

            int[][] binaryMatrix = QRTransform.convertToBinaryMatrix(transformBitmap);
            boolean foundOffset = false;
            int offset = 0;
            while(!foundOffset){
                int totalWhite = 0;
                for(int i = 0 ; i < qrBlockSize; i++){
                    for(int j = 0; j < qrBlockSize; j++){
                        if(i + offset < transformBitmap.getHeight() && j+ offset < transformBitmap.getWidth())
                            if(binaryMatrix[i + offset][j+offset] == 0)
                                totalWhite++;
                    }
                }
                if(totalWhite > qrBlockSize * qrBlockSize - qrBlockSize){
                    foundOffset = true;
                } else {
                    offset++;
                }
            }

            QRTransform.convertToBlockMatrix(binaryMatrix, outputQRModuleMatrix, transformBitmap.getWidth(), transformBitmap.getHeight(), offset, qrBlockSize, qrSize);
            int a = 0;
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
            System.out.println("Start Find Alignment Pattern here");
            int[][] binaryImage = QRTransform.convertToBinaryMatrix(affineBitmap);
            xAlign = new int[qrWidth + 1];
            yAlign = new int[qrWidth + 1];
            alignPoints = QRTransform.findAlignPattern(binaryImage, affineBitmap.getWidth(), affineBitmap.getHeight(), xAlign, yAlign, qrBlockSize);
        } catch (Exception e) {
            System.out.println("Error Load Image");
            throw new RuntimeException(e);
        }
    }

    public static List<Point> uniqueAlignArray (List<Point> alignPoints, int qrBlockSize) {
        List<Point> uniquePoints = new ArrayList<>();

        for (Point currentPoint : alignPoints) {
            boolean isUnique = true;
            for (Point uniquePoint : uniquePoints) {
                if (calculateDistance(currentPoint.x,currentPoint.y, uniquePoint.x, uniquePoint.y) < qrBlockSize) {
                    isUnique = false;
                    break;
                }
            }
            if (isUnique) {
                uniquePoints.add(currentPoint);
            }
        }

        return uniquePoints;

    }

    public static void addingAlignPattern(int qrVersion, int qrBlockSize) {
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

        int[] positions = alignmentPatternLocations[qrVersion - 1];
        int indexPoint = 3;
        for (Point p: alignPoints) {
            double minDistance = Double.MAX_VALUE;
            Point expectedLogicalPoint = null;
            for (int posX  : positions) {
                for (int posY  : positions) {
                    double expectedX = (posX + 0.5) * qrBlockSize;
                    double expectedY = (posY + 0.5) * qrBlockSize;
                    double distance = Math.hypot(p.x - expectedX, p.y - expectedY);
                    if (distance < minDistance) {
                        minDistance = distance;
                        expectedLogicalPoint = new Point(expectedX, expectedY);
                    }

                }
            }
            if (expectedLogicalPoint != null && minDistance < 4 * qrBlockSize * qrBlockSize) {
                srcPerspectiveTransformPoints[indexPoint] = p;
                dstPerspectiveTransformPoints[indexPoint] = expectedLogicalPoint;
                indexPoint++;
            }
        }
    }


    public static int[][] getQRMatrix(Bitmap bitmap) {
        try {
            inputBitmap = bitmap;
            QRTransform.startFindEye(inputBitmap);
            // after finding eye complete, affine image is available in QRTransform
            affineBitmap = QRTransform.getAffineBitmap();
            Mat srcImage = new Mat();
            Utils.bitmapToMat(affineBitmap, srcImage);

            qrBlockSize = QRTransform.getBlockSize();

            int[] qrSizeAndVersion = calculateVersion();
            qrSize = qrSizeAndVersion[0];
            int qrVersion = qrSizeAndVersion[1];

            if (qrVersion >= 7) {
                findAlignmentPattern();
                alignPoints = uniqueAlignArray(alignPoints, qrBlockSize);
                if(alignPoints.size() != 0){
                    int xEye1Aff = QRTransform.getXEyeAff1();
                    int yEye1Aff = QRTransform.getYEyeAff1();
                    int xEye2Aff = QRTransform.getXEyeAff2();
                    int yEye2Aff = QRTransform.getYEyeAff2();
                    int xEye3Aff = QRTransform.getXEyeAff3();
                    int yEye3Aff = QRTransform.getYEyeAff3();

                    srcPerspectiveTransformPoints = new Point[alignPoints.size() + 3];
                    dstPerspectiveTransformPoints = new Point[alignPoints.size() + 3];
                    srcPerspectiveTransformPoints[0] = new Point(xEye1Aff, yEye1Aff);
                    srcPerspectiveTransformPoints[1] = new Point(xEye2Aff, yEye2Aff);
                    srcPerspectiveTransformPoints[2] = new Point(xEye3Aff, yEye3Aff);
                    dstPerspectiveTransformPoints[0] = new Point(xEye1Aff, yEye1Aff);
                    dstPerspectiveTransformPoints[1] = new Point(xEye2Aff, yEye2Aff);
                    dstPerspectiveTransformPoints[2] = new Point(xEye3Aff, yEye3Aff);

                    addingAlignPattern(qrVersion, qrBlockSize);

                    MatOfPoint2f srcMat = new MatOfPoint2f(srcPerspectiveTransformPoints);
                    MatOfPoint2f dstMat = new MatOfPoint2f(dstPerspectiveTransformPoints);

                    // Compute homography using RANSAC
                    Mat homographyMatrix = Calib3d.findHomography(srcMat, dstMat, Calib3d.RANSAC, 3);
                    Mat dstImage = new Mat();
                    Imgproc.warpPerspective(srcImage, dstImage, homographyMatrix, srcImage.size());

                    try {
                        transformBitmap = matToBitmap(dstImage);
                    }
                    catch (CvException e){
                        Log.d("Exception",e.getMessage());
                    }
                } else{
                    transformBitmap = affineBitmap;
                }
            } else {
                  transformBitmap = affineBitmap;
            }
        } catch (Exception e) {
            System.out.println("Load Image Error");
            throw new RuntimeException(e);
        }
        outputQRModuleMatrix = new int[qrSize][qrSize];
        toBlockMatrixQR();
        return outputQRModuleMatrix;
    }
    public static Bitmap matToBitmap(Mat mat) {
        // Check if the Mat is empty
        if (mat.empty()) {
            Log.e("MatToBitmap", "Input Mat is empty.");
            return null;
        }

        Bitmap bitmap = null;
        Mat convertedMat = new Mat();

        try {
            switch (mat.channels()) {
                case 1:
                    // Grayscale image
                    Imgproc.cvtColor(mat, convertedMat, Imgproc.COLOR_GRAY2RGBA);
                    break;
                case 3:
                    // BGR to RGBA
                    Imgproc.cvtColor(mat, convertedMat, Imgproc.COLOR_BGR2RGBA);
                    break;
                case 4:
                    // BGRA to RGBA
                    Imgproc.cvtColor(mat, convertedMat, Imgproc.COLOR_BGRA2RGBA);
                    break;
                default:
                    Log.e("MatToBitmap", "Unsupported number of channels: " + mat.channels());
                    return null;
            }

            // Create Bitmap with the same dimensions as the converted Mat
            bitmap = Bitmap.createBitmap(convertedMat.cols(), convertedMat.rows(), Bitmap.Config.ARGB_8888);

            // Convert Mat to Bitmap
            Utils.matToBitmap(convertedMat, bitmap);

        } catch (Exception e) {
            Log.e("MatToBitmap", "Exception converting Mat to Bitmap: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // Release the converted Mat to free memory
            if (!convertedMat.empty()) {
                convertedMat.release();
            }
        }

        return bitmap;
    }

//    }
}
