package com.ozguryazilim.telve.link;

import java.util.HashMap;
import java.util.Map;

/**
 * Link Resolverlar için registery.
 * 
 * @see Feature
 * 
 * @author Hakan Uygun
 */
public class LinkRegistery {
    
    private static final Map<String, AbstractLinkResolver> resolvers = new HashMap<>();
    
    public static void register( AbstractLinkResolver resolver ){
        resolvers.put(resolver.getDomainName(), resolver);
    }
    
    
    public static AbstractLinkResolver getResolver( String name ){
        return resolvers.get(name);
    }
    
}
