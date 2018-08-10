package com.zdm.crawler.controller;

import com.aliyun.oss.OSS;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.zdm.crawler.dao.Illust;
import com.zdm.crawler.dao.IllustMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class IllustController {
    @Autowired
    private IllustMapper illustMapper;

    @GetMapping("/i/illusts")
    public Map i(int pn){
        Page<Illust> illusts = PageHelper.startPage(pn, 20)
                .doSelectPage(() -> illustMapper.getIllusts());
        PageInfo<Illust> pageInfo = PageInfo.of(illusts);
        Map map = Maps.newHashMap();
        map.put("result",illusts);
        map.put("pinfo",pageInfo);
        return map;
    }

}
