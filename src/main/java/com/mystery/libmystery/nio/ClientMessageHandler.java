package com.mystery.libmystery.nio;

import com.mystery.libmystery.event.DualHandler;
import java.io.Serializable;

public interface ClientMessageHandler <T extends Serializable> extends DualHandler<AsynchronousObjectSocketChannel, T>{
    
}
