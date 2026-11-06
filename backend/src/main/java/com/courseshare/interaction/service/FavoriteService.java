package com.courseshare.interaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.common.api.PageResult;
import com.courseshare.common.exception.BusinessException;
import com.courseshare.common.exception.ErrorCode;
import com.courseshare.common.util.PageQuery;
import com.courseshare.interaction.mapper.ResourceFavoriteMapper;
import com.courseshare.resource.mapper.CmsArticleMapper;
import com.courseshare.resource.vo.ResourceListItemVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    private final ResourceFavoriteMapper favoriteMapper;
    private final CmsArticleMapper articleMapper;

    public FavoriteService(ResourceFavoriteMapper favoriteMapper, CmsArticleMapper articleMapper) {
        this.favoriteMapper = favoriteMapper;
        this.articleMapper = articleMapper;
    }

    public void favorite(Long resourceId, Long userId) {
        requirePublicResource(resourceId);
        favoriteMapper.insertIgnore(userId, resourceId);
    }

    public void unfavorite(Long resourceId, Long userId) {
        favoriteMapper.delete(userId, resourceId);
    }

    public List<Long> resourceIds(Long userId) {
        return favoriteMapper.selectResourceIds(userId);
    }

    public PageResult<ResourceListItemVO> page(Long userId, PageQuery query) {
        Page<ResourceListItemVO> page = new Page<>(query.normalizedPage(), query.normalizedSize());
        return PageResult.from(favoriteMapper.selectFavoritePage(page, userId));
    }

    private void requirePublicResource(Long resourceId) {
        if (articleMapper.selectResourceDetail(resourceId, true) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资源不存在或尚未发布");
        }
    }
}
