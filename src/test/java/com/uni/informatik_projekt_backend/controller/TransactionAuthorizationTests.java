package com.uni.informatik_projekt_backend.controller;

import com.uni.informatik_projekt_backend.model.FamilyWallet;
import com.uni.informatik_projekt_backend.model.Transaction;
import com.uni.informatik_projekt_backend.model.User;
import com.uni.informatik_projekt_backend.model.Wallet;
import com.uni.informatik_projekt_backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionAuthorizationTests {
    @Mock private FamilyRepository familyRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private GoalRepository goalRepository;
    @Mock private GoalContributionRepository goalContributionRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private RecurringTransferRepository recurringTransferRepository;
    @Mock private PayoutRequestRepository payoutRequestRepository;
    @Mock private FamilyWalletRepository familyWalletRepository;

    @InjectMocks private ApiController controller;

    private UUID userFamilyId;
    private UUID personalWalletId;
    private UUID familyWalletId;

    @BeforeEach
    void setUp() {
        userFamilyId = UUID.randomUUID();
        personalWalletId = UUID.randomUUID();
        familyWalletId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("member@example.com", null));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsTransactionForFamilyWalletOutsideAuthenticatedUsersFamily() {
        UUID otherFamilyId = UUID.randomUUID();
        stubWallets(otherFamilyId, otherFamilyId);
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user(userFamilyId)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.createTransaction(transaction()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void savesTransactionOnlyWhenBothWalletsAreInAuthenticatedUsersFamily() {
        stubWallets(userFamilyId, userFamilyId);
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user(userFamilyId)));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Transaction transaction = transaction();
        transaction.setFamilyId(UUID.randomUUID());

        controller.createTransaction(transaction);

        verify(transactionRepository).save(transaction);
        assertEquals(userFamilyId, transaction.getFamilyId());
        assertEquals(personalWalletId, transaction.getWalletId());
        assertEquals(familyWalletId, transaction.getFamilyWalletId());
    }

    @Test
    void rejectsPersonalAndFamilyWalletsFromDifferentFamilies() {
        stubWallets(UUID.randomUUID(), UUID.randomUUID());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.createTransaction(transaction()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).findByEmail(any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    private void stubWallets(UUID personalWalletFamilyId, UUID familyWalletFamilyId) {
        Wallet wallet = new Wallet();
        wallet.setId(personalWalletId);
        wallet.setFamilyId(personalWalletFamilyId);
        FamilyWallet familyWallet = new FamilyWallet();
        familyWallet.setId(familyWalletId);
        familyWallet.setFamilyId(familyWalletFamilyId);
        when(walletRepository.findById(personalWalletId)).thenReturn(Optional.of(wallet));
        when(familyWalletRepository.findById(familyWalletId)).thenReturn(Optional.of(familyWallet));
    }

    private User user(UUID familyId) {
        User user = new User();
        user.setFamilyId(familyId);
        return user;
    }

    private Transaction transaction() {
        Transaction transaction = new Transaction();
        transaction.setWalletId(personalWalletId);
        transaction.setFamilyWalletId(familyWalletId);
        transaction.setAmountCents(250L);
        return transaction;
    }
}
