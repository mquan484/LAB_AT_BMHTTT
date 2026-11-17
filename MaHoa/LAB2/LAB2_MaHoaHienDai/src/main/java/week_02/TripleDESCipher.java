package week_02;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * TripleDES (3DES = DES 3 lần: mã - giải - mã) - khóa 24 byte = 3 khóa DES.
 * Chế độ CBC, đệm PKCS5. Bản mã = Base64( IV 8 byte ngẫu nhiên || dữ liệu đã mã hóa ).
 * Chỉ dùng để học: DESede đã lỗi thời, ứng dụng thật dùng AES.
 */
public final class TripleDESCipher {

    private static final String TRANSFORMATION = "DESede/CBC/PKCS5Padding";

    private TripleDESCipher() {
    }

    /** Khóa phải gõ đúng 24 ký tự ASCII (= 24 byte). */
    private static SecretKeySpec key(String key) {
        CipherUtil.requireKey(key);
        byte[] k = CipherUtil.utf8(key);
        if (k.length != 24) {
            throw new IllegalArgumentException("Khóa DESede phải đúng 24 byte (gõ 24 ký tự không dấu). Hiện có " + k.length + " byte.");
        }
        return new SecretKeySpec(k, "DESede");
    }

    public static String encrypt(String plaintext, String key) throws Exception {
        byte[] iv = CipherUtil.randomBytes(8);             // IV mới cho mỗi lần mã hóa
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key(key), new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(CipherUtil.utf8(plaintext));
        return CipherUtil.b64(CipherUtil.concat(iv, encrypted)); // gửi kèm IV ở đầu bản mã
    }

    public static String decrypt(String ciphertext, String key) throws Exception {
        byte[] all = CipherUtil.fromB64(ciphertext);
        if (all.length < 8 + 8) {
            throw new IllegalArgumentException("Bản mã quá ngắn hoặc bị hỏng.");
        }
        byte[] iv = java.util.Arrays.copyOfRange(all, 0, 8);
        byte[] data = java.util.Arrays.copyOfRange(all, 8, all.length);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key(key), new IvParameterSpec(iv));
        return CipherUtil.text(cipher.doFinal(data));
    }
}