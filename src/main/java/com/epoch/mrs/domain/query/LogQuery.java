package com.epoch.mrs.domain.query;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class LogQuery extends PageQuery{
    private String username;

    // 0: 不过滤, 1: WARN, 2: ERROR
    private int logLeveL;
}
