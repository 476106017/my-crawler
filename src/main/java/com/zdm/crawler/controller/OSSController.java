package com.zdm.crawler.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OSSController {

    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    @Autowired
    private OSS oss;

    @PostMapping("/{bucketName}")
    public String  postFile(@PathVariable("bucketName")String bucketName,MultipartFile file) throws IOException{
        if(!oss.doesBucketExist(bucketName))
            oss.createBucket(bucketName);
        //以输入流的形式上传文件
        InputStream is = file.getInputStream();
        //文件名
        String fileName = file.getOriginalFilename();
        //文件大小
        Long fileSize = file.getSize();
        //创建上传Object的Metadata
        ObjectMetadata metadata = new ObjectMetadata();
        //上传的文件的长度
        metadata.setContentLength(is.available());
        //指定该Object被下载时的网页的缓存行为
        metadata.setCacheControl("no-cache");
        //指定该Object下设置Header
        metadata.setHeader("Pragma", "no-cache");
        //指定该Object被下载时的内容编码格式
        metadata.setContentEncoding("utf-8");
        //文件的MIME，定义文件的类型及网页编码，决定浏览器将以什么形式、什么编码读取文件。如果用户没有指定则根据Key或文件名的扩展名生成，
        //如果没有扩展名则填默认值application/octet-stream
        String contentType = getContentType(fileName);
        if (contentType != null)
            metadata.setContentType(contentType);
        //指定该Object被下载时的名称（指示MINME用户代理如何显示附加的文件，打开或下载，及文件名称）
        metadata.setContentDisposition("filename/filesize=" + fileName + "/" + fileSize + "Byte.");
        //上传文件   (上传文件流的形式)
        PutObjectResult putResult = oss.putObject(bucketName, fileName, is, metadata);
        is.close();
        //解析结果
        return putResult.getETag();
    }

    @GetMapping("/")
    public List<String> getRoot(){
        return oss.listBuckets()
                .stream().map(p->"/"+p.getName()).sorted(String::compareTo).collect(Collectors.toList());
    }
    // 只取100条,上传时应该写入数据库分页
    @GetMapping({"/{bucketName}"})
    public List<OSSObjectSummary> getBucket(@PathVariable("bucketName")String bucketName){
        if(!oss.doesBucketExist(bucketName)) return null;
        ObjectListing objects = oss.listObjects(bucketName);
        objects.setMaxKeys(100);
        return objects.getObjectSummaries();
    }
    @GetMapping("/{bucketName}/{key}")
    public void  getFile(@PathVariable("bucketName")String bucketName, @PathVariable("key") String key,
                          HttpServletResponse response) throws IOException {
        if(!oss.doesObjectExist(bucketName,key)) return;
        OSSObject object = oss.getObject(bucketName,key);

        ObjectMetadata metadata = object.getObjectMetadata();
        response.setContentType(metadata.getContentType());
        OutputStream out = response.getOutputStream();
        byte[] temp = new byte[DEFAULT_BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = object.getObjectContent().read(temp)) != -1) {
            out.write(temp, 0, bytesRead);
        }

        object.close();
    }

    @DeleteMapping("/{bucketName}/{key}")
    public void  delFile(@PathVariable("bucketName")String bucketName, @PathVariable("key") String key){
        if(!oss.doesBucketExist(bucketName)) return;
        oss.deleteObject(bucketName,key);
    }
    @DeleteMapping("/{bucketName}")
    public void  delBucket(@PathVariable("bucketName")String bucketName){
        if(!oss.doesBucketExist(bucketName)) return;
        oss.deleteBucket(bucketName);
    }
    @DeleteMapping("/force/{bucketName}")
    public void  delBucketForce(@PathVariable("bucketName")String bucketName){
        if(!oss.doesBucketExist(bucketName)) return;
        DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName);
        List<String> bucketFileNames = getBucketFileNames(bucketName);
        if (!bucketFileNames.isEmpty()) {
            request.setKeys(bucketFileNames);
            oss.deleteObjects(request);
        }
        oss.deleteBucket(bucketName);
    }

    private String getContentType(String fileName){
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
        if(".html".equalsIgnoreCase(fileExtension)) {
            return "text/html";
        }
        if(".txt".equalsIgnoreCase(fileExtension)) {
            return "text/plain";
        }
        if(".ppt".equalsIgnoreCase(fileExtension) || "pptx".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.ms-powerpoint";
        }
        if(".doc".equalsIgnoreCase(fileExtension) || "docx".equalsIgnoreCase(fileExtension)) {
            return "application/msword";
        }
        if(".xml".equalsIgnoreCase(fileExtension)) {
            return "text/xml";
        }
        return null;
    }
    private List<String> getBucketFileNames(String bucketName){
        return oss.listObjects(bucketName).getObjectSummaries()
                .stream().map(OSSObjectSummary::getKey).collect(Collectors.toList());
    }
}
