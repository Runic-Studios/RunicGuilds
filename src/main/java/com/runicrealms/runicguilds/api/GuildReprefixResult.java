package com.runicrealms.runicguilds.api;

public enum GuildReprefixResult {

    PREFIX_NOT_UNIQUE("There is already a guild with that prefix! Try using a different prefix."),
    BAD_PREFIX("The guild prefix must be composed of 3-6 english characters only."),
    SUCCESSFUL("You have successfully changed your guild prefix."),
    INTERNAL_ERROR("There was an internal error with creating your guild.");

    private String message;

    private GuildReprefixResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
