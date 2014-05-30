package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IEXSS extends Middleware {

    private final boolean setOnOldIE;

    private static Pattern UA = Pattern.compile("MSIE (\\d+\\.\\d+)b?;");

    public IEXSS() {
        this(false);
    }

    public IEXSS(boolean setOnOldIE) {
        this.setOnOldIE = setOnOldIE;
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        String userAgentHeader = request.getHeader("user-agent", "");

        Matcher matcher = UA.matcher(userAgentHeader);

        boolean isIE = false;
        float version = 0;

        if (matcher.find()) {
            isIE = true;
            String browserVersion = matcher.group(1);

            if (browserVersion != null) {
                try {
                    version = Float.parseFloat(browserVersion);
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
        }

        String value;

        if ((!isIE) || (version >= 9) || (setOnOldIE)) {
            value = "1; mode=block";
        } else {
            value = "0";
        }

        request.response().putHeader("X-XSS-Protection", value);
        next.handle(null);
    }
}
