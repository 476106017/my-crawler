package com.zdm.crawler.schedule;

import com.aliyun.oss.OSS;
import com.google.common.collect.Lists;
import com.zdm.crawler.dao.IllustMapper;
import com.zdm.crawler.service.OSSService;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Configuration
@Controller
public class CrawlSchedule {

    @Autowired
    private OSSService ossService;

    private final static String RANKING = "https://www.pixiv.net/ranking.php?mode=daily&content=illust";

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10, 20,1,TimeUnit.HOURS,new LinkedBlockingDeque());


    @Autowired
    private IllustMapper illustMapper;

    @Scheduled(cron = "0 0 23 * * ?")
    @GetMapping("crawl")
    public void crawl() {
        Capa caps = new Capa();
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                "src/main/resources/phantomjs/phantomjs");
        caps.setting("loadImages", false);
        caps.setting("webSecurityEnabled", false);
        caps.setting("disk-cache", true);
        caps.setting("resourceTimeout", 1000);
        caps.setHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
        caps.setHeader("cookie", "PHPSESSID=12179525_1bd2a4cca1f0d26bb889af3930d00728;" +
                "device_token=907aa0596d1a81fe155bba55761eb755; login_ever:yes;");
        caps.setHeader("connection", "keep-alive");
        caps.setHeader("authority", "www.pixiv.net");
        caps.setHeader("referer", RANKING);

        AtomicReference<PhantomJSDriver> dvr = new AtomicReference<>();
        dvr.set(new PhantomJSDriver(caps));
        dvr.get().get(RANKING);
        List<WebElement> elements = dvr.get().findElements(By.className("_work"));
        if (elements==null){
            dvr.get().quit();
            throw new NotFoundException("找不到排行榜页面");
        }
        List<String> hrefs = elements.stream()
                .map(p->p.getAttribute("href")).collect(Collectors.toList());
        dvr.get().quit();

        hrefs.forEach(href -> {
            dvr.set(new PhantomJSDriver(caps));
            dvr.get().get(href);
            String pageSource = dvr.get().getPageSource();
            dvr.get().quit();
            executor.submit(new UploadThread(pageSource));
            try {
                // 1秒应该能下完一张图,加上爬页面的时间
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
}
