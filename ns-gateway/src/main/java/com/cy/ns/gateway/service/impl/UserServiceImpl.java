package com.cy.ns.gateway.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.gateway.service.UserService;
import com.cy.ns.gateway.util.RedisUtil;
import com.cy.ns.generate.domain.NSConfig;
import com.cy.ns.generate.domain.NSUser;
import com.cy.ns.generate.entity.param.UserParam;
import com.cy.ns.generate.entity.vo.UserVO;
import com.cy.ns.generate.service.NsConfigService;
import com.cy.ns.generate.service.NsUserService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 * @date 2025/4/13
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    RedisUtil redisUtil;

    @Resource
    NsUserService nsUserService;

    @Resource
    NsConfigService nsConfigService;

    @Value( "${sa-token.timeout}"  )
    private Long timeout;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String userRegister( UserParam userParam ) {
        // 校验验证码
        String code = redisUtil.get(NsConstants.NS_REDIS_CODE_PREFIX + userParam.getPhone() );
        if ( code == null ) {
            return "验证码已过期";
        }else if ( !code.equals( userParam.getVerificationCode() ) ) {
            return "验证码错误";
        }
        // 校验手机号格式
        if ( !userParam.getPhone().matches( "^1[3-9]\\d{9}$" ) ) {
            return "手机号格式错误";
        }
        QueryWrapper<NSUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "phone", userParam.getPhone() );
        // 校验手机号是否已注册
        if ( nsUserService.getOne( queryWrapper ) != null ) {
            return "手机号已注册";
        }
        // 注册用户
        NSUser user = createUser( userParam.getPhone(),SaSecureUtil.md5(userParam.getPassword() ) );
        // 插入数据库
        nsUserService.save( user );

        NSConfig config = new NSConfig();
        config.setUserId( user.getId() );

        nsConfigService.save( config );


        // 删除验证码
        redisUtil.delete(NsConstants.NS_REDIS_CODE_PREFIX + userParam.getPhone() );

        return NsConstants.REGISTER_SUCCESS;
    }


    @Override
    public UserVO getUserInfo() {
        QueryWrapper<NSUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "id", StpUtil.getLoginId() )
                .select( "phone", "name", "avatar_url", "create_time" );
        NSUser user = nsUserService.getOne( queryWrapper );
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties( user, userVO );

        userVO.setId( SaSecureUtil.md5(NsConstants.SYSTEM_NAME_ABBR + user.getId()) );

        userVO.setPhone( user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") );

        LocalDateTime now = LocalDateTime.now();

        long disableTime = StpUtil.getTokenTimeout();

        userVO.setLastLoginTime( now.minusSeconds(timeout - disableTime ) );


        return userVO;
    }


    @Override
    public String updateUserInfo( UserParam userParam ) {
        UpdateWrapper<NSUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq( "id",StpUtil.getLoginId() );
        if(userParam.getName()!=null && userParam.getName().length()!=0){
            updateWrapper.set( "name",userParam.getName() );
        }

        if(nsUserService.update(updateWrapper)){
            return NsConstants.UPDATE_SUCCESS;
        }
        return "更新失败";
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updatePassword( UserParam userParam ) {
        if ( userParam.getPassword() == null || userParam.getPassword().length() == 0 ) {
            return "密码不能为空";
        }

        if ( userParam.getNewPassword() == null || userParam.getNewPassword().length() == 0 ) {
            return "新密码不能为空";
        }

        if ( userParam.getNewPassword().equals( userParam.getPassword() ) ) {
            return "新密码不能与旧密码相同";
        }
        QueryWrapper<NSUser> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq( "id", StpUtil.getLoginIdAsLong() )
                .eq( "is_del",0 );
        NSUser user = nsUserService.getOne( queryWrapper );

        if ( user == null ) {
            return "用户不存在";
        }

        if( !user.getPassword().equals( SaSecureUtil.md5( userParam.getPassword() ) ) ) {
            return "旧密码错误";
        }

        UpdateWrapper<NSUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq( "id", StpUtil.getLoginIdAsLong() )
                .set( "password", SaSecureUtil.md5( userParam.getNewPassword() ) );

        if ( nsUserService.update( updateWrapper ) ) {
            return NsConstants.UPDATE_SUCCESS;
        } else {
            return "更新失败";
        }
    }


    @Override
    public String updatePhone( UserParam userParam ) {

        if ( !userParam.getVerificationCode().equals( redisUtil.get( NsConstants.NS_REDIS_CODE_PREFIX + userParam.getPhone() ) ) ) {
            return "验证码错误";
        }

        UpdateWrapper<NSUser> updateWrapper = new UpdateWrapper<>();

        updateWrapper.eq( "id", StpUtil.getLoginIdAsLong() )
                .set( "phone", userParam.getPhone() );

        if ( !nsUserService.update( updateWrapper ) ) {
            return "更新失败";
        }

        // 删除验证码
        redisUtil.delete( NsConstants.NS_REDIS_CODE_PREFIX + userParam.getPhone() );

        return NsConstants.UPDATE_SUCCESS;
    }


    private NSUser createUser( String phone, String password ) {
        NSUser user = new NSUser();
        user.setPhone( phone );
        user.setPassword( password );
        user.setName( "user"+phone.substring( phone.length()-4 ) );
        user.setAvatarUrl( NsConstants.DEFAULT_AVATAR_URL );
        user.setCreateTime( LocalDateTime.now() );
        user.setUpdateTime( LocalDateTime.now() );
        return user;
    }

}
