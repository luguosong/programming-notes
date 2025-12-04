package com.luguosong.ssiach11ex4.model;

import java.util.Objects;

public class Document {

    private String owner;

    public Document(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(owner, document.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner);
    }
}
