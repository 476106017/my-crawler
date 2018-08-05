package com.zdm.crawler.schedule;

import com.aliyun.oss.OSS;
import com.google.common.escape.Escapers;
import com.zdm.crawler.service.OSSService;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@Controller
public class CrawlSchedule {

    @Autowired
    private OSS oss;
    @Autowired
    private OSSService ossService;

    final static String RANKING = "https://www.pixiv.net/ranking.php?mode=daily&content=illust";

    ThreadPoolExecutor executor = new ThreadPoolExecutor(
            4, 8,1,TimeUnit.HOURS,new LinkedBlockingDeque());

    @Scheduled(cron = "0 0 23 * * ?")
    @GetMapping("crawl")
    public void crawl() {
        Capa caps = new Capa();
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                "src/main/resources/phantomjs/phantomjs.exe");
        caps.setting("loadImages", false);
        caps.setting("webSecurityEnabled", false);
        caps.setting("disk-cache", true);
        caps.setting("resourceTimeout", 1000);
        caps.setHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
        caps.setHeader("cookie", "PHPSESSID=12179525_1bd2a4cca1f0d26bb889af3930d00728;" +
                "device_token=907aa0596d1a81fe155bba55761eb755; login_ever:yes;");
        caps.setHeader("connection", "keep-alive");
        caps.setHeader("pragma", "no-cache");
        caps.setHeader("cache-control", "no-cache");
        caps.setHeader("authority", "www.pixiv.net");
        caps.setHeader("upgrade-insecure-requests", 1);
        caps.setHeader("referer", RANKING);

        PhantomJSDriver dv = new PhantomJSDriver(caps);
        dv.get(RANKING);
        List<WebElement> pages = dv.findElements(By.className("_work"));
        if (pages == null)
            throw new NotFoundException("找不到排行榜页面");

        pages.forEach(p -> {
            String href = p.getAttribute("href");
            PhantomJSDriver dv2 = new PhantomJSDriver(caps);
            dv2.get(href);
            String pageSource = dv2.getPageSource();
            executor.execute(new UploadThread(pageSource));
        });

    }



    class Capa extends DesiredCapabilities{
        public void setHeader(String key,Object value) {
            super.setCapability("phantomjs.page.customHeaders."+key, value);
        }
        public void setting(String key,Object value) {
            super.setCapability("phantomjs.page.settings."+key, value);
        }
    }

    class UploadThread implements Runnable{

        private String pageSource;

        public UploadThread(String pageSource) {
            this.pageSource = pageSource;
        }

        @Override
        public void run() {
            ossService.upload(pageSource);
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String input = "\u8f1d\u591c\u6708\u3061\u3083\u3093";
        System.out.println();
    }
}
