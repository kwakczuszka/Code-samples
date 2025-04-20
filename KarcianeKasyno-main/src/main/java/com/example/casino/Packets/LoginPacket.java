package com.example.casino.Packets;

import com.example.casino.Player;

public class LoginPacket extends Packet {

    private String login;
    private String password;
    private Player player;

    public enum Status {
        LOGIN,
        LOGOUT,
        PASWWORD_ERROR,
        ACOUNT_NOT_FOUND_ERROR
    }

    private Status status;

    public LoginPacket(String data, String login, String password, Status status) {
        super(PacketType.LOGIN, data);
        this.login = login;
        this.password = password;
        this.status = status;
    }

    public LoginPacket(String data, Player player, Status status) {
        super(PacketType.LOGIN, data);
        this.player = player;
        this.status = status;
    }

    public LoginPacket(String data, Status status) {
        super(PacketType.LOGIN, data);
        this.status = status;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Player getPlayer() {
        return player;
    }

    public Status getStatus() {
        return status;
    }
}
