package com.zdm.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SpringBootApplication
@EnableScheduling
public class CrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrawlerApplication.class, args);
	}


	@GetMapping("/upload")
	public String upload(){
		return "upload.html";
	}
	@GetMapping("/v")
	public String v(){
		return "v_page.html";
	}
	@GetMapping("/v/{bucketName}")
	public String vs(){
		return "v_subpage.html";
	}
}
