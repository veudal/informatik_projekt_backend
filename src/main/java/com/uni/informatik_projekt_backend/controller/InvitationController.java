package com.uni.informatik_projekt_backend.controller;

import com.uni.informatik_projekt_backend.model.Invitation;
import com.uni.informatik_projekt_backend.model.User;
import com.uni.informatik_projekt_backend.repository.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/family/invite")
public class InvitationController {
    private static final Logger LOGGER = LogManager.getLogger(InvitationController.class);
    private static final String POLL_STATUS_PENDING = "pending";
    private static final String POLL_STATUS_ACCEPTED = "accepted";
    private static final String POLL_STATUS_DENIED = "denied";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InvitationRepository invitationRepository;

    /**
     * Performs an action regarding the user, by first checking whether
     * the requesting user is authenticated and authorized to make requests
     */
    private <T> ResponseEntity<T> withAuthorizedUser(Function<User, ResponseEntity<T>> function) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email).map(function).orElseThrow();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Performs an action regarding the user, by first checking whether
     * the requesting user is authenticated and authorized to make requests,
     * and also whether they have rights to have insight regarding an invitation.
     */
    private <T> ResponseEntity<T> withAuthorizedIssuer(String invitationId, BiFunction<User, Invitation, ResponseEntity<T>> bifunction) {
        return this.withAuthorizedUser((user) -> {
            Optional<Invitation> maybeInvitation = this.invitationRepository.findById(invitationId);

            if (maybeInvitation.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Invitation invitation = maybeInvitation.get();

            if (!invitation.getIssuerId().equals(user.getId())) {
                // user should not be permitted to accept if they
                // are not the invitation issuer
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // possible TODO: check if user has lost parent role in meantime

            return bifunction.apply(user, invitation);
        });
    }

    /**
     * Regularly cleans out old invite requests that are over 30 minutes old.
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void cleanInvitations() {
        //int count = this.invitationRepository.deleteBefore(Instant.now().minus(30, ChronoUnit.MINUTES));
        //LOGGER.debug("Cleaned {} invitation entries older than 30 minutes.", count);
    }

    @PostMapping("/create-invitation")
    public ResponseEntity<?> createInvitation() {
        return this.withAuthorizedUser((user) -> {
            if (user.getFamilyId() == null) { // TODO: or user does not have role of parent
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            int tries = 4;
            String id = "";

            while (tries > 0) {
                --tries;
                id = RandomStringUtils.randomAlphanumeric(6);

                if (!this.invitationRepository.existsById(id)) {
                    break;
                } else if (tries == 0) {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
                }
            }

            Invitation invitation = new Invitation();
            invitation.setId(id);
            invitation.setIssuerId(user.getId());
            invitation.setRedeemStatus(Invitation.REDEEM_STATUS_REDEEMABLE);
            invitation = this.invitationRepository.save(invitation);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of("id", invitation.getId()));
        });
    }

    @GetMapping("/poll-redeemer/{id}")
    public ResponseEntity<?> pollRedeemer(@PathVariable String id) {
        return this.withAuthorizedIssuer(id, (user, invitation) -> {
            if (!Invitation.REDEEM_STATUS_PENDING.equals(invitation.getRedeemStatus())) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            if (invitation.getRedeemerId() == null) {
                // this should not happen
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            User redeemer = this.userRepository.getReferenceById(invitation.getRedeemerId());

            return ResponseEntity.ok(Map.of(
                    "id", redeemer.getId(),
                    "display_name", redeemer.getDisplayName()
            ));
        });
    }

    public record RespondRedeemerBody(boolean accept) {}

    @PostMapping("/respond-redeemer/{id}")
    public ResponseEntity<Void> respondRedeemer(@PathVariable String id, @RequestBody RespondRedeemerBody body) {
        return this.withAuthorizedIssuer(id, (user, invitation) -> {
            if (!Invitation.REDEEM_STATUS_PENDING.equals(invitation.getRedeemStatus())) {
                // user cannot accept or deny if nobody
                // has redeemed the invitation
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            User child = this.userRepository.findById(invitation.getRedeemerId()).orElseThrow(); // always present due to foreign key relation

            if (child.getFamilyId() != null) {
                // client has somehow managed to get accepted
                // into another family in the meantime
                invitation.setRedeemStatus(Invitation.REDEEM_STATUS_DENIED);
                invitation = this.invitationRepository.save(invitation);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            if (body.accept) {
                child.setFamilyId(user.getFamilyId());
                child = this.userRepository.save(child);
                invitation.setRedeemStatus(Invitation.REDEEM_STATUS_ACCEPTED);
            } else {
                invitation.setRedeemStatus(Invitation.REDEEM_STATUS_DENIED);
            }

            invitation = this.invitationRepository.save(invitation);

            return ResponseEntity.ok().build();
        });
    }

    @PostMapping("/redeem/{id}")
    public ResponseEntity<Void> redeem(@PathVariable String id) {
        return this.withAuthorizedUser((user) -> {
            if (user.getFamilyId() != null) {
                // user is already in a family
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Optional<Invitation> maybeInvitation = this.invitationRepository.findById(id);

            if (maybeInvitation.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Invitation invitation = maybeInvitation.get();

            if (!invitation.getRedeemStatus().equals(Invitation.REDEEM_STATUS_REDEEMABLE)) {
                // invitation already in use
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            invitation.setRedeemStatus(Invitation.REDEEM_STATUS_PENDING);
            invitation.setRedeemerId(user.getId());
            invitation = this.invitationRepository.save(invitation);

            return ResponseEntity.ok().build();
        });
    }

    @GetMapping("/poll-status/{id}")
    public ResponseEntity<?> pollStatus(@PathVariable String id) {
        return this.withAuthorizedUser((user) -> {
            Optional<Invitation> maybeInvitation = this.invitationRepository.findById(id);

            if (maybeInvitation.isEmpty()) {
                // check if user was picked up into a family with
                // the invitation record already being deleted
                if (user.getFamilyId() != null) {
                    return ResponseEntity.ok(Map.of("status", POLL_STATUS_ACCEPTED));
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            } else {
                Invitation invitation = maybeInvitation.get();

                // a user should not be permitted to have insight into
                // an invitation, that they don't play a role in
                if (!user.getId().equals(invitation.getRedeemerId())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }

                String status = switch (invitation.getRedeemStatus()) {
                    case Invitation.REDEEM_STATUS_ACCEPTED -> POLL_STATUS_ACCEPTED;
                    case Invitation.REDEEM_STATUS_PENDING -> POLL_STATUS_PENDING;
                    default -> POLL_STATUS_DENIED;
                };

                return ResponseEntity.ok(Map.of("status", status));
            }
        });
    }
}
