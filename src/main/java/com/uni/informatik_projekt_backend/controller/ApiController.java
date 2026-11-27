package com.uni.informatik_projekt_backend.controller;

import com.uni.informatik_projekt_backend.model.*;
import com.uni.informatik_projekt_backend.repository.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private GoalProgressRepository goalProgressRepository;
    @Autowired
    private FamilyRepository familyRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GoalRepository goalRepository;
    @Autowired
    private GoalContributionRepository goalContributionRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private RecurringTransferRepository recurringTransferRepository;
    @Autowired
    private PayoutRequestRepository payoutRequestRepository;
    @Autowired
    private FamilyWalletRepository familyWalletRepository;
    @Autowired
    private ExternalTransferRepository externalTransferRepository;
    @Autowired
    private InvitationRepository invitationRepository;

    public record CreateFamilyRequest(String name, String description) {
    }

    /**
     * direction: "deposit" (Bank -> Familienkonto) oder "withdrawal" (Familienkonto -> Bank).
     */
    public record ExternalTransferRequest(String direction, Long amountCents, String bankName, String note) {
    }

    public record MemberTransferRequest(UUID fromWalletId, UUID toWalletId, Long amountCents, String note) {
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/family")
    public Family getMyFamily() {
        var id = getCurrentUser().getFamilyId();
        if (id == null) {
            return null;
        }
        return familyRepository.findById(id).orElse(null);
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: You don't belong to this family");
        }
    }

    @Transactional
    @PostMapping("/families")
    public Family createFamily(@RequestBody CreateFamilyRequest req) {
        User currentUser = getCurrentUser();
        if (currentUser.getFamilyId() != null) {
            throw new RuntimeException("User already belongs to a family");
        }
        Family f = new Family();
        f.setName(req.name());
        f.setDescription(req.description());
        Family savedFamily = familyRepository.save(f);
        currentUser.setFamilyId(savedFamily.getId());
        userRepository.save(currentUser);

        // Familienkonto direkt mit anlegen
        FamilyWallet familyWallet = new FamilyWallet();
        familyWallet.setFamilyId(savedFamily.getId());
        familyWallet.setKind("family");
        familyWallet.setName("Familienkonto " + savedFamily.getName());
        familyWallet.setCurrency("EUR");
        familyWallet.setBalanceCents(0L);
        familyWallet.setCreatedAt(Instant.now());
        familyWalletRepository.save(familyWallet);

        // Einzelkonto für den Ersteller anlegen
        walletRepository.save(Wallet.newMemberWallet(savedFamily.getId(), currentUser.getId()));

        return savedFamily;
    }

    public record MeResponse(
            UUID id, String email, String firstName, String lastName,
            String displayName, LocalDate birthDate, String role, UUID familyId) {
    }

    @GetMapping("/users/me")
    public MeResponse me() {
        User u = getCurrentUser();
        return new MeResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getDisplayName(), u.getBirthDate(), u.getRole(), u.getFamilyId());
    }

    @GetMapping("/users/family/{familyId}")
    public List<User> getUsersByFamily(@PathVariable UUID familyId) {
        verifyUserBelongsToFamily(familyId);
        return userRepository.findByFamilyId(familyId);
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

    @GetMapping("/transactions/family-wallet/{familyWalletId}")
    public List<Transaction> getTransactionsByFamilyWallet(@PathVariable UUID familyWalletId) {
        var familyWallet = familyWalletRepository.findById(familyWalletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Family wallet not found"));
        verifyUserBelongsToFamily(familyWallet.getFamilyId());

        // NEU: Nur Transaktionen vom Typ 'transfer' (Ein- und Auszahlungen) zurückgeben
        return transactionRepository.findByFamilyWalletId(familyWalletId).stream()
                .filter(t -> "transfer".equals(t.getKind()))
                .toList();
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction tx) {
        if (tx.getWalletId() == null || tx.getFamilyWalletId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Both walletId and familyWalletId are required");
        }
        var wallet = walletRepository.findById(tx.getWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        var familyWallet = familyWalletRepository.findById(tx.getFamilyWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Family wallet not found"));
        if (!wallet.getFamilyId().equals(familyWallet.getFamilyId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Personal and family wallets must belong to the same family");
        }
        verifyUserBelongsToFamily(familyWallet.getFamilyId());

        // Nur Überweisungen Familienkonto -> Einzelkonto erlaubt (Betrag = Gutschrift beim Einzelkonto).
        // Einzelkonten können nicht mehr auf das Familienkonto überweisen.
        if (tx.getAmountCents() == null || tx.getAmountCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only transfers from the family wallet to member wallets are allowed (amountCents must be positive)");
        }
        long available = familyWallet.getBalanceCents() != null ? familyWallet.getBalanceCents() : 0L;
        if (tx.getAmountCents() > available) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance in family wallet");
        }

        User currentUser = getCurrentUser();
        tx.setCreatedBy(currentUser.getId());

        tx.setWalletId(wallet.getId());
        tx.setFamilyWalletId(familyWallet.getId());
        tx.setFamilyId(familyWallet.getFamilyId());
        tx.setId(null);
        tx.setKind("transfer");

        Transaction savedTx = transactionRepository.save(tx);

        // Kontostand des Familienkontos reduzieren (Gutschrift beim Einzelkonto = Abfluss beim Familienkonto)
        familyWallet.setBalanceCents(available - savedTx.getAmountCents());
        familyWalletRepository.save(familyWallet);

        return ResponseEntity.ok(savedTx);
    }

    // Überweisung zwischen zwei Einzelkonten derselben Familie.
    // Es entstehen zwei Buchungen: Abbuchung beim Sender, Gutschrift beim Empfänger.
    @PostMapping("/transactions/member-transfer")
    public ResponseEntity<List<Transaction>> createMemberTransfer(@RequestBody MemberTransferRequest req) {
        if (req.fromWalletId() == null || req.toWalletId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromWalletId and toWalletId are required");
        }
        if (req.fromWalletId().equals(req.toWalletId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender and recipient must differ");
        }
        if (req.amountCents() == null || req.amountCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amountCents must be positive");
        }
        var from = walletRepository.findById(req.fromWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender wallet not found"));
        var to = walletRepository.findById(req.toWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient wallet not found"));
        if (!from.getFamilyId().equals(to.getFamilyId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both wallets must belong to the same family");
        }
        verifyUserBelongsToFamily(from.getFamilyId());

        // transactions.family_wallet_id ist NOT NULL -> Familienkonto der Familie als Referenz verwenden
        var familyWallet = familyWalletRepository.findByFamilyId(from.getFamilyId()).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Family has no family wallet yet"));

        UUID createdBy = getCurrentUser().getId();
        String note = req.note() != null && !req.note().isBlank() ? req.note() : "Überweisung";

        Transaction debit = new Transaction();
        debit.setWalletId(from.getId());
        debit.setFamilyWalletId(familyWallet.getId());
        debit.setFamilyId(from.getFamilyId());
        debit.setAmountCents(-req.amountCents());
        debit.setKind("member_transfer");
        debit.setNote(note);
        debit.setCreatedBy(createdBy);

        Transaction credit = new Transaction();
        credit.setWalletId(to.getId());
        credit.setFamilyWalletId(familyWallet.getId());
        credit.setFamilyId(to.getFamilyId());
        credit.setAmountCents(req.amountCents());
        credit.setKind("member_transfer");
        credit.setNote(note);
        credit.setCreatedBy(createdBy);

        return ResponseEntity.ok(transactionRepository.saveAll(List.of(debit, credit)));
    }

    // Simulierte Ein-/Auszahlungen zwischen Familienkonto und externer Bank
    @GetMapping("/external-transfers/family-wallet/{familyWalletId}")
    public List<ExternalTransfer> getExternalTransfers(@PathVariable UUID familyWalletId) {
        var familyWallet = familyWalletRepository.findById(familyWalletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Family wallet not found"));
        verifyUserBelongsToFamily(familyWallet.getFamilyId());
        return externalTransferRepository.findByFamilyWalletId(familyWalletId);
    }

    @PostMapping("/family-wallets/{familyWalletId}/external-transfer")
    public ResponseEntity<ExternalTransfer> createExternalTransfer(@PathVariable UUID familyWalletId,
                                                                   @RequestBody ExternalTransferRequest req) {
        var familyWallet = familyWalletRepository.findById(familyWalletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Family wallet not found"));
        verifyUserBelongsToFamily(familyWallet.getFamilyId());

        if (req.amountCents() == null || req.amountCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amountCents must be positive");
        }
        if (req.bankName() == null || req.bankName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bankName is required");
        }
        boolean deposit;
        if ("deposit".equalsIgnoreCase(req.direction())) {
            deposit = true;
        } else if ("withdrawal".equalsIgnoreCase(req.direction())) {
            deposit = false;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "direction must be 'deposit' or 'withdrawal'");
        }

        long currentBalance = familyWallet.getBalanceCents() != null ? familyWallet.getBalanceCents() : 0L;
        if (!deposit && req.amountCents() > currentBalance) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance in family wallet");
        }

        ExternalTransfer transfer = new ExternalTransfer();
        transfer.setFamilyWalletId(familyWallet.getId());
        transfer.setFamilyId(familyWallet.getFamilyId());
        transfer.setAmountCents(deposit ? req.amountCents() : -req.amountCents());
        transfer.setBankName(req.bankName().trim());
        transfer.setNote(req.note());
        transfer.setCreatedBy(getCurrentUser().getId());
        ExternalTransfer saved = externalTransferRepository.save(transfer);

        familyWallet.setBalanceCents(currentBalance + saved.getAmountCents());
        familyWalletRepository.save(familyWallet);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/goals/family/{familyId}")
    public List<GoalProgress> getGoalsByFamily(@PathVariable UUID familyId) {
        verifyUserBelongsToFamily(familyId);
        return goalProgressRepository.findByFamilyId(familyId);
    }

    @PostMapping("/goals")
    public ResponseEntity<Goal> createGoal(@RequestBody Goal goal) {
        verifyUserBelongsToFamily(goal.getFamilyId());
        if (goal.getGoalName() == null || goal.getGoalName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "goalName is required");
        }
        if (goal.getTargetCents() == null || goal.getTargetCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetCents must be positive");
        }
        // Teilnehmer müssen Mitglieder derselben Familie sein
        var familyMemberIds = userRepository.findByFamilyId(goal.getFamilyId()).stream()
                .map(User::getId)
                .collect(java.util.stream.Collectors.toSet());
        if (!familyMemberIds.containsAll(goal.getParticipantIds())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "All participants must belong to the family");
        }
        goal.setId(null);
        goal.setGoalName(goal.getGoalName().trim());
        goal.setOwnerId(getCurrentUser().getId());
        goal.setStatus(goal.getStatus() != null ? goal.getStatus() : "active");
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
        contribution.setUserId(getCurrentUser().getId());
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