package com.zdm.crawler.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface IllustMapper {

    @Select("select name,file_name,update_time " +
            "from i_illust order by update_time desc")
    List<Illust> getIllusts();

    @Insert("insert into i_illust (id, author_id, name, description, src, suffix, file_name) values " +
            "(#{id},#{authorId},#{name},#{description},#{src},#{suffix},#{fileName}) " +
            "ON DUPLICATE KEY UPDATE " +
            "author_id=#{authorId},name=#{name},description=#{description}," +
            "src=#{src},suffix=#{suffix},file_name=#{fileName}")
    int putIllusts(Illust illust);
}
