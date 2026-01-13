package com.cencops.demo.controller;

import com.cencops.demo.config.security.JwtTokenUtil;
import com.cencops.demo.dto.request.LoginRequest;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.UserRepository;
import com.cencops.demo.service.TokenBlacklistService;
import com.cencops.demo.service.UserService;
import com.cencops.demo.utils.MessageConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.cencops.demo.dto.response.ApiResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        String accessToken = jwtTokenUtil.generateAccessToken(request.getUsername(), role);
        String refreshToken = jwtTokenUtil.generateRefreshToken(request.getUsername());

        setRefreshTokenCookie(response, refreshToken, 900);

        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !jwtTokenUtil.validateToken(refreshToken) ||
                !"REFRESH".equals(jwtTokenUtil.getTokenType(refreshToken))) {
            throw new AppExceptionHandler.CustomException("Invalid refresh token", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        String username = jwtTokenUtil.getUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        String newAccessToken = jwtTokenUtil.generateAccessToken(username, user.getRole().name());

        String newRefreshToken = jwtTokenUtil.generateRefreshToken(username);
        setRefreshTokenCookie(response, newRefreshToken, 900);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            long expiryTime = System.currentTimeMillis() + 360000;
            tokenBlacklistService.blacklistToken(token, expiryTime);
        }

        setRefreshTokenCookie(response, "", 0);

        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.LOGOUT_SUCCESS));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}