package com.runicrealms.runicguilds.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GuildCommandMapManager {
    private static final Map<UUID, UUID> transferOwnership = new HashMap<>();
    private static final Map<UUID, UUID> invites = new HashMap<>();
    private static final Set<UUID> disbanding = new HashSet<>();

    public static Map<UUID, UUID> getTransferOwnership() {
        return transferOwnership;
    }

    public static Map<UUID, UUID> getInvites() {
        return invites;
    }

    public static Set<UUID> getDisbanding() {
        return disbanding;
    }
}
