package com.runicrealms.runicguilds.guilds;

import java.util.List;

public class Guild {
	
	private List<GuildMember> members;
	private GuildMember owner;
	
	public Guild(List<GuildMember> members, GuildMember owner) {
		this.members = members;
		this.owner = owner;
	}
	
	public List<GuildMember> getMembers() {
		return this.members;
	}
	
	public GuildMember getOwner() {
		return this.owner;
	}
	
}
