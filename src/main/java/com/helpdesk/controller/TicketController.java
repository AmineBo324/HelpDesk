
package com.helpdesk.controller;

import com.helpdesk.dto.CreateTicketRequest;
import com.helpdesk.dto.UpdateTicketRequest;
import com.helpdesk.dto.TicketResponse;
import com.helpdesk.service.TicketService;

import jakarta.validation.Valid;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import com.helpdesk.entity.Priority;


@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // GET all tickets
    
    @GetMapping
    public ResponseEntity<Page<TicketResponse>> getAllTickets(

            @RequestParam(required = false)
            String status,

            @RequestParam(required = false)
            Priority priority,

            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                ticketService.searchTickets(
                        status,
                        priority,
                        pageable
                )
        );
    }


    // GET ticket by ID
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ticketService.getTicketById(id)
        );
    }

    // CREATE ticket
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {

        TicketResponse response = ticketService.createTicket(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // UPDATE ticket
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request) {

        return ResponseEntity.ok(
                ticketService.updateTicket(id, request)
        );
    }

    // DELETE ticket
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id) {

        ticketService.deleteTicket(id);

        return ResponseEntity.noContent().build();
    }
}
