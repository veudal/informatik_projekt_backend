package com.uni.informatik_projekt_backend.controller;

import com.uni.informatik_projekt_backend.model.User;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyMembersTests {
    @Mock private UserRepository userRepository;

    @InjectMocks private ApiController controller;

    private UUID familyId;

    @BeforeEach
    void setUp() {
        familyId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("member@example.com", null));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsMembersOfOwnFamily() {
        User member = user(familyId);
        member.setDisplayName("Mia");
        member.setBirthDate(LocalDate.of(2012, 3, 14));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user(familyId)));
        when(userRepository.findByFamilyId(familyId)).thenReturn(List.of(member));

        assertEquals(List.of(member), controller.getUsersByFamily(familyId));
    }

    @Test
    void rejectsMembersOfOtherFamily() {
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(user(familyId)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.getUsersByFamily(UUID.randomUUID()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(userRepository, never()).findByFamilyId(any());
    }

    private User user(UUID userFamilyId) {
        User user = new User();
        user.setFamilyId(userFamilyId);
        return user;
    }
}
