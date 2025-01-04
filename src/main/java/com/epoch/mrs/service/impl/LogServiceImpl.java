package com.epoch.mrs.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Log;
import com.epoch.mrs.domain.query.LogQuery;
import com.epoch.mrs.domain.vo.LogVo;
import com.epoch.mrs.mapper.LogMapper;
import com.epoch.mrs.service.ILogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper,Log> implements ILogService {
        @Autowired
        private LogMapper logMapper;

        @Override
        public PageDTO<LogVo> queryLogsPage(LogQuery logQuery) {
            // 验证 logLeveL 参数
            if(logQuery.getLogLeveL() < 0 || logQuery.getLogLeveL() > 2){
                throw new IllegalArgumentException("无效的日志级别参数");
            }

            // 创建分页对象，默认按 timestamp 降序排序
            if (logQuery.getSortBy() == null || logQuery.getSortBy().isEmpty()) {
                logQuery.setSortBy("timestamp");
                logQuery.setIsAsc(false);
            }

            IPage<LogVo> page = logQuery.toMpPageDefaultSortByTimestampDesc();

            // 调用自定义的 Mapper 方法进行分页查询
            IPage<LogVo> logVoPage = logMapper.selectLogVoPage(page, logQuery.getUsername(), logQuery.getLogLeveL());

            // 将 MyBatis-Plus 的 Page 转换为自定义的 PageDTO
            return PageDTO.from(logVoPage);
        }
}
