package com.epoch.mrs.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum CategoryStatus {
    ACTION("Action", "动作"),
    COMEDY("Comedy", "喜剧"),
    ROMANCE("Romance", "爱情"),
    ANIME("Anime", "动漫");
   @EnumValue
    private String value;
    private String description;

    // 构造函数
    CategoryStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    // 通过数据库的值获取枚举
    public static CategoryStatus fromValue(String value) {
        for (CategoryStatus category : CategoryStatus.values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("未知的类型 " + value);
    }
}