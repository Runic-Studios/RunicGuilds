package com.runicrealms.runicguilds.model;

import java.util.UUID;

public class GuildUUID {
    private final UUID uuid;

    public GuildUUID(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }

        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }
}
