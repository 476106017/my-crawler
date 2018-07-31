package com.zdm.crawler.conf;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OSSConf {

    @Bean
    public OSS oss(@Value("${oss.endpoint}")String endpoint,
                         @Value("${oss.accessKeyId}")String accessKeyId,
                         @Value("${oss.accessKeySecret}")String accessKeySecret){
        return  new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

}
