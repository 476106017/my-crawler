package com.zdm.crawler.dao;

import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.CharsetEncoder;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Illust {
    private String id;
    private String authorId;
    private String description;
    private String name;
    private String src;
    private String suffix;
    private String fileName;

    public String getId() {
        return id;
    }

    public Illust setId(String id) {
        this.id = id;
        return this;
    }

    public String getAuthorId() {
        return authorId;
    }

    public Illust setAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Illust setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public Illust setName(String name) {
        this.name = name;
        return this;
    }

    public String getSrc() {
        return src;
    }

    public Illust setSrc(String src) {
        this.src = src;
        return this;
    }

    public String getSuffix() {
        return suffix;
    }

    public Illust setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public Illust setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public static Illust fromPageSource(String pageSource){
        Illust illust = new Illust();
        String id = getVal(pageSource,"illustId");
        if (id==null)return null;
        String src = getVal(pageSource, "original");
        String suffix = src.substring(src.lastIndexOf("."));
        String name = getVal(pageSource, "illustTitle");
        String fileName = id+"-"+name+suffix;
        String description = getVal(pageSource, "description");


        return illust.setId(id).setAuthorId(getVal(pageSource,"authorId"))
                .setDescription(description)
                .setName(name).setSrc(src).setSuffix(suffix).setFileName(fileName);
    }


    private static String getVal(String source,String key){
        Matcher matcher = Pattern.compile("\"" + key + "\":\"([^\"]+)\"")
                .matcher(source);
        if(matcher.find())
            return StringEscapeUtils.unescapeJava(matcher.group(1));
        return null;
    }
}
