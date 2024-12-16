package com.app.src.abcqr.utils.QR.scanner;

//import java.awt.Color;
//import java.awt.image.BufferedImage;

import java.io.File;

//import javax.imageio.ImageIO;

//import java.awt.*;
import java.io.IOException;
//import java.awt.geom.AffineTransform;

//import javax.swing.*;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

//import androidx.ink.geometry.*;


public class QRTransform {

    private static final int BLOCK_SIZE = 65;
    public static int finderPatternLengthHori;
    public static int[][] matrixDotFinderPattern;
    private static double[][] transformMatrix;


    private static double qrWidth;
    private static double offsetEye;
    private static int blockSize;
    public static double xEye1 = -1, yEye1 = -1, xEye2 = -1, yEye2 = -1, xEye3 = -1, yEye3 = -1, xEye4 = -1, yEye4 = -1;
    private static int[] xEye;
    private static int[] yEye;
    private static int countEye;

    private static Bitmap affineBitmap;

    public static Bitmap getAffine() {
        return affineBitmap;
    }

    public static void setAfiine(Bitmap in) {
        affineBitmap = in;
    }

    /**
     * Calculate percentage difference between 2 numbers percentage
     * difference(%) = (a-b)/((a+b)/2)
     *
     * @param a
     * @param b
     * @return percentage differnt
     */
    public static double percentageDif(double a, double b) {
        double dif = Math.abs(a - b);
        double average = (a + b) / 2;
        return (dif / average);
    }

    /**
     * Check if this pixel have horizontal and vertical finder pattern
     *
     * @param binaryMatrix: input binary matrix
     * @param x             int row input
     * @param y             int column input
     * @return Boolean pixel (x,y) have finder pattern and this finder pattern
     * is on the row and column
     */
    private static boolean checkFinderPatternLength(int[][] binaryMatrix, int x, int y) {
        int[] moduleCountH = new int[6];
        int[] moduleCountV = new int[6];
        int width = binaryMatrix.length;
        int height = binaryMatrix[0].length;
        int i = x;
        int j = y;
        double errorRate = 0.3;
        //System.out.println("(x,y): " + "(" + x + "," + y + ")");
        while (i >= 0 && binaryMatrix[i][y] == 0) {     // Tim cac o den lien tiep ben phai diem (x,y)
            moduleCountH[0]++;                          // moduleCountH[0] luu so bit den tai day
            i++;                                        // check bit tiep theo
        }
        if (i >= width || moduleCountH[0] == 0) {       // Neu dang dem ma gap cuoi buc anh hoac dang bat dau tu o trang, tra ve false
            return false;
        }
        //System.out.println("0H:" + moduleCountH[0]);

        while (i < width && binaryMatrix[i][y] == 1) {  // Dem sang so o trang vung tiep theo ben phai vung den
            moduleCountH[1]++;                          // moduleCountH[1] luu  so bit trang
            i++;
        }
        if (i >= width || moduleCountH[1] == 0) {       // Neu khong dem duoc bit trang nao hoac con tro chay den cuoi buc anh
            return false;                               // tra ve false
        }
        //System.out.println("1H:" + moduleCountH[1]);
        while (i < width && binaryMatrix[i][y] == 0) {  // Dem sang vung tiep so bit den ben phai vung trang
            moduleCountH[2]++;                          // moduleCountH[2] luu so bit den
            i++;
        }
        if (i >= width || moduleCountH[2] == 0) {       // Neu khong dem duoc bit den nao hoac con tro chay den cuoi buc anh
            return false;
        }
        //System.out.println("2H:" + moduleCountH[2]);
        i = x;                                          // quay lai ve diem dau tien xet de bat dau dem nguoc lai
        while (i > 0 && binaryMatrix[i][y] == 0) {      // Dem so bit den nam ben trai diem dau
            moduleCountH[3]++;                          // moduleCountH[3] luu so bit den
            i--;
        }

        if (i <= 0 || moduleCountH[3] == 0) {           // Neu khong dem duoc bit den nao hoac con tro chay qua buc anh tra ve false
            return false;
        }
        //System.out.println("3H:" + moduleCountH[3]);

        while (i > 0 && binaryMatrix[i][y] == 1) {      // Dem so bit trang o ben trai vung bit den vua dem
            moduleCountH[4]++;                          // moduleCountH[4] luu so bit trang
            i--;
        }
        //System.out.println("4H:" + moduleCountH[4]);
        if (i <= 0 || moduleCountH[4] == 0) {           // Neu khong dem duoc bit trang hoac ngoai bien buc anh return false
            return false;
        }
        while (i > 0 && binaryMatrix[i][y] == 0) {      // Dem so bit den ben trai vung trang vua dem
            moduleCountH[5]++;                          // moduleCountH[5] luu so bit den
            i--;
        }
        if (i <= 0 || moduleCountH[5] == 0) {           // Neu khong dem duoc bit den hoac ngoai bien buc anh return false
            return false;
        }
        // System.out.println("5H:" + moduleCountH[5]);
        ///////////////////////////////////////////////////////
        //////// Xy ly hang doc
        while (j >= 0 && binaryMatrix[x][j] == 0) {     // Tim cac o den lien tiep ben duoi diem (x,y)
            moduleCountV[0]++;                          // moduleCountV[0] luu so bit den
            j++;
        }
        if (j >= height || moduleCountV[0] == 0) {      // Neu khong dem duoc bit den nao hoac con tro j chay qua bien buc anh return false
            return false;
        }
        //System.out.println("0V:" + moduleCountV[0]);

        while (j < height && binaryMatrix[x][j] == 1) { // Tim cac o trang lien tiep ben duoi vung den vua dem
            moduleCountV[1]++;                          // moduleCountV[1] luu so bit trang
            j++;
        }
        if (j >= height || moduleCountV[1] == 0) {      // Neu khong dem duoc bit trang nao hoac con tro j chay qua bien buc anh return false
            return false;
        }
        //System.out.println("1V:" + moduleCountV[1]);
        while (j < height && binaryMatrix[x][j] == 0) { // Tim cac o den lien tiep ben duoi vung den vua dem
            moduleCountV[2]++;                          // moduleCountV[2] luu so bit den
            j++;
        }
        if (j >= height || moduleCountV[2] == 0) {      // Neu khong dem duoc bit den nao hoac con tro j chay qua bien buc anh return false
            return false;
        }
        //System.out.println("2V:" + moduleCountV[2]);
        j = y;
        while (j > 0 && binaryMatrix[x][j] == 0) {      // Tim cac o den lien tiep ben tren bit xuat phat
            moduleCountV[3]++;                          // moduleCountV[3] luu so bit den
            j--;
        }

        if (j <= 0 || moduleCountV[3] == 0) {           // Neu khong dem duoc bit den nao hoac con tro j chay qua bien buc anh return false
            return false;
        }
        //System.out.println("3V:" + moduleCountV[3]);

        while (j > 0 && binaryMatrix[x][j] == 1) {      // Tim cac o trang lien tiep ben tren vung den vua dem
            moduleCountV[4]++;                          // moduleCountV[4] luu so bit trang
            j--;
        }
        //System.out.println("4V:" + moduleCountV[4]);
        if (j <= 0 || moduleCountV[4] == 0) {           // Neu khong dem duoc bit trang nao hoac con tro j chay qua bien buc anh return false
            return false;
        }
        while (j > 0 && binaryMatrix[x][j] == 0) {      // Tim cac o den lien tiep ben tren vung den vua dem
            moduleCountV[5]++;                          // moduleCountV[5] luu so bit den
            j--;
        }
        if (j <= 0 || moduleCountV[5] == 0) {           // Neu khong dem duoc bit den nao hoac con tro j chay qua bien buc anh return false
            return false;
        }
        //System.out.println("5V:" + moduleCountV[5]);
        double totalVertical = moduleCountV[0] // Tinh tong so bit cua finder pattern doc
                + moduleCountV[1]
                + moduleCountV[2]
                + moduleCountV[3]
                + moduleCountV[4]
                + moduleCountV[5];
        double totalHorizontal = moduleCountH[0] // Tinh tong so bit cua finder pattern ngang
                + moduleCountH[1]
                + moduleCountH[2]
                + moduleCountH[3]
                + moduleCountH[4]
                + moduleCountH[5];
        boolean tmp = false;
        tmp = (percentageDif(totalVertical, totalHorizontal) < errorRate) // Kiem tra sai so giua do dai hang ngang va doc
                && (percentageDif(moduleCountV[0], moduleCountH[0]) < errorRate) // Kiem tra sai so giua do dai vung den ben phai va ben duoi ngang va doc
                && (percentageDif(moduleCountV[1], moduleCountH[1]) < errorRate) // Kiem tra sai so giua do dai vung trang ben phai va duoi ngang va doc
                && (percentageDif(moduleCountV[2], moduleCountH[2]) < errorRate) // Kiem tra sai so giua do dai vung den ben phai va duoi ngang va doc
                && (percentageDif(moduleCountV[3], moduleCountH[3]) < errorRate) // Kiem tra sai so giua do dai vung den ben trai va tren ngang va doc
                && (percentageDif(moduleCountV[4], moduleCountH[4]) < errorRate) // Kiem tra sai so giua do dai vung trang ben trai va tren ngang va doc
                && (percentageDif(moduleCountV[5], moduleCountH[5]) < errorRate) // Kiem tra sai so giua do dai vung den ben trai va tren ngang va doc
                && (percentageDif(moduleCountV[3], moduleCountV[0]) < errorRate) // Kiem tra sai so giua do dai vung den ben trai va phai bit (x,y) finder pattern ngang
                && (percentageDif(moduleCountH[3], moduleCountV[0]) < errorRate) // Kiem tra sai so giua do dai vung den ben tren va duoi bit (x,y} finder pattern doc
                && (percentageDif(moduleCountH[0], moduleCountV[3]) < errorRate) // Kiem tra sai so giua do dai vung den ben phai va ben tren bit (x,y)
                && (percentageDif(moduleCountV[3], moduleCountH[0]) < errorRate);     // Kiem tra sai so giua do dai vung den ben trai va ben duoi bit (x,y)
        return tmp;
    }

    /**
     * checkFinderPattern1: kiem tra tai pixel (x,y) co finder pattern nam doc
     * hay khong
     *
     * @param binaryMatrix: ma tran danh dau vao
     * @param x:            hang pixel (x,y)
     * @param y:            cot pixel (x,y)
     * @return Boolean pixel (x,y) co la finder pattern dco hay khong
     */
    private static boolean checkFinderPattern1(int[][] binaryMatrix, int x, int y) {
        int[] moduleCount = new int[6];  // Luu do dai cac phan vung [ 5 , 4 , 3 , 0 , 1 , 2] = [den , trang , den , den , trang , den]
        int width = binaryMatrix.length; // Do dai hang
        int height = binaryMatrix[0].length; // Do dai cot
        int j = y;
        double errorRate = 0.30;            // phan tram sai so
        //System.out.println("(x,y): " + "(" + x + "," + y + ")");
        // Phân kho?ng ?en tr?ng ?en tr?ng ?en tr?ng ?en
        while (j >= 0 && binaryMatrix[x][j] == 0) {     // Tim cac o den lien tiep ben duoi pixel (x,y)
            moduleCount[0]++;
            j++;
        }
        if (j >= height || moduleCount[0] == 0) {       //  Neu khong tim thay o den nao return false
            return false;
        }
        //System.out.println("0:" + moduleCount[0]);

        while (j < height && binaryMatrix[x][j] == 1) { // Tim cac o trang lien tiep lien ke phan mau den ben duoi
            moduleCount[1]++;
            j++;
        }
        if (j >= height || moduleCount[1] == 0) {       // Neu khong tim thay duoc hoac con tro chay qua bien cua buc anh
            return false;
        }
        //System.out.println("1:" + moduleCount[1]);
        while (j < height && binaryMatrix[x][j] == 0) { // Tim cac o den lien tiep lien ke phan mau trang
            moduleCount[2]++;
            j++;
        }
        if (j >= height || moduleCount[2] == 0) {       // Neu khong tim thay o den nao hoac la con tro chay den bien buc anh
            return false;
        }
        //System.out.println("2:" + moduleCount[2]);
        j = y;                                          // quay tro lai vi tti pixel (x,y)
        while (j > 0 && binaryMatrix[x][j] == 0) {      // Dem so O den lien ke ben tren pixel (x,y)
            moduleCount[3]++;
            j--;
        }

        if (j <= 0 || moduleCount[3] == 0) {            // Neu khong tim thay o den nao hoac con tro chay den bien
            return false;
        }
        //System.out.println("3:" + moduleCount[3]);

        while (j > 0 && binaryMatrix[x][j] == 1) {      // Dem so o trang lien ke nam ben tren phan mau den
            moduleCount[4]++;
            j--;
        }
        //System.out.println("4:" + moduleCount[4]);
        if (j <= 0 || moduleCount[4] == 0) {            // Neu khong tim thay hoac chay ra bien
            return false;
        }
        while (j > 0 && binaryMatrix[x][j] == 0) {      // Dem so o den lien ke nam ben tren phan mau den
            moduleCount[5]++;
            j--;
        }
        if (j <= 0 || moduleCount[5] == 0) {            // Neu khong tim thay
            return false;
        }
        double total = moduleCount[0] // Tinh Tong do dai finder Pattern
                + moduleCount[1]
                + moduleCount[2]
                + moduleCount[3]
                + moduleCount[4]
                + moduleCount[5];
        double unitSize = total / 7.0;                  // Gia tri uoc luong cho 1 phan vung
        return percentageDif(moduleCount[1], unitSize) < errorRate //So sanh sai so giua do dai 1 phan vung dem duoc va phan vung uoc luong
                && percentageDif(moduleCount[2], unitSize) < errorRate
                && percentageDif(moduleCount[3] + moduleCount[0], unitSize * 3) < errorRate
                && percentageDif(moduleCount[4], unitSize) < errorRate
                && percentageDif(moduleCount[5], unitSize) < errorRate
                && percentageDif(moduleCount[3], moduleCount[0]) < errorRate;

    }

    /**
     * checkFinderPattern1: kiem tra tai pixel (x,y) co finder pattern nam ngan
     * co hay khong
     *
     * @param binaryMatrix: ma tran binary anh dau vao
     * @param x:            vi tri hang pixel(x,y)
     * @param y:            vi tri cot pixel(x,y)
     * @return boolean: lieu pixel co phai la trung diem cua 1 finder pattern
     * nam ngang hay khong
     */
    private static boolean checkFinderPattern2(int[][] binaryMatrix, int x, int y) {
        int[] moduleCount = new int[6];  // Luu do dai cac phan vung [ 5 , 4 , 3 , 0 , 1 , 2] = [den , trang , den , den , trang , den]
        int width = binaryMatrix.length; // Do dai hang
        int height = binaryMatrix[0].length; // Do dai cot
        double errorRate = 0.30;
        int i = x;                       // Tao con tro bat dau tu x
        //System.out.println("(x,y): " + "(" + x + "," + y + ")");
        while (i < width && binaryMatrix[i][y] == 0) {  // Tim cac o den lien tiep ben phai pixel (x,y)
            moduleCount[0]++;
            i++;
        }
        //System.out.println("i= " + i);
        if (i >= width || moduleCount[0] == 0) {        // Neu khong tim duoc hoac con tro chay ra bien
            return false;
        }
        //System.out.println("0:" + moduleCount[0]);

        while (i < width && binaryMatrix[i][y] == 1) {  // Tim cac o trang lien tiep ben phai vung den
            moduleCount[1]++;
            i++;
        }
        if (i >= width || moduleCount[1] == 0) {
            return false;
        }
        //System.out.println("1:" + moduleCount[1]);

        while (i < width && binaryMatrix[i][y] == 0) {  // Tim cac o den lien tiep ben phai vung trang
            moduleCount[2]++;
            i++;
        }
        if (i >= width || moduleCount[2] == 0) {
            return false;
        }

        i = x;                                          // Bat dau lai tu pixel (x,y)
        //System.out.println("2:" + moduleCount[2]);

        while (i > 0 && binaryMatrix[i][y] == 0) {      // Dem so o den ben trai pixel (x,y)
            moduleCount[3]++;
            i--;
        }
        if (i <= 0 || moduleCount[3] == 0) {
            return false;
        }
        //System.out.println("3:" + moduleCount[3]);

        while (i > 0 && binaryMatrix[i][y] == 1) {      // Tim cac o trang lien tiep ben phai vung den
            moduleCount[4]++;
            i--;
        }
        if (i <= 0 || moduleCount[4] == 0) {
            return false;
        }
        //System.out.println("4:" + moduleCount[4]);
        while (i > 0 && binaryMatrix[i][y] == 0) {      // Tim cac o den lien tiep ben phai vung trang
            moduleCount[5]++;
            i--;
        }

        if (i <= 0 || moduleCount[5] == 0) {
            return false;
        }
        //System.out.println("5:" + moduleCount[5]);
        finderPatternLengthHori = moduleCount[0] + moduleCount[1] + moduleCount[2] + moduleCount[3] + moduleCount[4] + moduleCount[5]; // Luu do dai finder pattern de dung cho sau nay
        double total = moduleCount[0] // Tinh tong do dai finder pattern
                + moduleCount[1]
                + moduleCount[2]
                + moduleCount[3]
                + moduleCount[4]
                + moduleCount[5];
        double unitSize = total / 7.0;  // Tim do dai uoc luong khoang ti le
        //System.out.println("total:" + total);
        //System.out.println("unitSize:" + unitSize);
        boolean ans = percentageDif(moduleCount[1], unitSize) < errorRate //  Kiem tra sai so do dai moi phan so voi uoc luong
                && percentageDif(moduleCount[2], unitSize) < errorRate
                && percentageDif(moduleCount[3] + moduleCount[0], unitSize * 3) < errorRate
                && percentageDif(moduleCount[4], unitSize) < errorRate
                && percentageDif(moduleCount[5], unitSize) < errorRate
                && percentageDif(moduleCount[3], moduleCount[0]) < errorRate;
        return ans;
    }

    /**
     * Ham tinh do dai 2 diem (x1,y1) va (x2,y2)
     *
     * @param x1: gia tri x diem dau tien
     * @param y1: gia tri y diem dau tien
     * @param x2: gia tri x diem thu 2
     * @param y2: gia tri y diem thu 2
     * @return double do dai giua 2 diem nay
     */
    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Ham kiem tra toa do 3 mat trong danh sach cac mat xEye[] va yEye[]
     *
     * @param index1: gia tri thu index1 trong danh sach mat
     * @param index2: gia tri thu index2 trong danh sach mat
     * @param index3: gia tri thu index3 trong danh sach mat
     * @return boolean: lieu 3 con mat ung voi cac thu tu tren co phu hop cho 1
     * qr hay khong
     */
    private static boolean check3Eye(int index1, int index2, int index3) {
        //System.out.println("(" + index1 + "," + index2 + "," + index3 + ")");
        double angle1 = calculateAngle2Line(xEye[index1], yEye[index1], xEye[index2], yEye[index2], xEye[index3], yEye[index3]);    //  Tinh goc giua 2 duong thang Eye1Eye2 va Eye2Eye3
        double angle2 = calculateAngle2Line(xEye[index3], yEye[index3], xEye[index1], yEye[index1], xEye[index2], yEye[index2]);    //  Tinh goc giua 2 duong thang Eye3Eye1 va Eye1Eye2
        double angle3 = calculateAngle2Line(xEye[index2], yEye[index2], xEye[index3], yEye[index3], xEye[index1], yEye[index1]);    //  Tinh goc giua 2 duong thang Eye2Eye3 va Eye3Eye1

        /*
            Xu ly cac goc tu de bien 3 goc nay la 3 goc trong 1 tam giac
            Khong can qua tam cac truong hop nhu 1 duong thang hay tam giac tu chung ta chi can tim duoc goc nao xap xi 90 do
         */
        if (angle1 > 100) {
            angle1 = 180 - angle1;
        }
        if (angle2 > 100) {
            angle2 = 180 - angle2;
        }
        if (angle3 > 100) {
            angle2 = 180 - angle2;
        }
        /*
         Ham tinh khoang cach giua toa do 3 mat
         */
        double dis1 = calculateDistance(xEye[index1], yEye[index1], xEye[index2], yEye[index2]);    // Khoang cach giua Eye1 va Eye2
        double dis2 = calculateDistance(xEye[index3], yEye[index3], xEye[index2], yEye[index2]);    // Khoang cach giua Eye2 va Eye3
        double dis3 = calculateDistance(xEye[index3], yEye[index3], xEye[index1], yEye[index1]);    // Khoang cach giua Eye3 va Eye1
        /*
        Xu ly truong hop 3 toa do nam trong 1 cung 1 mat, khi do khoang cach giua 3 diem nay thuong se khong vuot qua 3 pixel
        luc nay se return false
         */
        if (dis1 < 50 || dis2 < 50 || dis3 < 50) {
            return false;
        }
        /*
        Tim cac goc ma xuat hien goc xap xi 90 va co khoang cach du lon
         */
        if (angle1 > 85 && angle1 < 95 && dis1 > 50 && dis2 > 50 && dis3 > 50) {
            return true;
        }
        if (angle2 > 85 && angle2 < 95 && dis1 > 50 && dis2 > 50 && dis3 > 50) {
            return true;
        }
        if (angle3 > 85 && angle3 < 95 && dis1 > 50 && dis2 > 50 && dis3 > 50) {
            return true;
        }
        return false;
    }

    /**
     * detectFilter: ham xu ly viec tim cac Eye tron Qr va tim affine
     * Transformation Matrix
     *
     * @param binaryMatrix: ma tran anh binary dau vao
     * @param width:        chieu ngang buc anh
     * @param height:       chieu doc buc anh
     */
    private static void detectFilter(int[][] binaryMatrix, int width, int height) {
        countEye = 0;                                       // Dem so toa do mat phat hien duoc
        matrixDotFinderPattern = new int[width][height];    // Ma tran luu lai cac pixel nao da duoc duyet
        xEye = new int[width * height];                     // mang luu toa do cac con mat tim duoc
        yEye = new int[width * height];
        /*
        Duyet toan bo buc anh
        Neu tim duoc vi tri pixel nao co finder pattern nam ngang
        -> kiem tra vi tri do co finder pattern nam doc khong va do dai ca 2 finder pattern co bang nhau khong va da duoc duyet chua
                -> luu lai gia tri vi tri do va tang so mat dem duoc
         */
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //if (x > 238 && x < 350 && y < 825 && y > 710) {
                if (checkFinderPattern2(binaryMatrix, x, y)) {
                    if (checkFinderPatternLength(binaryMatrix, x, y) && matrixDotFinderPattern[x][y] == 0 && checkFinderPattern1(binaryMatrix, x, y)) {
                        xEye[countEye] = x;                             // Luu hang
                        yEye[countEye] = y;                             // Luu cot
                        countEye++;                                     // Tang so luong mat dem duoc
                        matrixDotFinderPattern[x][y] = 1;
                        //}
                    }
                }
            }
        }
        for (int i = 0; i < countEye; i++) {
            System.out.println("i: " + i + ", x=" + xEye[i] + ", y=" + yEye[i]);
        }

        /*
        Tim ra 3 toa do mat phu hop
        neu tim duoc 3 con mat thoa man tao thanh 1 tam giac vuong luu lai vao bien de xu ly
        lua chon Cap mat dau tien de toi uu ham tim
         */
        boolean found3Eye = false;
        for (int i = 0; i < countEye - 2; i++) {
            if (found3Eye) {        // Neu da tim thay 3 Eye ngat viec tim
                break;
            }
            for (int j = i + 1; j < countEye - 1; j++) {
                if (found3Eye) {    // Neu da tim thay 3 Eye ngat viec tim
                    break;
                }
                for (int k = j + 1; k < countEye; k++) {
                    if (found3Eye) {    // Neu da tim thay 3 Eye ngat viec tim
                        break;
                    }
                    found3Eye = check3Eye(i, j, k);         // Kiem tra 3 mat co tao thanh tam giac vuong hay khong
                    if (found3Eye) {                        // Neu co thi luu lai toa do 3 con mat
                        xEye1 = xEye[i];
                        xEye2 = xEye[j];
                        xEye3 = xEye[k];
                        yEye1 = yEye[i];
                        yEye2 = yEye[j];
                        yEye3 = yEye[k];
                    }
                }
            }
        }
        if (!found3Eye) {   // Neu khong tim thay 3 mat phu hop thi anh khong chua QR hoac qr bi loi
            System.out.println("Cant find QR, QR corrupted");
            return;
        }
        /*
        Chinh sua lai toa do 3 con mat cho phu hop voi cau truc
         */
        correctingCoordinate();
        /*
        Tim diem co dinh thu 4 cua QR su dung toa do 3 mat da co
         */
        find4Eye();

        /*
        Tim Affine Transform matrix
        Khoi tao ma tran toa do nguon va toa do dich
         */
        double[][] sourcePoints = {
                {xEye1, yEye1},
                {xEye2, yEye2},
                {xEye3, yEye3}
        };

        double eyeWidth = calEyeWidth(binaryMatrix, (int) xEye1, (int) yEye1); // tinh do dai chieu ngang cua 1 con mat
        System.out.println("EyeWidth= " + eyeWidth);
        double disEye1 = calculateDistance(xEye1, yEye1, xEye2, yEye2);        // Tinh khoang cach giua 2 mat
        offsetEye = eyeWidth / 2;                                              // Do lech giua toa do trung tap va goc (0,0) QR sau khi chuyen doi
        offsetEye = Math.round(offsetEye);                                     // Lam tron double
        qrWidth = disEye1 + eyeWidth - 1;                                          // Chieu gai QR = Khoang cach 2 mat + chieu rong 1 mat
        blockSize = (int) Math.round(eyeWidth) / 7 - 1;                            // Chieu dai va rong 1 module dua tren chieu rong 1 mat
        qrWidth = Math.round(qrWidth);                                         // Lam tron 1 mat
        /*
        Ma tran diem dich su dung boi affine transformation
         */
        double[][] destinationPoints = {
                {offsetEye, offsetEye},
                {qrWidth - offsetEye, offsetEye},
                {offsetEye, qrWidth - offsetEye}
        };
        //System.out.println(Math.round(eyeWidth) + "," + Math.round(eyeWidth + disEye1));
        /*
        Tim ma tran Affine Transformation Matrix tu diem dau va diem dich
         */
        transformMatrix = findAffineTransformation(sourcePoints, destinationPoints);
        System.out.println("Affine Transformation Matrix:");          // In Ma tran Affine transformation matrix
        for (double[] row : transformMatrix) {
            for (double value : row) {
                System.out.printf("%.3f ", value);
            }
            System.out.println();
        }
    }

    /**
     * calEyeWidth: Ham tinh toan chieu rong mat
     *
     * @param input: ma tran binary dau vao
     * @param x:     toa do x
     * @param y:     toa do y
     * @return double chieu rong mat
     */
    public static double calEyeWidth(int[][] input, int x, int y) {
        boolean tmp = checkFinderPattern2(input, x, y); // Goi lai ham kiem tra finder pattern vi da luu gia tri do rong cua finder pattern
        double answer = finderPatternLengthHori;
        System.out.println("ans= " + answer);
        double angle = calculateAngle(xEye1, yEye1, xEye2, yEye2);
        System.out.println("angle= " + angle);
        double angleRate = Math.cos(Math.toRadians(angle));
        System.out.println("angleRate= " + angleRate);
        return answer / angleRate;
    }

    public static double calculateAngle(double x1, double y1, double x2, double y2) {
        // Use atan2 to calculate the angle in radians
        double angleRadians = Math.atan2(y2 - y1, x2 - x1);

        // Convert radians to degrees
        double angleDegrees = Math.toDegrees(angleRadians);

        // Ensure the angle is positive (0 to 360 degrees)
        if (angleDegrees < 0) {
            angleDegrees += 360;
        }

        return angleDegrees;
    }

    /**
     * calculateAngle2Line: Tinh goc tao boi 2 duong thang, 2 duong thang tao
     * boi 3 diem
     *
     * @param x1: Toa do x diem 1
     * @param y1: Toa do y diem 1
     * @param x2: Toa do x diem 2
     * @param y2: Toa do y diem 2
     * @param x3: Toa do x diem 3
     * @param y3: Toa do 7 diem 3
     * @return double: goc tao boi 2 duong thang nay voi don vi do (do)
     */
    private static double calculateAngle2Line(double x1, double y1, //A
                                              double x2, double y2, //B
                                              double x3, double y3) //C
    {
        // Tinh vecto AB
        double abX = x2 - x1;
        double abY = y2 - y1;

        // Vector BC
        double bcX = x3 - x2;
        double bcY = y3 - y2;

        // tinh tich vo huong
        double dotProduct = (abX * bcX) + (abY * bcY);

        // tinh do dai tich vo huong
        double magnitudeAB = Math.sqrt(abX * abX + abY * abY);
        double magnitudeBC = Math.sqrt(bcX * bcX + bcY * bcY);

        // tinh Cos goc tao boi 2 duong thang = do dai tich vo huong giua 2 vecto AB va BC
        double cosTheta = dotProduct / (magnitudeAB * magnitudeBC);

        // Goc theo don vi Radian
        double angleInRadians = Math.acos(cosTheta);

        // Chuyen sang do
        double angleInDegree = Math.toDegrees(angleInRadians);
        return angleInDegree;
    }

    /**
     * correctingCoordinate: Ham sua lai toa do 3 mat ve dung vi tri trong QR
     */
    private static void correctingCoordinate() {
        /* Goal:
        [   (xEye1,yEye1)       (xEye2,yEye2)

            (xEye3,yEye3)            x       ]
         */
        boolean is1 = false, is2 = false, is3 = false; // is1 = (x1,y1) is the square corner
        double angle = 0;
        angle = calculateAngle2Line(xEye3, yEye3, xEye1, yEye1, xEye2, yEye2);  // Gia su Eye1 la goc vuong
        if (angle > 80 && angle < 100) {                                        // Xac dinh Eye1 la goc vuong hay khong
            is1 = true;
        }
        angle = calculateAngle2Line(xEye1, yEye1, xEye2, yEye2, xEye3, yEye3);  // Gia su Eye2 la goc vuong
        if (angle > 80 && angle < 100) {                                        // Xac dinh Eye2 la goc vuong hay khong
            is2 = true;
        }

        angle = calculateAngle2Line(xEye2, yEye2, xEye3, yEye3, xEye1, yEye1);  // Gia su Eye3 la goc vuong
        if (angle > 80 && angle < 100) {                                        // Xac dinh Eye3 la goc vuong hay khong
            is3 = true;
        }
        System.out.println("angle= " + angle);
        if (is2) {                  // Neu Eye2 la goc vuong Doi cho vi tri Eye2 va Eye1
            double tmpx = xEye2;
            double tmpy = yEye2;
            xEye2 = xEye1;
            yEye2 = yEye1;
            xEye1 = tmpx;
            yEye1 = tmpy;
        }
        if (is3) {                  // Neu Eye2 la goc vuong Doi cho vi tri Eye3 va Eye1
            double tmpx = xEye3;
            double tmpy = yEye3;
            xEye3 = xEye1;
            yEye3 = yEye1;
            xEye1 = tmpx;
            yEye1 = tmpy;
        }
        System.out.println("xEye1= " + xEye1 + " ,yEye1= " + yEye1);
        System.out.println("xEye2= " + xEye2 + " ,yEye2= " + yEye2);
        System.out.println("xEye3= " + xEye3 + " ,yEye3= " + yEye3);

        // assume start at Eye3 go to Eye1(vector Eye31) and end at Eye2 (vector Eye12)
        double xEye31 = (xEye1 - xEye3);
        double yEye31 = (yEye1 - yEye3);
        //System.out.println("xEye31= " + xEye31 + " ,yEye31= " + yEye31);
        double xEye12 = (xEye1 - xEye2);
        double yEye12 = (yEye1 - yEye2);
        //System.out.println("xEye12= " + xEye12 + " ,yEye12= " + yEye12);
        // Cross product of 2 vector Eye31 and Eye12
        double crossProduct = (xEye31 * yEye12) - (xEye12 * yEye31);
        //System.out.println("CrossProduct= " + crossProduct);
        if (crossProduct > 0) { // Direction is counter-clockwise -> swap Eye2 and Eye3
            double tmpx = xEye2;
            double tmpy = yEye2;
            xEye2 = xEye3;
            yEye2 = yEye3;
            xEye3 = tmpx;
            yEye3 = tmpy;
        }
        if (crossProduct == 0) { // Invalid state: Eye1 Eye2 Eye3 on the same line
            System.out.println("Invalid Eye");
        }
        // else is the same order
        System.out.println("xEye1= " + xEye1 + " ,yEye1= " + yEye1);
        System.out.println("xEye2= " + xEye2 + " ,yEye2= " + yEye2);
        System.out.println("xEye3= " + xEye3 + " ,yEye3= " + yEye3);
    }

    /**
     * findAffineTransformation: tim ma tran affine transformation matrix
     *
     * @param sourcePoints:      ma tran diem dau
     * @param destinationPoints: ma tran diem dich
     * @return double[][]: ma tran 2x3 ma tran Affine transformation
     */
    public static double[][] findAffineTransformation(
            double[][] sourcePoints, double[][] destinationPoints) {
        System.out.println("StattFind");
        if (sourcePoints.length != 3 || destinationPoints.length != 3) {
            throw new IllegalArgumentException("Exactly 3 source and 3 destination points are required.");
        }
        // Construct the augmented matrix for the equations
        double[][] matrix = new double[6][7];
        for (int i = 0; i < 3; i++) {
            double x = sourcePoints[i][0];
            double y = sourcePoints[i][1];
            double xPrime = destinationPoints[i][0];
            double yPrime = destinationPoints[i][1];

            // Equation for x'
            matrix[2 * i][0] = x;
            matrix[2 * i][1] = y;
            matrix[2 * i][2] = 1;
            matrix[2 * i][3] = 0;
            matrix[2 * i][4] = 0;
            matrix[2 * i][5] = 0;
            matrix[2 * i][6] = xPrime;

            // Equation for y'
            matrix[2 * i + 1][0] = 0;
            matrix[2 * i + 1][1] = 0;
            matrix[2 * i + 1][2] = 0;
            matrix[2 * i + 1][3] = x;
            matrix[2 * i + 1][4] = y;
            matrix[2 * i + 1][5] = 1;
            matrix[2 * i + 1][6] = yPrime;
        }
        // GaussJordan method
        gaussJordan(matrix);
        return new double[][]{
                {matrix[0][6], matrix[1][6], matrix[2][6]},
                {matrix[3][6], matrix[4][6], matrix[5][6]}
        };
    }

    /**
     * gaussJordan: ham xu ly gauss Jordan ma tran
     *
     * @param matrix: tra lai ma tran da xu ly
     */
    private static void gaussJordan(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            // Find the maximum element in the current column for pivoting
            int maxRow = i;
            for (int k = i + 1; k < rows; k++) {
                if (Math.abs(matrix[k][i]) > Math.abs(matrix[maxRow][i])) {
                    maxRow = k;
                }
            }

            // Swap rows to bring the maximum element to the diagonal
            double[] temp = matrix[i];
            matrix[i] = matrix[maxRow];
            matrix[maxRow] = temp;

            // Check for singular matrix
            if (Math.abs(matrix[i][i]) < 1e-9) {
                throw new IllegalArgumentException("Matrix is singular or nearly singular.");
            }

            // Make the diagonal element 1
            double diagElement = matrix[i][i];
            for (int j = 0; j < cols; j++) {
                matrix[i][j] /= diagElement;
            }

            // Make the other elements in the column 0
            for (int k = 0; k < rows; k++) {
                if (k != i) {
                    double factor = matrix[k][i];
                    for (int j = 0; j < cols; j++) {
                        matrix[k][j] -= factor * matrix[i][j];
                    }
                }
            }
        }
    }

    /**
     * transformImage: Ham bien doi anh voi ma tran Affine transformation
     *
     * @param image: anh dau vao
     * @return BufferedImage: anh da duoc bien doi
     */
    public static Bitmap transformImage(Bitmap image) {

        // Create a new BufferedImage to hold the transformed image
        int width = image.getWidth();
        int height = image.getHeight();
        //Bitmap transformedImage = new Bitmap(width, height);
        // Get the Graphics2D object
        //Graphics2D g2d = transformedImage.createGraphics();
        Matrix affineMatrix = new Matrix();
        // Create an AffineTransform
//        AffineTransform transform = new AffineTransform(
//                transformMatrix[0][0], // a
//                transformMatrix[1][0], // c
//                transformMatrix[0][1], // b
//                transformMatrix[1][1], // d
//                transformMatrix[0][2], // tx
//                transformMatrix[1][2] // ty
//        );
        affineMatrix.setValues(new float[]{
                (float) transformMatrix[0][0], // a
                (float) transformMatrix[0][1], // b
                (float) transformMatrix[0][2], // tx
                (float) transformMatrix[1][0], // c
                (float) transformMatrix[1][1], // d
                (float) transformMatrix[1][2], // ty
                0, 0, 1
        });
        /*
         Apply transformation matrix
         */
//        g2d.setTransform(transform);
//        //transform.scale(0.9, 0.9);
//        // g2d.setTransform(transform);
//        //g2d.drawImage(image, (int) (finderPatternLengthHori), (int) (finderPatternLengthHori), null);
//        g2d.drawImage(image, 0, 0, null);
//        g2d.dispose();

        Bitmap transformedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), affineMatrix, true);

        return transformedImage;
    }

    /**
     * drawImageFromBinaryMatrix: tao anh tu ma tran binary
     *
     * @param binaryMatrix: ma tran binary dau vao
     * @return BufferedImage: anh tao boi ma tran binary
     */
    public static Bitmap drawImageFromBinaryMatrix(int[][] binaryMatrix) {
        int width = binaryMatrix.length;
        int height = binaryMatrix[0].length;
        Bitmap binaryImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);   // Tao bien luu gia tri anh

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (binaryMatrix[x][y] == 0) {
                    binaryImage.setPixel(x, y, Color.BLACK); // Set pixel to black
                } else {
                    binaryImage.setPixel(x, y, Color.WHITE); // Set pixel to white
                }
            }
        }
        return binaryImage;
    }

    /**
     * convertToGrayImage: ham chuyen anh tu mau sang anh gray
     *
     * @param image: anh mau goc
     * @return BufferedImage: anh gray sau khi xu ly
     */
    public static Bitmap convertToGrayImage(Bitmap image) {
//        Bitmap grayImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
//        for (int x = 0; x < image.getWidth(); x++) {
//            for (int y = 0; y < image.getHeight(); y++) {
//                int rgb = image.getPixel(x, y);
//                Color color = new Color(rgb);
//                float[] hsv = new float[3];
//                Color.RGBToHSV((int) color.red(), (int) color.green(), (int) color.blue(), hsv);
//                int grayLevel = Math.round(hsv[2] * 255);
//                int grayColor = new Color(grayLevel, grayLevel, grayLevel).getRGB();
//                grayImage.setPixel(x, y, grayColor);
//            }
//        }
//        return grayImage;
        Bitmap grayscaleBitmap = Bitmap.createBitmap(
                image.getWidth(),
                image.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        // Create a Canvas to draw on the grayscale Bitmap
        Canvas canvas = new Canvas(grayscaleBitmap);

        // Create a Paint object with a ColorMatrix for grayscale
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0); // Set saturation to 0 for grayscale
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);

        // Draw the image Bitmap onto the canvas with   the grayscale Paint
        canvas.drawBitmap(image, 0, 0, paint);

        return grayscaleBitmap;
    }

    /**
     * calculateBlockThreshold: Ham tinh gia tri Threshold trong gia tri 1 block
     *
     * @param grayImage: Anh gray
     * @param startX:    toa do bat dau block theo truc X
     * @param startY:    toa do bat dau block theo truc Y
     * @param blockSize: kich co 1 block
     * @return int: gia tri threshold theo 1 block
     */
    private static int calculateBlockThreshold(Bitmap grayImage, int startX, int startY, int blockSize) {
        int threshold = 128;
        int newThreshold;

        do {
            int sumDark = 0, sumLight = 0;
            int countDark = 0, countLight = 0;

            for (int x = startX; x < startX + blockSize && x < grayImage.getWidth(); x++) {
                for (int y = startY; y < startY + blockSize && y < grayImage.getHeight(); y++) {
                    int gray = Color.red(grayImage.getPixel(x, y));
                    if (gray < threshold) {
                        sumDark += gray;
                        countDark++;
                    } else {
                        sumLight += gray;
                        countLight++;
                    }
                }
            }

            int V1 = (countDark == 0) ? 0 : sumDark / countDark;
            int V2 = (countLight == 0) ? 0 : sumLight / countLight;

            newThreshold = (V1 + V2) / 2;

            if (newThreshold == threshold) {
                break;
            }
            threshold = newThreshold;
        } while (true);

        return threshold;
    }

    /**
     * convertToBinaryMatrix: ham chuyen tu anh gray sang ma tran binary
     *
     * @param grayImage: anh gray dau vao
     * @return int[][]: ma tran binary Tu anh gray
     */
    public static int[][] convertToBinaryMatrix(Bitmap grayImage) {
        int height = grayImage.getHeight();
        int width = grayImage.getWidth();
        int[][] binaryMatrix = new int[width][height];

        for (int startX = 0; startX < width; startX += BLOCK_SIZE) {
            for (int startY = 0; startY < height; startY += BLOCK_SIZE) {
                int threshold = calculateBlockThreshold(grayImage, startX, startY, BLOCK_SIZE);

                for (int x = startX; x < startX + BLOCK_SIZE && x < width; x++) {
                    for (int y = startY; y < startY + BLOCK_SIZE && y < height; y++) {
                        int gray = Color.red(grayImage.getPixel(x, y));
                        binaryMatrix[x][y] = (gray < threshold) ? 0 : 1;
                    }
                }
            }
        }
        return binaryMatrix;
    }
//    public static int[][] convertToQRMatrix(BufferedImage inputImage,int offset,int QRLength){
//
//    }

    /**
     * Tim gia tri diem thu 4 trong ma tran dua vao 3 toa do Eye da tim duoc
     */
    public static void find4Eye() {
        double mx = (xEye2 + xEye3) / 2;
        double my = (yEye2 + yEye3) / 2;
        xEye4 = 2 * mx - xEye1;
        yEye4 = 2 * my - yEye1;
    }

    /*
    Find affine 4 point
     */
    public static double[][] findAffineMatrix(double[][] sourcePoints, double[][] destinationPoints) {
        int n = 4; // Number of points
        double[][] A = new double[2 * n][6];
        double[] B = new double[2 * n];

        // Construct A and B matrices
        for (int i = 0; i < n; i++) {
            double x = sourcePoints[i][0];
            double y = sourcePoints[i][1];
            double xPrime = destinationPoints[i][0];
            double yPrime = destinationPoints[i][1];

            A[2 * i][0] = x;
            A[2 * i][1] = y;
            A[2 * i][2] = 1;
            A[2 * i][3] = 0;
            A[2 * i][4] = 0;
            A[2 * i][5] = 0;
            B[2 * i] = xPrime;

            A[2 * i + 1][0] = 0;
            A[2 * i + 1][1] = 0;
            A[2 * i + 1][2] = 0;
            A[2 * i + 1][3] = x;
            A[2 * i + 1][4] = y;
            A[2 * i + 1][5] = 1;
            B[2 * i + 1] = yPrime;
        }

        // Solve for T using least squares
        double[] T = solveLeastSquares(A, B);

        // Reshape T into a 2x3 matrix
        return new double[][]{
                {T[0], T[1], T[2]},
                {T[3], T[4], T[5]}
        };
    }

    private static double[] solveLeastSquares(double[][] A, double[] B) {
        int rows = A.length;
        int cols = A[0].length;

        // Compute A^T A
        double[][] ATA = new double[cols][cols];
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < cols; j++) {
                for (int k = 0; k < rows; k++) {
                    ATA[i][j] += A[k][i] * A[k][j];
                }
            }
        }

        // Compute A^T B
        double[] ATB = new double[cols];
        for (int i = 0; i < cols; i++) {
            for (int k = 0; k < rows; k++) {
                ATB[i] += A[k][i] * B[k];
            }
        }

        // Solve (ATA) * T = ATB using Gaussian elimination
        return gaussianElimination(ATA, ATB);
    }

    private static double[] gaussianElimination(double[][] A, double[] B) {
        int n = A.length;

        for (int i = 0; i < n; i++) {
            // Make the diagonal element 1
            double factor = A[i][i];
            for (int j = 0; j < n; j++) {
                A[i][j] /= factor;
            }
            B[i] /= factor;

            // Eliminate the current column for all rows below
            for (int k = i + 1; k < n; k++) {
                double factor2 = A[k][i];
                for (int j = 0; j < n; j++) {
                    A[k][j] -= factor2 * A[i][j];
                }
                B[k] -= factor2 * B[i];
            }
        }

        // Back substitution
        double[] T = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            T[i] = B[i];
            for (int j = i + 1; j < n; j++) {
                T[i] -= A[i][j] * T[j];
            }
        }

        return T;
    }

    /**
     * convertToBlockMatrix: Ham chuyen tu anh sau khi xu ly qua Affine
     * transformation sang ma tran binary chia anh thanh cac o vuong nho
     *
     * @param inputMatrix: ma tran anh
     * @param width:       chieu dai anh
     * @param height       : chieu rong anh
     */
    public static void convertToBlockMatrix(int[][] inputMatrix, int[][] outputMatrix, int width, int height, int offset, int blockSize, int qrWidth, int qrSize) {
        // Prefix Sum binary Image
        double errorRate = 0.2;
        int[][] prefixSum = new int[width][height];
        //int matrixWidth = (int) qrWidth / blockSize;

        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                prefixSum[x][y] = prefixSum[x - 1][y] + prefixSum[x][y - 1] - prefixSum[x - 1][y - 1] + inputMatrix[x][y];
            }
        }
        // Tinh tong cua cac o dua tren mang cong don
        //System.out.println("Math.round(qrWidth / blockSize)=" + Math.round(qrWidth / blockSize));
        for (int x = 0; x < qrSize; x++) {
            for (int y = 0; y < qrSize; y++) {
                int totalWhite = prefixSum[(x + 1) * blockSize + offset][(y + 1) * blockSize + offset] - prefixSum[x * blockSize + offset][(y + 1) * blockSize + offset] - prefixSum[(x + 1) * blockSize + offset][y * blockSize + offset] + prefixSum[x * blockSize + offset][y * blockSize + offset];
                if (totalWhite * 2 > blockSize * blockSize) {
                    outputMatrix[x][y] = 1;
                } else {
                    outputMatrix[x][y] = 0;
                }
            }
        }
    }

    public static boolean isAlignPatternMiddle(int[][] inputMatrix, int x, int y, int width, int height) {
        int i = x;
        int leftCount = 0;
        int rightCount = 0;
        int upCount = 0;
        int downCount = 0;
        double errorRate = 0.25;
        while (i > 0 && inputMatrix[i][y] == 0) {
            rightCount++;
            i++;
        }
        if (i >= width || rightCount == 0) {
            return false;
        }
        i = x;
        while (i >= 0 && inputMatrix[i][y] == 0) {
            leftCount++;
            i--;
        }
        if (i < 0 || leftCount == 0) {
            return false;
        }
        i = y;
        while (i >= 0 && inputMatrix[x][i] == 0) {
            downCount++;
            i++;
        }
        if (i >= width || downCount == 0) {
            return false;
        }
        i = y;
        while (i >= 0 && inputMatrix[x][i] == 0) {
            upCount++;
            i--;
        }
        if (i < 0 || upCount == 0) {
            return false;
        }
        boolean ans = percentageDif(leftCount, rightCount) < errorRate
                && percentageDif(upCount, downCount) < errorRate
                && percentageDif(rightCount, downCount) < errorRate;
        return ans;
    }

    public static boolean isAlignPattern(int[][] inputMatrix, int x, int y, int width, int height, int blockSize) {
        int[][] expectedPattern = {
                {0, 0, 0, 0, 0},
                {0, 1, 1, 1, 0},
                {0, 1, 0, 1, 0},
                {0, 1, 1, 1, 0},
                {0, 0, 0, 0, 0}
        };
        if (inputMatrix[x][y] == 1) {
            return false;
        }
        if (!isAlignPatternMiddle(inputMatrix, x, y, width, height)) {
            return false;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int nx = x + dx * blockSize;
                int ny = y + dy * blockSize;
                if (nx < 0 || ny < 0 || nx > width || ny > height) {
                    return false;
                }
                int expectedPixel = expectedPattern[dx + 2][dy + 2];
                if (inputMatrix[nx][ny] != expectedPixel) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int findAlignPattern(int[][] inputMatrix, int width, int height, int[] xAlign, int[] yAlign, int blockSize) {
        int countAlign = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (isAlignPattern(inputMatrix, i, j, width, height, blockSize)) {
                    xAlign[countAlign] = i;
                    yAlign[countAlign] = j;
                    countAlign++;
                }
            }
        }
        return countAlign;
    }

    public static int getxEye1() {
        return (int) xEye1;
    }

    public static int getxEye2() {
        return (int) xEye2;
    }

    public static int getxEye3() {
        return (int) xEye3;
    }

    public static int getyEye1() {
        return (int) yEye1;
    }

    public static int getyEye2() {
        return (int) yEye2;
    }

    public static int getyEye3() {
        return (int) yEye3;
    }

    public static int getyEye4() {
        return (int) yEye4;
    }

    public static int getxEye4() {
        return (int) xEye4;
    }

    public static int getBlockSize() {
        return blockSize;
    }

    public static int getqrWidth() {
        return (int) qrWidth;
    }

    public static int getoffsetEye() {
        return (int) offsetEye;
    }

    public static void startFindEye(Bitmap image) {
        try {
//            File inputFile = new File(inputPath);
            Bitmap grayImage = convertToGrayImage(image);    //  Chuyen sang anh gray
            int[][] binaryMatrix = convertToBinaryMatrix(grayImage);// Chuyen sang ma tran binary
            int height = grayImage.getHeight();
            int width = grayImage.getWidth();
            detectFilter(binaryMatrix, width, height);              // Xu ly tim Eye cua QR trong ma tran

            /*
                Rotate QR
             */
            Bitmap binaryImage = drawImageFromBinaryMatrix(binaryMatrix);

            Bitmap transformedImage;
            transformedImage = transformImage(binaryImage);
            setAfiine(transformedImage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int[] calculateVersion() {
        int[] qrSizeAndVersion = {-1,-1};
        int offsetEye = getoffsetEye();
        int[][] binaryImage = QRTransform.convertToBinaryMatrix(affineBitmap);
        int qrWidth = QRTransform.getqrWidth();
        int qrBlockSize = QRTransform.getBlockSize();
        int offsetX = offsetEye + qrBlockSize * 3;
        int offsetY = offsetEye + qrBlockSize * 3;
        int[] countBlock = {13, 0};
        System.out.println("offsetX=" + offsetX);
        System.out.println("offsetEye=" + offsetEye);
        for (int x = offsetX; x < qrWidth - offsetEye; x++) {
            int currentState = binaryImage[x][offsetY];
            for (int i = x; i < qrWidth - offsetEye; i++) {
                if (binaryImage[i][offsetY] != currentState) {
                    //System.out.println("i=" + i + " ,currentstate=" + currentState);
                    countBlock[currentState]++;
                    currentState = binaryImage[i][offsetY];
                    x = i;
                }
            }
        }
        int totalBlockH = countBlock[0] + countBlock[1];
        System.out.println("countBlockBlackH=" + countBlock[0]);
        System.out.println("countBlockWhiteH=" + countBlock[1]);
        countBlock[0] = 13;
        countBlock[1] = 0;
        for (int y = offsetY; y < qrWidth - offsetEye; y++) {
            int currentState = binaryImage[offsetX][y];
            for (int i = y; i < qrWidth - offsetEye; i++) {
                if (binaryImage[offsetX][i] != currentState) {
                    //System.out.println("i=" + i + " ,currentstate=" + currentState);
                    countBlock[currentState]++;
                    currentState = binaryImage[offsetX][i];
                    y = i;
                }
            }
        }
        int totalBlockV = countBlock[0] + countBlock[1];
        System.out.println("countBlockBlackV=" + countBlock[0]);
        System.out.println("countBlockWhiteV=" + countBlock[1]);

        int expectedBlockCount = Math.round(qrWidth / qrBlockSize);
        System.out.println("expectedBlock=" + expectedBlockCount);
        double percentageH = QRTransform.percentageDif((double) totalBlockH, (double) expectedBlockCount);
        double percentageV = QRTransform.percentageDif((double) totalBlockV, (double) expectedBlockCount);
        int totalBlock = 0;
        System.out.println("percentageH=" + percentageH);
        System.out.println("percentageV=" + percentageV);
        if (percentageH > percentageV) {
            totalBlock = totalBlockV;
        } else {
            totalBlock = totalBlockH;
        }

        int version = (totalBlock - 21) / 4 + 1;
        qrSizeAndVersion[0] = totalBlock;
        qrSizeAndVersion[1] = version;
        return qrSizeAndVersion;
    }

}
