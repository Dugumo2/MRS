package com.epoch.mrs.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.epoch.mrs.domain.enums.AdminStatus;
import com.epoch.mrs.domain.enums.UserStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@TableName("users")
@Accessors(chain = true)
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String email;
    private UserStatus status;
    private AdminStatus role;
    private LocalDateTime applicationTime;
    private LocalDateTime reviewTime;
}
