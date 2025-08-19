open module com.ishidaw.snakefxgl {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires com.almasb.fxgl.entity;
    requires javafx.graphics;
    requires com.almasb.fxgl.core;

    exports com.ishidaw.snakefxgl;
    exports com.ishidaw.snakefxgl.Entities;
    exports com.ishidaw.snakefxgl.Enums;
    exports com.ishidaw.snakefxgl.Utils;
}