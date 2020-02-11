package com.runicrealms.runicguilds.api;

public enum GuildCreationResult {
	
	CREATOR_IN_GUILD("You are already in a guild! Leave it in order to create a new guild."),
	PREFIX_NOT_UNIQUE("There is already a guild with that prefix! Try using a different prefix."),
	NAME_NOT_UNIQUE("There is already a guild with that name! Try using a different name."),
	BAD_PREFIX("The guild prefix must be composed of three english characters only."),
	SUCCESSFUL("You have successfully created your guild.");
	
	private String message;
	
	private GuildCreationResult(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
	
}