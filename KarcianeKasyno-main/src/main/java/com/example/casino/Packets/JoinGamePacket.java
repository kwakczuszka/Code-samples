package com.example.casino.Packets;

import com.example.casino.Player;

import java.util.List;

public class JoinGamePacket extends Packet {
    private String UUID;
    private Player player;
    private List<Player> players;

    public enum GameType {
        POKER,
        RUMMY
    }

    private GameType gameType;


    public enum Status {
        JOIN,
        LEAVE,
        USER_JOIN,
        USER_LEFT,
        JOINED,
        LEFT
    }

    private Status status;

    public JoinGamePacket(String data, String UUID, Status status) {
        super(PacketType.JOINGAME, data);
        this.UUID = UUID;
        this.status = status;
    }

    public JoinGamePacket(String data, String UUID, GameType gameType, Player player, Status status) {
        super(PacketType.JOINGAME, data);
        this.UUID = UUID;
        this.player = player;
        this.gameType = gameType;
        this.status = status;
    }


    public JoinGamePacket(String data, String UUID, List<Player> players, Status status) {
        super(PacketType.JOINGAME, data);
        this.UUID = UUID;
        this.players = players;
        this.status = status;
    }

    public JoinGamePacket(String data, String UUID, GameType gameType, List<Player> players, Status status) {
        super(PacketType.JOINGAME, data);
        this.UUID = UUID;
        this.players = players;
        this.status = status;
        this.gameType = gameType;
    }

    public JoinGamePacket(String data, String UUID, GameType gameType, Status status) {
        super(PacketType.JOINGAME, data);
        this.UUID = UUID;
        this.status = status;
        this.gameType = gameType;
    }


    public String getUUID() {
        return UUID;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Status getStatus() {
        return status;
    }

    public GameType getGameType() {
        return gameType;
    }
}
