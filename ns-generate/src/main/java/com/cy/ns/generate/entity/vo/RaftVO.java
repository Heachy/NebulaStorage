package com.cy.ns.generate.entity.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/4/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaftVO {

    RaftNodeVO leader;
    List<RaftNodeVO> nodes;

}
