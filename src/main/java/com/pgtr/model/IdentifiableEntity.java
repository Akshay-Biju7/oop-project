package com.pgtr.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base abstract class demonstrating OOP Abstraction and Inheritance for all domain entities.
 */
public abstract class IdentifiableEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected LocalDateTime createdAt;

    public IdentifiableEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public IdentifiableEntity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentifiableEntity that = (IdentifiableEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
