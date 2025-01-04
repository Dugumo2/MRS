package com.epoch.mrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epoch.mrs.domain.dto.PageDTO;
import com.epoch.mrs.domain.po.Log;
import com.epoch.mrs.domain.query.LogQuery;
import com.epoch.mrs.domain.vo.LogVo;

public interface ILogService extends IService<Log> {
    PageDTO<LogVo> queryLogsPage(LogQuery logQuery);
}
