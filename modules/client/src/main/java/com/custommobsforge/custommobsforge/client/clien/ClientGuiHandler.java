package com.custommobsforge.custommobsforge.client.clien;

import com.custommobsforge.custommobsforge.common.network.OpenGuiPacket;

public class ClientGuiHandler {
    public static void requestGui() {
        ClientNetworkHandler.sendToServer(new OpenGuiPacket());
    }
}