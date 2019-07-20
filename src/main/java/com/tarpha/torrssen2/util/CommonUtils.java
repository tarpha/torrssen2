package com.tarpha.torrssen2.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.SettingRepository;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtils {
    public static String getRename(String rename, String title, String season, String episode, String quality,
            String releaseGroup, String rssDate) {
        return StringUtils.replaceEach(rename,
                new String[] { "${TITLE}", "${SEASON}", "${EPISODE}", "${QUALITY}", "${RELEASE_GROUP}", "${DATE}" },
                new String[] { title, season, episode, quality, releaseGroup, rssDate });
    }

    public static boolean renameFile(String path, String from, String to) {
        log.info("Rename File To: " + to);
        File srcFile = new File(path + File.separator, from);
        File destFile = new File(path, to + "." + FilenameUtils.getExtension(from));

        return srcFile.renameTo(destFile);
    }

    public static boolean removeDirectory(String path, String outer, String inner, SettingRepository settingRepository) {
        Optional<Setting> delDirSetting = settingRepository.findByKey("DEL_DIR");
        
        if (delDirSetting.isPresent()) {
            if(Boolean.parseBoolean(delDirSetting.get().getValue())) {
                boolean ret = true;

                File file = new File(path, outer);
                if (file.isDirectory()) {
                    File src = new File(path + File.separator + outer, inner);
                    File trg = new File(path);
                    try {
                        File remove = new File(path, inner);
                        if (remove.isFile()) {
                            FileUtils.forceDelete(remove);
                        }
                        FileUtils.moveFileToDirectory(src, trg, true);
                        FileUtils.forceDelete(new File(path, outer));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        ret = false;
                    }
                } else {
                    ret = false;
                }

                return ret;
            }
        }
        
        return false;
    }

    public static boolean removeDirectory(String path, String outer, List<String> innerList, SettingRepository settingRepository) {
        String[] exts = {};
        Optional<Setting> exceptExtSetting = settingRepository.findByKey("EXCEPT_EXT");
        if(exceptExtSetting.isPresent()) {
            exts = StringUtils.split(StringUtils.lowerCase(exceptExtSetting.get().getValue()), ",");
        }
        
        Optional<Setting> delDirSetting = settingRepository.findByKey("DEL_DIR");

        log.debug("delete List");
        
        if (delDirSetting.isPresent()) {
            log.debug(delDirSetting.get().getValue());
            if(Boolean.parseBoolean(delDirSetting.get().getValue())) {
                boolean ret = true;

                File file = new File(path, outer);
                if (file.isDirectory()) {
                    try {
                        for (String inner : innerList) {
                            if(!StringUtils.containsAny(StringUtils.lowerCase(FilenameUtils.getExtension(inner)), exts)) {
                                File src = new File(path + File.separator + outer, inner);
                                File trg = new File(path);
                                File remove = new File(path, inner);
                                if (remove.isFile()) {
                                    FileUtils.forceDelete(remove);
                                }
                                FileUtils.moveFileToDirectory(src, trg, true);
                            }
                        }
                        FileUtils.forceDelete(new File(path, outer));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        ret = false;
                    }
                } else {
                    ret = false;
                }

                return ret;
            }
        }
        return false;
    }

    public static List<String> removeDirectory(String path, String outer, SettingRepository settingRepository) {
        String[] exts = {};
        Optional<Setting> exceptExtSetting = settingRepository.findByKey("EXCEPT_EXT");
        if(exceptExtSetting.isPresent()) {
            exts = StringUtils.split(StringUtils.lowerCase(exceptExtSetting.get().getValue()), ",");
        }

        Optional<Setting> delDirSetting = settingRepository.findByKey("DEL_DIR");
        
        if (delDirSetting.isPresent()) {
            if(Boolean.parseBoolean(delDirSetting.get().getValue())) {
                File file = new File(path, outer);
                if (file.isDirectory()) {
                    Collection<File> subFiles;
                    if(exts != null) {
                        NotFileFilter fileFilter = new NotFileFilter(new SuffixFileFilter(exts)); 
                        subFiles = FileUtils.listFiles(new File(path, outer), fileFilter, TrueFileFilter.INSTANCE);
                    } else {
                        subFiles = FileUtils.listFiles(new File(path, outer), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                    }
                    
                    List<String> ret = new ArrayList<>();

                    try {
                        for(File subFile: subFiles) {
                            log.debug(subFile.getPath() + ":" + subFile.getName());
                            File remove = new File(path, subFile.getName());
                            if (remove.isFile()) {
                                FileUtils.forceDelete(remove);
                            }
                            FileUtils.moveFileToDirectory(subFile, new File(path), true);
                            ret.add(subFile.getName());
                        }
                        FileUtils.forceDelete(new File(path, outer));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }

                    return ret;
                } 
            }
        }

        return null;
    }

}