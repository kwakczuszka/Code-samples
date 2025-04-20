package com.example.casino.Packets;

import java.io.Serializable;

public class Packet implements Serializable {
    private PacketType type;
    private String desc;

    public Packet(PacketType type, String data) {
        this.type = type;
        this.desc = data;
    }

    public PacketType getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
