package com.example.casino.Packets;

import com.example.casino.Player;

import java.util.List;

public class RemikPacket extends Packet {
    public enum Status {
        START
    }

    private Status status;
    private Player player;
    private List<Player> players;


    public RemikPacket(String data, Status status) {
        super(PacketType.REMIK, data);
        this.status = status;
    }

    public RemikPacket(String data, Status status, Player player, List<Player> players) {
        super(PacketType.REMIK, data);
        this.status = status;
        this.player = player;
        this.players = players;
    }

    public Status getStatus() {
        return status;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
