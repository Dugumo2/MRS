package com.epoch.mrs.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class FilmInfoVo {

    private String title;

    private String info;

    private String description;

    private BigDecimal avgScore;

    private String imageUrl;
}
