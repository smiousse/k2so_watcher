package com.k2so.watcher.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class TotpAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String totpCode;

    public TotpAuthenticationToken(Object principal, String totpCode) {
        super(null);
        this.principal = principal;
        this.totpCode = totpCode;
        setAuthenticated(false);
    }

    public TotpAuthenticationToken(Object principal, String totpCode,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.totpCode = totpCode;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return totpCode;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getTotpCode() {
        return totpCode;
    }
}
