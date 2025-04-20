package com.example.casino.Server;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static com.example.casino.Server.GameServer.connection;

public class FutureTaskCallback extends FutureTask<HashMap<ClientHandler, Integer>> {
    public FutureTaskCallback(Callable<HashMap<ClientHandler, Integer>> callable) {
        super(callable);
    }

    public void done() {
        if (isCancelled()) System.out.println("gra przerwana ;{");
        else {
            try {
                HashMap<ClientHandler, Integer> chMap = get();
                for (Map.Entry<ClientHandler, Integer> set : chMap.entrySet()) {
                    Statement st = connection.createStatement();
                    String sql = "UPDATE pokerRanking SET Points = Points + " + "'" + set.getValue() + "'" + "WHERE UserID = " + "'" + set.getKey().getPlayer().getPlayerID() + "'";
                    st.executeUpdate(sql);
                }
            } catch (InterruptedException | ExecutionException | SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
