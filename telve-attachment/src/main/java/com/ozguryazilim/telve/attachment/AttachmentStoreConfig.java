/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.telve.attachment;

/**
 *
 * @author oyas
 */
public interface AttachmentStoreConfig {

    boolean canSupportFeature( String featureName, String storeName );
    
}
