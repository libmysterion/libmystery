
package com.mystery.libmystery.persistence;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.BeanUtils;


public class PersistantObject implements Serializable {
    
    private transient String path;
    
    public PersistantObject(String path) {
        this.path = path;
        loadIfExisting();
    }

    private void loadIfExisting() {
        File file = new File(path);
        if(file.exists()){
            
            if(file.isDirectory()){
                throw new IllegalArgumentException("path cannot be a directory");
            }
            
            try {
                Object read = new ObjectReader(path).read();
                
                if(read.getClass() != this.getClass()){
                    throw new InvalidClassException("The specified file is of a different class: " + read.getClass().getSimpleName() + " != " + this.getClass().getSimpleName());
                }
                BeanUtils.copyProperties(this, read);
            } catch (IOException ex) {
                Logger.getLogger(PersistantObject.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(PersistantObject.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(PersistantObject.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(PersistantObject.class.getName()).log(Level.SEVERE, null, ex);
            }   
        }
    }
    
    public void save() throws IOException {
        new ObjectWriter(path, this).write();
    }
    
}
