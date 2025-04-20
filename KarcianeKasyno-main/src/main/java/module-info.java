module com.example.casino {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.sql;

    exports com.example.casino;
    exports com.example.casino.Server;
    exports com.example.casino.Packets;
    exports com.example.casino.Controllers;
    exports com.example.casino.Remik;

    opens com.example.casino to javafx.fxml;
    opens com.example.casino.Server to javafx.fxml;
    opens com.example.casino.Packets to javafx.fxml;
    opens com.example.casino.Controllers to javafx.fxml;
    opens com.example.casino.Remik to javafx.fxml;
}
