package com.pborsa.api.security;

import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating security contexts with mock Firebase users.
 * <p>
 * This factory is used by the {@link WithMockFirebaseUser} annotation to
 * create a properly configured security context for testing.
 */
public class WithMockFirebaseUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockFirebaseUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockFirebaseUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                annotation.uid(),
                annotation.email(),
                annotation.displayName(),
                annotation.provider(),
                annotation.admin()
        );

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (annotation.admin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );

        context.setAuthentication(auth);
        return context;
    }
}
