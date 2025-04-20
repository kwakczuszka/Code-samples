package com.example.casino.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private ServerSocket serverSocket;
    public static String connectionUrl = "jdbc:mysql://i3m.h.filess.io:3307/JavaProject_fourthtalk";
    public static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(GameServer.connectionUrl,
                    "JavaProject_fourthtalk", "26c741dadf126863995c714674f8a4c681c4dfb3");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, PokerGame> pokerGames = new HashMap<>();
    public static Map<String, RummyGame> rummyGames = new HashMap<>();
    private static ArrayList<String> nicknames = new ArrayList<>();
    public static ExecutorService executorService;
    public static ArrayList<ClientHandler> handlers = new ArrayList<>();
    public static ArrayList<FutureTask<ArrayList<ClientHandler>>> furas = new ArrayList<>();

    public static boolean checkAvailability(String nick) {
        return !nicknames.contains(nick);
    }

    public static ArrayList<String> getNicknames() {
        return nicknames;
    }

    private static TaskManager taskManager;

    private static class TaskManager {
        ExecutorService executor;
        private List<PokerGame> tasks;
        private List<FutureTaskCallback> CallbackTasks;

        public TaskManager() {
            executor = Executors.newCachedThreadPool();
            tasks = new ArrayList<>();
            CallbackTasks = new ArrayList<>();
        }

        public void startTask(PokerGame pokerGame) {
            tasks.add(pokerGame);
            FutureTaskCallback callback = new FutureTaskCallback(pokerGame);
            executor.execute(callback);
        }
    }


    public static void addNick(String nick) {
        if (!nicknames.contains(nick))
            nicknames.add(nick);
    }

    public static void addNewGameThread(PokerGame pokerGame) {
        GameServer.taskManager.startTask(pokerGame);
    }

    public static void removeNick(String nick) {
        nicknames.remove(nick);
    }

    public void start(int port) throws IOException {
        executorService = Executors.newFixedThreadPool(100);
        serverSocket = new ServerSocket(port);
        this.taskManager = new TaskManager();
        System.out.println("---------- URUCHOMIONO SERWER ----------\n");
        System.out.println("ADRES IP SERWERA: " + serverSocket.getInetAddress().getHostAddress());
        System.out.println("PORT: " + serverSocket.getLocalPort());
        while (true) {
            handlers.add(new ClientHandler(serverSocket.accept()));
            handlers.getLast().start();
        }
    }

    public static void main(String[] args) {
        GameServer gs = new GameServer();
        try {
            gs.start(1234);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
