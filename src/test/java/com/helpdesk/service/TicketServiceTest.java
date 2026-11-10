package com.helpdesk.service;

import com.helpdesk.entity.Priority;
import com.helpdesk.entity.Ticket;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {

        Page<Ticket> emptyPage = new PageImpl<>(List.of());

        when(ticketRepository.findAll(any(Pageable.class)))
                .thenReturn(emptyPage);

        when(ticketRepository.findByPriority(any(Priority.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        when(ticketRepository.findByStatusIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        when(ticketRepository.findByStatusIgnoreCaseAndPriority(
                anyString(), any(Priority.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        when(ticketRepository.findByCreatedBy_Email(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        when(ticketRepository.findByCreatedBy_EmailAndStatusIgnoreCase(
                anyString(), anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        when(ticketRepository.findByCreatedBy_EmailAndPriority(
                anyString(), any(Priority.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        when(ticketRepository.findByCreatedBy_EmailAndStatusIgnoreCaseAndPriority(
                anyString(), anyString(), any(Priority.class), any(Pageable.class)))
                .thenReturn(emptyPage);
    }

    private void authenticateAs(String email, String role) {

        var authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ADMIN TESTS

    @Test
    void adminShouldSeeAllTickets() {

        authenticateAs("john@gmail.com", "ADMIN");

        ticketService.searchTickets(null, null, pageable);

        verify(ticketRepository).findAll(pageable);
    }

    @Test
    void adminShouldFilterTicketsByPriority() {

        authenticateAs("john@gmail.com", "ADMIN");

        ticketService.searchTickets(null, Priority.HIGH, pageable);

        verify(ticketRepository).findByPriority(Priority.HIGH, pageable);
    }

    // USER TESTS

    @Test
    void userShouldSeeOnlyTheirOwnTickets() {

        authenticateAs("sarah@gmail.com", "USER");

        ticketService.searchTickets(null, null, pageable);

        verify(ticketRepository).findByCreatedBy_Email("sarah@gmail.com", pageable);
    }

    @Test
    void userShouldFilterOwnTicketsByStatus() {

        authenticateAs("sarah@gmail.com", "USER");

        ticketService.searchTickets("OPEN", null, pageable);

        verify(ticketRepository).findByCreatedBy_EmailAndStatusIgnoreCase(
                "sarah@gmail.com", "OPEN", pageable
        );
    }

    @Test
    void userShouldFilterOwnTicketsByPriority() {

        authenticateAs("sarah@gmail.com", "USER");

        ticketService.searchTickets(null, Priority.HIGH, pageable);

        verify(ticketRepository).findByCreatedBy_EmailAndPriority(
                "sarah@gmail.com", Priority.HIGH, pageable
        );
    }

    @Test
    void userShouldFilterOwnTicketsByStatusAndPriority() {

        authenticateAs("sarah@gmail.com", "USER");

        ticketService.searchTickets("OPEN", Priority.HIGH, pageable);

        verify(ticketRepository).findByCreatedBy_EmailAndStatusIgnoreCaseAndPriority(
                "sarah@gmail.com", "OPEN", Priority.HIGH, pageable
        );
    }
}