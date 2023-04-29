package com.runicrealms.runicguilds.model;

public enum GuildDataField {

    EXP("exp"),
    GUILD_UUID("guildUUID"),
    NAME("name"),
    PREFIX("prefix"),
    RANK("rank"),
    SCORE("score"),
    UUID("uuid"); // Used for member data

    private final String field;

    GuildDataField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
