
package com.helpdesk.repository;

import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // ADMIN: retrieve all tickets with filters
    Page<Ticket> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<Ticket> findByPriority(Priority priority, Pageable pageable);

    Page<Ticket> findByStatusIgnoreCaseAndPriority(
            String status,
            Priority priority,
            Pageable pageable
    );

    // USER: retrieve only tickets created by this user
    Page<Ticket> findByCreatedBy_Email(
            String email,
            Pageable pageable
    );

    Page<Ticket> findByCreatedBy_EmailAndStatusIgnoreCase(
            String email,
            String status,
            Pageable pageable
    );

    Page<Ticket> findByCreatedBy_EmailAndPriority(
            String email,
            Priority priority,
            Pageable pageable
    );

    Page<Ticket> findByCreatedBy_EmailAndStatusIgnoreCaseAndPriority(
            String email,
            String status,
            Priority priority,
            Pageable pageable
    );
}
