package com.courseshare.interaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.common.api.PageResult;
import com.courseshare.common.util.PageQuery;
import com.courseshare.resource.mapper.CmsArticleMapper;
import com.courseshare.resource.vo.DiscoverResourceVO;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class DiscoverService {

    private static final Set<String> SORTS = Set.of("latest", "popular", "rating", "likes");
    private final CmsArticleMapper articleMapper;

    public DiscoverService(CmsArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    public PageResult<DiscoverResourceVO> page(String sort, PageQuery query) {
        String normalized = sort == null ? "popular" : sort.toLowerCase(Locale.ROOT);
        if (!SORTS.contains(normalized)) {
            normalized = "popular";
        }
        Page<DiscoverResourceVO> page = new Page<>(query.normalizedPage(), query.normalizedSize());
        return PageResult.from(articleMapper.selectDiscoverPage(page, normalized));
    }
}
