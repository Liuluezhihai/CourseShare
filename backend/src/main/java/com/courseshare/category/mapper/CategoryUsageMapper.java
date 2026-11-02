package com.courseshare.category.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CategoryUsageMapper {

    @Select("""
            SELECT COUNT(*)
            FROM cms_article
            WHERE category_id = #{categoryId}
              AND deleted = 0
            """)
    long countResources(@Param("categoryId") Long categoryId);

    @Select("""
            SELECT category_id AS categoryId, COUNT(*) AS resourceCount
            FROM cms_article
            WHERE deleted = 0
              AND status = 2
            GROUP BY category_id
            """)
    List<Map<String, Object>> countPublishedByCategory();
}

