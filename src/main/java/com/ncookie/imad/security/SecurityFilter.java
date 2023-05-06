package com.ncookie.imad.security;

import com.ncookie.imad.domain.type.AuthProvider;
import com.ncookie.imad.exception.BadRequestException;
import com.ncookie.imad.repository.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private final SecurityUtil securityUtil;
    private final UserAccountRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        log.debug("**** SECURITY FILTER START");

        try {
            String authorizationHeader = request.getHeader("Authorization");
            String token;
            String userId;
            String provider;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);

                if (securityUtil.isExpiration(token)) { // 만료되었는지 체크
                    throw new BadRequestException("EXPIRED_ACCESS_TOKEN");
                }

                userId = (String) securityUtil.get(token).get("userId");
                provider = (String) securityUtil.get(token).get("provider");

                if(!userRepository.existsByUserIdAndAuthProvider(userId, AuthProvider.findByCode(provider))){
                    throw new BadRequestException("CANNOT_FOUND_USER");
                }
            }

            filterChain.doFilter(request, response);
        } catch (BadRequestException e) {
            if (e.getMessage().equalsIgnoreCase("EXPIRED_ACCESS_TOKEN")) {
                writeErrorLogs("EXPIRED_ACCESS_TOKEN", e.getMessage(), e.getStackTrace());
                JSONObject jsonObject = createJsonError(String.valueOf(UNAUTHORIZED.value()), e.getMessage());
                setJsonResponse(response, UNAUTHORIZED, jsonObject.toString());
            } else if (e.getMessage().equalsIgnoreCase("CANNOT_FOUND_USER")) {
                writeErrorLogs("CANNOT_FOUND_USER", e.getMessage(), e.getStackTrace());
                JSONObject jsonObject = createJsonError(String.valueOf(UNAUTHORIZED.value()), e.getMessage());
                setJsonResponse(response, UNAUTHORIZED, jsonObject.toString());
            }
        } catch (Exception e) {
            writeErrorLogs("Exception", e.getMessage(), e.getStackTrace());

            if (response.getStatus() == HttpStatus.OK.value()) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } finally {
            log.debug("**** SECURITY FILTER FINISH");
        }
    }

    private JSONObject createJsonError(String errorCode, String errorMessage) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("error_code", errorCode);
        jsonObject.put("error_message", errorMessage);

        return jsonObject;
    }

    private void setJsonResponse(HttpServletResponse response, HttpStatus httpStatus, String jsonValue) {
        response.setStatus(httpStatus.value());
        response.setContentType(APPLICATION_JSON_VALUE);

        try {
            response.getWriter().write(jsonValue);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException ex) {
            writeErrorLogs("IOException", ex.getMessage(), ex.getStackTrace());
        }
    }

    private void writeErrorLogs(String exception, String message, StackTraceElement[] stackTraceElements) {
        log.error("**** " + exception + " ****");
        log.error("**** error message : " + message);
        log.error("**** stack trace : " + Arrays.toString(stackTraceElements));
    }

}
