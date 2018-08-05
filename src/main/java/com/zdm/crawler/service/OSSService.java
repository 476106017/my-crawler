package com.zdm.crawler.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import org.apache.commons.text.StringEscapeUtils;
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

    public static void main(String[] args) throws IOException {
        String pageSource = "illust: { 69988918: {\"illustId\":\"69988918\",\"illustTitle\":\"\\u8f1d\\u591c\\u6708\\u3061\\u3083\\u3093\\u3068\\u4ef2\\u9593\\u305f\\u3061\\uff01\",\"illustComment\":\"C94\\u590f\\u30b3\\u30df\\u65b0\\u520a\\u306e\\u8868\\u7d19\\u3067\\u3059\\uff01\\u003Cbr \\/\\u003EA4 40p\\u3000\\u30d5\\u30eb\\u30ab\\u30e9\\u30fc\\u306e\\u4e88\\u5b9a\\u3067\\u3059\\u3002\\u304c\\u3093\\u3070\\u308b\\u305e\\u301c\\u3002\\u003Cbr \\/\\u003E\\u30b5\\u30fc\\u30af\\u30eb\\u300c\\uff2d\\uff49\\uff4b\\uff41 \\uff30\\uff49\\uff4b\\uff41\\uff3a\\uff4f\\u300d\\u65e5\\u66dc\\u65e5\\u897f\\u5730\\u533a\\u201c\\u3042\\u201d\\uff0d52a\",\"id\":\"69988918\",\"title\":\"\\u8f1d\\u591c\\u6708\\u3061\\u3083\\u3093\\u3068\\u4ef2\\u9593\\u305f\\u3061\\uff01\",\"description\":\"C94\\u590f\\u30b3\\u30df\\u65b0\\u520a\\u306e\\u8868\\u7d19\\u3067\\u3059\\uff01\\u003Cbr \\/\\u003EA4 40p\\u3000\\u30d5\\u30eb\\u30ab\\u30e9\\u30fc\\u306e\\u4e88\\u5b9a\\u3067\\u3059\\u3002\\u304c\\u3093\\u3070\\u308b\\u305e\\u301c\\u3002\\u003Cbr \\/\\u003E\\u30b5\\u30fc\\u30af\\u30eb\\u300c\\uff2d\\uff49\\uff4b\\uff41 \\uff30\\uff49\\uff4b\\uff41\\uff3a\\uff4f\\u300d\\u65e5\\u66dc\\u65e5\\u897f\\u5730\\u533a\\u201c\\u3042\\u201d\\uff0d52a\",\"illustType\":0,\"createDate\":\"2018-08-02T15:03:27+00:00\",\"uploadDate\":\"2018-08-02T15:03:27+00:00\",\"restrict\":0,\"xRestrict\":0,\"urls\":{\"mini\":\"https:\\/\\/i.pximg.net\\/c\\/48x48\\/img-master\\/img\\/2018\\/08\\/03\\/00\\/03\\/27\\/69988918_p0_square1200.jpg\",\"thumb\":\"https:\\/\\/i.pximg.net\\/c\\/240x240\\/img-master\\/img\\/2018\\/08\\/03\\/00\\/03\\/27\\/69988918_p0_master1200.jpg\",\"small\":\"https:\\/\\/i.pximg.net\\/c\\/540x540_70\\/img-master\\/img\\/2018\\/08\\/03\\/00\\/03\\/27\\/69988918_p0_master1200.jpg\",\"regular\":\"https:\\/\\/i.pximg.net\\/img-master\\/img\\/2018\\/08\\/03\\/00\\/03\\/27\\/69988918_p0_master1200.jpg\",\"original\":\"https:\\/\\/i.pximg.net\\/img-original\\/img\\/2018\\/08\\/03\\/00\\/03\\/27\\/69988918_p0.png\"}";
        String id = getVal(pageSource,"illustId");
        if (id==null)return;
        String name = getVal(pageSource,"illustTitle");

        String src = getVal(pageSource,"original");
        String suffix = src.substring(src.lastIndexOf("."));
        String fileName = id+"-"+name+suffix;

        URL url;
        try {
            url = new URL(src);
        } catch (MalformedURLException e) { e.printStackTrace();return;}
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("referer", "https://www.pixiv.net/ranking.php?mode=daily&content=illust");
        InputStream inputStream = urlConnection.getInputStream();
    }
    public void upload(String pageSource){
        String id = getVal(pageSource,"illustId");
        if (id==null)return;
        String name = getVal(pageSource,"illustTitle");
        String src = getVal(pageSource,"original");
        String suffix = src.substring(src.lastIndexOf("."));
        String fileName = id+"-"+name+suffix;

        URL url;
        try {
            url = new URL(src);
        } catch (MalformedURLException e) { e.printStackTrace();return;}

        //以输入流的形式上传文件
        InputStream is;
        int size;
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("referer", "https://www.pixiv.net/ranking.php?mode=daily&content=illust");
            is = urlConnection.getInputStream();
            size = urlConnection.getHeaderFieldInt("Content-Length", -1);
        } catch (IOException e) { e.printStackTrace();return; }
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
        String contentType = getContentType(fileName);
        if (contentType != null)
            metadata.setContentType(contentType);
        //上传文件   (上传文件流的形式)
        PutObjectResult putResult = oss.putObject("zengdm-pixiv", fileName, is, metadata);
        try {
            is.close();
        } catch (IOException e) { e.printStackTrace(); }
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

    private static String getVal(String source,String key){
        Matcher matcher = Pattern.compile("\"" + key + "\":\"([^\"]+)\"")
                .matcher(source);
        if(matcher.find())
            return StringEscapeUtils.unescapeJava(matcher.group(1));
        return null;
    }

}

