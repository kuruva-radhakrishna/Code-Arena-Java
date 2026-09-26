package com.codearena.backend.security;

import com.codearena.backend.user.Role;

/**
 * The JWT-derived principal placed in the security context for each authenticated
 * request. Built purely from token claims (no database lookup per request).
 */
public record AuthenticatedUser(String id, String email, Role role) {
}
