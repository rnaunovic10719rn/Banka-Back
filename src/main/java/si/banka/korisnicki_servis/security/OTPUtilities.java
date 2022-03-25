package si.banka.korisnicki_servis.security;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;

public class OTPUtilities {

    private static SecureRandom _random = new SecureRandom();

    public static String generateTOTPSecretKey() {
        byte[] bytes = new byte[20];
        _random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public static String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }

    public static String getTOTPUrl(String secretKey, String account, String issuer) {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static BitMatrix createTOTPQRCodeMatrix(String totpUrl, int height, int width) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(totpUrl, BarcodeFormat.QR_CODE, width, height);
            return matrix;
        } catch (WriterException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String createTOTPQRCodeBase64Png(String totpUrl, int height, int width) {

        var matrix = createTOTPQRCodeMatrix(totpUrl, height, width);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matrix, "png", out);
            var array = out.toByteArray();
            var encoded = Base64.getEncoder().encode(array);
            var base64String = new String(encoded);
            return "data:image/png;base64, " + base64String;
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }
}
