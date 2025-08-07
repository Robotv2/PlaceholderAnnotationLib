package fr.robotv2.placeholderannotationlib.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import fr.robotv2.placeholderannotationlib.annotations.Cache;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public enum CacheSystem {

    INSTANCE;

    private final ScheduledExecutorService CACHE_POOL = Executors.newSingleThreadScheduledExecutor();
    private final Table<UUID, String, String> cache = Tables.synchronizedTable(HashBasedTable.create());

    public boolean isCached(UUID uuid, String placeholder) {
        return getCache(uuid, placeholder) != null;
    }

    public String getCache(UUID uuid, String placeholder) {
        return this.cache.get(uuid, placeholder);
    }

    public void cache(UUID uuid, String placeholder, String result, Cache cache) {
        this.cache.put(uuid, placeholder, result);
        this.CACHE_POOL.schedule(() -> this.cache.remove(uuid, placeholder), cache.value(), cache.unit());
    }
}
