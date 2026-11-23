package week_03;
import java.util.Scanner;
/** Bài 3.1 - RSA chạy trong cửa sổ Output (console). Chạy: nhấp phải RSADemo.java -> Run File. */
public final class RSADemo {
 public static void main(String[] args) throws Exception {
 Scanner sc = new Scanner(System.in, "UTF-8");
 System.out.println("===== BAI 3.1 - RSA (OAEP, SHA-256) =====");
 System.out.print("Nhap kich thuoc khoa (2048 hoac 3072): ");
 int bits = Integer.parseInt(sc.nextLine().trim());
 RSACipher rsa = new RSACipher();
 long t0 = System.nanoTime();
 rsa.generateKeys(bits);
 System.out.printf("Da tao cap khoa RSA-%d trong %.0f ms%n", bits, (System.nanoTime() - t0) / 1e6);
 System.out.println("Khoa cong khai (Base64, " + rsa.getPublicKeyBase64().length() + " ky tu): "
 + rsa.getPublicKeyBase64().substring(0, 40) + "...");
 System.out.println("Gioi han ban ro: " + rsa.maxPlaintextBytes() + " byte");
 System.out.print("Nhap ban ro: ");
 String plain = sc.nextLine();
 String cipher = rsa.encrypt(plain);
 System.out.println("Ban ma (Base64, " + cipher.length() + " ky tu): " + cipher);
 String back = rsa.decrypt(cipher);
 System.out.println("Giai ma: " + back);
 System.out.println("Khoi phuc dung: " + plain.equals(back));
 }
}