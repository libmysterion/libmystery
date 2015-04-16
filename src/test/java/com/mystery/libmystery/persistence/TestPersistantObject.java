
package com.mystery.libmystery.persistence;

import java.io.IOException;
import java.util.ArrayList;


public class TestPersistantObject extends PersistantObject {

    private String field1;
    private String field2;
    private ArrayList<String> items;
    private int number;
    
    public TestPersistantObject() {
        super("C:\\Test\\test_peristant_object");
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    
    
    
    
    
    public static void main(String[] args) throws IOException {
        
      
        
    }
    
}
