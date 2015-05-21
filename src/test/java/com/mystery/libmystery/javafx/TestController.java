package com.mystery.libmystery.javafx;

import com.mystery.libmystery.injection.Inject;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class TestController {

    @Inject
    private TestInjected testThing;

    public TestInjected getTestThing() {
        return testThing;
    }
    
    @FXML
    private VBox vBox;

    public VBox getVbox() {
        return vBox;
    }
    
    
    
}
