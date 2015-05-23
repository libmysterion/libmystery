package com.mystery.libmystery.javafx;

import com.mystery.libmystery.injection.Inject;
import com.mystery.libmystery.injection.PostConstruct;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class TestController {

    private boolean postConstructCalled = false;
    
    @Inject
    private TestInjected testThing;
    
    @Inject
    private TestInjectedSingleton singleton;

    public TestInjected getTestThing() {
        return testThing;
    }
    
    @PostConstruct
    private void anything(){
        postConstructCalled = true;
    }
    
    public boolean wasPostConstructCalled(){
        return this.postConstructCalled;
    }
    
    @FXML
    private VBox vBox;

    public VBox getVbox() {
        return vBox;
    }

    public TestInjectedSingleton getSingleton() {
        return singleton;
    }
    
    
    
    
    
}
