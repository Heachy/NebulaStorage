package com.cy.ns.web.service;

import com.cy.ns.generate.entity.param.UserParam;
import com.cy.ns.generate.entity.vo.UserVO;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Haechi
 * @date 2025/4/13
 */
public interface UserService {
    String updateAvatar( MultipartFile avatarFile  ) throws IOException;
}
