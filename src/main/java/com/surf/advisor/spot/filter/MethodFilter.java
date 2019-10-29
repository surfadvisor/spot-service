package com.surf.advisor.spot.filter;


import static java.util.Locale.ENGLISH;
import static org.springframework.util.StringUtils.hasLength;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class MethodFilter extends OncePerRequestFilter {

    private static final String HTTP_METHOD_OVERRIDE_HEADER = "X-HTTP-Method-Override";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
        throws ServletException, IOException {

        var headerValue = request.getHeader(HTTP_METHOD_OVERRIDE_HEADER);

        if (RequestMethod.POST.name().equals(request.getMethod()) && hasLength(headerValue)) {
            var method = headerValue.toUpperCase(ENGLISH);
            request = new HttpMethodRequestWrapper(request, method);
        }
        filterChain.doFilter(request, response);
    }

    @Getter
    private class HttpMethodRequestWrapper extends HttpServletRequestWrapper {

        private final String method;

        HttpMethodRequestWrapper(HttpServletRequest request, String method) {
            super(request);
            this.method = method;
        }
    }
}
