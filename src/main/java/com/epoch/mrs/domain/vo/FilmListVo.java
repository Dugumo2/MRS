package com.epoch.mrs.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class FilmListVo {
    private Long id; // 主键

    private String title;


    private BigDecimal avgScore;

    private String imageUrl;
}
