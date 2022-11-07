package com.runicrealms.runicguilds.model;

import java.util.ArrayList;
import java.util.List;

public enum GuildDataField {

    PREFIX("prefix"),
    BANK_SIZE("bank-size"),
    GUILD_BANNER("guild-banner"),
    GUILD_EXP("guild-exp"),
    GUILD_NAME("name"),
    GUILD_SCORE("score"),
    MEMBER_UUID("uuid"),
    MEMBER_RANK("rank"),
    MEMBER_SCORE("score");

    public static final List<String> FIELD_STRINGS;

    static {
        FIELD_STRINGS = new ArrayList<>();
        for (GuildDataField guildDataField : GuildDataField.values())
            FIELD_STRINGS.add(guildDataField.getField());
    }

    private final String field;

    GuildDataField(String field) {
        this.field = field;
    }

    /**
     * Returns the corresponding RedisField from the given string version
     *
     * @param field a string matching a constant
     * @return the constant
     */
    public static GuildDataField getFromFieldString(String field) {
        for (GuildDataField guildDataField : GuildDataField.values()) {
            if (guildDataField.getField().equalsIgnoreCase(field))
                return guildDataField;
        }
        return null;
    }

    public String getField() {
        return field;
    }
}
