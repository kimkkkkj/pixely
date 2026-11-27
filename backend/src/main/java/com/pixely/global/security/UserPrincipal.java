package com.pixely.global.security;

import com.pixely.domain.user.Role;

/** JWT 에서 복원한 인증 사용자 정보 */
public record UserPrincipal(Long id, String email, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
