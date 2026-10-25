package com.courseshare.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.courseshare.user.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<SysRole> selectActiveByCodes(@Param("codes") List<String> codes);
}

