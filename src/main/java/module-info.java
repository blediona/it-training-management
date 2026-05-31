module com.ittraining.management {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires mysql.connector.j;
    requires jakarta.mail;

    opens app to javafx.fxml;
    opens controllers to javafx.fxml;
    opens models to javafx.base;
    opens models.dto to javafx.base;

    exports app;
    exports controllers;
    exports models;
    exports models.dto;
    exports services;
    exports repository;
}
