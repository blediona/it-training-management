package services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneService {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadView(String viewName, String title) {
        try {
            boolean wasShowing = primaryStage.isShowing();
            boolean wasMaximized = primaryStage.isMaximized();
            boolean wasFullScreen = primaryStage.isFullScreen();
            double previousX = primaryStage.getX();
            double previousY = primaryStage.getY();
            double previousWidth = primaryStage.getWidth();
            double previousHeight = primaryStage.getHeight();

            FXMLLoader loader = new FXMLLoader(SceneService.class.getResource("/views/" + viewName), I18nService.getBundle());
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(SceneService.class.getResource("/styles/app.css").toExternalForm());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);

            if (wasShowing) {
                primaryStage.setX(previousX);
                primaryStage.setY(previousY);
                primaryStage.setWidth(previousWidth);
                primaryStage.setHeight(previousHeight);
                primaryStage.setMaximized(wasMaximized);
                primaryStage.setFullScreen(wasFullScreen);
            }

            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException("Could not load view: " + viewName, e);
        }
    }
}