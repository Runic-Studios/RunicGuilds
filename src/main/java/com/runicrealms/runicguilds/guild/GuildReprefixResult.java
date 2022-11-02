package com.runicrealms.runicguilds.guild;

public enum GuildReprefixResult {

    PREFIX_NOT_UNIQUE("There is already a guild with that prefix! Try using a different prefix."),
    BAD_PREFIX("The guild prefix must be composed of 3-4 english characters only."),
    SUCCESSFUL("You have successfully changed the guild prefix."),
    INTERNAL_ERROR("There was an internal error with creating the guild.");

    private final String message;

    GuildReprefixResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
