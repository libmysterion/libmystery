
package com.mystery.libmystery.persistence;

import com.mystery.libmystery.bytes.ByteFunctions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;


public class ObjectWriter<T extends Serializable> {
    
    private T object;
    private String path;
    
    public ObjectWriter (String path, T object){
        this.path = path;
        this.object = object;
    }
    
    public void write() throws IOException {
        File f = new File(this.path);
        try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            byte[] serialized = ByteFunctions.serialize(this.object);
            fileOutputStream.write(serialized);
        }
    }

}
