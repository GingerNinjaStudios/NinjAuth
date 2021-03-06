package me.gingerninja.authenticator.util;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.gingerninja.authenticator.data.db.entity.Account;

public class Parser {
    private static final Pattern LABEL_PATTERN = Pattern.compile("(?:(.+)(?::|%3A|%3a)(?:(?:%20)| )*(.+))|(.+)");

    @NonNull
    public static String createUrl(@NonNull Account account) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("otpauth");

        builder.path("//" + account.getType());

        switch (account.getType()) {
            case Account.TYPE_TOTP:
                builder.appendQueryParameter("period", Long.toString(account.getTypeSpecificData()));
                break;
            case Account.TYPE_HOTP:
                builder.appendQueryParameter("counter", Long.toString(account.getTypeSpecificData()));
                break;
        }

        builder.appendQueryParameter("secret", account.getSecret());
        builder.appendQueryParameter("digits", Integer.toString(account.getDigits()));

        if (!TextUtils.isEmpty(account.getIssuer())) {
            builder.appendQueryParameter("issuer", account.getIssuer());
            builder.appendPath(account.getIssuer() + ":" + account.getAccountName());
        } else {
            builder.appendPath(account.getAccountName());
        }

        switch (account.getAlgorithm()) {
            case Account.ALGO_SHA1:
                builder.appendQueryParameter("algorithm", "SHA1");
                break;
            case Account.ALGO_SHA256:
                builder.appendQueryParameter("algorithm", "SHA256");
                break;
            case Account.ALGO_SHA512:
                builder.appendQueryParameter("algorithm", "SHA512");
                break;
        }

        return builder.build().toString();
    }

    public static Account parseUrl(@NonNull String url) {
        Uri uri = Uri.parse(url);

        final String scheme = uri.getScheme();
        final String type = uri.getHost();
        final String label = (uri.getLastPathSegment() != null) ? uri.getLastPathSegment().trim() : null;
        final Set<String> paramNames = uri.getQueryParameterNames();

        if (scheme != null && scheme.equalsIgnoreCase("otpauth")) {
            Account account = new Account();

            if (type != null) {
                switch (type) {
                    case "hotp":
                        if (uri.getQueryParameter("counter") == null) {
                            // TODO throw error
                        }
                        account.setType(Account.TYPE_HOTP);
                        account.setTypeSpecificData(Account.DEFAULT_COUNTER);
                        break;
                    case "totp":
                        account.setType(Account.TYPE_TOTP);
                        account.setTypeSpecificData(Account.DEFAULT_PERIOD);
                        break;
                    default:
                        // TODO throw error?
                }
            }

            for (String paramName : paramNames) {
                String paramValue = uri.getQueryParameter(paramName);
                if (paramValue != null) {
                    paramValue = paramValue.trim();
                }

                setParamOnAccount(account, paramName, paramValue);
            }

            if (label != null) {
                Matcher labelMatcher = LABEL_PATTERN.matcher(label);
                if (labelMatcher.matches()) {
                    final String accountName, issuer;
                    if (labelMatcher.group(3) == null) {
                        issuer = labelMatcher.group(1);
                        accountName = labelMatcher.group(2);
                    } else {
                        issuer = null;
                        accountName = labelMatcher.group(3);
                    }

                    if (issuer != null && TextUtils.isEmpty(account.getIssuer())) {
                        account.setIssuer(issuer);
                    }

                    account.setAccountName(accountName);

                    account.setTitle(label); // TODO is this good like this?
                }
            }

            account.setSource(Account.SOURCE_URI);

            return account;
        }

        return null;
    }

    private static void setParamOnAccount(Account account, String name, String value) {
        if (name == null || value == null) {
            return;
        }

        name = name.toLowerCase(Locale.US);
        value = value.trim();

        switch (name) {
            case "secret":
                int paddingIdx = value.indexOf('=');
                if (paddingIdx > -1) {
                    value = value.substring(0, paddingIdx);
                }
                account.setSecret(value);
                break;
            case "issuer":
                account.setIssuer(value);
                break;
            case "algorithm":
                account.setAlgorithm(getAlgorithmByUriValue(value));
                break;
            case "digits":
                account.setDigits(Integer.parseInt(value));
                break;
            case "counter":
            case "period":
                account.setTypeSpecificData(Long.parseLong(value));
                break;
        }
    }

    private static String getAlgorithmByUriValue(@NonNull String value) {
        value = value.toUpperCase(Locale.US);

        switch (value) {
            case "SHA1":
                return Account.ALGO_SHA1;
            case "SHA256":
                return Account.ALGO_SHA256;
            case "SHA512":
                return Account.ALGO_SHA512;
        }
        return Account.ALGO_SHA1; // TODO maybe throw error instead?
    }
}
