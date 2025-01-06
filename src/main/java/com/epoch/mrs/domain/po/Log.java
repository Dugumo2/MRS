package com.epoch.mrs.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@TableName("logs")
@Accessors(chain = true)
public class Log {

    @TableId(type = IdType.AUTO)
    private Long id; // 主键

    private Long userId; // 用户ID

    private String username; // 用户名

    private String action; // 操作类型

    private String logLevel; // 日志级别

    private String logMessage; // 日志内容

    private LocalDateTime timestamp; // 日志时间

}