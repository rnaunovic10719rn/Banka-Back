package rs.edu.raf.banka.user_service.security;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

public class OTPUtilities {

    private static SecureRandom _random = new SecureRandom();


    private static TimeProvider timeProvider = new SystemTimeProvider();
    private static CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private static CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    public static String generateTOTPSecretKey() {
        byte[] bytes = new byte[20];
        _random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public static boolean isValidSeecret(String str) {
        try{
            Base32 base32 = new Base32();
            return base32.decode(str).length == 20;
        }
        catch(Exception ex)
        {
            return false;
        }
    }

    public static boolean validate(String secretKey, String otp) {
        return verifier.isValidCode(secretKey, otp);
    }

    public static QrData createTOTPQRCodeData(String secretKey, String label, String issuer) {
        QrData data = new QrData.Builder()
                .label(label)
                .secret(secretKey)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1) // More on this below
                .digits(6)
                .period(30)
                .build();
        return data;
    }

    public static String createTOTPQRCodeBase64Png(String secretKey, String label, String issuer) {
        try {
            var data = createTOTPQRCodeData(secretKey, label, issuer);
            QrGenerator generator = new ZxingPngQrGenerator();
            byte[] imageData = generator.generate(data);
            String mimeType = generator.getImageMimeType();
            return getDataUriForImage(imageData, mimeType);
        } catch (QrGenerationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String createTOTPQrUri(String secretKey, String account, String issuer) {
        var data = createTOTPQRCodeData(secretKey, account, issuer);
        return data.getUri();
    }
}
