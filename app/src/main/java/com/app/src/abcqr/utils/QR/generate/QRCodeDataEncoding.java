    package com.app.src.abcqr.utils.QR.generate;

    import java.nio.charset.StandardCharsets;

    import static com.app.src.abcqr.utils.QR.QRVersion.ErrorCorrectionLevel;
    import static com.app.src.abcqr.utils.QR.QRVersion.getVersionECInfo;

    import com.app.src.abcqr.utils.QR.QRVersion;


    public class QRCodeDataEncoding {

        private int version;
        private ErrorCorrectionLevel errorCorrectionLevel;

        public QRCodeDataEncoding(int v, ErrorCorrectionLevel e) {
            version = v;
            errorCorrectionLevel = e;
        }

        public static String getModeIndicator() {
            return "0100";
        }

        public String dataEncode(String data) {
            System.out.println(version);
            String bitString = getModeIndicator() + getCharacterCountIndicator(data) + encodeUTF8(data);
            QRVersion.VersionECInfo vi = getVersionECInfo(version, errorCorrectionLevel);

            int requiredBits = vi.getTotalDataCodewords() * 8;
            bitString = addTerminator(bitString, requiredBits);
            bitString = padToMultipleOf8(bitString);
            bitString = addPadBytes(bitString, requiredBits);
            return bitString;
        }

        public String getCharacterCountIndicator(String data) {
            int length = data.length();
            int bits = version <= 9 ? 8 : (version <= 26 ? 16 : 16);
            String binary = Integer.toBinaryString(length);
            while (binary.length() < bits) {
                binary = "0" + binary;
            }
            return binary;
        }

        public static String encodeUTF8(String data) {
            StringBuilder result = new StringBuilder();

            byte[] utf8Bytes = data.getBytes(StandardCharsets.UTF_8);

            for (byte b : utf8Bytes) {
                String binary = Integer.toBinaryString(b & 0xFF);

                while (binary.length() < 8) {
                    binary = "0" + binary;
                }

                result.append(binary);
            }

            return result.toString();
        }

        public static String addTerminator(String data, int requiredBits) {
            int terminatorLength = Math.min(4, requiredBits - data.length());

            StringBuilder terminator = new StringBuilder();
            for (int i = 0; i < terminatorLength; i++) {
                terminator.append("0");
            }
            return data + terminator.toString();
        }

        public static String padToMultipleOf8(String data) {
            while (data.length() % 8 != 0) {
                data = data + "0";
            }
            return data;
        }

        public static String addPadBytes(String data, int requiredBits) {
            String[] padBytes = {"11101100", "00010001"};
            int index = 0;
            while (data.length() < requiredBits) {
                data += padBytes[index];
                index = (index + 1) % 2;
            }
            return data;
        }
    }
