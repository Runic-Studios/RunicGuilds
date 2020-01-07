package com.runicrealms.runicguilds.result;

public enum GuildPromotionResult {
	
	MEMBER_IS_OWNER("You cannot promote the guild owner! To transfer ownership, use the command /guild transfer."),
	MEMBER_IS_OFFICER("You cannot promote an officer! To transfer ownership, use the command /guild transfer."),
	SUCCESSFUL("You have successfully promote that guild member.");
	
	private String message;
	
	private GuildPromotionResult(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
	
}
