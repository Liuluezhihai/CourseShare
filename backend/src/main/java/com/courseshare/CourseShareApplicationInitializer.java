package com.courseshare;

import com.courseshare.config.RootConfig;
import com.courseshare.config.WebConfig;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import java.util.EnumSet;

public class CourseShareApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{RootConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter("UTF-8", true);
        return new Filter[]{encodingFilter};
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(new MultipartConfigElement("", 50L * 1024 * 1024, 55L * 1024 * 1024, 1024 * 1024));
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("spring.profiles.default", "production");
        super.onStartup(servletContext);
        SessionCookieConfig cookie = servletContext.getSessionCookieConfig();
        cookie.setName("CS_SESSION");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(Boolean.parseBoolean(System.getProperty("courseshare.secure-cookie", "false")));
        servletContext.setSessionTimeout(Integer.getInteger("courseshare.session-timeout-minutes", 30));
        registerApiFilter(servletContext, "requestIdFilter", "requestIdFilter");
        registerApiFilter(servletContext, "sameOriginFilter", "sameOriginFilter");
    }

    private void registerApiFilter(ServletContext context, String registrationName, String beanName) {
        FilterRegistration.Dynamic registration = context.addFilter(
                registrationName,
                new DelegatingFilterProxy(beanName)
        );
        if (registration != null) {
            registration.addMappingForUrlPatterns(
                    EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC),
                    false,
                    "/api/*"
            );
        }
    }
}

