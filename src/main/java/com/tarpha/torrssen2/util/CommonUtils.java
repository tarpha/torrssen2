package com.tarpha.torrssen2.util;

import org.apache.commons.lang3.StringUtils;

public class CommonUtils {

    public static String getRename(String rename, String title, String season, String episode, String quality, String ReleaseGroup) {
        return StringUtils.replaceEach(rename
            , new String[]{"${TITLE}", "${SEASON}", "${EPISODE}", "${QUALITY}", "${RELEASE_GROUP}"}
            , new String[]{title, season, episode, quality, ReleaseGroup});
    }

}