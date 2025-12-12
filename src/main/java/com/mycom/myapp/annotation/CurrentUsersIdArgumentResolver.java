package com.mycom.myapp.annotation;

import com.mycom.myapp.auth.details.CustomUserDetails;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentUsersIdArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(CurrentUsersId.class);
        boolean isIntegerType = Integer.class.equals(parameter.getParameterType())
                || int.class.equals(parameter.getParameterType());
        return hasAnnotation && isIntegerType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        CurrentUsersId annotation = parameter.getParameterAnnotation(CurrentUsersId.class);
        boolean required = annotation == null || annotation.required();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null || auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal())) {
            if(required == false) {
                return null;
            }
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Object principal = auth.getPrincipal();
        if(!(principal instanceof CustomUserDetails userDetails)) {
            if(required == false) {
                return null;
            }
            throw new RuntimeException("인증 객체 타입이 일치하지 않습니다.");
        }

        Integer usersId = userDetails.getId();
        if(usersId == null && required == true) {
            throw new RuntimeException("인증된 사용자의 식별자 정보가 세션에 존재하지 않습니다.");
        }
        return usersId;
    }
}
