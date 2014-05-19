package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IEXSS extends Middleware {

    private final boolean setOnOldIE;

    private static Pattern UA = Pattern.compile("([^/\\s]*)(/([^\\s]*))?(\\s*\\[[a-zA-Z][a-zA-Z]\\])?\\s*(\\((([^()]|(\\([^()]*\\)))*)\\))?\\s*");

    public IEXSS(boolean setOnOldIE) {
        this.setOnOldIE = setOnOldIE;
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        String userAgentHeader = request.getHeader("user-agent", "");

        Matcher matcher = UA.matcher(userAgentHeader);

        String browserName = matcher.group(1);
        String browserVersion = matcher.group(3);
        float version = 0;

        if (browserVersion != null) {
            try {
                version = Float.parseFloat(browserVersion);
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }

        boolean isIE = "IE".equals(browserName);

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
