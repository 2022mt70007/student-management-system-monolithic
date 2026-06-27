package com.sms.monolith.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class JwtHeaderRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> headers = new LinkedHashMap<>();

    JwtHeaderRequestWrapper(HttpServletRequest request, Claims claims) {
        super(request);
        addHeader("X-User-Email", claims.getSubject());
        addHeader("X-User-Role", String.valueOf(claims.get("role")));
        Object profileId = claims.get("profileId");
        if (profileId != null) {
            addHeader("X-Profile-Id", String.valueOf(profileId));
        }
    }

    private void addHeader(String name, String value) {
        if (value != null && !value.isBlank() && !"null".equals(value)) {
            headers.put(name, value);
        }
    }

    private String lookup(String name) {
        if (name == null) {
            return null;
        }
        String direct = headers.get(name);
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public String getHeader(String name) {
        String value = lookup(name);
        return value != null ? value : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String value = lookup(name);
        if (value != null) {
            return Collections.enumeration(List.of(value));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
        names.addAll(headers.keySet());
        return Collections.enumeration(names);
    }
}
