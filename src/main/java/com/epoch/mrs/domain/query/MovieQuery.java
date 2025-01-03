package com.epoch.mrs.domain.query;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MovieQuery extends PageQuery{
    private String type;
}
