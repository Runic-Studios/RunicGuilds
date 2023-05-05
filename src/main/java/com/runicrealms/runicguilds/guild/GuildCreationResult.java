package com.runicrealms.runicguilds.guild;

public enum GuildCreationResult {

    CREATOR_IN_GUILD("You are already in a guild! Leave it in order to create a new guild."),
    PREFIX_NOT_UNIQUE("There is already a guild with that prefix! Try using a different prefix."),
    NAME_NOT_UNIQUE("There is already a guild with that name! Try using a different name."),
    BAD_PREFIX("The guild prefix must be composed of 3-4 english characters only."),
    SUCCESSFUL("You have successfully created your guild! You are now the guild master."),
    INTERNAL_ERROR("There was an internal error with creating your guild."),
    NAME_TOO_LONG("That name is too long, the max is 16 characters."),
    INAPPROPRIATE_CONTENT("That name or prefix contains inappropriate content.");

    private final String message;

    private GuildCreationResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
