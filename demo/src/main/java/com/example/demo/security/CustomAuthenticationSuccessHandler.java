package com.example.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Lưu thông tin người dùng vào session
        HttpSession session = request.getSession();
        session.setAttribute("username", authentication.getName()); // Lưu tên người dùng vào session

        // Redirect theo vai trò
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/user/admin/create-account");
        } else if (roles.contains("ROLE_STAFF")) {
            response.sendRedirect("/user/staff");
        } else if (roles.contains("ROLE_CUSTOMER_CARE")) {
            response.sendRedirect("/user/customer-care");
        } else if (roles.contains("ROLE_CUSTOMER")) {
            response.sendRedirect("/user/customer");
        } else {
            response.sendRedirect("/");
        }
    }
}
