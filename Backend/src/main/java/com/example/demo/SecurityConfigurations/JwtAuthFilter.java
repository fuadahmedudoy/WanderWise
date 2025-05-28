package com.example.demo.SecurityConfigurations;

import com.example.demo.service.TokenBlacklistService;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.demo.service.UserDetailService;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    public  JwtUtility jwtUtility;
    @Autowired
    UserDetailService userDetailService;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader=request.getHeader("Authorization");
        String token=null,username=null;

        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            token=authHeader.substring(7);
            if (tokenBlacklistService.isBlacklisted(token)) {
                // Token is blacklisted, deny authentication
                filterChain.doFilter(request, response);
                return;
            }
            username=jwtUtility.extractUserName(token);
        }
        if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetails=userDetailService.loadUserByUsername(username);
            boolean isValid= jwtUtility.validateToken(userDetails,token);

            if(isValid){
                UsernamePasswordAuthenticationToken authenticationToken=
                        new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request,response);
    }

}
