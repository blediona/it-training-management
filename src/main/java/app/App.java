package app;

import javafx.application.Application;
import javafx.stage.Stage;
import services.DatabaseService;
import services.I18nService;
import services.SceneService;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        DatabaseService.initializeDatabase();
        SceneService.setPrimaryStage(stage);
        SceneService.loadView("login-view.fxml", I18nService.get("app.title"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
