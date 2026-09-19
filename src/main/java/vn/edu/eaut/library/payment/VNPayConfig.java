package vn.edu.eaut.library.payment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Doc cau hinh VNPay tu src/main/resources/vnpay.properties.
 * Khi trien khai that (production that su, khong phai sandbox), chi can
 * doi noi dung file vnpay.properties, KHONG can sua code Java nao trong
 * package payment nay.
 */
public class VNPayConfig {

    private static final Properties props = new Properties();
    private static boolean loaded = false;

    private static synchronized void loadProps() {
        if (loaded) return;
        try (InputStream is = VNPayConfig.class.getClassLoader()
                .getResourceAsStream("vnpay.properties")) {
            if (is == null) {
                throw new RuntimeException(
                        "Khong tim thay vnpay.properties trong classpath (src/main/resources)");
            }
            props.load(is);
            loaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Loi doc vnpay.properties", e);
        }
    }

    public static String getTmnCode() {
        loadProps();
        return props.getProperty("vnp_TmnCode");
    }

    public static String getHashSecret() {
        loadProps();
        return props.getProperty("vnp_HashSecret");
    }

    public static String getPayUrl() {
        loadProps();
        return props.getProperty("vnp_Url");
    }

    public static String getReturnUrl() {
        loadProps();
        return props.getProperty("vnp_ReturnUrl");
    }
}
