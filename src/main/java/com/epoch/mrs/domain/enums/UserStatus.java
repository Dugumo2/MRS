package com.epoch.mrs.domain.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserStatus {
    PENDING(0, "申请中"),
    APPROVED(1, "申请成功"),
    REJECTED(2, "已拒绝");

    @EnumValue
    private int value;
    private String desc;

    UserStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    // 静态方法：根据 value 获取枚举实例
    public static UserStatus of(int value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("账户状态错误: " + value);
    }
}