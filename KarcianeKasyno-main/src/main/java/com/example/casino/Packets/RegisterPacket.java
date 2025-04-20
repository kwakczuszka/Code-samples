package com.example.casino.Packets;

import com.example.casino.Player;
import com.example.casino.Server.GameServer;
import com.example.casino.Server.PassHash;
import javafx.util.Pair;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.*;

public class RegisterPacket extends Packet {

    private String email;
    private String date;
    private String login;
    private String password;
    private String salt;
    private Player player;

    public String getSalt() {
        return salt;
    }


    public enum Status {
        REGISTER,
        ACOUNT_ALREADY_EXISTS_ERROR,
    }

    private Status status;

    public RegisterPacket(String data, String email, String login, String password, String date, Status status)
            throws NoSuchAlgorithmException, InvalidKeySpecException, ExecutionException, InterruptedException {
        super(PacketType.REGISTER, data);
        this.email = email;
        this.date = date;
        this.login = login;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        FutureTask<Pair<String, String>> future = (FutureTask<Pair<String, String>>)
                executorService.submit(new PassHash(password));
        this.password = future.get().getKey();
        this.salt = future.get().getValue();
        this.status = status;
    }

    public RegisterPacket(String data, Player player, Status status) {
        super(PacketType.REGISTER, data);
        this.player = player;
        this.status = status;
    }

    public RegisterPacket(String data, Status status) {
        super(PacketType.REGISTER, data);
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

    public String getEmail() {
        return email;
    }

    public String getDate() {
        return date;
    }

    public Status getStatus() {
        return status;
    }
}
