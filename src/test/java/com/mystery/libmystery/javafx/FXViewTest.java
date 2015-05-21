package com.mystery.libmystery.javafx;

import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;


public class FXViewTest {

    public FXViewTest() {
    }

    @Test
    public void shouldLoadFxmlFile() {

        class TestView extends FXView<TestController> {
              
        }
        
        TestView test = new TestView();
        assertNotNull(test.getScene());
        assertNotNull(test.getController());
        assertNotNull(test.getController().getVbox());          // we can inject with @FXML
        assertNotNull(test.getController().getTestThing());     // we can inject with @Inject
        
    }
    
    
    @Test
    public void shouldThrowFxmlViewException() {

        class TesterView extends FXView<TestController> {
              
        }
        
        TesterView test = new TesterView();
        boolean caught = false;
        try{
            test.getScene();
        }catch(FXViewException e){
            assertThat(e.getMessage(), is("could not locate .fxml resource [com.mystery.libmystery.javafx.tester.fxml]"));
            caught = true;
        }
        
        assertThat(caught, is(true));
        
    }
    
    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfNotNamedWithViewSuffix() {

        class Test extends FXView<TestController> {
              
        }
        Test test = new Test();        
    }

}
