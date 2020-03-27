package com.runicrealms.runicguilds.api;

public enum GuildRenameResult {

    NAME_NOT_UNIQUE("There is already a guild with that name! Try using a different name."),
    SUCCESSFUL("You have successfully changed your guild name."),
    INTERNAL_ERROR("There was an internal error with creating your guild."),
    NAME_TOO_LONG("That name is too long, the max is 16 characters.");

    private String message;

    private GuildRenameResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
