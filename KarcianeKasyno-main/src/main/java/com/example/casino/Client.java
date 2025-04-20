package com.example.casino;

import com.example.casino.Controllers.*;
import com.example.casino.Packets.*;
import com.example.casino.Server.GameServer;
import com.example.casino.Server.Karta;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Client extends Thread {

    private Socket clientSocket;
    public ObjectOutputStream out;
    private ObjectInputStream in;
    private LoginController lc;
    private PokerLobbyController plbc;

    private RummyLobbyController rlbc;
    private MenuController mc;
    private RegisterController rc;
    private PokerTableController ptc;
    private PokerRankingController prc;
    private RemikRankingController rrc;

    public void run() {
        try {
            sendPacket(new Packet(PacketType.ACK, "hello"));
            Packet respone;
            while (clientSocket.isConnected()) {
                try {
                    respone = (Packet) in.readObject();
                    if (respone == null) break;
                    parseRespone(respone);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (SocketException e) {
                    System.err.println("Połączenie z serwerem zostało zamknięte.");
                    break;
                }

            }
            stopConnection();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    private void parseRespone(Packet respone) {
        switch (respone.getType()) {
            case ACK:
                System.out.println("Ustanowiono połączenie z serwerem");
                break;
            case LOGIN: {
                LoginPacket loginRespone = (LoginPacket) respone;
                String data = loginRespone.getDesc();
                LoginPacket.Status status = loginRespone.getStatus();
                if (status.equals(LoginPacket.Status.LOGIN)) {
                    System.out.println("Zalogowano");
                    Main.player = loginRespone.getPlayer();
                    Platform.runLater(() ->
                            openMenu()
                    );
                } else if (status.equals(LoginPacket.Status.LOGOUT)) {
                    GameServer.removeNick(loginRespone.getLogin() + "Client71");
                    System.out.println("Wylogowano");
                    Platform.runLater(() ->
                            logOut()
                    );
                } else if (status.equals(LoginPacket.Status.PASWWORD_ERROR)) {
                    Platform.runLater(() ->
                            showLoginErr("PODANO NIEPOPRAWNE HASŁO")
                    );
                } else if (status.equals(LoginPacket.Status.ACOUNT_NOT_FOUND_ERROR)) {
                    Platform.runLater(() ->
                            showLoginErr("NIE ISTNIEJE TAKIE KONTO")
                    );
                } else {
                    System.err.println("NIE ZNANE DANE: " + data);
                }
                break;
            }
            case REGISTER: {
                RegisterPacket registerRespone = (RegisterPacket) respone;
                String data = registerRespone.getDesc();
                RegisterPacket.Status status = registerRespone.getStatus();
                if (status.equals(RegisterPacket.Status.REGISTER)) {
                    System.out.println("Zarejestrowano");
                    Main.player = registerRespone.getPlayer();
                    Platform.runLater(() ->
                            openMenu()
                    );
                } else if (status.equals(RegisterPacket.Status.ACOUNT_ALREADY_EXISTS_ERROR)) {
                    System.out.println("To konto już istnieje");
                    Platform.runLater(() ->
                            showRegisterErr("TAKIE KONTO JUŻ ISTNIEJE")
                    );
                } else {
                    System.err.println("NIE ZNANE DANE: " + data);
                }
                break;
            }
            case CREATEGAME: {
                CreateGamePacket createGamePacket = (CreateGamePacket) respone;
                String data = createGamePacket.getDesc();
                if (data.split(":")[0].equals("GameCreated")) {
                    System.out.println("utworzono gre");
                    Platform.runLater(() ->
                            openLobby(String.valueOf(createGamePacket.getGameType()), createGamePacket.getUUID(), new ArrayList<>(List.of(Main.player)))
                    );
                }
                break;
            }
            case JOINGAME: {
                JoinGamePacket joinGamePacket = (JoinGamePacket) respone;
                String data = joinGamePacket.getDesc();
                JoinGamePacket.Status status = joinGamePacket.getStatus();
                JoinGamePacket.GameType gameType = joinGamePacket.getGameType();
                if (status.equals(JoinGamePacket.Status.JOINED)) {
                    System.out.println("dołączono do gry");
                    Platform.runLater(() ->
                            openLobby(String.valueOf(gameType), joinGamePacket.getUUID(), joinGamePacket.getPlayers())
                    );
                } else if (status.equals(JoinGamePacket.Status.USER_JOIN)) {
                    System.out.println("Dołączył nowy gracz");
                    System.out.println("Player:" + joinGamePacket.getPlayer().getPlayerData());
                    Platform.runLater(() ->
                            refreshLobby(String.valueOf(gameType), true, joinGamePacket.getPlayer())
                    );
                } else if (status.equals(JoinGamePacket.Status.LEFT)) {
                    System.out.println("Opuszczono lobby");
                    Platform.runLater(() ->
                            leaveLobby()
                    );
                } else if (status.equals(JoinGamePacket.Status.USER_LEFT)) {
                    System.out.println("użytkownik opuścił lobby");
                    Platform.runLater(() ->
                            refreshLobby(String.valueOf(gameType), false, joinGamePacket.getPlayer())
                    );
                }
                break;
            }
            case GAME_READY_STATUS: {
                GameReadyPacket gameReadyPacket = (GameReadyPacket) respone;
                GameReadyPacket.Status status = gameReadyPacket.getStatus();
                GameReadyPacket.GameType gameType = gameReadyPacket.getGameType();
                if (status.equals(GameReadyPacket.Status.READY)) {
                    Platform.runLater(() ->
                            changeStatus(String.valueOf(gameType), true, gameReadyPacket.getPlayer())
                    );
                } else {
                    Platform.runLater(() ->
                            changeStatus(String.valueOf(gameType), false, gameReadyPacket.getPlayer())
                    );
                }
                break;
            }
            case GAME: {
                GamePacket gamePacket = (GamePacket) respone;
                GamePacket.Status status = gamePacket.getStatus();
                if (status.equals(GamePacket.Status.START)) {
                    Platform.runLater(() -> {
                        startGame(gamePacket.getPlayers(), gamePacket.getPlayer());
                        gamePacket.getPlayer().setReady(false);
                    });
                } else if (status.equals(GamePacket.Status.FIRST_HAND_CARD)) {
                    Platform.runLater(() ->
                            setFirstCard(gamePacket.getCard())
                    );
                } else if (status.equals(GamePacket.Status.SECOND_HAND_CARD)) {
                    Platform.runLater(() ->
                            setSecondCard(gamePacket.getCard())
                    );
                } else if (status.equals(GamePacket.Status.SMALL_BLIND)) {
                    if (gamePacket.getPlayer() != null) {
                        Platform.runLater(() ->
                                otherSmallBlind(gamePacket.getPlayer())
                        );
                    } else {
                        Platform.runLater(() ->
                                smallBlind()

                        );
                    }
                } else if (status.equals(GamePacket.Status.BIG_BLIND)) {
                    if (gamePacket.getPlayer() != null) {
                        Platform.runLater(() ->
                                otherBigBlind(gamePacket.getPlayer())
                        );
                    } else {
                        Platform.runLater(() ->
                                bigBlind()
                        );
                    }
                } else if (status.equals(GamePacket.Status.TABLE_CARDS)) {
                    Platform.runLater(() ->
                            setTableCard(gamePacket.getCardindex(), gamePacket.getCard())
                    );
                } else if (status.equals(GamePacket.Status.MOVE)) {
                    if (gamePacket.getPlayer() != null) {
                        Platform.runLater(() ->
                                        otherMakeMove(gamePacket.getPlayer(), gamePacket.getMove_type(), gamePacket.getCurrentBid())
                                //tutaj w getcurrentbid jest liczba na jaka ma usatwic sie wartoc gracza
                        );
                    } else {
                        Platform.runLater(() ->
                                makeMove(gamePacket.getMove_type(), gamePacket.getCurrentBid())
                        );
                    }
                } else if (status.equals(GamePacket.Status.WINNER)) {
                    Platform.runLater(() ->
                            showWinner(gamePacket.getDesc(), gamePacket.getPlayer(), gamePacket.getCard(), gamePacket.getCard2())
                    );
                } else if (status.equals(GamePacket.Status.END_GAME)) {
                    Platform.runLater(() ->
                            openMenu()
                    );
                }
                break;
            }
            case REMIK: {
                RemikPacket remikPacket = (RemikPacket) respone;
                RemikPacket.Status status = remikPacket.getStatus();
                if (status.equals(RemikPacket.Status.START)) {
                    Platform.runLater(() -> {
                        startGameRemik(remikPacket.getPlayers(), remikPacket.getPlayer().getPlayerData());
                    });


                }
                break;
            }
            case RANKING: {
                RankingPacket rankingPacket = (RankingPacket) respone;
                RankingPacket.Status status = rankingPacket.getStatus();
                if (status.equals(RankingPacket.Status.POKER)) {
                    Platform.runLater(() -> {
                        goToPokerRanking(rankingPacket.getRankingMap());
                    });
                } else if (status.equals(RankingPacket.Status.REMIK)) {
                    Platform.runLater(() -> {
                        goToRemikRanking(rankingPacket.getRankingMap());
                    });
                }

                break;
            }
            default: {
                System.err.println("NIEZNANY PAKIET");
            }
        }
    }

    private void goToPokerRanking(HashMap<String, Integer> RankingMap) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PokerRanking.fxml"));
            Parent root = loader.load();
            this.prc = loader.getController();
            this.prc.setPokerRankingMap(RankingMap);
            this.prc.initTop10List();
            Main.stage.close();
            Main.stage.setTitle("PokerRanking");
            Main.stage.setScene(new Scene(root));
            Main.stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error switching to poker ranking ");
        }
    }

    private void goToRemikRanking(HashMap<String, Integer> RankingMap) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RemikRanking.fxml"));
            Parent root = loader.load();
            this.rrc = loader.getController();
            this.rrc.setRemikRankingMap(RankingMap);
            this.rrc.initTop10List();
            Main.stage.close();
            Main.stage.setTitle("RemikRanking");
            Main.stage.setScene(new Scene(root));
            Main.stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error switching to remik ranking ");
        }
    }


    private void logOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            this.lc = loader.getController();
            Main.stage.close();
            Main.stage.setTitle("Login");
            Main.stage.setScene(new Scene(root));
            Main.stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in loading table");
        }

    }

    private void openMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
            Parent root = loader.load();
            this.mc = loader.getController();
            Main.stage.close();
            Main.stage.setTitle("Menu");
            Main.stage.setScene(new Scene(root));
            Main.stage.show();
            Main.stage.sizeToScene();
            Main.stage.setFullScreen(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in loading table");
        }

    }


    private void openLobby(String gameType, String uuid, List<Player> players) {
        try {
            if (gameType.equals("POKER")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PokerLobby.fxml"));
                Parent root = loader.load();
                this.plbc = loader.getController();
                Main.stage.close();
                Main.stage.setTitle("Lobby");
                Main.stage.setScene(new Scene(root));
                Main.stage.show();
                plbc.setUuid(uuid);
                for (Player p : players) {
                    plbc.addPlayer(p);
                }
                plbc.refreshPlayerContainer();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("RummyLobby.fxml"));
                Parent root = loader.load();
                this.rlbc = loader.getController();
                Main.stage.close();
                Main.stage.setTitle("Lobby");
                Main.stage.setScene(new Scene(root));
                Main.stage.show();
                rlbc.setUuid(uuid);
                for (Player p : players) {
                    rlbc.addPlayer(p);
                }
                rlbc.refreshPlayerContainer();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in loading table");
        }
    }

    private void refreshLobby(String gameType, boolean join, Player player) {
        if (gameType.equals("POKER")) {
            if (join) {
                plbc.addPlayer(player);
                plbc.refreshPlayerContainer();
            } else {
                plbc.removePlayer(player);
                plbc.refreshPlayerContainer();
            }
        } else {
            if (join) {
                rlbc.addPlayer(player);
                rlbc.refreshPlayerContainer();
            } else {
                rlbc.removePlayer(player);
                rlbc.refreshPlayerContainer();
            }
        }
    }

    private void leaveLobby() {
        plbc = null;
        rlbc = null;
        openMenu();
    }

    private void changeStatus(String gameType, boolean isReady, Player player) {
        System.out.println(gameType);
        if (gameType.equals("POKER")) {
            plbc.changeStatus(isReady, player);
            plbc.refreshPlayerContainer();
        } else {
            rlbc.changeStatus(isReady, player);
            rlbc.refreshPlayerContainer();
        }
    }

    private void startGameRemik(List<Player> players, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("remikMain.fxml"));
            Parent root = loader.load();
            Main.stage.close();
            Main.stage.setScene(new Scene(root));
            Main.stage.setFullScreen(true);
            Main.stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in loading table");
        }
    }

    private void startGame(List<Player> players, Player player) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PokerTable.fxml"));
            Parent root = loader.load();
            this.ptc = loader.getController();
            this.ptc.setYourData(player.getPlayerData(), player.money);
            this.ptc.assignPlayers(players);
            Main.stage.close();
            Main.stage.setScene(new Scene(root));
            Main.stage.setFullScreen(true);
            Main.stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error in loading table");
        }
    }

    private void setFirstCard(Karta card) {
        this.ptc.setYourCard1(card);
    }

    private void setSecondCard(Karta card) {
        this.ptc.setYourCard2(card);
    }

    private void smallBlind() {
        this.ptc.smallBlind();
    }

    private void otherSmallBlind(Player player) {
        this.ptc.otherSmallBlind(player);
    }

    private void bigBlind() {
        this.ptc.bigBlind();
    }

    private void otherBigBlind(Player player) {
        this.ptc.otherBigBlind(player);
    }

    private void setTableCard(Integer cardIndex, Karta card) {
        this.ptc.setTableCard(cardIndex, card);
    }

    private void makeMove(GamePacket.MOVE_TYPE moveType, Integer currentBid) {
        this.ptc.makeMove(moveType, currentBid);
    }

    private void otherMakeMove(Player player, GamePacket.MOVE_TYPE moveType, Integer money) {
        this.ptc.otherMakeMove(player, moveType, money);
    }

    private void showWinner(String winnerUsername, Player player, Karta card1, Karta card2) {
        this.ptc.showWinner(winnerUsername, player, card1, card2);
    }


    private void showLoginErr(String error) {
        lc.enableErr(error);
    }

    private void showRegisterErr(String error) {
        rc.enableErr(error);
    }

    public void sendPacket(Packet packet) {
        try {
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Client() {
        try {
            clientSocket = new Socket("127.0.0.1", 1234);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLoginController(LoginController lc) {
        this.lc = lc;
    }

    public void setMenuController(MenuController mc) {
        this.mc = mc;
    }

    public void setRegisterController(RegisterController rc) {
        this.rc = rc;
    }
}
