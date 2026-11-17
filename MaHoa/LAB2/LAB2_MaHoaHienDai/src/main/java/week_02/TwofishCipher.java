package week_02;

import java.security.MessageDigest;
import java.util.Arrays;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Twofish (thư viện BouncyCastle) chế độ CBC, đệm PKCS7.
 * Khóa 256 bit = SHA-256(Key). IV 16 byte, nhập dạng HEX (32 ký tự). Giải mã cần ĐÚNG Key và ĐÚNG
IV.
 */
public final class TwofishCipher {

    private TwofishCipher() {
    }

    /** Sinh IV ngẫu nhiên 16 byte, trả về 32 ký tự HEX. */
    public static String newIv() {
        return CipherUtil.hex(CipherUtil.randomBytes(16));
    }

    private static byte[] run(boolean encrypt, byte[] input, String key, String ivHex) throws Exception {
        CipherUtil.requireKey(key);
        byte[] iv = CipherUtil.fromHex(ivHex);
        if (iv.length != 16) {
            throw new IllegalArgumentException("IV phải đúng 16 byte = 32 ký tự HEX. Bấm Generate IV để tạo.");
        }
        byte[] k = MessageDigest.getInstance("SHA-256").digest(CipherUtil.utf8(key));
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(CBCBlockCipher.newInstance(new TwofishEngine()));
        cipher.init(encrypt, new ParametersWithIV(new KeyParameter(k), iv));
        byte[] out = new byte[cipher.getOutputSize(input.length)];
        int len = cipher.processBytes(input, 0, input.length, out, 0);
        len += cipher.doFinal(out, len);
        return Arrays.copyOf(out, len);
    }

    public static String encrypt(String plaintext, String key, String ivHex) throws Exception {
        return CipherUtil.b64(run(true, CipherUtil.utf8(plaintext), key, ivHex));
    }

    public static String decrypt(String ciphertext, String key, String ivHex) throws Exception {
        return CipherUtil.text(run(false, CipherUtil.fromB64(ciphertext), key, ivHex));
    }

    /** Nội dung tệp lưu: dòng 1 = IV, dòng 2 = bản mã (KHÔNG lưu Key). */
    public static String toFile(String ivHex, String ciphertext) {
        return "IV=" + ivHex.trim() + "\n" + ciphertext.trim() + "\n";
    }

    /** Tách tệp đã lưu thành {IV, bản mã}. */
    public static String[] fromFile(String content) {
        String[] lines = content.trim().split("\\R", 2);
        if (lines.length < 2 || !lines[0].startsWith("IV=")) {
            throw new IllegalArgumentException("Tệp không đúng định dạng: dòng 1 phải là IV=..., dòng 2 là bản mã.");
        }
        return new String[] {lines[0].substring(3).trim(), lines[1].trim()};
    }
}