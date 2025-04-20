package com.example.casino.Server;

import com.example.casino.Client;
import com.example.casino.Packets.GamePacket;
import com.example.casino.Packets.Packet;
import com.example.casino.Packets.PacketType;
import com.example.casino.Player;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PokerGame implements Callable<HashMap<ClientHandler, Integer>> {
    String id;
    int playersReady;
    List<ClientHandler> players;
    List<Player> playersData;
    ArrayList<Karta> tableCards;
    private Talia deck = new Talia(false);
    final Lock lock = new ReentrantLock();
    final Condition next = lock.newCondition();
    Integer currentBid;
    Integer moneyPool;
    boolean isBankrupt = false;

    List<ClientHandler> recentWinners = new ArrayList<>();

    public void handlerRaiseBetween(ClientHandler ch, Integer r) {
        if (ch.getPlayer().curBid < currentBid) {
            if (currentBid - ch.getPlayer().curBid > ch.getPlayer().money) {
                ch.getPlayer().money = 0;
                isBankrupt = true;
            } else {
                ch.getPlayer().money -= (currentBid - ch.getPlayer().curBid);
            }
        }
        currentBid += r;
        if (r > ch.getPlayer().money) {
            moneyPool += ch.getPlayer().money;
            ch.getPlayer().money = 0;
            isBankrupt = true;
        } else {
            moneyPool += currentBid - ch.getPlayer().curBid;
            ch.getPlayer().money -= r;
        }
        ch.getPlayer().curBid = currentBid;
    }

    public void handlerRaise(ClientHandler ch, Integer r) {
        if (ch.getPlayer().curBid < currentBid) {
            if (currentBid - ch.getPlayer().curBid > ch.getPlayer().money) {
                ch.getPlayer().money = 0;
                isBankrupt = true;
            } else {
                ch.getPlayer().money -= (currentBid - ch.getPlayer().curBid);
            }
        }
        currentBid += r;
        if (r > ch.getPlayer().money) {
            isBankrupt = true;
            moneyPool += ch.getPlayer().money;
            ch.getPlayer().money = 0;
        } else {
            moneyPool += currentBid - ch.getPlayer().curBid;
            ch.getPlayer().money -= r;
        }
        ch.getPlayer().curBid = currentBid;


        for (ClientHandler chInner : players) {
            if (!ch.equals(chInner)) {
                chInner.sendPacket(new GamePacket("other user called", GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.RAISE, ch.getPlayer(), ch.getPlayer().money));
            }
        }
    }

    public void handlerCall(ClientHandler ch) {
        if (ch.getPlayer().curBid < currentBid) {
            if (currentBid - ch.getPlayer().curBid > ch.getPlayer().money) {
                isBankrupt = true;
                moneyPool += ch.getPlayer().money;
                ch.getPlayer().money = 0;
            } else {
                ch.getPlayer().money -= currentBid - ch.getPlayer().curBid;
                moneyPool += currentBid - ch.getPlayer().curBid;
            }
        }
        ch.getPlayer().curBid = currentBid;

        for (ClientHandler chInner : players) {
            if (!ch.equals(chInner)) {
                chInner.sendPacket(new GamePacket("other user called", GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.CALL, ch.getPlayer(), ch.getPlayer().money));
            }
        }
    }

    public void handlerFold(ClientHandler ch) {
        ch.getPlayer().pass();
        for (ClientHandler chInner : players) {
            if (!ch.equals(chInner)) {
                chInner.sendPacket(new GamePacket("other user called", GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.FOLD, ch.getPlayer(), ch.getPlayer().money));
            }
        }
    }

    public void updateMoneyPool() {
        for (Player p : playersData) {
            moneyPool += p.curBid;
        }
    }

    public boolean canProceed() {
        boolean ret = true;
        for (Player p : playersData) {
            if (p.curBid < currentBid && !p.passedAway && p.money > 0) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    private ArrayList<ClientHandler> Showdown() {
        ClientHandler highestHand = players.get(0);
        for (ClientHandler ch :
                players) {
            if (!ch.getPlayer().passedAway &&
                    highestHand.getPlayer().pokerHand.compareWith(ch.getPlayer().pokerHand)
                            == PokerHand.Result.LOSS) {
                highestHand = ch;
            }
        }
        ArrayList<ClientHandler> ret = new ArrayList<>();
        for (ClientHandler ch :
                players) {
            if (!ch.getPlayer().passedAway &&
                    highestHand.getPlayer().pokerHand.compareWith(ch.getPlayer().pokerHand)
                            == PokerHand.Result.TIE)
                ret.add(ch);

        }
        return ret;
    }


    boolean nextPlayer = false;

    Talia talia;


    public PokerGame(String id) {
        currentBid = 0;
        moneyPool = 0;
        playersReady = 0;
        players = new ArrayList<>();
        playersData = new ArrayList<>();
        this.id = id;
        this.talia = new Talia(false);
    }

    public PokerGame(String id, ClientHandler ch) {
        currentBid = 0;
        playersReady = 0;
        players = new ArrayList<>();
        playersData = new ArrayList<>();
        players.add(ch);
        playersData.add(ch.getPlayer());
        this.id = id;
        this.talia = new Talia(false);
    }

    public void broadcast(Packet packet) {
        for (ClientHandler ch : players) {
            ch.sendPacket(packet);
        }
    }


    private void deal(boolean isFirstRound) throws InterruptedException {
        this.isBankrupt = false;
        this.tableCards = new ArrayList<>();
        talia = new Talia(false);
        talia.SzuflujTalie();
        List<Player> otherPlayers = new ArrayList<>();

        for (ClientHandler ch : players) {
            ch.getPlayer().passedAway = false;
            ch.getPlayer().clearHand();
            moneyPool = 0;
            ch.getPlayer().setMoney(1000);

        }

        for (int i = 0; i < players.size(); i++) {
            otherPlayers.clear();
            ClientHandler ch = players.get(i);
            for (int j = i + 1; j < players.size(); j++) {
                otherPlayers.add(playersData.get(j));
            }
            for (int k = 0; k < i; k++) {
                otherPlayers.add(playersData.get(k));
            }

            ch.sendPacket(new GamePacket("Game Starting",
                    GamePacket.Status.START, ch.getPlayer(), otherPlayers));
        }


        for (ClientHandler ch : players) {
            Karta card = talia.KartaZTalii();
            ch.sendPacket(new GamePacket("FirstHandCard",
                    GamePacket.Status.FIRST_HAND_CARD, card));
            ch.getPlayer().setCard1(card);
        }

        for (ClientHandler ch : players) {
            Karta card = talia.KartaZTalii();
            ch.sendPacket(new GamePacket("SecondHandCard",
                    GamePacket.Status.SECOND_HAND_CARD, card));
            ch.getPlayer().setCard2(card);
        }

        for (int i = 0; i < 5; i++) {
            this.tableCards.add(talia.KartaZTalii());
        }

        for (ClientHandler ch : players) {
            ch.getPlayer().pokerHand.addHand(tableCards);
        }
    }

    @Override
    public HashMap<ClientHandler, Integer> call() throws Exception { //rozdanie

        HashMap<ClientHandler, Integer> finalPoints = new HashMap<>();

        for (int round = 0; round < 2; round++) {
            deal(round == 0);
            Thread.sleep(1000);
            handlerRaiseBetween(players.get(0), 5);
            players.get(0).sendPacket(new GamePacket("small blind",
                    GamePacket.Status.SMALL_BLIND
                    , moneyPool));

            for (ClientHandler ch : players) {
                if (!ch.equals(players.get(0))) {
                    ch.sendPacket(new GamePacket("someone put small blind",
                            GamePacket.Status.SMALL_BLIND, players.get(0).getPlayer()));
                }
            }
            Thread.sleep(2000);
            handlerRaiseBetween(players.get(1), 5);
            players.get(1).sendPacket(new GamePacket("big blind",
                    GamePacket.Status.BIG_BLIND, moneyPool));
            for (ClientHandler ch : players) {
                if (!ch.equals(players.get(1))) {
                    ch.sendPacket(new GamePacket("someone put big blind",
                            GamePacket.Status.BIG_BLIND, players.get(1).getPlayer()));
                }
            }
            for (int i = 2; i < playersData.size(); i++) {
                players.get(i).sendPacket(new GamePacket("your move",
                        GamePacket.Status.MOVE,
                        GamePacket.MOVE_TYPE.CALL, currentBid));

                lock.lock();
                while (!nextPlayer) {
                    next.await();
                }
                lock.unlock();
                nextPlayer = false;
            }
            players.get(0).sendPacket(new GamePacket("your move",
                    GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.CALL, (currentBid - 5)));
            lock.lock();
            while (!nextPlayer) {
                next.await();
            }
            lock.unlock();
            nextPlayer = false;
            turn(false);
            for (int i = 1; i <= 3; i++) {
                broadcast(new GamePacket("środkowe karta:",
                        GamePacket.Status.TABLE_CARDS, i, tableCards.get(i - 1)));
                Thread.sleep(1500);
            }
            currentBid = 0;
            turn(true);
            int temp = players.size();

            for (ClientHandler ch : players) {
                if (ch.getPlayer().passedAway) temp--;
            }
            if (temp <= 1 || isBankrupt) {
                recentWinners = Showdown();
                for (ClientHandler winner : recentWinners) {
                    if (finalPoints.containsKey(winner)) {
                        finalPoints.put(winner, finalPoints.get(winner) + moneyPool / recentWinners.size());
                    } else {
                        finalPoints.put(winner, moneyPool / recentWinners.size());
                    }
                    broadcast(new GamePacket(winner.getPlayer().getPlayerData(), GamePacket.Status.WINNER));
                    Thread.sleep(7000);
                }
                moneyPool = 0;
                continue;
            }
            broadcast(new GamePacket("środkowe karta:",
                    GamePacket.Status.TABLE_CARDS, 4, tableCards.get(3)));
            Thread.sleep(1500);
            currentBid = 0;
            turn(true);
            temp = players.size();

            for (ClientHandler ch : players) {
                if (ch.getPlayer().passedAway) temp--;
            }
            if (temp <= 1 || isBankrupt) {
                recentWinners = Showdown();
                for (ClientHandler winner : recentWinners) {
                    if (finalPoints.containsKey(winner)) {
                        finalPoints.put(winner, finalPoints.get(winner) + moneyPool / recentWinners.size());
                    } else {
                        finalPoints.put(winner, moneyPool / recentWinners.size());
                    }

                    broadcast(new GamePacket(winner.getPlayer().getPlayerData(), GamePacket.Status.WINNER, winner.getPlayer(), winner.getPlayer().getCard1(), winner.getPlayer().getCard2()));

                    Thread.sleep(7000);
                }
                moneyPool = 0;
                continue;
            }
            broadcast(new GamePacket("środkowe karta:",
                    GamePacket.Status.TABLE_CARDS, 5, tableCards.get(4)));
            Thread.sleep(1500);
            currentBid = 0;
            turn(true);
            players.addLast(players.removeFirst());
            playersData.addLast(playersData.removeFirst());
            recentWinners = Showdown();
            for (ClientHandler winner : recentWinners) {
                if (finalPoints.containsKey(winner)) {
                    finalPoints.put(winner, finalPoints.get(winner) + moneyPool / recentWinners.size());
                } else {
                    finalPoints.put(winner, moneyPool / recentWinners.size());
                }
                broadcast(new GamePacket(winner.getPlayer().getPlayerData(), GamePacket.Status.WINNER, winner.getPlayer(), winner.getPlayer().getCard1(), winner.getPlayer().getCard2()));
                Thread.sleep(7000);
            }

            moneyPool = 0;
        }
        Thread.sleep(2000);
        broadcast(new GamePacket("Game ended", GamePacket.Status.END_GAME));
        return finalPoints;
    }

    private void turn(boolean nextRound) throws InterruptedException {
        if (nextRound) {
            for (ClientHandler ch : players) {
                ch.getPlayer().curBid = 0;
            }
        }
        while (!canProceed() || nextRound) {
            for (int i = 0; i < playersData.size(); i++) {
                if (!playersData.get(i).passedAway) {
                    if (currentBid - players.get(i).getPlayer().curBid == 0) {
                        players.get(i).sendPacket(new GamePacket("your move", GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.CHECK, currentBid - players.get(i).getPlayer().curBid));
                    } else {
                        players.get(i).sendPacket(new GamePacket("your move", GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.CALL, currentBid - players.get(i).getPlayer().curBid));
                    }
                    lock.lock();
                    while (!nextPlayer) {
                        next.await();
                    }
                    lock.unlock();
                    nextPlayer = false;
                }
            }
            nextRound = false;
            if (this.isBankrupt) break;
        }
    }


    public void unlockLock() {
        this.nextPlayer = true;
        lock.lock();
        this.next.signal();
        lock.unlock();
    }
}
