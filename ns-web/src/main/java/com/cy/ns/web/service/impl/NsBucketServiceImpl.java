package com.cy.ns.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.generate.domain.Bucket;
import com.cy.ns.generate.domain.NSConfig;
import com.cy.ns.generate.entity.param.BucketParam;
import com.cy.ns.generate.entity.vo.BucketVO;
import com.cy.ns.generate.service.BucketService;
import com.cy.ns.generate.service.NsConfigService;
import com.cy.ns.web.service.NsBucketService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Haechi
 * @date 2025/4/13
 */
@Service
public class NsBucketServiceImpl implements NsBucketService {

    @Resource
    BucketService bucketService;

    @Resource
    NsConfigService nsConfigService;

    @Override
    public List<BucketVO> bucketList() {
        QueryWrapper<Bucket> queryWrapper = new QueryWrapper<>();
        queryWrapper.select( "id, name, authority, memory_usage, max_memory, create_time" );
        queryWrapper.eq( "user_id", StpUtil.getLoginId() );
        return bucketService.list( queryWrapper ).stream().map( bucket -> {
            BucketVO bucketVO = new BucketVO();
            BeanUtils.copyProperties( bucket, bucketVO );
            return bucketVO;
        } ).toList();
    }


    @Override
    public BucketVO getBucketById( Long bucketId ) {
        Bucket bucket = bucketService.getById( bucketId );
        BucketVO bucketVO = new BucketVO();
        BeanUtils.copyProperties( bucket, bucketVO );
        return bucketVO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createBucket( BucketParam bucketParam ) {
        // 检验该用户下是否已经存在同名的bucket
        QueryWrapper<Bucket> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "user_id", StpUtil.getLoginId() );
        queryWrapper.eq( "name", bucketParam.getName() );
        if ( bucketService.count( queryWrapper ) > 0 ) {
            return "该用户下已经存在同名的bucket";
        }
        // 是否超过最大bucket数量
        QueryWrapper<NSConfig> nsConfigQueryWrapper = new QueryWrapper<>();
        nsConfigQueryWrapper.eq( "user_id", StpUtil.getLoginId() );
        NSConfig nsConfig = nsConfigService.getOne( nsConfigQueryWrapper );

        QueryWrapper<Bucket> bucketQueryWrapper = new QueryWrapper<>();
        bucketQueryWrapper.eq( "user_id", StpUtil.getLoginId() )
                .eq( "is_del", 0 );
        if ( bucketService.count( bucketQueryWrapper ) == nsConfig.getMaxBucketCount()) {
            return "超过最大bucket数量";
        }

        // 创建bucket
        Bucket bucket = buildBucket();
        bucket.setName( bucketParam.getName() );
        bucket.setMaxMemory( nsConfig.getDefaultBucketMemory() );
        bucket.setUserId( StpUtil.getLoginIdAsLong() );
        bucket.setAuthority( bucketParam.getAuthority() );
        bucketService.save( bucket );

        return NsConstants.CREATE_SUCCESS;

    }


    @Override
    public String updateBucket( BucketParam bucketParam ) {
        // 检验该用户下是否已经存在同名的bucket
        QueryWrapper<Bucket> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "user_id", StpUtil.getLoginId() );
        queryWrapper.eq( "name", bucketParam.getName() );
        queryWrapper.ne( "id", bucketParam.getId() );
        if ( bucketService.count( queryWrapper ) > 0 ) {
            return "该用户下已经存在同名的bucket";
        }
        UpdateWrapper<Bucket> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq( "id", bucketParam.getId() );
        if ( bucketParam.getName() != null ) {
            updateWrapper.set( "name", bucketParam.getName() );
        }
        if ( bucketParam.getAuthority() != null ) {
            updateWrapper.set( "authority", bucketParam.getAuthority() );
        }
        updateWrapper.set( "update_time", LocalDateTime.now() );
        return bucketService.update( updateWrapper ) ? NsConstants.UPDATE_SUCCESS : NsConstants.UPDATE_FAIL;
    }


    private Bucket buildBucket() {
        Bucket bucket = new Bucket();
        bucket.setCreateTime( LocalDateTime.now() );
        bucket.setUpdateTime( LocalDateTime.now() );
        return bucket;
    }

}
