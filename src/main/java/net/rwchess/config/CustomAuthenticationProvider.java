package net.rwchess.config;

import net.rwchess.persistent.Member;
import net.rwchess.persistent.dao.MemberDAO;
import net.rwchess.utils.UsefulMethods;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private MemberDAO memberDAO;

    public CustomAuthenticationProvider(MemberDAO memberDAO) {
        this.memberDAO = memberDAO;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        Member user = memberDAO.getMemberByUsername(username);

        if (user == null) {
            throw new BadCredentialsException("Username not found.");
        }

        if (!UsefulMethods.getMD5(password).equals(user.getPasswordHash())) {
            throw new BadCredentialsException("Wrong password.");
        }

        if (!user.isConfirmed())
            throw new BadCredentialsException("Your account is unconfirmed.");

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("member"));
        if (user.getGroup() > 1)
            authorities.add(new SimpleGrantedAuthority("td"));
        if (user.getGroup() > 2)
            authorities.add(new SimpleGrantedAuthority("admin"));

        return new UsernamePasswordAuthenticationToken(user, password, authorities);
    }

    @Override
    public boolean supports(Class<?> arg0) {
        return true;
    }
}
