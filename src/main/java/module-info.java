module com.ishidaw.snakefxgl {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires com.almasb.fxgl.entity;
    requires javafx.graphics;

    opens com.ishidaw.snakefxgl to javafx.fxml;
    exports com.ishidaw.snakefxgl;
}