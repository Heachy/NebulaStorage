package com.cy.ns.web.controller;

import com.cy.ns.common.api.CommonResult;
import com.cy.ns.common.constants.NsConstants;
import com.cy.ns.generate.domain.Bucket;
import com.cy.ns.generate.entity.param.BucketParam;
import com.cy.ns.generate.entity.vo.BucketVO;
import com.cy.ns.web.service.NsBucketService;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Haechi
 * @date 2025/4/13
 */
@RestController
@RequestMapping("/bucket")
public class BucketController {

    @Resource
    NsBucketService nsBucketService;

    @GetMapping("/list")
    public CommonResult<List<BucketVO>> list() {
        return CommonResult.success(nsBucketService.bucketList());
    }

    @PostMapping("/create")
    public CommonResult<String> create(@RequestBody BucketParam bucketParam ) {
        String msg = nsBucketService.createBucket( bucketParam );
        if( msg != null && msg.equals( NsConstants.CREATE_SUCCESS )) {
            return CommonResult.success( msg );
        }
        return CommonResult.failed( msg );
    }

    @PostMapping("/update")
    public CommonResult<String> update(@RequestBody BucketParam bucketParam ) {
        if ( bucketParam.getId() == null ) {
            return CommonResult.failed("id不能为空");
        }
        String msg = nsBucketService.updateBucket( bucketParam );
        if( msg != null && msg.equals( NsConstants.UPDATE_SUCCESS )) {
            return CommonResult.success( msg );
        }
        return CommonResult.failed( msg );
    }

    @GetMapping("/{id}")
    public CommonResult<BucketVO> getBucketById(@PathVariable("id") Long id ) {
        BucketVO bucketVO = nsBucketService.getBucketById( id );
        if ( bucketVO != null ) {
            return CommonResult.success( bucketVO );
        }
        return CommonResult.failed( "bucket不存在" );
    }
}
