package ch.denicola.konfi.brunch.security;

import ch.denicola.konfi.brunch.data.BrunchAuthorization;
import ch.denicola.konfi.brunch.data.BrunchAuthorizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.service.spi.InjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Log
@RequiredArgsConstructor
public class BrunchPasswordAuthenticationProvider implements AuthenticationProvider {

    private final BrunchAuthorizationRepository authorizationRepository;
    private final PasswordEncoder passwordEncoder;
    private Authentication handleBlankBrunchId(){
        return null;
    }
    private static Supplier<BrunchAuthorization> getSupplierForBrunchId(String brunchId){
       return () -> BrunchAuthorization.builder().adminPasswordHash(null).votingPasswordHash(null).brunch_id(brunchId).build();
    }

    private BrunchPasswordAuthenticationToken runAuthentication(BrunchAuthorization authData,BrunchPasswordAuthenticationToken token){
        boolean votingPasswordIsEmpty = StringUtils.isEmpty(authData.getVotingPasswordHash());
        boolean adminPasswordIsEmpty = StringUtils.isEmpty(authData.getAdminPasswordHash());
        if (votingPasswordIsEmpty) {
            if (adminPasswordIsEmpty){
                BrunchPasswordAuthenticationToken adminToken;
                if (token.isAdmin()){
                    adminToken = token;
                }
                else {
                    adminToken = BrunchPasswordAuthenticationToken.forAdmin(authData.getBrunch_id(),"");
                }
                adminToken.clearCredentialsAndSetAuth(true);
                return adminToken;
            }
            if (!token.isAdmin()){
                token.clearCredentialsAndSetAuth(true);
                return token;
            }
        }
        else {
            if (!token.isAdmin() || adminPasswordIsEmpty){
                if (passwordEncoder.matches(token.getCredentials(), authData.getVotingPasswordHash())) {
                    token.clearCredentialsAndSetAuth(true);
                    return token;
                }
                throw new BadCredentialsException("Invalid Voting password");
            }
        }
        if (passwordEncoder.matches(token.getCredentials(), authData.getAdminPasswordHash())) {
            token.clearCredentialsAndSetAuth(true);
            return token;
        }
        throw new BadCredentialsException("Invalid Admin password");
    }
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof BrunchPasswordAuthenticationToken token){
            var brunchId = token.getPrincipal();
            if (StringUtils.isBlank(brunchId)) return handleBlankBrunchId();
            var brunchAuthorization = authorizationRepository.findById(brunchId).orElseGet(getSupplierForBrunchId(brunchId));
            log.info("Authenticating for brunch: " + brunchId);
            return runAuthentication(brunchAuthorization,token);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BrunchPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
