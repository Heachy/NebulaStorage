package com.cy.ns.web.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.generate.domain.NsAdmin;
import com.cy.ns.generate.service.NsAdminService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * @author Haechi
 * @date 2025/4/20
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    @Resource
    NsAdminService nsAdminService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询权限
        List<String> list = new ArrayList<>();
        list.add("file.upload");
        list.add("user.download");
        list.add("art.*");
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询角色
        List<String> list = new ArrayList<>();
        if(loginId.toString().startsWith( NsConstants.GUEST_PREFIX)) {
            list.add("guest");
        }else{
            QueryWrapper<NsAdmin> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", loginId).eq( "is_del", 0 );
            NsAdmin nsAdmin = nsAdminService.getOne( queryWrapper );
            list.add("user");
            if(nsAdmin == null) {
                return list;
            }
            list.add("admin");
        }
        return list;
    }

}