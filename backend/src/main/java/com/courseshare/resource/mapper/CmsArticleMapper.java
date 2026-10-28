package com.courseshare.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.resource.dto.AuditVersionItem;
import com.courseshare.resource.dto.ResourceQuery;
import com.courseshare.resource.entity.CmsArticle;
import com.courseshare.resource.vo.DiscoverResourceVO;
import com.courseshare.resource.vo.ResourceDetailVO;
import com.courseshare.resource.vo.ResourceListItemVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface CmsArticleMapper extends BaseMapper<CmsArticle> {

    IPage<ResourceListItemVO> selectResourcePage(
            Page<ResourceListItemVO> page,
            @Param("query") ResourceQuery query,
            @Param("categoryIds") Set<Long> categoryIds,
            @Param("publicOnly") boolean publicOnly
    );

    ResourceDetailVO selectResourceDetail(@Param("id") Long id, @Param("publicOnly") boolean publicOnly);

    IPage<DiscoverResourceVO> selectDiscoverPage(
            Page<DiscoverResourceVO> page,
            @Param("sort") String sort
    );

    int incrementViewCount(@Param("id") Long id);

    int incrementDownloadCount(@Param("id") Long id);

    int batchPublish(
            @Param("items") List<AuditVersionItem> items,
            @Param("auditBy") Long auditBy,
            @Param("now") LocalDateTime now
    );
}

