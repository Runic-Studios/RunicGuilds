package com.runicrealms.runicguilds.model;

public enum GuildDataField {

    EXP("exp"),
    GUILD_UUID("guildUuid"),
    NAME("name"),
    PREFIX("prefix"),
    RANK("rank"),
    SCORE("score");

    private final String field;

    GuildDataField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
