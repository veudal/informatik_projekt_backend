package com.uni.informatik_projekt_backend.controller;

import com.uni.informatik_projekt_backend.model.*;
import com.uni.informatik_projekt_backend.repository.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired private FamilyRepository familyRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private ChoreRepository choreRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/families")
    public List<Family> getFamilies() {
        return familyRepository.findAll();
    }

    @GetMapping("/families/count")
    public long getFamilyCount() {
        return familyRepository.count();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void verifyUserBelongsToFamily(UUID familyId) {
        User currentUser = getCurrentUser();
        if (currentUser.getFamilyId() == null || !currentUser.getFamilyId().equals(familyId)) {
            throw new RuntimeException("Access denied: You don't belong to this family");
        }
    }

    @PostMapping("/families")
    public Family createFamily(@RequestParam String name) {
        User currentUser = getCurrentUser();
        if (currentUser.getFamilyId() != null) {
            throw new RuntimeException("User already belongs to a family");
        }
        Family f = new Family();
        f.setName(name);
        f.setCreated_at(new Date());
        Family savedFamily = familyRepository.save(f);
        currentUser.setFamilyId(savedFamily.getId());
        userRepository.save(currentUser);
        return savedFamily;
    }

    @GetMapping("/wallets/family/{familyId}")
    public List<Wallet> getWalletsByFamily(@PathVariable UUID familyId) {
        verifyUserBelongsToFamily(familyId);
        return walletRepository.findByFamilyId(familyId);
    }

    @PostMapping("/wallets")
    public ResponseEntity<Wallet> createWallet(@RequestBody Wallet wallet) {
        verifyUserBelongsToFamily(wallet.getFamilyId());
        wallet.setId(null);
        wallet.setCurrency(wallet.getCurrency() != null ? wallet.getCurrency() : "EUR");
        wallet.setName(wallet.getName() != null ? wallet.getName() : "Spending");
        wallet.setCreatedAt(new Date());
        return ResponseEntity.ok(walletRepository.save(wallet));
    }

    @GetMapping("/wallets/balance/{walletId}")
    public ResponseEntity<Long> getBalance(@PathVariable UUID walletId) {
        var wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        verifyUserBelongsToFamily(wallet.getFamilyId());
        return ResponseEntity.ok(walletRepository.getBalanceByWalletId(walletId));
    }

    @GetMapping("/transactions/wallet/{walletId}")
    public List<Transaction> getTransactionsByWallet(@PathVariable UUID walletId) {
        var wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        verifyUserBelongsToFamily(wallet.getFamilyId());
        return transactionRepository.findByWalletId(walletId);
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction tx) {
        var wallet = walletRepository.findById(tx.getWalletId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        verifyUserBelongsToFamily(wallet.getFamilyId());
        tx.setFamilyId(wallet.getFamilyId());
        tx.setId(null);
        tx.setKind(tx.getKind() != null ? tx.getKind() : "manual");
        tx.setCreatedAt(new Date());
        return ResponseEntity.ok(transactionRepository.save(tx));
    }

    @GetMapping("/chores/family/{familyId}")
    public List<Chore> getChoresByFamily(@PathVariable UUID familyId) {
        verifyUserBelongsToFamily(familyId);
        return choreRepository.findByFamilyId(familyId);
    }

    @PostMapping("/chores")
    public ResponseEntity<Chore> createChore(@RequestBody Chore chore) {
        verifyUserBelongsToFamily(chore.getFamilyId());
        chore.setId(null);
        chore.setStatus(chore.getStatus() != null ? chore.getStatus() : "open");
        chore.setRewardCents(chore.getRewardCents() != null ? chore.getRewardCents() : 0L);
        chore.setCreatedAt(new Date());
        return ResponseEntity.ok(choreRepository.save(chore));
    }
}