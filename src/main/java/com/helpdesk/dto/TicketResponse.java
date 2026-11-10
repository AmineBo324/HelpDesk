
package com.helpdesk.dto;

import com.helpdesk.entity.Priority;

public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private String status;

    private Long categoryId;
    private String categoryName;

    public TicketResponse(
            Long id,
            String title,
            String description,
            Priority priority,
            String status,
            Long categoryId,
            String categoryName) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
