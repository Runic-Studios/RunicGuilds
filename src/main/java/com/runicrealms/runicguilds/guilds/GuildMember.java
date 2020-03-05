package com.runicrealms.runicguilds.guilds;

import java.util.UUID;

public class GuildMember {
	
	private UUID uuid;
	private GuildRank rank;
	private Integer score;
	private String name;
	
	public GuildMember(UUID uuid, GuildRank rank, Integer score, String name) {
		this.uuid = uuid;
		this.rank = rank;
		this.score = score;
		this.name = name;
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

	public void setRank(GuildRank rank) {
		this.rank = rank;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getLastKnownName() {
		return this.name;
	}
	
}