package com.academit.education.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-key-that-is-long-enough-for-sha256-signing", 3600000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken("admin", "ADMINISTRADOR", 1);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("admin", jwtUtil.extractUsername(token));
        assertEquals("ADMINISTRADOR", jwtUtil.extractRole(token));
        assertEquals(1, jwtUtil.extractIdUsuario(token));
    }

    @Test
    void testTamperedTokenIsInvalid() {
        String token = jwtUtil.generateToken("admin", "ADMINISTRADOR", 1);
        String tampered = token + "corrupted";
        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    void testInvalidTokenStructure() {
        assertFalse(jwtUtil.validateToken("invalid.token"));
        assertNull(jwtUtil.extractUsername("invalid.token"));
    }
}

