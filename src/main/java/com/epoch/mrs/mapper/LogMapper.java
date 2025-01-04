package com.epoch.mrs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.epoch.mrs.domain.po.Log;
import com.epoch.mrs.domain.vo.LogVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LogMapper extends BaseMapper<Log> {
    /**
     * 分页查询日志信息
     *
     * @param page     分页对象
     * @param userName 用户名，可选
     * @param logLeveL 日志级别，0: 不过滤, 1: WARN, 2: ERROR
     * @return 分页的 LogVo 列表
     */
    IPage<LogVo> selectLogVoPage(IPage<LogVo> page, @Param("userName") String userName, @Param("logLeveL") int logLeveL);
}
