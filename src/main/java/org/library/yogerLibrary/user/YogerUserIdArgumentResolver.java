package org.library.yogerLibrary.user;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class YogerUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(YogerUserId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        YogerUserId annotation = parameter.getParameterAnnotation(YogerUserId.class);

        String headerName = annotation.headerName();
        if (headerName == null || headerName.isEmpty()) {
            throw new RuntimeException("headerName is empty");
        }
        String headerValue = webRequest.getHeader(headerName);
        if (headerValue == null || headerValue.isEmpty()) {
            throw new RuntimeException("headerValue is empty");
        }

        return Long.valueOf(headerValue);
    }
}
