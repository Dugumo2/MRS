package com.epoch.mrs.domain.vo;

import com.epoch.mrs.domain.po.Log;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class LogVo {
    private Long id; // 主键

    private String username; // 用户名

    private String action; // 操作类型

    private String logLevel; // 日志级别

    private String logMessage; // 日志内容

    private LocalDateTime timestamp; // 日志时间
}
