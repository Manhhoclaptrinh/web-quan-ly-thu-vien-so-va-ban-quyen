package vn.edu.eaut.library.payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Tien ich dung chung cho tich hop VNPay:
 *  - Ky du lieu (HMAC-SHA512) khi tao URL thanh toan gui sang VNPay
 *  - Kiem tra chu ky khi nhan du lieu tra ve tu VNPay (Return URL)
 *
 * Code dua theo dung mau chinh thuc cua VNPay (sandbox.vnpayment.vn/apis/docs),
 * chi doi ten bien cho ro nghia hon.
 */
public class VNPayUtil {

    private VNPayUtil() {
    }

    /**
     * Tinh HMAC-SHA512 cua 1 chuoi du lieu voi secret key cho truoc.
     * Tra ve chuoi hex (khong co dau cach, chu thuong), dung dinh dang VNPay yeu cau.
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Loi tinh HMAC-SHA512 cho VNPay", e);
        }
    }

    /**
     * Xay dung URL thanh toan hoan chinh (da ky chu ky) tu 1 map tham so vnp_*.
     * Map truyen vao KHONG duoc chua vnp_SecureHash - ham nay tu tinh va gan vao.
     */
    public static String buildPaymentUrl(Map<String, String> params, String hashSecret, String payUrl) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String secureHash = hmacSHA512(hashSecret, hashData.toString());
        return payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Kiem tra chu ky cua du lieu VNPay tra ve (Return URL).
     * params: toan bo tham so vnp_* nhan duoc TRU vnp_SecureHash / vnp_SecureHashType.
     * receivedHash: gia tri vnp_SecureHash nhan duoc tu request.
     */
    public static boolean verifySignature(Map<String, String> params, String receivedHash, String hashSecret) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        String computedHash = hmacSHA512(hashSecret, hashData.toString());
        return computedHash.equalsIgnoreCase(receivedHash);
    }

    /** Sinh mã tham chiếu đơn hàng (vnp_TxnRef) duy nhất trong ngày. */
    public static String generateTxnRef() {
        return String.valueOf(System.currentTimeMillis());
    }
}
