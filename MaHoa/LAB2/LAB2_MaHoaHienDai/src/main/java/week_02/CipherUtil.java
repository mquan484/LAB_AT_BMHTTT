package week_02;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Các hàm tiện ích dùng chung cho Lab 2: đổi chuỗi <-> byte, Base64, Hex, số ngẫu nhiên, PBKDF2.
*/
public final class CipherUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CipherUtil() {
    }

    /** Chuỗi -> mảng byte UTF-8 (giữ đúng tiếng Việt). */
    public static byte[] utf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    /** Mảng byte UTF-8 -> chuỗi. */
    public static String text(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }

    /** Bản mã là dãy byte bất kỳ nên đổi sang Base64 để hiện được trong JTextArea. */
    public static String b64(byte[] b) {
        return Base64.getEncoder().encodeToString(b);
    }

    public static byte[] fromB64(String s) {        
        try {
            return Base64.getDecoder().decode(s.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Dữ liệu không phải Base64 hợp lệ.");
        }
    }

    /** Sinh n byte ngẫu nhiên an toàn (dùng cho IV, nonce, salt, khóa). */
    public static byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        RANDOM.nextBytes(b);
        return b;
    }

    /** Byte -> chuỗi HEX chữ hoa, ví dụ {0xBB, 0xF3} -> "BBF3". */
    public static String hex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    public static byte[] fromHex(String hex) {
        String s = hex.trim();
        if (s.length() % 2 != 0 || !s.matches("[0-9A-Fa-f]*")) {
            throw new IllegalArgumentException("Chuỗi HEX không hợp lệ (chỉ gồm 0-9, A-F và có số ký tự chẵn).");
        }
                byte[] out = new byte[s.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }
        return out;
    }

    /** Kiểm tra khóa không rỗng. */
    public static void requireKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Hãy nhập Key.");
        }
    }

    /** Ghép nhiều mảng byte thành một (ví dụ IV || bản mã). */
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    /** PBKDF2-HMAC-SHA256: biến mật khẩu thành khóa / giá trị băm, chậm có chủ ý để chống dò mật khẩu. */
    public static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bits);
            return
SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Không tạo được khóa PBKDF2", ex);
        }
    }
}