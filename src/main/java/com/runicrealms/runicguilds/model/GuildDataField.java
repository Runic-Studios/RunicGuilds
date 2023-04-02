package com.runicrealms.runicguilds.model;

public enum GuildDataField {

    UUID("guildUuid");

    private final String field;

    GuildDataField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
