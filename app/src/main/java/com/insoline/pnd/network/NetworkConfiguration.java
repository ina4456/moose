package com.insoline.pnd.network;

import com.insoline.pnd.BuildConfig;

import java.util.concurrent.Executor;

/**
 * 전문 통신에 필요한 Config
 */
public class NetworkConfiguration {
    private static final String TAG = NetworkConfiguration.class.getName();

    private final int DEFAULT_GENERAL_NETWORK_READ_TIMEOUT = 15 * 1000 * 2;
    private final int DEFAULT_GENERAL_NETWORK_CONNECTION_TIMEOUT = 15 * 1000 * 2;
    private final int DEFAULT_GENERAL_NETWORK_THREAD_POOL_SIZE = 3;
    private final int DEFAULT_GENERAL_NETWORK_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

    public final Executor taskExecutor;
    public int readTimeOut;
    public final int connectionTimeOut;

    public NetworkConfiguration() {
        this.readTimeOut = DEFAULT_GENERAL_NETWORK_READ_TIMEOUT;
        if (BuildConfig.DEBUG) {
            this.readTimeOut = DEFAULT_GENERAL_NETWORK_READ_TIMEOUT * 2;
        }
        this.connectionTimeOut = DEFAULT_GENERAL_NETWORK_CONNECTION_TIMEOUT;
        this.taskExecutor = NetworkExecutor.createExecutor(DEFAULT_GENERAL_NETWORK_THREAD_POOL_SIZE, DEFAULT_GENERAL_NETWORK_THREAD_PRIORITY);
    }
}
