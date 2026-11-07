package com.courseshare.interaction.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.courseshare.resource.vo.ResourceListItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ResourceFavoriteMapper {

    int insertIgnore(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    int delete(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    List<Long> selectResourceIds(@Param("userId") Long userId);

    IPage<ResourceListItemVO> selectFavoritePage(
            Page<ResourceListItemVO> page,
            @Param("userId") Long userId
    );
}
