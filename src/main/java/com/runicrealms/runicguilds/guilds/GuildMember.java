package com.runicrealms.runicguilds.guilds;

import java.util.UUID;

public class GuildMember {
	
	private UUID uuid;
	private GuildRank rank;
	private Integer score;
	
	public GuildMember(UUID uuid, GuildRank rank, Integer score) {
		this.uuid = uuid;
		this.rank = rank;
		this.score = score;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	public GuildRank getRank() {
		return this.rank;
	}
	
	public Integer getScore() {
		return this.score;
	}
	
}