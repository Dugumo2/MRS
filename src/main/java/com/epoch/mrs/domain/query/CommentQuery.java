package com.epoch.mrs.domain.query;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CommentQuery extends PageQuery{
    private Integer filmId;
}
