package com.app.src.abcqr.utils;

import java.util.HashMap;
import java.util.Locale;

public class VietQRCodeGenerator {
    private static final HashMap<String, String> BANK_CODES = new HashMap<>();

    static {
        // Khởi tạo danh sách các mã ngân hàng
        BANK_CODES.put("VietinBank", "970415");
        BANK_CODES.put("Vietcombank", "970436");
        BANK_CODES.put("BIDV", "970418");
        BANK_CODES.put("Agribank", "970405");
        BANK_CODES.put("OCB", "970448");
        BANK_CODES.put("MBBank", "970422");
        BANK_CODES.put("Techcombank", "970407");
        BANK_CODES.put("ACB", "970416");
        BANK_CODES.put("VPBank", "970432");
        BANK_CODES.put("TPBank", "970423");
        BANK_CODES.put("Sacombank", "970403");
        BANK_CODES.put("HDBank", "970437");
        BANK_CODES.put("VietCapitalBank", "970454");
        BANK_CODES.put("SCB", "970429");
        BANK_CODES.put("VIB", "970441");
        BANK_CODES.put("SHB", "970443");
        BANK_CODES.put("Eximbank", "970431");
        BANK_CODES.put("MSB", "970426");
        BANK_CODES.put("CAKE", "546034");
        BANK_CODES.put("Ubank", "546035");
        BANK_CODES.put("Timo", "963388");
        BANK_CODES.put("ViettelMoney", "971005");
        BANK_CODES.put("VNPTMoney", "971011");
        BANK_CODES.put("SaigonBank", "970400");
        BANK_CODES.put("BacABank", "970409");
        BANK_CODES.put("PVcomBank", "970412");
        BANK_CODES.put("Oceanbank", "970414");
        BANK_CODES.put("NCB", "970419");
        BANK_CODES.put("ShinhanBank", "970424");
        BANK_CODES.put("ABBANK", "970425");
        BANK_CODES.put("VietABank", "970427");
        BANK_CODES.put("NamABank", "970428");
        BANK_CODES.put("PGBank", "970430");
        BANK_CODES.put("VietBank", "970433");
        BANK_CODES.put("BaoVietBank", "970438");
        BANK_CODES.put("SeABank", "970440");
        BANK_CODES.put("COOPBANK", "970446");
        BANK_CODES.put("LPBank", "970449");
        BANK_CODES.put("KienLongBank", "970452");
        BANK_CODES.put("KBank", "668888");
        BANK_CODES.put("KookminHN", "970462");
        BANK_CODES.put("KEBHanaHCM", "970466");
        BANK_CODES.put("KEBHanaHN", "970467");
        BANK_CODES.put("MAFC", "977777");
        BANK_CODES.put("Citibank", "533948");
        BANK_CODES.put("KookminHCM", "970463");
        BANK_CODES.put("VBSP", "999888");
        BANK_CODES.put("Woori", "970457");
        BANK_CODES.put("VRB", "970421");
        BANK_CODES.put("UnitedOverseas", "970458");
        BANK_CODES.put("StandardChartered", "970410");
        BANK_CODES.put("PublicBank", "970439");
        BANK_CODES.put("Nonghyup", "801011");
        BANK_CODES.put("IndovinaBank", "970434");
        BANK_CODES.put("IBKHCM", "970456");
        BANK_CODES.put("IBKHN", "970455");
        BANK_CODES.put("HSBC", "458761");
        BANK_CODES.put("HongLeong", "970442");
        BANK_CODES.put("GPBank", "970408");
        BANK_CODES.put("DongABank", "970406");
        BANK_CODES.put("DBSBank", "796500");
        BANK_CODES.put("CIMB", "422589");
        BANK_CODES.put("CBBank", "970444");
    }


    /**
     * Generate a VietQR string based on the input parameters.
     *
     * @param bankCode The bank code (e.g., "Viettinbank", "MBBank").
     * @param accountNumber The beneficiary's account number.
     * @return The formatted VietQR string.
     */
    public static String generateID38(String bankCode, String accountNumber) {
        String GUID = "A000000727";
        String service_code = "QRIBFTTA";
        String ID38_data00 = "00" + countCharacters(GUID) + GUID;
        String bank_info ="00" + countCharacters(bankCode) + bankCode;
        String acc_info  ="01" + countCharacters(accountNumber) + accountNumber;
        String service_info = "02" + countCharacters(service_code) + service_code;
        String ID38_data01 = bank_info + acc_info ;
        String  data_ID38 = ID38_data00 +"01" +  countCharacters(ID38_data01)+ ID38_data01 + service_info;
        return "38" + countCharacters(data_ID38) + data_ID38;
    }
    public static String countCharacters(String input) {
        if (input == null) {
            return "00"; // Trả về "00" nếu chuỗi là null
        }
        int count = input.length();
        // Nếu độ dài nhỏ hơn 10, thêm "0" vào trước
        if (count < 10) {
            return "0" + count;
        }
        return String.valueOf(count);
    }
    public static String generateVietQRCode(String bankName, String accountNumber, String Amount_VND) {
        if (bankName == null || accountNumber == null ) {
            throw new IllegalArgumentException("All inputs must be non-null.");
        }

        String bankCode = BANK_CODES.get(bankName);
        // Fixed VietQR fields
        String payloadFormat = "000201"; //ID 00
        String method_In  = "010211"; //ID01 - QR tinh
        String DVCNTT_Napas = generateID38(bankCode, accountNumber); // ID38
        String currencyCode = "5303704";
        String amount_VND = "";
        if (Amount_VND != null) {
             amount_VND = "54" + countCharacters(Amount_VND) + Amount_VND;
        }
        String Country = "5802VN";
        String data_fin = payloadFormat + method_In + DVCNTT_Napas + currencyCode + amount_VND + Country + "6304";
        String CRC = calculateCRC(data_fin);
        System.out.printf (data_fin);
        return data_fin + CRC;
        //return String.valueOf(GUID);
    }

    /**
     * Format a field's length to a fixed-width string.
     *
     * @param length The length of the field.
     * @param width The width of the length field.
     * @return The formatted length.
     */
    private static String formatLength(int length, int width) {
        return String.format(Locale.ROOT, "%0" + width + "d", length);
    }

    /**
     * Calculate CRC16 checksum for a given string.
     *
     * @param data The input string to calculate CRC.
     * @return The CRC16 checksum as a hexadecimal string.
     */
    private static String calculateCRC(String data) {
        int crc = 0xFFFF; // Initial value
        int polynomial = 0x1021; // Polynomial used for CRC calculation

        byte[] bytes = data.getBytes();
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i)) & 1) == 1;
                boolean c15 = ((crc >> 15) & 1) == 1;
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }

        crc &= 0xFFFF; // Ensure CRC is 16-bit
        return String.format(Locale.ROOT, "%04X", crc); // Return as uppercase hexadecimal
    }
}
