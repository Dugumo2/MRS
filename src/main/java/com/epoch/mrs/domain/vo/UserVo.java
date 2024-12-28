package com.epoch.mrs.domain.vo;

import cn.dev33.satoken.stp.SaTokenInfo;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserVo {

    private SaTokenInfo saTokenInfo;
    private String username;

    private String email;
}
