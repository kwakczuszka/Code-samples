package com.example.casino.Packets;

import java.util.HashMap;

public class RankingPacket extends Packet {
    private Status status;
    private HashMap<String, Integer> rankingMap;

    public enum Status {
        POKER,
        REMIK
    }

    public RankingPacket(String data, Status status) {
        super(PacketType.RANKING, data);
        this.status = status;
    }

    public RankingPacket(String data, Status status, HashMap<String, Integer> rankingMap) {
        super(PacketType.RANKING, data);
        this.status = status;
        this.rankingMap = rankingMap;
    }

    public enum GameType {
        POKER,
        RUMMY
    }

    public Status getStatus() {
        return status;
    }

    public HashMap<String, Integer> getRankingMap() {
        return rankingMap;
    }
}
