package com.epoch.mrs.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;


@Getter
public enum AdminStatus {
        USER(0, "user"),
        ADMIN(1, "admin");

        @EnumValue
        private int value;
        private String desc;

        AdminStatus(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }


        // 静态方法：根据 value 获取枚举实例
        public static AdminStatus of(int value) {
            for (AdminStatus status : AdminStatus.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("账户状态错误: " + value);
        }
    }

