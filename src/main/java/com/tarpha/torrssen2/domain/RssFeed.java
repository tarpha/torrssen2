package com.tarpha.torrssen2.domain;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.rometools.rome.feed.synd.SyndEntry;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Entity
@Data
public class RssFeed {

    @Id
    private String link;

    private String title;

    private String desc;

    private String rssEpisode;

    private String rssSeason = "01";

    private String rssTitle;

    private String rssQuality;

    private String rssReleaseGroup;

    private String rssSite;

    private String rssPoster;

    private String rssDate;

    private Long downloadId;

    private Boolean downloading = false;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDt = new Date();

    public void setRssEpisodeByTitle(String title) {
        Pattern pattern = Pattern.compile(".*e(\\d{2,}).*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(title);

        String episode = "01";

        if (matcher.matches()) {
            episode = matcher.group(1);
        }

        this.rssEpisode = episode;
    }

    public void setRssSeasonByTitle(String title) {
        Pattern pattern = Pattern.compile(".*s(\\d{1,}).*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(title);

        String season = "01";

        if (matcher.matches()) {
            season = matcher.group(1);
        } else {
            pattern = Pattern.compile(".*시즌(\\d{1,}).*", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(title);

            if (matcher.matches()) {
                season = matcher.group(1);
            }
        }

        this.rssSeason = season;
    }

    public void setRssTitleByTitle(String title) {
        Pattern pattern = Pattern.compile(".*(e\\d{2,}).*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(title);

        String rssTitle = title;

        if (matcher.matches()) {
            rssTitle = rssTitle.substring(0, matcher.start(1)).replaceAll("\\.", "");
        }

        pattern = Pattern.compile("\\d{1,}-\\d{1,}회 합본");
        rssTitle = RegExUtils.removeAll(rssTitle, pattern);

        pattern = Pattern.compile("\\[.{1,}\\]");
        rssTitle = RegExUtils.removeAll(rssTitle, pattern);

        this.rssTitle = StringUtils.trim(rssTitle);
    }

    public void setRssQualityBytitle(String title) {
        Pattern pattern = Pattern.compile(".*[^0-9](\\d{3,4}p).*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(title);

        String quality = "";

        if (matcher.matches()) {
            quality = matcher.group(1);
        }

        this.rssQuality = quality;
    }

    public void setRssReleaseGroupByTitle(String title) {
        String rssReleaseGroup = "OTHERS";

        if (StringUtils.containsIgnoreCase(title, "next")) {
            rssReleaseGroup = "NEXT";
        } else if (StringUtils.containsIgnoreCase(title, "once")) {
            rssReleaseGroup = "ONCE";
        } else if (StringUtils.containsIgnoreCase(title, "Chaos")) {
            rssReleaseGroup = "Chaos";
        } else if (StringUtils.containsIgnoreCase(title, "Hel")) {
            rssReleaseGroup = "Hel";
        } else if (StringUtils.containsIgnoreCase(title, "DWBH")) {
            rssReleaseGroup = "DWBH";
        }

        this.rssReleaseGroup = rssReleaseGroup;
    }

    public void setRssDateBytitle(String title) {
        Pattern pattern = Pattern.compile(".*(\\d{6,8}).*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(title);

        String rssDate = "";

        if (matcher.matches()) {
            rssDate = matcher.group(1);
        }

        this.rssDate = rssDate;
    }

    public void setLinkByKey(String key, SyndEntry syndEntry) {
        if (StringUtils.isEmpty(key) || StringUtils.equals(key, "link")) {
            this.link = syndEntry.getLink();
        }
    }

    public String getLinkByKey(String key, SyndEntry syndEntry) {
        if (StringUtils.isEmpty(key) || StringUtils.equals(key, "link")) {
            return syndEntry.getLink();
        }

        return null;
    }

}