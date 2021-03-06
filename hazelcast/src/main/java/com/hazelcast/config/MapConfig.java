/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.config;

import com.hazelcast.map.eviction.LFUEvictionPolicy;
import com.hazelcast.map.eviction.LRUEvictionPolicy;
import com.hazelcast.map.eviction.MapEvictionPolicy;
import com.hazelcast.map.eviction.RandomEvictionPolicy;
import com.hazelcast.map.merge.PutIfAbsentMapMergePolicy;
import com.hazelcast.spi.partition.IPartition;

import java.util.ArrayList;
import java.util.List;

import static com.hazelcast.util.Preconditions.checkAsyncBackupCount;
import static com.hazelcast.util.Preconditions.checkBackupCount;
import static com.hazelcast.util.Preconditions.checkFalse;
import static com.hazelcast.util.Preconditions.checkNotNull;
import static com.hazelcast.util.Preconditions.isNotNull;

/**
 * Contains the configuration for an {@link com.hazelcast.core.IMap}.
 */
public class MapConfig {

    /**
     * The number of minimum backup counter
     */
    public static final int MIN_BACKUP_COUNT = 0;
    /**
     * The number of default backup counter
     */
    public static final int DEFAULT_BACKUP_COUNT = 1;
    /**
     * The number of maximum backup counter
     */
    public static final int MAX_BACKUP_COUNT = IPartition.MAX_BACKUP_COUNT;

    /**
     * The number of minimum eviction percentage
     */
    public static final int MIN_EVICTION_PERCENTAGE = 0;
    /**
     * The number of default eviction percentage
     */
    public static final int DEFAULT_EVICTION_PERCENTAGE = 25;
    /**
     * The number of maximum eviction percentage
     */
    public static final int MAX_EVICTION_PERCENTAGE = 100;

    /**
     * Minimum time in milliseconds which should pass before asking
     * if a partition of this map is evictable or not.
     */
    public static final long DEFAULT_MIN_EVICTION_CHECK_MILLIS = 100L;

    /**
     * The number of default Time to Live in seconds
     */
    public static final int DEFAULT_TTL_SECONDS = 0;

    /**
     * The number of default time to wait eviction in seconds
     */
    public static final int DEFAULT_MAX_IDLE_SECONDS = 0;

    /**
     * Default policy for eviction
     */
    public static final EvictionPolicy DEFAULT_EVICTION_POLICY = EvictionPolicy.NONE;

    /**
     * Default policy for merging
     */
    public static final String DEFAULT_MAP_MERGE_POLICY = PutIfAbsentMapMergePolicy.class.getName();
    /**
     * Default In-Memory format is binary
     */
    public static final InMemoryFormat DEFAULT_IN_MEMORY_FORMAT = InMemoryFormat.BINARY;

    /**
     * We want to cache values only when an index is defined.
     */
    public static final CacheDeserializedValues DEFAULT_CACHED_DESERIALIZED_VALUES = CacheDeserializedValues.INDEX_ONLY;

    private String name;

    private int backupCount = DEFAULT_BACKUP_COUNT;

    private int asyncBackupCount = MIN_BACKUP_COUNT;

    private int evictionPercentage = DEFAULT_EVICTION_PERCENTAGE;

    private long minEvictionCheckMillis = DEFAULT_MIN_EVICTION_CHECK_MILLIS;

    private int timeToLiveSeconds = DEFAULT_TTL_SECONDS;

    private int maxIdleSeconds = DEFAULT_MAX_IDLE_SECONDS;

    private MaxSizeConfig maxSizeConfig = new MaxSizeConfig();

    private EvictionPolicy evictionPolicy = DEFAULT_EVICTION_POLICY;

    private MapEvictionPolicy mapEvictionPolicy;

    private MapStoreConfig mapStoreConfig = new MapStoreConfig().setEnabled(false);

    private NearCacheConfig nearCacheConfig;

    private boolean readBackupData;

    private CacheDeserializedValues cacheDeserializedValues = DEFAULT_CACHED_DESERIALIZED_VALUES;

    private String mergePolicy = DEFAULT_MAP_MERGE_POLICY;

    private InMemoryFormat inMemoryFormat = DEFAULT_IN_MEMORY_FORMAT;

    private WanReplicationRef wanReplicationRef;

    private List<EntryListenerConfig> entryListenerConfigs;

    private List<MapPartitionLostListenerConfig> partitionLostListenerConfigs;

    private List<MapIndexConfig> mapIndexConfigs;

    private List<MapAttributeConfig> mapAttributeConfigs;

    private List<QueryCacheConfig> queryCacheConfigs;

    private boolean statisticsEnabled = true;

    private PartitioningStrategyConfig partitioningStrategyConfig;

    private String quorumName;

    private HotRestartConfig hotRestartConfig = new HotRestartConfig();

    private MapConfigReadOnly readOnly;

    // we use these 2 flags to detect a conflict between (deprecated) #setOptimizeQueries()
    // and #setCacheDeserializedValues()
    private boolean optimizeQueryExplicitlyInvoked;
    private boolean setCacheDeserializedValuesExplicitlyInvoked;


    public MapConfig(String name) {
        this.name = name;
    }

    public MapConfig() {
    }

    public MapConfig(MapConfig config) {
        this.name = config.name;
        this.backupCount = config.backupCount;
        this.asyncBackupCount = config.asyncBackupCount;
        this.evictionPercentage = config.evictionPercentage;
        this.minEvictionCheckMillis = config.minEvictionCheckMillis;
        this.timeToLiveSeconds = config.timeToLiveSeconds;
        this.maxIdleSeconds = config.maxIdleSeconds;
        this.maxSizeConfig = config.maxSizeConfig != null ? new MaxSizeConfig(config.maxSizeConfig) : null;
        this.evictionPolicy = config.evictionPolicy;
        this.mapEvictionPolicy = config.mapEvictionPolicy;
        this.inMemoryFormat = config.inMemoryFormat;
        this.mapStoreConfig = config.mapStoreConfig != null ? new MapStoreConfig(config.mapStoreConfig) : null;
        this.nearCacheConfig = config.nearCacheConfig != null ? new NearCacheConfig(config.nearCacheConfig) : null;
        this.readBackupData = config.readBackupData;
        this.cacheDeserializedValues = config.cacheDeserializedValues;
        this.statisticsEnabled = config.statisticsEnabled;
        this.mergePolicy = config.mergePolicy;
        this.wanReplicationRef = config.wanReplicationRef != null ? new WanReplicationRef(config.wanReplicationRef) : null;
        this.entryListenerConfigs = new ArrayList<EntryListenerConfig>(config.getEntryListenerConfigs());
        this.partitionLostListenerConfigs =
                new ArrayList<MapPartitionLostListenerConfig>(config.getPartitionLostListenerConfigs());
        this.mapIndexConfigs = new ArrayList<MapIndexConfig>(config.getMapIndexConfigs());
        this.mapAttributeConfigs = new ArrayList<MapAttributeConfig>(config.getMapAttributeConfigs());
        this.queryCacheConfigs = new ArrayList<QueryCacheConfig>(config.getQueryCacheConfigs());
        this.partitioningStrategyConfig = config.partitioningStrategyConfig != null
                ? new PartitioningStrategyConfig(config.getPartitioningStrategyConfig()) : null;
        this.quorumName = config.quorumName;
        this.hotRestartConfig = new HotRestartConfig(config.hotRestartConfig);
    }

    /**
     * Gets immutable version of this configuration.
     *
     * @return Immutable version of this configuration.
     * @deprecated this method will be removed in 4.0; it is meant for internal usage only.
     */
    public MapConfigReadOnly getAsReadOnly() {
        if (readOnly == null) {
            readOnly = new MapConfigReadOnly(this);
        }
        return readOnly;
    }

    /**
     * Returns the name of this {@link com.hazelcast.core.IMap}
     *
     * @return the name of the {@link com.hazelcast.core.IMap}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the {@link com.hazelcast.core.IMap}
     *
     * @param name the name to set for this {@link com.hazelcast.core.IMap}
     */
    public MapConfig setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the data type that will be used for storing records.
     *
     * @return data type that will be used for storing records.
     */
    public InMemoryFormat getInMemoryFormat() {
        return inMemoryFormat;
    }

    /**
     * Binary type that will be used for storing records.
     * Possible values:
     * BINARY (default): keys and values will be stored as binary data
     * OBJECT : values will be stored in their object forms
     * NATIVE : values will be stored in non-heap region of JVM
     *
     * @param inMemoryFormat the record type to set for this {@link com.hazelcast.core.IMap}
     * @throws IllegalArgumentException if inMemoryFormat is null.
     */
    public MapConfig setInMemoryFormat(InMemoryFormat inMemoryFormat) {
        this.inMemoryFormat = isNotNull(inMemoryFormat, "inMemoryFormat");
        return this;
    }

    /**
     * Returns the backupCount for this {@link com.hazelcast.core.IMap}
     *
     * @return the backupCount for this {@link com.hazelcast.core.IMap}
     * @see #getAsyncBackupCount()
     */
    public int getBackupCount() {
        return backupCount;
    }

    /**
     * Number of synchronous backups. For example, if 1 is set as the backup count,
     * then all entries of the map will be copied to another JVM for
     * fail-safety. 0 means no sync backup.
     *
     * @param backupCount the number of synchronous backups to set for this {@link com.hazelcast.core.IMap}
     * @see #setAsyncBackupCount(int)
     */
    public MapConfig setBackupCount(final int backupCount) {
        this.backupCount = checkBackupCount(backupCount, asyncBackupCount);
        return this;
    }

    /**
     * Returns the asynchronous backup count for this {@link com.hazelcast.core.IMap}.
     *
     * @return the asynchronous backup count
     * @see #setBackupCount(int)
     */
    public int getAsyncBackupCount() {
        return asyncBackupCount;
    }

    /**
     * Sets the number of asynchronous backups. 0 means no backups.
     *
     * @param asyncBackupCount the number of asynchronous synchronous backups to set
     * @return the updated CacheConfig
     * @throws IllegalArgumentException if asyncBackupCount smaller than 0,
     *                                  or larger than the maximum number of backup
     *                                  or the sum of the backups and async backups is larger than the maximum number of backups
     * @see #setBackupCount(int)
     * @see #getAsyncBackupCount()
     */
    public MapConfig setAsyncBackupCount(int asyncBackupCount) {
        this.asyncBackupCount = checkAsyncBackupCount(backupCount, asyncBackupCount);
        return this;
    }

    /**
     * Returns the total number of backups: backupCount plus asyncBackupCount.
     *
     * @return the total number of backups: synchronous + asynchronous
     */
    public int getTotalBackupCount() {
        return backupCount + asyncBackupCount;
    }

    /**
     * Returns the evictionPercentage: specified percentage of the map to be evicted.
     *
     * @return the evictionPercentage: specified percentage of the map to be evicted
     * @deprecated As of version 3.7, eviction mechanism changed.
     * It uses a probabilistic algorithm based on sampling. Please see documentation for further details.
     */
    @Deprecated
    public int getEvictionPercentage() {
        return evictionPercentage;
    }

    /**
     * When maximum size is reached, the specified percentage of the map will be evicted.
     * Any integer between 0 and 100 is allowed.
     * For example, if 25 is set, 25% of the entries will be evicted.
     *
     * Beware that eviction mechanism is different for NATIVE in-memory format (It uses a probabilistic algorithm
     * based on sampling. Please see documentation for further details) and this parameter has no effect.
     *
     * @param evictionPercentage the evictionPercentage to set: the specified percentage of the map to be evicted
     * @throws IllegalArgumentException if evictionPercentage is not in the 0-100 range.
     * @deprecated As of version 3.7, eviction mechanism changed.
     * It uses a probabilistic algorithm based on sampling. Please see documentation for further details.
     */
    public MapConfig setEvictionPercentage(final int evictionPercentage) {
        if (evictionPercentage < MIN_EVICTION_PERCENTAGE) {
            throw new IllegalArgumentException("eviction percentage must be greater or equal than 0");
        }
        if (evictionPercentage > MAX_EVICTION_PERCENTAGE) {
            throw new IllegalArgumentException("eviction percentage must be smaller or equal than 100");
        }
        this.evictionPercentage = evictionPercentage;
        return this;
    }

    /**
     * Returns the minimum milliseconds which should pass before asking if a partition of this map is evictable or not.
     *
     * Default value is {@value #DEFAULT_MIN_EVICTION_CHECK_MILLIS} milliseconds.
     *
     * @return number of milliseconds that should pass before asking for the next eviction.
     * @since 3.3
     * @deprecated As of version 3.7, eviction mechanism changed.
     * It uses a probabilistic algorithm based on sampling. Please see documentation for further details.
     */
    public long getMinEvictionCheckMillis() {
        return minEvictionCheckMillis;
    }

    /**
     * Sets the minimum time in milliseconds which should pass before asking if a partition of this map is evictable or not.
     *
     * Default value is {@value #DEFAULT_MIN_EVICTION_CHECK_MILLIS} milliseconds.
     *
     * Beware that eviction mechanism is different for NATIVE in-memory format (It uses a probabilistic algorithm
     * based on sampling. Please see documentation for further details) and this parameter has no effect.
     *
     * @param minEvictionCheckMillis time in milliseconds that should pass before asking for the next eviction
     * @since 3.3
     * @deprecated As of version 3.7, eviction mechanism changed.
     * It uses a probabilistic algorithm based on sampling. Please see documentation for further details.
     */
    public MapConfig setMinEvictionCheckMillis(long minEvictionCheckMillis) {
        if (minEvictionCheckMillis < 0) {
            throw new IllegalArgumentException("Parameter minEvictionCheckMillis can not get a negative value");
        }
        this.minEvictionCheckMillis = minEvictionCheckMillis;
        return this;
    }

    /**
     * Returns the maximum number of seconds for each entry to stay in the map.
     *
     * @return the maximum number of seconds for each entry to stay in the map
     */
    public int getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    /**
     * The maximum number of seconds for each entry to stay in the map. Entries that are
     * older than timeToLiveSeconds will be automatically evicted from the map.
     * Updates on the entry do not change the eviction time.
     * Any integer between 0 and Integer.MAX_VALUE.
     * 0 means infinite. Default is 0.
     *
     * @param timeToLiveSeconds the timeToLiveSeconds to set
     */
    public MapConfig setTimeToLiveSeconds(int timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
        return this;
    }

    /**
     * Returns the maximum number of seconds for each entry to stay idle in the map.
     *
     * @return the maximum number of seconds for each entry to stay idle in the map
     */
    public int getMaxIdleSeconds() {
        return maxIdleSeconds;
    }

    /**
     * Maximum number of seconds for each entry to stay idle in the map. Entries that are
     * idle (not touched) for more than {@code maxIdleSeconds} will get automatically evicted from the map.
     * Entry is touched if {@code get()}, {@code getAll()}, {@code put()} or {@code containsKey()} is called.
     * Any integer between {@code 0} and {@code Integer.MAX_VALUE}.
     * {@code 0} means infinite. Default is {@code 0}.
     *
     * @param maxIdleSeconds the maxIdleSeconds (the maximum number of seconds for each entry to stay idle in the map) to set
     */
    public MapConfig setMaxIdleSeconds(int maxIdleSeconds) {
        this.maxIdleSeconds = maxIdleSeconds;
        return this;
    }

    public MaxSizeConfig getMaxSizeConfig() {
        return maxSizeConfig;
    }

    public MapConfig setMaxSizeConfig(MaxSizeConfig maxSizeConfig) {
        this.maxSizeConfig = maxSizeConfig;
        return this;
    }

    /**
     * Returns the {@link EvictionPolicy}.
     *
     * @return the evictionPolicy
     */
    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }

    /**
     * Sets the {@link EvictionPolicy}.
     *
     * @param evictionPolicy the evictionPolicy to set
     */
    public MapConfig setEvictionPolicy(EvictionPolicy evictionPolicy) {
        this.evictionPolicy = checkNotNull(evictionPolicy, "evictionPolicy cannot be null");
        this.mapEvictionPolicy = findMatchingMapEvictionPolicy(evictionPolicy);
        return this;
    }

    private static MapEvictionPolicy findMatchingMapEvictionPolicy(EvictionPolicy evictionPolicy) {
        switch (evictionPolicy) {
            case LRU:
                return LRUEvictionPolicy.INSTANCE;
            case LFU:
                return LFUEvictionPolicy.INSTANCE;
            case RANDOM:
                return RandomEvictionPolicy.INSTANCE;
            case NONE:
                return null;
            default:
                throw new IllegalArgumentException("Not known eviction policy: " + evictionPolicy);
        }
    }

    /**
     * Returns custom eviction policy if it is set otherwise returns null.
     *
     * @return custom eviction policy or null.
     */
    public MapEvictionPolicy getMapEvictionPolicy() {
        return mapEvictionPolicy;
    }

    /**
     * Sets custom eviction policy implementation for this map.
     *
     * Internal eviction algorithm finds most appropriate entry to evict from this map by using supplied policy.
     *
     * @param mapEvictionPolicy custom eviction policy implementation
     * @return the updated map configuration
     */
    public MapConfig setMapEvictionPolicy(MapEvictionPolicy mapEvictionPolicy) {
        this.mapEvictionPolicy = checkNotNull(mapEvictionPolicy, "mapEvictionPolicy cannot be null");
        return this;
    }

    /**
     * Returns the map store configuration
     *
     * @return the mapStoreConfig (map store configuration)
     */
    public MapStoreConfig getMapStoreConfig() {
        return mapStoreConfig;
    }

    /**
     * Sets the map store configuration
     *
     * @param mapStoreConfig the mapStoreConfig (map store configuration) to set
     */
    public MapConfig setMapStoreConfig(MapStoreConfig mapStoreConfig) {
        this.mapStoreConfig = mapStoreConfig;
        return this;
    }

    /**
     * Returns the Near Cache configuration
     *
     * @return the Near Cache configuration
     */
    public NearCacheConfig getNearCacheConfig() {
        return nearCacheConfig;
    }

    /**
     * Sets the Near Cache configuration
     *
     * @param nearCacheConfig the Near Cache configuration
     * @return the updated map configuration
     */
    public MapConfig setNearCacheConfig(NearCacheConfig nearCacheConfig) {
        this.nearCacheConfig = nearCacheConfig;
        return this;
    }

    /**
     * Gets the map merge policy {@link com.hazelcast.map.merge.MapMergePolicy}
     *
     * @return the updated map configuration
     */
    public String getMergePolicy() {
        return mergePolicy;
    }

    /**
     * Sets the map merge policy {@link com.hazelcast.map.merge.MapMergePolicy}
     *
     * @param mergePolicy the map merge policy to set
     * @return the updated map configuration
     */
    public MapConfig setMergePolicy(String mergePolicy) {
        this.mergePolicy = mergePolicy;
        return this;
    }

    /**
     * Checks if statistics are enabled for this map.
     *
     * @return True if statistics are enabled, false otherwise.
     */
    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    /**
     * Sets statistics to enabled or disabled for this map.
     *
     * @param statisticsEnabled True to enable map statistics, false to disable.
     * @return The current map config instance.
     */
    public MapConfig setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
        return this;
    }

    /**
     * Checks if read-backup-data (reading local backup entries) is enabled for this map.
     *
     * @return True if read-backup-data is enabled, false otherwise.
     */
    public boolean isReadBackupData() {
        return readBackupData;
    }

    /**
     * Sets read-backup-data (reading local backup entries) for this map.
     *
     * @param readBackupData True to enable read-backup-data, false to disable.
     * @return The current map config instance.
     */
    public MapConfig setReadBackupData(boolean readBackupData) {
        this.readBackupData = readBackupData;
        return this;
    }

    /**
     * Gets the WAN target replication reference.
     *
     * @return The WAN target replication reference.
     */
    public WanReplicationRef getWanReplicationRef() {
        return wanReplicationRef;
    }

    /**
     * Sets the WAN target replication reference.
     *
     * @param wanReplicationRef the WAN target replication reference.
     * @return The current map config instance.
     */
    public MapConfig setWanReplicationRef(WanReplicationRef wanReplicationRef) {
        this.wanReplicationRef = wanReplicationRef;
        return this;
    }

    public MapConfig addEntryListenerConfig(EntryListenerConfig listenerConfig) {
        getEntryListenerConfigs().add(listenerConfig);
        return this;
    }

    public List<EntryListenerConfig> getEntryListenerConfigs() {
        if (entryListenerConfigs == null) {
            entryListenerConfigs = new ArrayList<EntryListenerConfig>();
        }
        return entryListenerConfigs;
    }

    public MapConfig setEntryListenerConfigs(List<EntryListenerConfig> listenerConfigs) {
        this.entryListenerConfigs = listenerConfigs;
        return this;
    }

    public MapConfig addMapPartitionLostListenerConfig(MapPartitionLostListenerConfig listenerConfig) {
        getPartitionLostListenerConfigs().add(listenerConfig);
        return this;
    }

    public List<MapPartitionLostListenerConfig> getPartitionLostListenerConfigs() {
        if (partitionLostListenerConfigs == null) {
            partitionLostListenerConfigs = new ArrayList<MapPartitionLostListenerConfig>();
        }

        return partitionLostListenerConfigs;
    }

    public MapConfig setPartitionLostListenerConfigs(List<MapPartitionLostListenerConfig> listenerConfigs) {
        this.partitionLostListenerConfigs = listenerConfigs;
        return this;
    }


    public MapConfig addMapIndexConfig(MapIndexConfig mapIndexConfig) {
        getMapIndexConfigs().add(mapIndexConfig);
        return this;
    }

    public List<MapIndexConfig> getMapIndexConfigs() {
        if (mapIndexConfigs == null) {
            mapIndexConfigs = new ArrayList<MapIndexConfig>();
        }
        return mapIndexConfigs;
    }

    public MapConfig setMapIndexConfigs(List<MapIndexConfig> mapIndexConfigs) {
        this.mapIndexConfigs = mapIndexConfigs;
        return this;
    }

    public MapConfig addMapAttributeConfig(MapAttributeConfig mapAttributeConfig) {
        getMapAttributeConfigs().add(mapAttributeConfig);
        return this;
    }

    public List<MapAttributeConfig> getMapAttributeConfigs() {
        if (mapAttributeConfigs == null) {
            mapAttributeConfigs = new ArrayList<MapAttributeConfig>();
        }
        return mapAttributeConfigs;
    }

    public MapConfig setMapAttributeConfigs(List<MapAttributeConfig> mapAttributeConfigs) {
        this.mapAttributeConfigs = mapAttributeConfigs;
        return this;
    }

    /**
     * Adds a new {@link QueryCacheConfig} to this {@code MapConfig}.
     *
     * @param queryCacheConfig the config to be added.
     * @return this {@code MapConfig} instance.
     * @throws java.lang.IllegalArgumentException if there is already a {@code QueryCache}
     *                                            with the same {@code QueryCacheConfig#name}.
     */
    public MapConfig addQueryCacheConfig(QueryCacheConfig queryCacheConfig) {
        String queryCacheName = queryCacheConfig.getName();
        List<QueryCacheConfig> queryCacheConfigs = getQueryCacheConfigs();
        for (QueryCacheConfig cacheConfig : queryCacheConfigs) {
            checkFalse(cacheConfig.getName().equals(queryCacheName),
                    "A query cache already exists with name = [" + queryCacheName + ']');
        }
        queryCacheConfigs.add(queryCacheConfig);
        return this;
    }

    /**
     * Returns all {@link QueryCacheConfig} instances defined on this {@code MapConfig}.
     *
     * @return all {@link QueryCacheConfig} instances defined on this {@code MapConfig}
     */
    public List<QueryCacheConfig> getQueryCacheConfigs() {
        if (queryCacheConfigs == null) {
            queryCacheConfigs = new ArrayList<QueryCacheConfig>();
        }
        return queryCacheConfigs;
    }

    /**
     * Sets {@link QueryCacheConfig} instances to this {@code MapConfig}.
     */
    public void setQueryCacheConfigs(List<QueryCacheConfig> queryCacheConfigs) {
        this.queryCacheConfigs = queryCacheConfigs;
    }

    public PartitioningStrategyConfig getPartitioningStrategyConfig() {
        return partitioningStrategyConfig;
    }

    public MapConfig setPartitioningStrategyConfig(PartitioningStrategyConfig partitioningStrategyConfig) {
        this.partitioningStrategyConfig = partitioningStrategyConfig;
        return this;
    }

    /**
     * Checks if Near Cache is enabled.
     *
     * @return {@code true} if Near Cache is enabled, {@code false} otherwise
     */
    public boolean isNearCacheEnabled() {
        return nearCacheConfig != null;
    }

    /**
     * Checks if queries are optimized.
     *
     * @return {@code true} if queries are optimized, {@code false} otherwise
     * @deprecated use {@link #getQueryCacheConfigs()} instead.
     */
    public boolean isOptimizeQueries() {
        return cacheDeserializedValues == CacheDeserializedValues.ALWAYS;
    }

    /**
     * Enable de-serialized value caching when evaluating predicates. It has no effect when {@link InMemoryFormat}
     * is {@link InMemoryFormat#OBJECT} or when {@link com.hazelcast.nio.serialization.Portable} serialization is used.
     *
     * @param optimizeQueries {@code true} if queries should be optimized, {@code false} otherwise
     * @return this {@code MapConfig} instance.
     * @see CacheDeserializedValues
     * @deprecated use {@link #setCacheDeserializedValues(CacheDeserializedValues)} instead
     */
    public MapConfig setOptimizeQueries(boolean optimizeQueries) {
        validateSetOptimizeQueriesOption(optimizeQueries);
        if (optimizeQueries) {
            this.cacheDeserializedValues = CacheDeserializedValues.ALWAYS;
        }
        //this is used to remember the method has been called explicitly
        this.optimizeQueryExplicitlyInvoked = true;
        return this;
    }

    private void validateSetOptimizeQueriesOption(boolean optimizeQueries) {
        if (setCacheDeserializedValuesExplicitlyInvoked) {
            if (optimizeQueries && cacheDeserializedValues == CacheDeserializedValues.NEVER) {
                throw new ConfigurationException("Deprecated option 'optimize-queries' is set to true, "
                        + "but 'cacheDeserializedValues' is set to NEVER. "
                        + "These are conflicting options. Please remove the `optimize-queries'");
            } else if (!optimizeQueries && cacheDeserializedValues == CacheDeserializedValues.ALWAYS) {
                throw new ConfigurationException("Deprecated option 'optimize-queries' is set to false, "
                        + "but 'cacheDeserializedValues' is set to ALWAYS. "
                        + "These are conflicting options. Please remove the `optimize-queries'");
            }
        }
    }

    /**
     * Configure de-serialized value caching.
     * Default: {@link CacheDeserializedValues#INDEX_ONLY}
     *
     * @param cacheDeserializedValues
     * @return this {@code MapConfig} instance.
     * @see CacheDeserializedValues
     * @since 3.6
     */
    public MapConfig setCacheDeserializedValues(CacheDeserializedValues cacheDeserializedValues) {
        validateCacheDeserializedValuesOption(cacheDeserializedValues);
        this.cacheDeserializedValues = cacheDeserializedValues;
        this.setCacheDeserializedValuesExplicitlyInvoked = true;
        return this;
    }

    private void validateCacheDeserializedValuesOption(CacheDeserializedValues validatedCacheDeserializedValues) {
        if (optimizeQueryExplicitlyInvoked) {
            // deprecated {@link #setOptimizeQueries(boolean)} was explicitly invoked
            // we need to be strict with validation to detect conflicts
            boolean optimizeQuerySet = (cacheDeserializedValues == CacheDeserializedValues.ALWAYS);
            if (optimizeQuerySet && validatedCacheDeserializedValues == CacheDeserializedValues.NEVER) {
                throw new ConfigurationException("Deprecated option 'optimize-queries' is set to `true`, "
                        + "but 'cacheDeserializedValues' is set to NEVER. These are conflicting options. "
                        + "Please remove the `optimize-queries'");
            }

            if (cacheDeserializedValues != validatedCacheDeserializedValues) {
                boolean optimizeQueriesFlagState = cacheDeserializedValues == CacheDeserializedValues.ALWAYS;
                throw new ConfigurationException("Deprecated option 'optimize-queries' is set to "
                        + optimizeQueriesFlagState + " but 'cacheDeserializedValues' is set to "
                        + validatedCacheDeserializedValues + ". These are conflicting options. "
                        + "Please remove the `optimize-queries'");
            }
        }
    }

    /**
     * Gets the {@code HotRestartConfig} for this {@code MapConfig}
     *
     * @return hot restart config
     */
    public HotRestartConfig getHotRestartConfig() {
        return hotRestartConfig;
    }

    /**
     * Sets the {@code HotRestartConfig} for this {@code MapConfig}
     *
     * @param hotRestartConfig hot restart config
     * @return this {@code MapConfig} instance
     */
    public MapConfig setHotRestartConfig(HotRestartConfig hotRestartConfig) {
        this.hotRestartConfig = hotRestartConfig;
        return this;
    }

    /**
     * Get current value cache settings
     *
     * @return current value cache settings
     * @since 3.6
     */
    public CacheDeserializedValues getCacheDeserializedValues() {
        return cacheDeserializedValues;
    }

    public String getQuorumName() {
        return quorumName;
    }

    public MapConfig setQuorumName(String quorumName) {
        this.quorumName = quorumName;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.backupCount;
        result = prime * result + this.asyncBackupCount;
        result = prime * result + this.evictionPercentage;
        result = prime * result + (int) (minEvictionCheckMillis ^ (minEvictionCheckMillis >>> 32));
        result = prime
                * result
                + ((this.evictionPolicy == null) ? 0 : this.evictionPolicy
                .hashCode());
        result = prime
                * result
                + ((this.mapEvictionPolicy == null) ? 0 : this.mapEvictionPolicy
                .hashCode());
        result = prime
                * result
                + ((this.mapStoreConfig == null) ? 0 : this.mapStoreConfig
                .hashCode());
        result = prime * result + this.maxIdleSeconds;
        result = prime * result + this.maxSizeConfig.getSize();
        result = prime
                * result
                + ((this.mergePolicy == null) ? 0 : this.mergePolicy.hashCode());
        result = prime * result
                + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime
                * result
                + ((this.nearCacheConfig == null) ? 0 : this.nearCacheConfig
                .hashCode());
        result = prime * result + this.timeToLiveSeconds;
        result = prime * result + cacheDeserializedValues.hashCode();
        result = prime * result + (this.readBackupData ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MapConfig)) {
            return false;
        }
        MapConfig other = (MapConfig) obj;
        return
                (this.name != null ? this.name.equals(other.name) : other.name == null)
                        && this.backupCount == other.backupCount
                        && this.asyncBackupCount == other.asyncBackupCount
                        && this.evictionPercentage == other.evictionPercentage
                        && this.minEvictionCheckMillis == other.minEvictionCheckMillis
                        && this.maxIdleSeconds == other.maxIdleSeconds
                        && this.maxSizeConfig.getSize() == other.maxSizeConfig.getSize()
                        && this.timeToLiveSeconds == other.timeToLiveSeconds
                        && this.readBackupData == other.readBackupData
                        && (this.cacheDeserializedValues == other.cacheDeserializedValues)
                        && (this.mergePolicy != null ? this.mergePolicy.equals(other.mergePolicy) : other.mergePolicy == null)
                        && (this.inMemoryFormat != null ? this.inMemoryFormat.equals(other.inMemoryFormat)
                        : other.inMemoryFormat == null)
                        && (this.evictionPolicy != null ? this.evictionPolicy.equals(other.evictionPolicy)
                        : other.evictionPolicy == null)
                        && (this.mapEvictionPolicy != null ? this.mapEvictionPolicy.equals(other.mapEvictionPolicy)
                        : other.mapEvictionPolicy == null)
                        && (this.mapStoreConfig != null ? this.mapStoreConfig.equals(other.mapStoreConfig)
                        : other.mapStoreConfig == null)
                        && (this.nearCacheConfig != null ? this.nearCacheConfig.equals(other.nearCacheConfig)
                        : other.nearCacheConfig == null);
    }

    @Override
    public String toString() {
        return "MapConfig{"
                + "name='" + name + '\''
                + ", inMemoryFormat=" + inMemoryFormat + '\''
                + ", backupCount=" + backupCount
                + ", asyncBackupCount=" + asyncBackupCount
                + ", timeToLiveSeconds=" + timeToLiveSeconds
                + ", maxIdleSeconds=" + maxIdleSeconds
                + ", evictionPolicy='" + evictionPolicy + '\''
                + ", mapEvictionPolicy='" + mapEvictionPolicy + '\''
                + ", evictionPercentage=" + evictionPercentage
                + ", minEvictionCheckMillis=" + minEvictionCheckMillis
                + ", maxSizeConfig=" + maxSizeConfig
                + ", readBackupData=" + readBackupData
                + ", hotRestart=" + hotRestartConfig
                + ", nearCacheConfig=" + nearCacheConfig
                + ", mapStoreConfig=" + mapStoreConfig
                + ", mergePolicyConfig='" + mergePolicy + '\''
                + ", wanReplicationRef=" + wanReplicationRef
                + ", entryListenerConfigs=" + entryListenerConfigs
                + ", mapIndexConfigs=" + mapIndexConfigs
                + ", mapAttributeConfigs=" + mapAttributeConfigs
                + ", quorumName=" + quorumName
                + ", queryCacheConfigs=" + queryCacheConfigs
                + ", cacheDeserializedValues=" + cacheDeserializedValues
                + '}';
    }
}
