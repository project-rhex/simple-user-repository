package org.mitre.openid.connect.repository;

public enum SortBy {
    USERNAME ("users.sort_by_username"),
    FIRST_NAME ("users.by_first_name"),
    LAST_NAME ("users.by_last_name"),
    EMAIL ("users.by_email");

    private final String namedQuery;

    private SortBy(String namedQuery) {
        this.namedQuery = namedQuery;
    }

    public String getNamedQuery() {
        return namedQuery;
    }
}