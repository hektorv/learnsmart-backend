package com.learnsmart.content.exception;

import java.util.UUID;

/**
 * Exception thrown when a Domain is not found by its ID.
 * Mapped to HTTP 404 NOT FOUND.
 */
public class DomainNotFoundException extends RuntimeException {

    public DomainNotFoundException(UUID domainId) {
        super("Domain not found: " + domainId);
    }

    public DomainNotFoundException(String message) {
        super(message);
    }
}
