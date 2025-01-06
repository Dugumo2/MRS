package com.epoch.mrs.domain.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentQuery extends PageQuery{
    private Integer filmId;
    private Integer currentUserId;
}
