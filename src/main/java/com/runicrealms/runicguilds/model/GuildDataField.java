package com.runicrealms.runicguilds.model;

public enum GuildDataField {

    EXP("exp"),
    NAME("name"),
    PREFIX("prefix"),
    RANK("rank"),
    SCORE("score"),
    UUID("guildUuid");

    private final String field;

    GuildDataField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
