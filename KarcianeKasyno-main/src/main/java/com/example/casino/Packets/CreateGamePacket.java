package com.example.casino.Packets;

public class CreateGamePacket extends Packet {

    private String UUID;

    public enum GameType {
        POKER,
        RUMMY
    }

    private GameType gameType;

    public CreateGamePacket(String data, String UUID) {
        super(PacketType.CREATEGAME, data);
        this.UUID = UUID;
    }

    public CreateGamePacket(String data, String UUID, GameType gameType) {
        super(PacketType.CREATEGAME, data);
        this.UUID = UUID;
        this.gameType = gameType;
    }

    public GameType getGameType() {
        return gameType;
    }

    public String getUUID() {
        return UUID;
    }
}
