package com.runicrealms.runicguilds.guilds;

import java.util.List;

public class Guild {
	
	private List<GuildMember> members;
	private GuildMember owner;
	private String guildName;
	private String guildPrefix;
	
	public Guild(List<GuildMember> members, GuildMember owner, String guildName, String guildPrefix) {
		this.members = members;
		this.owner = owner;
		this.guildName = guildName;
		this.guildPrefix = guildPrefix;
	}
	
	public List<GuildMember> getMembers() {
		return this.members;
	}
	
	public GuildMember getOwner() {
		return this.owner;
	}

	public String getGuildName() {
		return this.guildName;
	}

	public String getGuildPrefix() {
		return this.guildPrefix;
	}
	
}
