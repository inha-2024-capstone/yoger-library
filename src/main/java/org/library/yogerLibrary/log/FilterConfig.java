package org.library.yogerLibrary.log;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LogFilter> logFilter() {
        FilterRegistrationBean<LogFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        
        filterRegistrationBean.setFilter(new LogFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(1);

        return filterRegistrationBean;
    }
}
