package com.quathar.chatserver;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Test extends Application {

    private TextArea messageArea;
    private TextField inputField;
    private String testStr;

    Button button;

    public Test() {
        System.out.println("COnstructor");
    }

    @Override
    public void init() {
        System.out.println("Init");
        Parameters parameters = getParameters();
        List<String> args = parameters.getRaw();
        System.out.println(args.get(1));
        testStr = "Test if this is shown in start";
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println(testStr);

        System.out.println("Start");
        primaryStage.setTitle("Chat Server");

        // Create UI components
        messageArea = new TextArea();
        messageArea.setEditable(false);
        inputField = new TextField();
        Button sendButton = new Button("Send");
//        sendButton.setOnAction(event -> sendMessage());

        // Create layout
        VBox messageBox = new VBox(messageArea);
        messageBox.setPadding(new Insets(10));

        HBox inputBox = new HBox(inputField, sendButton);
        inputBox.setSpacing(10);
        inputBox.setPadding(new Insets(10));

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(messageBox);
        borderPane.setBottom(inputBox);

        // Set scene and show the stage
        Scene scene = new Scene(borderPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        args = new String[]{
                "test1", "test2"
        };
        launch(args);
    }

}