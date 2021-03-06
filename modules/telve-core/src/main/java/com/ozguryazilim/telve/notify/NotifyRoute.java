package com.ozguryazilim.telve.notify;

import javax.enterprise.context.Dependent;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;

/**
 * Telve MessageBus üzerinden gelecek Notify Mesajları için Route
 * @author Hakan Uygun
 */
@Dependent @ContextName("telve")
public class NotifyRoute extends RouteBuilder{

    @Override
    public void configure() throws Exception {
        from("seda:notify")
           .to("bean:notifyDispacher");
    }
    
}
