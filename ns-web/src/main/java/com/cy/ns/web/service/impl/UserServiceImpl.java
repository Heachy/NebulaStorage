package com.cy.ns.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.common.utils.OssManagerUtil;
import com.cy.ns.generate.domain.NSUser;
import com.cy.ns.generate.service.NsUserService;
import com.cy.ns.web.service.UserService;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 * @date 2025/4/13
 */
@Service
public class UserServiceImpl implements UserService {


    @Resource
    NsUserService nsUserService;


    @Override
    public String updateAvatar( MultipartFile avatarFile ) throws IOException {
        String url = OssManagerUtil.getUrl( Objects.requireNonNull( avatarFile.getOriginalFilename() ), avatarFile.getInputStream() );

        if ( url == null ) {
            return "上传头像失败";
        }
        UpdateWrapper<NSUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq( "id", StpUtil.getLoginId() )
                .eq( "is_del",0 )
                .set( "avatar_url", url );
        if(nsUserService.update( updateWrapper )){
            return NsConstants.UPDATE_SUCCESS;
        }
        return "更新头像失败";

    }


}
