package com.example.casino.Server;

import com.example.casino.*;
import com.example.casino.Packets.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.casino.Server.GameServer.*;

public class ClientHandler extends Thread {
    private Player player;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String UUID;

    @Override
    public String toString() {
        return player.getPlayerData();
    }

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Błąd połączenia komunikacyjnego z klientem");
        }
    }

    public PokerHand.Ranks hand() {
        return player.pokerHand.rank();
    }

    public void run() {
        Packet request;
        while (true) {
            try {
                request = (Packet) in.readObject();
                if (request == null) break;
                parseRequest(request);

            } catch (EOFException e) {
                System.err.println("Klient zamknął połączenie: " + clientSocket.getInetAddress());
                GameServer.removeNick(this.player.getPlayerData());
                break;

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Nie udało się odczytac pakietu");
                GameServer.removeNick(this.player.getPlayerData());
                throw new RuntimeException(e);
            }
        }

        stopConnection();
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseRequest(Packet request) {
        switch (request.getType()) {
            case ACK:
                System.out.println("Dołączył nowy użytkownik: " + clientSocket.getInetAddress());
                sendPacket(new Packet(PacketType.ACK, "hello"));
                break;
            case LOGIN: {
                System.out.println("Prośba logowania: " + clientSocket.getInetAddress());
                LoginPacket loginRequest = (LoginPacket) request;
                String desc = request.getDesc();
                String username = loginRequest.getLogin();
                String passw = loginRequest.getPassword();
                LoginPacket.Status status = loginRequest.getStatus();
                if (status.equals(LoginPacket.Status.LOGOUT)) {
                    sendPacket(new LoginPacket("Loging out", LoginPacket.Status.LOGOUT));
                    GameServer.removeNick(this.player.getPlayerData());
                } else {
                    try {
                        Statement st = connection.createStatement();
                        ResultSet rs = st.executeQuery("select UserID, Password, Salt from users where Username='"
                                + username + "'");
                        if (rs.next()) {
                            Integer userID = rs.getInt(1);
                            String password = rs.getString(2);
                            String salt = rs.getString(3);
                            if (PassHash.comparePasswd(passw, salt, password) && GameServer.checkAvailability(username)) {
                                player = new Player(userID, username, false);
                                GameServer.addNick(username);
                                sendPacket(new LoginPacket("Logged In", player, LoginPacket.Status.LOGIN));
                                sendPacket(new LoginPacket("PasswordError", LoginPacket.Status.PASWWORD_ERROR));
                            } else {
                                sendPacket(new LoginPacket("PasswordError", LoginPacket.Status.PASWWORD_ERROR));
                            }
                        } else {
                            sendPacket(new LoginPacket("AccountNotFoundError",
                                    LoginPacket.Status.ACOUNT_NOT_FOUND_ERROR));
                        }
                    } catch (SQLException | InvalidKeySpecException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case REGISTER: {
                System.out.println("Prośba rejestracji " + clientSocket.getInetAddress());
                RegisterPacket registerRequest = (RegisterPacket) request;
                String data = registerRequest.getDesc();
                String email = registerRequest.getEmail();
                String username = registerRequest.getLogin();
                String date = registerRequest.getDate();
                String passw = registerRequest.getPassword();   //zahashowane
                String slt = registerRequest.getSalt();
                try {
                    Statement st = connection.createStatement();
                    ResultSet rs = st.executeQuery("select * from users where Email='" + email + "'");
                    //TODO: ResultSet rs2 = st.executeQuery("select * from users where Username='" + username + "'");

                    if (rs.next()) {
                        sendPacket(new RegisterPacket("Account already exists",
                                RegisterPacket.Status.ACOUNT_ALREADY_EXISTS_ERROR));
                    } else {
                        String sql = "INSERT INTO users (Username, Email, Password, Salt, DateOfBirth) " +
                                "VALUES (?,?,?,?,?)";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);
                        preparedStatement.setString(1, username);
                        preparedStatement.setString(2, email);
                        preparedStatement.setString(3, passw);
                        preparedStatement.setString(4, slt);
                        preparedStatement.setString(5, date);
                        preparedStatement.execute();
                        st = connection.createStatement();
                        rs = st.executeQuery("select userID from users where Username='" + username + "'");
                        if (rs.next()) {
                            Integer userID = rs.getInt(1);
                            player = new Player(userID, username, false);
                            GameServer.addNick(username);
                            sql = "INSERT INTO pokerRanking (UserID, Points) " +
                                    "VALUES (?,?)";
                            preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setInt(1, userID);
                            preparedStatement.setInt(2, 0);
                            preparedStatement.execute();
                            sql = "INSERT INTO rummyRanking (UserID, Points) " +
                                    "VALUES (?,?)";
                            preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setInt(1, userID);
                            preparedStatement.setInt(2, 0);
                            preparedStatement.execute();
                        }

                        sendPacket(new RegisterPacket("Registered", player, RegisterPacket.Status.REGISTER));

                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case CREATEGAME: {
                System.out.println("Tworzenie gry");
                CreateGamePacket createGamePacket = (CreateGamePacket) request;
                String data = createGamePacket.getDesc();
                CreateGamePacket.GameType gameType = createGamePacket.getGameType();
                String UUID = createGamePacket.getUUID();
                this.UUID = createGamePacket.getUUID();
                if (gameType.equals(CreateGamePacket.GameType.POKER)) {
                    GameServer.pokerGames.put(UUID, new PokerGame(UUID, this));
                    System.out.println("Stworzono nową gre pokera: " + UUID);
                    sendPacket(new CreateGamePacket("GameCreated:" + UUID, UUID,
                            CreateGamePacket.GameType.POKER));
                } else {
                    GameServer.rummyGames.put(UUID, new RummyGame(UUID, this));
                    System.out.println("Stworzono nową gre remika: " + UUID);
                    sendPacket(new CreateGamePacket("GameCreated:" + UUID, UUID,
                            CreateGamePacket.GameType.RUMMY));
                }
                break;
            }
            case JOINGAME: {
                JoinGamePacket joinRequest = (JoinGamePacket) request;
                System.out.println("Dołączanie do gry");
                System.out.println(joinRequest.getUUID());
                this.UUID = joinRequest.getUUID();
                JoinGamePacket.GameType gameType = joinRequest.getGameType();
                JoinGamePacket.Status status = joinRequest.getStatus();
                System.out.println(gameType);


                if (gameType.equals(JoinGamePacket.GameType.POKER)) {
                    PokerGame game = GameServer.pokerGames.get(joinRequest.getUUID());

                    if (game == null) {
                        System.err.println("Gra z podaneym kodem nie istnieje: " + joinRequest.getUUID());
                        break;
                    }

                    if (status.equals(JoinGamePacket.Status.LEAVE)) {
                        System.out.println("user opuszcza lobby");
                        game.players.remove(this);
                        game.playersData.remove(this.player);
                        if (game.players.isEmpty()) {
                            GameServer.pokerGames.remove(joinRequest.getUUID());
                        } else {
                            game.broadcast(new JoinGamePacket("USERLEFT", joinRequest.getUUID(),
                                    JoinGamePacket.GameType.POKER, this.player, JoinGamePacket.Status.USER_LEFT));
                        }
                        sendPacket(new JoinGamePacket("LEFT", joinRequest.getUUID(),
                                JoinGamePacket.GameType.POKER, JoinGamePacket.Status.LEFT));
                    } else {
                        if (game.players.size() == 5) {
                            System.out.println("pełne lobby");
                        } else {
                            game.broadcast(new JoinGamePacket("USERJOINED", joinRequest.getUUID(),
                                    JoinGamePacket.GameType.POKER, this.player, JoinGamePacket.Status.USER_JOIN));
                            game.players.add(this);
                            game.playersData.add(this.player);
                            sendPacket(new JoinGamePacket("JOINED", joinRequest.getUUID(),
                                    JoinGamePacket.GameType.POKER, game.playersData, JoinGamePacket.Status.JOINED));
                        }
                    }
                } else {
                    RummyGame game = GameServer.rummyGames.get(joinRequest.getUUID());

                    if (game == null) {
                        System.err.println("Gra z podaneym kodem nie istnieje: " + joinRequest.getUUID());
                        break;
                    }

                    if (status.equals(JoinGamePacket.Status.LEAVE)) {
                        System.out.println("user opuszcza lobby");
                        game.players.remove(this);
                        game.playersData.remove(this.player);
                        if (game.players.isEmpty()) {
                            GameServer.rummyGames.remove(joinRequest.getUUID());
                        } else {
                            game.broadcast(new JoinGamePacket("USERLEFT", joinRequest.getUUID(),
                                    JoinGamePacket.GameType.RUMMY, this.player, JoinGamePacket.Status.USER_LEFT));
                        }
                        sendPacket(new JoinGamePacket("LEFT", joinRequest.getUUID(),
                                JoinGamePacket.GameType.RUMMY, JoinGamePacket.Status.LEFT));
                    } else {
                        game.broadcast(new JoinGamePacket("USERJOINED", joinRequest.getUUID(),
                                JoinGamePacket.GameType.RUMMY, this.player, JoinGamePacket.Status.USER_JOIN));
                        game.players.add(this);
                        game.playersData.add(this.player);
                        sendPacket(new JoinGamePacket("JOINED", joinRequest.getUUID(),
                                JoinGamePacket.GameType.RUMMY, game.playersData, JoinGamePacket.Status.JOINED));
                    }
                }
                break;
            }
            case GAME_READY_STATUS: {
                GameReadyPacket gameReadyRequest = (GameReadyPacket) request;
                String data = gameReadyRequest.getDesc();
                String uuid = gameReadyRequest.getUUID();
                GameReadyPacket.Status status = gameReadyRequest.getStatus();
                GameReadyPacket.GameType gameType = gameReadyRequest.getGameType();
                if (gameType.equals(GameReadyPacket.GameType.POKER)) {
                    if (status.equals(GameReadyPacket.Status.READY)) {
                        GameServer.pokerGames.get(uuid).playersReady++;
                        player.setReady(true);
                        GameServer.pokerGames.get(uuid).broadcast(new GameReadyPacket("ready", gameType,
                                player, uuid, GameReadyPacket.Status.READY));
                        if (GameServer.pokerGames.get(uuid).playersReady > 1 && pokerGames.get(uuid).playersReady == pokerGames.get(uuid).players.size()) {

                            GameServer.addNewGameThread(GameServer.pokerGames.get(uuid));
                        }
                    } else {
                        GameServer.pokerGames.get(uuid).playersReady--;
                        player.setReady(false);
                        GameServer.pokerGames.get(uuid).broadcast(new GameReadyPacket("notready", gameType,
                                player, uuid, GameReadyPacket.Status.NOT_READY));
                    }
                } else {
                    if (status.equals(GameReadyPacket.Status.READY)) {
                        GameServer.rummyGames.get(uuid).playersReady++;
                        player.setReady(true);
                        GameServer.rummyGames.get(uuid).broadcast(new GameReadyPacket("ready", gameType,
                                player, uuid, GameReadyPacket.Status.READY));
                        if (GameServer.rummyGames.get(uuid).playersReady == 2) {
                            Future<Player> future = GameServer.executorService.submit(GameServer.rummyGames.get(uuid));
                        }
                    } else {
                        GameServer.rummyGames.get(uuid).playersReady--;
                        player.setReady(false);
                        GameServer.rummyGames.get(uuid).broadcast(new GameReadyPacket("notready", gameType,
                                player, uuid, GameReadyPacket.Status.NOT_READY));
                    }
                }
                break;
            }
            case GAME: {
                GamePacket gamePacket = (GamePacket) request;
                GamePacket.Status gamePacketStatus = gamePacket.getStatus();
                if (gamePacketStatus.equals(GamePacket.Status.MOVE)) {
                    switch (gamePacket.getMove_type()) {
                        case FOLD -> GameServer.pokerGames.get(UUID).handlerFold(this);
                        case CALL -> GameServer.pokerGames.get(UUID).handlerCall(this);
                        case RAISE -> GameServer.pokerGames.get(UUID).handlerRaise(this, 50);
                    }
                    GameServer.pokerGames.get(this.UUID).unlockLock();
                }
                break;

            }
            case RANKING: {
                RankingPacket rankingPacket = (RankingPacket) request;
                RankingPacket.Status rankingPacketStatus = rankingPacket.getStatus();
                if (rankingPacketStatus.equals(RankingPacket.Status.POKER)) {
                    HashMap<String, Integer> pokerRankingMap = new HashMap<>();

                    try {
                        Statement st = connection.createStatement();
                        ResultSet rs = st.executeQuery("SELECT users.username, pokerRanking.Points FROM pokerRanking JOIN users USING(UserID) ORDER BY pokerRanking.Points DESC LIMIT 10");

                        while (rs.next()) {
                            String username = rs.getString("Username");
                            int points = rs.getInt("Points");
                            pokerRankingMap.put(username, points);
                        }

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    sendPacket(new RankingPacket("Switching to Poker Ranking", RankingPacket.Status.POKER, pokerRankingMap));
                } else if (rankingPacketStatus.equals(RankingPacket.Status.REMIK)) {
                    HashMap<String, Integer> remikRankingMap = new HashMap<>();

                    try {
                        Statement st = connection.createStatement();
                        ResultSet rs = st.executeQuery("SELECT users.username, rummyRanking.Points FROM rummyRanking JOIN users USING(UserID) ORDER BY rummyRanking.Points DESC LIMIT 10"); //do zmiany na remik

                        while (rs.next()) {
                            String username = rs.getString("Username");
                            int points = rs.getInt("Points");
                            remikRankingMap.put(username, points);
                        }

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    sendPacket(new RankingPacket("Switching to Remik Ranking", RankingPacket.Status.REMIK, remikRankingMap));
                }

                break;
            }
            default: {
                System.err.println("NIEZNANY PAKIET");
            }
        }

    }


    public void sendPacket(Packet packet) {
        try {
            out.writeObject(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Player getPlayer() {
        return player;
    }
}
