package com.mystery.libmystery.javafx;

import com.mystery.libmystery.injection.Injector;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.LoggerFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.slf4j.Logger;

public abstract class FXView<Controller> {

    private Injector injector;
    protected FXMLLoader fxmlLoader;
    private static final Logger log = LoggerFactory.getLogger(FXView.class);

    public FXView() {
        if (!getClass().getSimpleName().endsWith("View")) {
            throw new IllegalStateException("An FXView subclass must be named with the \"View\" suffix e.g. MysteryView");
        }
        injector = new Injector();
    }

    private void load() {
        if (fxmlLoader == null) {
            String viewClassName = getClass().getSimpleName().toLowerCase();
            String fxmlViewName = "/" + viewClassName.substring(0, viewClassName.length() - "view".length()) + ".fxml";
            String packageName = getClass().getPackage().getName();
            packageName = packageName.replaceAll("\\.", "/");
            load("/" + packageName + fxmlViewName);
        }
    }

    private void load(String fxmlView) {
        try {
            fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory((clazz) -> injector.create(clazz));
            InputStream fxmlStream = getClass().getResourceAsStream(fxmlView);
            if(fxmlStream == null){
                throw new FXViewException("could not locate .fxml resource [" + fxmlView.replaceAll("/", ".").substring(1) + "]");
            }
            fxmlLoader.load(fxmlStream);
        } catch (IOException ex) {
            log.error("could not load fxml view", ex);
            throw new FXViewException("could not load fxml view", ex);
        }
    }

    public Controller getController() {
        load();
        return fxmlLoader.getController();
    }

    public Scene getScene() {
        load();
        return new Scene(fxmlLoader.getRoot());
    }

    public Parent getRootNode() {
        load();
        return fxmlLoader.getRoot();
    }

}


class FXViewException extends RuntimeException {

        
    public FXViewException(String msg) {
        super(msg);
    }
    
    public FXViewException(String msg, Throwable t) {
        super(msg, t);
    }

        
    public FXViewException(Throwable t) {
        super(t);
    }
    
    
}
