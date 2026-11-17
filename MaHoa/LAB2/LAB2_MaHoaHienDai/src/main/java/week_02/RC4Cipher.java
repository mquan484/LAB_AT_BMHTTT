package week_02;

/**
 * RC4 - mã dòng: sinh dòng khóa (keystream) rồi XOR với dữ liệu. Mã hóa và giải mã là CÙNG một phép
toán.
 * Kết quả mã hóa hiển thị dạng HEX. RC4 đã bị cấm dùng trong TLS - chỉ để học.
 */
public final class RC4Cipher {

    private RC4Cipher() {
    }

    /** KSA + PRGA: XOR data với keystream sinh từ key. */
    public static byte[] apply(byte[] data, byte[] key) {
        if (key.length < 1 || key.length > 256) {
            throw new IllegalArgumentException("Khóa RC4 phải từ 1 đến 256 byte.");
        }
        int[] s = new int[256];
        for (int i = 0; i < 256; i++) {
            s[i] = i;
        }
        // KSA: trộn mảng S theo khóa
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + s[i] + (key[i % key.length] & 0xFF)) & 0xFF;
            int t = s[i]; s[i] = s[j]; s[j] = t;
        }
        // PRGA: mỗi byte dữ liệu XOR với một byte dòng khóa
        byte[] out = new byte[data.length];
        int i = 0;
        j = 0;
        for (int k = 0; k < data.length; k++) {
            i = (i + 1) & 0xFF;
            j = (j + s[i]) & 0xFF;
            int t = s[i]; s[i] = s[j]; s[j] = t;
            out[k] = (byte) (data[k] ^ s[(s[i] + s[j]) & 0xFF]);
        }
        return out;
    }

    public static String encrypt(String plaintext, String key) {
        CipherUtil.requireKey(key);
        return CipherUtil.hex(apply(CipherUtil.utf8(plaintext), CipherUtil.utf8(key)));
    }

    public static String decrypt(String hexCiphertext, String key) {
        CipherUtil.requireKey(key);
        return CipherUtil.text(apply(CipherUtil.fromHex(hexCiphertext), CipherUtil.utf8(key)));
    }
}