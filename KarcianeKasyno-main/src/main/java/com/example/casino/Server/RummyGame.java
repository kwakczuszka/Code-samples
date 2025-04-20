package com.example.casino.Server;

import com.example.casino.Packets.GamePacket;
import com.example.casino.Packets.Packet;
import com.example.casino.Packets.RemikPacket;
import com.example.casino.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class RummyGame implements Callable<Player> {

    String id;

    int playersReady;

    List<ClientHandler> players;
    List<Player> playersData;

    public RummyGame(String id) {
        playersReady = 0;
        players = new ArrayList<>();
        playersData = new ArrayList<>();
        this.id = id;
    }

    public RummyGame(String id, ClientHandler ch) {
        playersReady = 0;
        players = new ArrayList<>();
        playersData = new ArrayList<>();
        players.add(ch);
        playersData.add(ch.getPlayer());
        this.id = id;
    }

    public void broadcast(Packet packet) {
        for (ClientHandler ch : players) {
            ch.sendPacket(packet);
        }
    }

    @Override
    public Player call() throws Exception {
        Thread.sleep(1000);

        List<Player> otherPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            otherPlayers.clear();
            ClientHandler ch = players.get(i);
            for (int j = i + 1; j < players.size(); j++) {
                otherPlayers.add(playersData.get(j));
            }
            for (int k = 0; k < i; k++) {
                otherPlayers.add(playersData.get(k));
            }

            broadcast(new RemikPacket("Game Starting", RemikPacket.Status.START, ch.getPlayer(), otherPlayers));

        }

        return playersData.get(0);
    }
}
