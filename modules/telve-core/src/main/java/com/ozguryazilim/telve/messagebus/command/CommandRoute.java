package com.ozguryazilim.telve.messagebus.command;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;

/**
 * Telve Komut aktarımı için route tanımları.
 *
 * @author Hakan Uygun
 */
@Dependent @ContextName("telve")
public class CommandRoute extends RouteBuilder {


    @Inject
    private CommandProcessor commandProcessor;
    
    @Override
    public void configure() throws Exception {
        //Command endpointinden alınan mesajlar ilgili komut dinleyicisine aktarılıyor.       
        
        from("seda:command")
            .process(commandProcessor);
        

        for( String command : CommandRegistery.getCommands()){
            from(CommandRegistery.getDispacher(command))
               .to(CommandRegistery.getEndpoint(command));
        }
        
        /*
         errorHandler(deadLetterChannel("mock:error"));
 
         from("direct:a")
         .choice()
         .when(header("foo").isEqualTo("bar"))
         .to("direct:b")
         .when(header("foo").isEqualTo("cheese"))
         .to("direct:c")
         .otherwise()
         .to("direct:d");
         */
    }

}
