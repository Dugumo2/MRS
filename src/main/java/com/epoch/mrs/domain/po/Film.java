package com.epoch.mrs.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.epoch.mrs.domain.enums.CategoryStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@TableName("films")
@Accessors(chain = true)
public class Film {

    @TableId(value = "id", type = IdType.AUTO) // 主键自增
    private Long id;

    private String title;

    private String info;

    private String description;

    private BigDecimal avgScore;

    private CategoryStatus category;

    private String imageUrl;

}