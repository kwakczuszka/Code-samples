package com.example.casino.Packets;

import com.example.casino.Player;

public class GameReadyPacket extends Packet {
    private String UUID;
    private Player player;


    public enum Status {
        READY,
        NOT_READY,
    }

    public enum GameType {
        POKER,
        RUMMY,
    }

    private GameType gameType;
    private Status status;

    public GameReadyPacket(String data, Status status) {
        super(PacketType.GAME_READY_STATUS, data);
        this.status = status;
    }

    public GameReadyPacket(String data, GameType gameType, Player player, String uuid, Status status) {
        super(PacketType.GAME_READY_STATUS, data);
        this.player = player;
        this.UUID = uuid;
        this.status = status;
        this.gameType = gameType;
    }

    public GameReadyPacket(String data, String uuid, Status status) {
        super(PacketType.GAME_READY_STATUS, data);
        this.UUID = uuid;
        this.status = status;
    }

    public GameReadyPacket(String data, GameType gameType, String uuid, Status status) {
        super(PacketType.GAME_READY_STATUS, data);
        this.UUID = uuid;
        this.status = status;
        this.gameType = gameType;
    }

    public String getUUID() {
        return UUID;
    }

    public Player getPlayer() {
        return player;
    }

    public Status getStatus() {
        return status;
    }

    public GameType getGameType() {
        return gameType;
    }
}
