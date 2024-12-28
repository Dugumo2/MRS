package com.epoch.mrs.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.epoch.mrs.domain.enums.AdminStatus;
import com.epoch.mrs.domain.po.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {


    @Override
    public List<String> getPermissionList(Object o, String s) {
        return null;
    }

    /**
     *
     * @param loginId 用户ID
     * @param loginType 多用户验证需要，未使用
     * @return 权限角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roleList = new ArrayList<>();

        AdminStatus role = Db.lambdaQuery(User.class)
                .eq(User::getId, loginId)
                .one()
                .getRole();
        if(role == AdminStatus.USER) {
            roleList.add(AdminStatus.USER.getDesc());
        }else if(role == AdminStatus.ADMIN) {
            roleList.add(AdminStatus.USER.getDesc());
            roleList.add(AdminStatus.ADMIN.getDesc());
        }
        return roleList;
    }
}
