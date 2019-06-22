package com.tarpha.torrssen2.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtils {

    protected static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static String getRename(
        String rename, 
        String title, 
        String season, 
        String episode, 
        String quality, 
        String releaseGroup, 
        String rssDate) {
        return StringUtils.replaceEach(rename
            , new String[]{"${TITLE}", "${SEASON}", "${EPISODE}", "${QUALITY}", "${RELEASE_GROUP}", "${DATE}"}
            , new String[]{title, season, episode, quality, releaseGroup, rssDate});
    }

    public static boolean renameFile(String path, String from, String to) {
        logger.info("Rename File To: " + to);
        File srcFile = new File(path + File.separator, from);
        File destFile = new File(path, to + "." + FilenameUtils.getExtension(from));
 
        return srcFile.renameTo(destFile);
    }

    public static boolean removeDirectory(String path, String outer, String inner) {
        boolean ret = true;

        File file = new File(path, outer);
        if (file.isDirectory()) {
            File src = new File(path + File.separator + outer, inner);
            File trg = new File(path);
            // ret = src.renameTo(trg); 
            try {
                FileUtils.moveFileToDirectory(src, trg, false);
                FileUtils.forceDelete(new File(path, outer));
            } catch (IOException e) {
                logger.error(e.getMessage());
                ret = false;
            }
        } else {
            ret = false;
        }

        return ret;
    }

}