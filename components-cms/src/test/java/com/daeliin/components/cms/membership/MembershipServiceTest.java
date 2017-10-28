package com.daeliin.components.cms.membership;

import com.daeliin.components.cms.Application;
import com.daeliin.components.cms.credentials.account.Account;
import com.daeliin.components.cms.credentials.account.AccountService;
import com.daeliin.components.cms.exception.InvalidTokenException;
import com.daeliin.components.cms.library.AccountLibrary;
import com.daeliin.components.cms.sql.QAccount;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import javax.inject.Inject;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = Application.class)
public class MembershipServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Inject
    private MembershipService membershipService;

    @Inject
    private AccountService accountService;

    @Test(expected = IllegalStateException.class)
    public void shouldThrowException_whenSigningUpExistingAccount() {
        Account existingAccount = AccountLibrary.admin();
        SignUpRequest signUpRequest = new SignUpRequest(existingAccount.username, existingAccount.email, "password");

        membershipService.signUp(signUpRequest);
    }

    @Test
    public void shouldNotCreateAccount_whenSigningUpExistingAccount() {
        Account existingAccount = AccountLibrary.admin();
        SignUpRequest signUpRequest = new SignUpRequest(existingAccount.username, existingAccount.email, "password");

        int accountCountBeforeSignUp = countAccountRows();

        try {
            membershipService.signUp(signUpRequest);
        } catch (IllegalStateException e) {
        }

        int accountCountAfterSignUp = countAccountRows();

        assertThat(accountCountAfterSignUp).isEqualTo(accountCountBeforeSignUp);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowException_whenActivatingNonExistentAccountId() throws InvalidTokenException {
        membershipService.activate("AOADAZD-65454", "ok");
    }

    @Test(expected = InvalidTokenException.class)
    public void shouldThrowException_whenTokenDoesntMatchWhenActivatingAccount() throws Exception {
        Account account = AccountLibrary.inactive();

        membershipService.activate(account.getId(), "wrongToken");
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowException_whenRequestingANewPasswordForNonExistingAccount() throws Exception {
        membershipService.newPassword("AFEZAFEZ-6544");
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowException_whenResetingPasswordForNonExistingAccount() throws Exception {
        membershipService.resetPassword("AFEZAFEZ-6544", "token", "newPassword");
    }

    @Test(expected = InvalidTokenException.class)
    public void shouldThrowException_whenTokenDoesntMatchWhenResetingPassword() throws Exception {
        Account account = AccountLibrary.admin();
        membershipService.resetPassword(account.getId(), "wrongToken", "newPassword");
    }

    private int countAccountRows() {
        return countRowsInTable(QAccount.account.getTableName());
    }
}