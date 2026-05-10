package view;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainView {
    private Stage stage;

    public MainView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        Label title = new Label("Assignment 6 - Polymorphic Pals");

        VBox root = new VBox(10);
        root.getChildren().add(title);

        Scene scene = new Scene(root, 600, 400);

        stage.setTitle("Assignment 6");
        stage.setScene(scene);
        stage.show();
    }
}