
package com.helpdesk.service;

import com.helpdesk.dto.CreateTicketRequest;
import com.helpdesk.dto.UpdateTicketRequest;
import com.helpdesk.dto.TicketResponse;
import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.Category;
import com.helpdesk.entity.Priority;
import com.helpdesk.exception.TicketNotFoundException;
import com.helpdesk.repository.CategoryRepository;
import com.helpdesk.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.helpdesk.entity.AppUser;
import com.helpdesk.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final CategoryRepository categoryRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketService(
            TicketRepository ticketRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // GET all tickets
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET ticket by ID
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        return mapToResponse(ticket);
    }

    // CREATE ticket
    public TicketResponse createTicket(CreateTicketRequest request) {

        // Get the authenticated user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        // Retrieve the user from PostgreSQL
        AppUser currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found")
                );

        // Create ticket
        Ticket ticket = new Ticket();

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setStatus("OPEN");

        // Set category
        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found")
                );

        ticket.setCategory(category);

        // Associate ticket with its creator
        ticket.setCreatedBy(currentUser);

        Ticket savedTicket = ticketRepository.save(ticket);

        return mapToResponse(savedTicket);
    }

    // UPDATE ticket
    @Transactional
    public TicketResponse updateTicket(
            Long id,
            UpdateTicketRequest request) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new TicketNotFoundException(id)
                );

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        String currentEmail = authentication.getName();

        boolean isOwner = ticket.getCreatedBy() != null
                && ticket.getCreatedBy().getEmail().equals(currentEmail);

        // Only ADMIN or ticket creator can update
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You are not allowed to update this ticket"
            );
        }

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        Ticket updatedTicket = ticketRepository.save(ticket);

        return mapToResponse(updatedTicket);
    }

    // DELETE ticket
    public void deleteTicket(Long id) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        ticketRepository.delete(ticket);
    }


    // SEARCH tickets
    @Transactional(readOnly = true)
        public Page<TicketResponse> searchTickets(
                String status,
                Priority priority,
                Pageable pageable
        ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        Page<Ticket> tickets;

        if (isAdmin) {

                // ADMIN can access all tickets
                if (status != null && priority != null) {
                tickets = ticketRepository
                        .findByStatusIgnoreCaseAndPriority(
                                status, priority, pageable);

                } else if (status != null) {
                tickets = ticketRepository
                        .findByStatusIgnoreCase(status, pageable);

                } else if (priority != null) {
                tickets = ticketRepository
                        .findByPriority(priority, pageable);

                } else {
                tickets = ticketRepository.findAll(pageable);
                }

        } else {

                // USER can access only their own tickets
                if (status != null && priority != null) {
                tickets = ticketRepository
                        .findByCreatedBy_EmailAndStatusIgnoreCaseAndPriority(
                                email, status, priority, pageable);

                } else if (status != null) {
                tickets = ticketRepository
                        .findByCreatedBy_EmailAndStatusIgnoreCase(
                                email, status, pageable);

                } else if (priority != null) {
                tickets = ticketRepository
                        .findByCreatedBy_EmailAndPriority(
                                email, priority, pageable);

                } else {
                tickets = ticketRepository
                        .findByCreatedBy_Email(email, pageable);
                }
        }

        return tickets.map(this::mapToResponse);
        }



    // Convert Entity to DTO
    private TicketResponse mapToResponse(Ticket ticket) {

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCategory() != null
                        ? ticket.getCategory().getId()
                        : null,
                ticket.getCategory() != null
                        ? ticket.getCategory().getName()
                        : null
        );
    }
}
