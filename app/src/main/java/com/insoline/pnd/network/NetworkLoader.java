package com.insoline.pnd.network;

/**
 * 전문 통신을 위한 시작점 클래스
 * 싱클톤으로 작동한다.
 */
public class NetworkLoader {

    private volatile static NetworkLoader instance;
    private NetworkConfiguration configuration;

    /**
     * 싱글톤
     */
    public static NetworkLoader getInstance() {
        if (instance == null) {
            synchronized (NetworkLoader.class) {
                if (instance == null) {
                    instance = new NetworkLoader();
                }
            }
        }

        return instance;
    }

    private NetworkLoader() {
    }

    public void sendRequest(NetworkData networkData) {
        if (networkData == null) {
            throw new IllegalArgumentException("NetworkData 값 Null");
        }

        if (networkData.getContext() == null) {
            throw new IllegalArgumentException("NetworkData Context 값 Null");
        }

        if (this.configuration == null)
            configuration = new NetworkConfiguration();

        if (networkData.getReadTimeOut() == -1) {
            networkData.setReadTimeOut(this.configuration.readTimeOut);
        }

        if (networkData.getConnectionTimeOut() == -1) {
            networkData.setConnectionTimeOut(this.configuration.connectionTimeOut);
        }

        // 네트워크 시작 ( EXECUTE 전 )
        if (networkData.getNetworkListener() != null)
            networkData.getNetworkListener().preStart(networkData);

        this.configuration.taskExecutor.execute(new NetworkRunnable(networkData));

    }
}
