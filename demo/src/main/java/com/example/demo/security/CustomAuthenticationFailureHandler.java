package com.example.demo.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        String errorMessage = null;

        if (exception instanceof UsernameNotFoundException) {
            // Email not found in database
            errorMessage = "Email is not valid";
            request.getSession().setAttribute("emailError", errorMessage);
            getRedirectStrategy().sendRedirect(request, response, "/login?error=email");
        } else {
            errorMessage = "Password is wrong";
            request.getSession().setAttribute("passwordError", errorMessage);
            getRedirectStrategy().sendRedirect(request, response, "/login?error=EmailOrPassword");
        }
    }
}