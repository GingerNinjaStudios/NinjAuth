package me.gingerninja.authenticator.util;

import android.util.Log;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import me.gingerninja.authenticator.data.db.entity.Account;

@Singleton
public class CodeGenerator {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final char[] BASE32_ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000}; // from 0 to 8

    private static final HashMap<Character, Integer> BASE32_CHAR_MAP;

    private static final String ALGO_SHA1 = "HmacSHA1";
    private static final String ALGO_SHA256 = "HmacSHA256";
    private static final String ALGO_SHA512 = "HmacSHA512";

    @StringDef({ALGO_SHA1, ALGO_SHA256, ALGO_SHA512})
    @interface Algorithm {
    }

    @NonNull
    private TimeCorrector timeCorrector;

    static {
        BASE32_CHAR_MAP = new HashMap<>();
        for (int i = 0; i < BASE32_ARRAY.length; i++) {
            BASE32_CHAR_MAP.put(BASE32_ARRAY[i], i);
        }
    }

    @Inject
    public CodeGenerator(@NonNull TimeCorrector timeCorrector) {
        this.timeCorrector = timeCorrector;
    }

    /**
     * @param secret    the shared secret
     * @param period    the period in seconds
     * @param steps     the extra period count (can be negative)
     * @param algorithm the algorithm to use
     * @param digits    number of digits to use between 0 and 8
     * @return the OTP
     * @throws InvalidKeyException      if the generated key is invalid
     * @throws NoSuchAlgorithmException if the given crypto algorithm is non-existent
     */
    private long getTOTP(@NonNull String secret, long period, long steps, @NonNull @Algorithm String algorithm, @IntRange(from = 1) int digits) throws InvalidKeyException, NoSuchAlgorithmException {
        //    long value = -1; // 18446744073709551615
        //    BigDecimal maxLong = new BigDecimal(Long.MAX_VALUE).add(BigDecimal.ONE).multiply(new BigDecimal(2));
        //    BigDecimal bigValue = new BigDecimal(value);
        //    BigDecimal actualValue = (value < 0) ? bigValue.add(maxLong) : bigValue;
        long T = (long) (Math.floor(timeCorrector.getCurrentTime(TimeUnit.SECONDS) / period) + steps);

        StringBuilder timeStr = new StringBuilder(Long.toHexString(T).toUpperCase());
        while (timeStr.length() < 16) {
            timeStr.insert(0, "0");
        }

        return getHOTP(secret, hexToBytes(timeStr.toString()), algorithm, digits);
    }

    private long getHOTP(@NonNull String secret, long data, @NonNull @Algorithm String algorithm, @IntRange(from = 1) int digits) throws InvalidKeyException, NoSuchAlgorithmException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        //buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(data);

        return getHOTP(secret, buffer.array(), algorithm, digits);
    }

    private long getHOTP(@NonNull String secret, byte[] data, @NonNull @Algorithm String algorithm, @IntRange(from = 1) int digits) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] hash = getRawHMAC(data, decodeBase32(secret), algorithm);

        // DT
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);

        // ST
        return binary % DIGITS_POWER[digits];
        //return binary % (long) Math.pow(10, digits);
    }

    public long getRemainingTime(@NonNull Account account, TimeUnit timeUnit) {
        long period = timeUnit.convert(Math.max(account.getTypeSpecificData(), 1), TimeUnit.SECONDS);
        long time = timeCorrector.getCurrentTime(timeUnit);
        long fracturedTime = (time / period) * period;

        return period - (time - fracturedTime);
    }

    public String getCode(@NonNull Account account) {
        final String secret = account.getSecret();
        final int digits = account.getDigits();
        final String algo = getAccountAlgorithm(account);

        long code;

        try {
            switch (account.getType()) {
                case Account.TYPE_HOTP:
                    code = getHOTP(secret, account.getTypeSpecificData(), algo, digits);
                    break;
                case Account.TYPE_TOTP:
                    code = getTOTP(secret, account.getTypeSpecificData(), 0, algo, digits);
                    break;
                default:
                    throw new IllegalArgumentException("No suitable account type found: " + account.getType());
            }

            return Long.toString(code);
        } catch (Exception e) {
            Log.e("CodeGenerator", "Error while generating OTP", e);
        }
        return null;
    }

    @Nullable
    public String getFormattedCode(@NonNull Account account) {
        String code = getCode(account);

        if (code != null) {
            code = formatCode(code, account.getDigits());
        }

        return code;
    }

    @NonNull
    public String formatCode(String code, @IntRange(from = 1) int digits) {
        StringBuilder stringBuilder = new StringBuilder(digits);
        stringBuilder.append(code);

        while (stringBuilder.length() < digits) {
            stringBuilder.insert(0, "0");
        }

        // TODO spacing or something

        return stringBuilder.toString();
    }

    private static String getAccountAlgorithm(@NonNull Account account) {
        switch (account.getAlgorithm()) {
            case Account.ALGO_SHA1:
                return ALGO_SHA1;
            case Account.ALGO_SHA256:
                return ALGO_SHA256;
            case Account.ALGO_SHA512:
                return ALGO_SHA512;
        }

        throw new IllegalArgumentException("No suitable algorithm found: " + account.getAlgorithm());
    }

    /*public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.SIZE / Byte.SIZE];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }*/

    @NonNull
    private static byte[] getRawHMAC(@NonNull byte[] data, @NonNull byte[] key, @NonNull @Algorithm String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key, algorithm); // algorithm was "RAW" here?
        Mac mac = Mac.getInstance(algorithm);
        mac.init(signingKey);

        return mac.doFinal(data);
    }

    @NonNull
    private static String getHMAC(@NonNull byte[] data, @NonNull byte[] key, @NonNull @Algorithm String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        return bytesToHex(getRawHMAC(data, key, algorithm));
    }

    /*@NonNull
    private static String toHexString(@NonNull byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }*/

    @NonNull
    private static String bytesToHex(@NonNull byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int unit = bytes[i] & 0xFF;

            hexChars[i * 2] = HEX_ARRAY[unit >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[unit & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] hexToBytes(String hex) {
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    @NonNull
    private static byte[] decodeBase32(@NonNull String data) {
        int n = data.length();

        /*if (n % 16 != 0) {
            throw new IllegalArgumentException("The data's length must be a mod of 16");
        }*/

        data = data.toUpperCase(Locale.US);

        byte[] bytes = new byte[n * 5 / 8];

        int buffer = 0, remBits = 0;
        int i = 0;

        for (char c : data.toCharArray()) {
            if (!BASE32_CHAR_MAP.containsKey(c)) {
                throw new IllegalArgumentException("Invalid character: " + c);
            }
            buffer <<= 5;
            //noinspection ConstantConditions
            buffer |= BASE32_CHAR_MAP.get(c) & 31;
            remBits += 5;

            if (remBits >= 8) {
                bytes[i++] = (byte) (buffer >> (remBits - 8));
                remBits -= 8;
            }
        }

        return bytes;
    }
}
