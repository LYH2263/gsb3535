package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.domain.LibraryUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LibraryUserMapper extends BaseMapper<LibraryUser> {
}
