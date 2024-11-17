package org.library.yogerLibrary.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@Slf4j
public class LogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

        // 요청의 헤더와 바디를 로그로 출력
        log.debug("\n<Headers>\n{}\n" +
                "<Body>\n{}\n" +
                "<ETC>\n" +
                "request url: {}\n",
                getHeadersAsString(cachedRequest),
                cachedRequest.getBody(),
                cachedRequest.getRequestURL()
        );

        // 응답을 로깅하기 위해 커스텀 HttpServletResponseWrapper 사용
        CustomHttpResponseWrapper responseWrapper = new CustomHttpResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        // 필터 체인을 통과시키면서, 응답을 캡처
        filterChain.doFilter(request, responseWrapper);
        long endTime = System.currentTimeMillis();

        // 응답을 로깅하는 부분
        log.debug("\n<Response>\n" +
                        "time: {}ms\n" +
                        "Headers\n{}\n" +
                        "Response Status: {}\n" +
                        "Response Body: {}\n",
                (endTime - startTime),
                responseWrapper.getHeadersAsString(),
                responseWrapper.getStatus(),
                responseWrapper.getCaptureAsString()
        );

        // 실제 응답 내용을 클라이언트에 전달
        response.getOutputStream().write(responseWrapper.getCaptureAsBytes());
    }

    private String getHeadersAsString(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder headers = new StringBuilder();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.append(headerName).append(": ").append(headerValue).append("\n");
        }

        return headers.toString();
    }
}
