package com.epoch.mrs.domain.vo;


import com.epoch.mrs.domain.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewUserVo {
    private String id;
    private String username;
    private String email;
    private UserStatus status;
    private LocalDateTime applicationTime;
}
