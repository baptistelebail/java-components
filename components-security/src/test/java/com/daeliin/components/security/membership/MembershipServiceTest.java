package com.daeliin.components.security.membership;

import com.daeliin.components.core.exception.ResourceNotFoundException;
import com.daeliin.components.security.Application;
import com.daeliin.components.security.credentials.account.Account;
import com.daeliin.components.security.credentials.account.AccountRepository;
import com.daeliin.components.security.exception.AccountAlreadyExistException;
import com.daeliin.components.security.exception.InvalidTokenException;
import com.daeliin.components.test.IntegrationTest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

@ContextConfiguration(classes = Application.class)
public class MembershipServiceTest extends IntegrationTest {
    
    @Autowired
    private MembershipService membershipService;
    
    @Autowired
    private AccountRepository accountRepository;
    
    
    @Test(expectedExceptions = AccountAlreadyExistException.class)
    public void signup_existingAccount_throwsException() {
        Account existingAccount = accountRepository.findOne(1L);
        existingAccount.setClearPassword("password");
        
        membershipService.signUp(existingAccount);
    }
    
    @Test
    public void signup_existingAccount_doesntCreateAccount() {
        long userDetailsCountBeforeSignUp = accountRepository.count();
        Account existingAccount = accountRepository.findOne(1L);
        existingAccount.setClearPassword("password");
        
        try {
            membershipService.signUp(existingAccount);
        } catch (AccountAlreadyExistException e) {
        }
        
        long userDetailsCountAfterSignUp = accountRepository.count();
        
        assertEquals(userDetailsCountAfterSignUp, userDetailsCountBeforeSignUp);
    }
    
    
    @Test
    public void signup_account_createsAccount() {
        Account account = createAccount("username", "email@email.com", "password");
        
        membershipService.signUp(account);
        Account createdAccount = accountRepository.findByUsernameIgnoreCase("username");
        
        assertEquals(createdAccount, account);
        assertTrue(StringUtils.isNotBlank(createdAccount.getPassword()));
        assertTrue(StringUtils.isNotBlank(createdAccount.getToken()));
    }
    
    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void activate_nonExistentAccountId_throwsException() throws InvalidTokenException {
        membershipService.activate(-1L, "token");
    }
    
    @Test(expectedExceptions = InvalidTokenException.class)
    public void activate_wrongActivationToken_throwsException() throws Exception {
        Account userDetails = accountRepository.findOne(1L);
        
        membershipService.activate(userDetails.getId(), "wrong" + userDetails.getToken());
    }
    
    @Test
    public void activate_wrongActivationToken_doesntActivateAccount() {
        Account userDetails = accountRepository.findOne(3L);
        
        try {
            membershipService.activate(userDetails.getId(), "wrong" + userDetails.getToken());
        } catch (InvalidTokenException e) {
        }
        
        userDetails = accountRepository.findOne(3L);
        assertFalse(userDetails.isEnabled());
    }
    
    
    @Test
    public void activate_validAccountIdAndActivationToken_activatesAccount() throws Exception {
        Account userDetails = accountRepository.findOne(3L);
        
        membershipService.activate(userDetails.getId(), userDetails.getToken());
        
        userDetails = accountRepository.findOne(3L);
        
        assertTrue(userDetails.isEnabled());
    }
    
    @Test
    public void activate_validAccountIdAndActivationToken_setsNewToken() throws Exception {
        Account userDetails = accountRepository.findOne(3L);
        String activationToken = userDetails.getToken();
        
        membershipService.activate(userDetails.getId(), userDetails.getToken());
        
        userDetails = accountRepository.findOne(3L);
        
        assertNotEquals(userDetails.getToken(), activationToken);
    }
    
    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void newPassword_nonExistentAccountId_returnsHttpNotFound() throws Exception {
        membershipService.newPassword(-1L);
    }
    
    @Test
    public void newPassword_validAccountId_doesntThrowAnyException() {
        Account userDetails = accountRepository.findOne(3L);
        
        try {
            membershipService.newPassword(userDetails.getId());
        } catch (Exception e) {
            fail();
        }
    }

        
    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void resetPassword_nonExistentAccountId_throwsException() throws Exception {
        membershipService.resetPassword(-1L, "token", "newPassword");
    }
    
    
    @Test(expectedExceptions = InvalidTokenException.class)
    public void resetPassword_invalidToken_throwsException() throws Exception { 
        Account account = accountRepository.findOne(3L);
        membershipService.resetPassword(account.getId(), "wrong" + account.getToken(), "newPassword");
    }
    
    @Test
    public void resetPassword_wrongActivationToken_doesntResetAccountPassword() {
        Account account = accountRepository.findOne(3L);
        String tokenBeforeResetPassword = account.getToken();
        String passwordBeforeResetPassword = account.getPassword();
        
        try {
            membershipService.resetPassword(account.getId(), "wrong" + account.getToken(), "newPassword");
        } catch (InvalidTokenException e) {
        }
            
        account = accountRepository.findOne(3L);
        String tokenAfterResetPassword = account.getToken();
        String passwordAfterResetPassword = account.getPassword();
        
        assertEquals(passwordAfterResetPassword, passwordBeforeResetPassword);
        assertEquals(tokenAfterResetPassword, tokenBeforeResetPassword);
        assertFalse(account.isEnabled());
    }
    
    @Test
    public void resetPassword_validResetPasswordRequest_resetAccountPassword() throws Exception {
        Account account = accountRepository.findOne(3L);
        String passwordBeforeResetPassword = account.getPassword();
        String tokenBeforeResetPassword = account.getToken();
        
        membershipService.resetPassword(account.getId(), account.getToken(), "newPassword");
        
        account = accountRepository.findOne(3L);
        String tokenAfterResetPassword = account.getToken();
        String passwordAfterResetPassword = account.getPassword();
        
        assertNotEquals(passwordAfterResetPassword, passwordBeforeResetPassword);
        assertNotEquals(tokenAfterResetPassword, tokenBeforeResetPassword);
        assertTrue(account.isEnabled());
    }
    
    private Account createAccount(final String username, final String email, final String password) {
        Account account = new Account();
        
        account.setUsername(username);
        account.setEmail(email);
        account.setClearPassword(password);
        
        return account;
    }
}