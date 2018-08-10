package com.zdm.crawler.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.zdm.crawler.dao.Illust;
import com.zdm.crawler.dao.IllustMapper;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OSSService {
    @Autowired
    private OSS oss;
    @Autowired
    private IllustMapper illustMapper;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void upload(String pageSource){
        String bucket = "zengdm-pixiv";
        Illust illust = Illust.fromPageSource(pageSource);
        if (illust==null){
            logger.error("源网页不是插画详情页!");return; }
        try {
            illustMapper.putIllusts(illust);
        }catch (Exception e){
            logger.error("sql出错,description:"+illust.getDescription(),e);
            return;
        }
        if(oss.doesObjectExist(bucket,illust.getFileName())){
            logger.error("已存在的插画: {}",illust.getFileName());return; }

        URL url;
        try {
            url = new URL(illust.getSrc());
        } catch (MalformedURLException e) {
            logger.error("地址不正确: {}",illust.getSrc());return;}

        //以输入流的形式上传文件
        InputStream is;
        int size;
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("referer", "https://www.pixiv.net/ranking.php?mode=daily&content=illust");
            is = urlConnection.getInputStream();
            size = urlConnection.getHeaderFieldInt("Content-Length", -1);
        } catch (IOException e) { logger.error("输入流错误!");return; }
        //文件名
        //创建上传Object的Metadata
        ObjectMetadata metadata = new ObjectMetadata();
        //上传的文件的长度
        metadata.setContentLength(size);
        //指定该Object下设置Header
        metadata.setHeader("Pragma", "no-cache");
        //指定该Object被下载时的内容编码格式
        metadata.setContentEncoding("utf-8");
        //文件的MIME，定义文件的类型及网页编码，决定浏览器将以什么形式、什么编码读取文件。如果用户没有指定则根据Key或文件名的扩展名生成，
        //如果没有扩展名则填默认值application/octet-stream
        String contentType = getContentType(illust.getFileName());
        if (contentType != null)
            metadata.setContentType(contentType);
        //上传文件   (上传文件流的形式)
        PutObjectResult putResult = oss.putObject(bucket, illust.getFileName(), is, metadata);
        try {
            is.close();
        } catch (IOException e) { logger.error("输入流未关闭!"); }
        logger.info("上传成功: {}", illust.getFileName());
    }

    private static String getContentType(String fileName){
        //文件的后缀名
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        if(".bmp".equalsIgnoreCase(fileExtension)) {
            return "image/bmp";
        }
        if(".gif".equalsIgnoreCase(fileExtension)) {
            return "image/gif";
        }
        if(".jpeg".equalsIgnoreCase(fileExtension) || ".jpg".equalsIgnoreCase(fileExtension)
                || ".png".equalsIgnoreCase(fileExtension) ) {
            return "image/jpeg";
        }
        return null;
    }

}

