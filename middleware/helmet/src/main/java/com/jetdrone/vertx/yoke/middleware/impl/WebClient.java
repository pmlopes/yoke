package com.jetdrone.vertx.yoke.middleware.impl;

/*
 *  Copyright 2010 Richard Nichols.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

import com.jetdrone.vertx.yoke.middleware.YokeRequest;

/**
 * A mechanism to detect client software, version and platform from a user-agent string.
 *
 * @author Richard Nichols
 */
public final class WebClient {

    public static enum UserAgent {
        IE,
        FIREFOX,
        CHROME,
        CHROME_MOBILE,
        OPERA,
        OPERA_MINI,
        SAFARI,
        GOOGLEBOT,
        YAHOO_SLURP,
        MSNBOT,
        UNKNOWN
    }

    private final UserAgent userAgent;
    private final int majorVersion;
    private final String fullVersion;

    private WebClient(UserAgent userAgent, int majorVersion, String fullVersion) {
        this.userAgent = userAgent;
        this.majorVersion = majorVersion;
        this.fullVersion = fullVersion;
    }

    public String getFullVersion() {
        return fullVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    public static WebClient detect(YokeRequest req) {
        return detect(req.getHeader("User-Agent"));
    }

    public static WebClient detect(String userAgentString) {
        UserAgent ua = UserAgent.UNKNOWN;
        int version = 0;
        String ver = null;

        if (userAgentString != null && !"".equals(userAgentString)) {
            try {
                if (userAgentString.contains("Yahoo! Slurp")) {
                    ua = UserAgent.YAHOO_SLURP;
                } else if (userAgentString.contains("Googlebot/")) {
                    ua = UserAgent.GOOGLEBOT;
                    ver = userAgentString.substring(userAgentString.indexOf("Googlebot/")+10);
                    ver = ver.substring(0, (ver.indexOf(";") > 0 ? ver.indexOf(";") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("msnbot/")) {
                    ua = UserAgent.MSNBOT;
                    ver = userAgentString.substring(userAgentString.indexOf("msnbot/")+7);
                    ver = ver.substring(0, (ver.indexOf(" ") > 0 ? ver.indexOf(" ") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if(userAgentString.contains("Chrome/")) {
                    ua = UserAgent.CHROME;
                    ver = userAgentString.substring(userAgentString.indexOf("Chrome/")+7);
                    ver = ver.substring(0, ver.indexOf(" ")).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                    // TODO: also check if it is mobile
                } else if (userAgentString.contains("Safari/")) {
                    ua = UserAgent.SAFARI;
                    ver = userAgentString.substring(userAgentString.indexOf("Version/")+8);
                    ver = ver.substring(0, (ver.indexOf(" ") > 0 ? ver.indexOf(" ") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("Opera Mini/")) {
                    ua = UserAgent.OPERA_MINI;
                    ver = userAgentString.substring(userAgentString.indexOf("Opera Mini/")+11);
                    ver = ver.substring(0, (ver.indexOf("/") > 0 ? ver.indexOf("/") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("Opera ")) {
                    ua = UserAgent.OPERA;
                    ver = userAgentString.substring(userAgentString.indexOf("Opera ")+6);
                    ver = ver.substring(0, (ver.indexOf(" ") > 0 ? ver.indexOf(" ") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("Firefox/")) {
                    ua = UserAgent.FIREFOX;
                    ver = userAgentString.substring(userAgentString.indexOf("Firefox/")+8);
                    ver = ver.substring(0, (ver.indexOf(" ") > 0 ? ver.indexOf(" ") : ver.length())).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                }  else if (userAgentString.contains("MSIE ")) {
                    ua = UserAgent.IE;
                    ver = userAgentString.substring(userAgentString.indexOf("MSIE ")+5);
                    ver = ver.substring(0, ver.indexOf(";")).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                } else if (userAgentString.contains("Opera/")) {
                    ua = UserAgent.OPERA;
                    ver = userAgentString.substring(userAgentString.indexOf("Opera/")+6);
                    ver = ver.substring(0, ver.indexOf(" ")).trim();
                    version = Integer.parseInt(ver.substring(0, ver.indexOf(".")));
                }
            } catch (NumberFormatException nfe) {
                ver = null;
                version = 0;
            }
        }

        return new WebClient(ua, version, ver);
    }

    @Override
    public String toString() {
        return userAgent+" "+fullVersion;
    }
}
