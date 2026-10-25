package com.uni.informatik_projekt_backend.controller;

import com.uni.informatik_projekt_backend.model.*;
import com.uni.informatik_projekt_backend.repository.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired private FamilyRepository familyRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private GoalRepository goalRepository;
    @Autowired private GoalContributionRepository goalContributionRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private RecurringTransferRepository recurringTransferRepository;
    @Autowired private PayoutRequestRepository payoutRequestRepository;
    @Autowired private FamilyWalletRepository familyWalletRepository;

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
        wallet.setKind(wallet.getKind() != null ? wallet.getKind() : "member");
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
        return ResponseEntity.ok(transactionRepository.save(tx));
    }

    // Goals endpoints
    @GetMapping("/goals/family/{familyId}")
    public List<Goal> getGoalsByFamily(@PathVariable UUID familyId) {
        verifyUserBelongsToFamily(familyId);
        return goalRepository.findByFamilyId(familyId);
    }

    @PostMapping("/goals")
    public ResponseEntity<Goal> createGoal(@RequestBody Goal goal) {
        verifyUserBelongsToFamily(goal.getFamilyId());
        goal.setId(null);
        goal.setStatus(goal.getStatus() != null ? goal.getStatus() : "active");
        goal.setCurrentCents(0L);
        return ResponseEntity.ok(goalRepository.save(goal));
    }

    // Goal contributions endpoints
    @GetMapping("/goal-contributions/goal/{goalId}")
    public List<GoalContribution> getContributionsByGoal(@PathVariable UUID goalId) {
        return goalContributionRepository.findByGoalId(goalId);
    }

    @PostMapping("/goal-contributions")
    public ResponseEntity<GoalContribution> createContribution(@RequestBody GoalContribution contribution) {
        contribution.setId(null);
        return ResponseEntity.ok(goalContributionRepository.save(contribution));
    }

    // Tasks endpoints
    @GetMapping("/tasks/family/{familyId}")
    public List<Task> getTasksByFamily(@PathVariable UUID familyId) {
        verifyUserBelongsToFamily(familyId);
        return taskRepository.findByFamilyId(familyId);
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        verifyUserBelongsToFamily(task.getFamilyId());
        task.setId(null);
        task.setStatus(task.getStatus() != null ? task.getStatus() : "open");
        task.setRewardCents(task.getRewardCents() != null ? task.getRewardCents() : 0L);
        return ResponseEntity.ok(taskRepository.save(task));
    }

    // Recurring transfers endpoints
    @GetMapping("/recurring-transfers/family/{familyId}")
    public List<RecurringTransfer> getRecurringTransfersByFamily(@PathVariable UUID familyId) {
        verifyUserBelongsToFamily(familyId);
        return recurringTransferRepository.findByFamilyId(familyId);
    }

    @PostMapping("/recurring-transfers")
    public ResponseEntity<RecurringTransfer> createRecurringTransfer(@RequestBody RecurringTransfer transfer) {
        verifyUserBelongsToFamily(transfer.getFamilyId());
        transfer.setId(null);
        transfer.setActive(transfer.getActive() != null ? transfer.getActive() : true);
        return ResponseEntity.ok(recurringTransferRepository.save(transfer));
    }

    // Payout requests endpoints
    @GetMapping("/payout-requests/family-wallet/{familyWalletId}")
    public List<PayoutRequest> getPayoutRequestsByFamilyWallet(@PathVariable UUID familyWalletId) {
        return payoutRequestRepository.findByFamilyWalletId(familyWalletId);
    }

    @PostMapping("/payout-requests")
    public ResponseEntity<PayoutRequest> createPayoutRequest(@RequestBody PayoutRequest request) {
        request.setId(null);
        request.setStatus(request.getStatus() != null ? request.getStatus() : "requested");
        return ResponseEntity.ok(payoutRequestRepository.save(request));
    }

    // Family wallets endpoints
    @GetMapping("/family-wallets/family/{familyId}")
    public List<FamilyWallet> getFamilyWalletsByFamily(@PathVariable UUID familyId) {
        verifyUserBelongsToFamily(familyId);
        return familyWalletRepository.findByFamilyId(familyId);
    }

    @PostMapping("/family-wallets")
    public ResponseEntity<FamilyWallet> createFamilyWallet(@RequestBody FamilyWallet familyWallet) {
        verifyUserBelongsToFamily(familyWallet.getFamilyId());
        familyWallet.setId(null);
        return ResponseEntity.ok(familyWalletRepository.save(familyWallet));
    }
}