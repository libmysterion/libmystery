
package com.mystery.libmystery.persistence;

import com.mystery.libmystery.bytes.ByteFunctions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

public class ObjectReader<T> {

    private T readObject;
    private String path;

    public ObjectReader(String path) {
        this.path = path;
    }

    public T read() throws FileNotFoundException, IOException, ClassNotFoundException {
        if (readObject == null) {
            File file = new File(this.path);
            int len = (int) file.length();
            FileInputStream is = new FileInputStream(file);

            byte[] buffer = new byte[len];
            is.read(buffer);
            Serializable deSerialized = ByteFunctions.deSerialize(buffer, null);
            readObject = (T) deSerialized;
        }
        return readObject;
    }
}
