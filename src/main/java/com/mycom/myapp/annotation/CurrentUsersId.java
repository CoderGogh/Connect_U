package com.mycom.myapp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUsersId {
    boolean required() default true; // 인증 정보 필수 여부: 기본값 true. 필수가 아닌 경우 해당 속성값에 false를 설정합니다.
}
