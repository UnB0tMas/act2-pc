package com.upsjb.act2.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public class TrimPlainTextPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword == null ? "" : rawPassword.toString().trim();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String raw = rawPassword == null ? "" : rawPassword.toString().trim();
        String stored = encodedPassword == null ? "" : encodedPassword.trim();
        return raw.equals(stored);
    }
}