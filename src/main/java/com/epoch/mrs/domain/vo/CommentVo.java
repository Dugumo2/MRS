package com.epoch.mrs.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class CommentVo {
    private Long id; // 主键
    private String Username;
    private String content;
    private BigDecimal score;
    private LocalDateTime createTime;
}
