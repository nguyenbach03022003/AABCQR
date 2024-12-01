package com.app.src.abcqr.utils;

import java.util.HashMap;
import java.util.Locale;

public class VietQRCodeGenerator {
    private static final HashMap<String, String> BANK_CODES = new HashMap<>();

    static {
        // Khởi tạo danh sách các mã ngân hàng
        BANK_CODES.put("Agribank", "970499");
        BANK_CODES.put("Vietinbank", "970489");
        BANK_CODES.put("DongABank", "970406");
        BANK_CODES.put("Saigonbank", "161087");
        BANK_CODES.put("BIDV", "970488");
        BANK_CODES.put("SeABank", "970468");
        BANK_CODES.put("GP.Bank", "970408");
        BANK_CODES.put("PG Bank", "970430");
        BANK_CODES.put("PVcomBank", "970412");
        BANK_CODES.put("Kienlongbank", "970452");
        BANK_CODES.put("Vietcapital Bank", "970454");
        BANK_CODES.put("VietBank", "970433");
        BANK_CODES.put("OceanBank", "970414");
        BANK_CODES.put("Sacombank", "970403");
        BANK_CODES.put("ABBank", "970459");
        BANK_CODES.put("VRB", "970421");
        BANK_CODES.put("Vietcombank", "686868");
        BANK_CODES.put("ACB", "970416");
        BANK_CODES.put("Eximbank", "452999");
        BANK_CODES.put("TPBank", "970423");
        BANK_CODES.put("SHB", "970443");
        BANK_CODES.put("HDBank", "970437");
        BANK_CODES.put("MBBank", "970422");
        BANK_CODES.put("VPBank", "981957");
        BANK_CODES.put("VIB", "180906");
        BANK_CODES.put("VietNam Asia Bank", "166888");
        BANK_CODES.put("Techcombank", "888899");
        BANK_CODES.put("OCB", "970448");
        BANK_CODES.put("NCB", "818188");
        BANK_CODES.put("HLBVN", "970442");
        BANK_CODES.put("LienVietPostBank", "970449");
        BANK_CODES.put("BacABank", "970409");
        BANK_CODES.put("BVB", "970438");
        BANK_CODES.put("ShinhanVN", "970424");
        BANK_CODES.put("Public Bank Viet Nam", "970439");
        BANK_CODES.put("SCB", "157979");
        BANK_CODES.put("Maritime Bank", "970426");
        BANK_CODES.put("NamABank", "970428");
        BANK_CODES.put("Indovina Bank", "970434");
        BANK_CODES.put("Viet Nam Woori Bank", "970457");
        BANK_CODES.put("IBK Bank", "970455");
        BANK_CODES.put("Co-op Bank", "970446");
        BANK_CODES.put("CIMB", "422589");
        BANK_CODES.put("UOB", "970458");
        // Thêm các ngân hàng khác nếu cần
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
        String ID38_data01 = bank_info + acc_info + service_info;
        String  data_ID38 = ID38_data00 +"01" +  countCharacters(ID38_data01)+ ID38_data01;
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
    public static String generateVietQRCode(String bankName, String accountNumber, String beneficiaryName) {
        if (bankName == null || accountNumber == null || beneficiaryName == null) {
            throw new IllegalArgumentException("All inputs must be non-null.");
        }
        String bankCode = BANK_CODES.get(bankName);
        // Fixed VietQR fields
        String payloadFormat = "000201"; //ID 00
        String method_In  = "010211"; //ID01 - QR tinh
        String DVCNTT_Napas = generateID38(bankCode, accountNumber); // ID38
        String currencyCode = "5303704";
        String Country = "5802VN";
        String data_fin = payloadFormat + method_In + DVCNTT_Napas + currencyCode + Country + "6304";
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
