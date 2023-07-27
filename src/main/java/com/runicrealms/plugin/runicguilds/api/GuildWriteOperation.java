package com.runicrealms.plugin.runicguilds.api;

import com.runicrealms.plugin.rdb.api.WriteCallback;

import java.util.UUID;

public interface GuildWriteOperation {

    /**
     * Updates a single field of the mapped 'GuildData' document object
     *
     * @param guildUUID of the GUILD
     * @param fieldName of the document "memberDataMap"
     * @param newValue  the new value for the field
     * @param <T>       the type of object to set as the field value "memberDataMap"
     */
    <T> void updateGuildData(UUID guildUUID, String fieldName, T newValue, WriteCallback callback);

}
