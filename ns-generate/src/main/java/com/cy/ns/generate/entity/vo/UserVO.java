package com.cy.ns.generate.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Haechi
 * @date 2025/4/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private String id;
    private String name;
    private String avatarUrl;
    private LocalDateTime createTime;
    private String phone;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;
}
