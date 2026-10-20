package com.chesnake.security;

import com.chesnake.common.BusinessException;
import com.chesnake.common.R;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户的工具
 */
public final class SecurityUtil {
    private SecurityUtil() {}

    public static LoginUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        throw new BusinessException(R.UNAUTHORIZED, "未登录或登录已过期");
    }

    public static String currentEmail() {
        return current().getEmail();
    }

    public static String currentUsername() {
        return current().getUsername();
    }

    public static boolean isAdmin() {
        LoginUser u = current();
        return u.getRole() != null && u.getRole() == 1;
    }
}
