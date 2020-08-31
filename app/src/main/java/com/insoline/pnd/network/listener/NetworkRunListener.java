package com.insoline.pnd.network.listener;

import com.insoline.pnd.network.NetworkData;

public interface NetworkRunListener {

    void preStart(NetworkData data);

    void doInBackground(NetworkData data);

    void onSuccess(NetworkData data);

    void onError(NetworkData data);

}
