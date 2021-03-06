package com.ozguryazilim.telve.messagebus.command;

import java.io.Serializable;

/**
 * MessageBus komutları için interface.
 * 
 * @author Hakan Uygun
 */
public interface Command extends Serializable{
 
    /**
     * Kullanıcı, log ve raporlarda görünecek komut adı
     * @return 
     */
    String getName();
    
}
