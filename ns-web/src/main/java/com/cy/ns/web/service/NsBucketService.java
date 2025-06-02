package com.cy.ns.web.service;

import com.cy.ns.generate.domain.Bucket;
import com.cy.ns.generate.entity.param.BucketParam;
import com.cy.ns.generate.entity.vo.BucketVO;
import java.util.List;

/**
 * @author Haechi
 * @date 2025/4/13
 */
public interface NsBucketService {
    List<BucketVO> bucketList();

    BucketVO getBucketById(Long bucketId);
    String createBucket( BucketParam bucketParam);

    String updateBucket( BucketParam bucketParam);
}
