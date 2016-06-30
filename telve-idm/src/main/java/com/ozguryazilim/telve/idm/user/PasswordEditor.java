/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.telve.idm.user;


import com.google.common.base.Strings;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.config.AbstractOptionPane;
import com.ozguryazilim.telve.config.OptionPane;
import com.ozguryazilim.telve.idm.config.IdmPages;
import com.ozguryazilim.telve.idm.entities.User;
import com.ozguryazilim.telve.messages.FacesMessages;
import javax.inject.Inject;
import org.apache.deltaspike.jpa.api.transaction.Transactional;


/**
 * Parola Değiştirme Ekranı.
 * 
 * Bir OptionPane olarak görünür.
 * 
 * @author Hakan Uygun
 */
@OptionPane(optionPage = IdmPages.PasswordOptionPane.class)
public class PasswordEditor extends AbstractOptionPane{

    private String newPassword;
    private String oldPassword;

    @Inject
    private UserRepository userRepository;
    
    @Inject
    private Identity identity;

    
    @Override @Transactional
    public void save() {
        
        User user = userRepository.findAnyByLoginName(identity.getLoginName());
        
        //FIXME: Eski parolayı da doğrulamak lazım.
        
        if( user == null ){
            FacesMessages.error("passwordEditor.message.PasswordError");
            return;
        }


        //Yeni parola var mı bir kontrol edelim.
        if( Strings.isNullOrEmpty(newPassword)){
            FacesMessages.error("passwordEditor.message.PasswordError");
            return;
        }

        //FIXME: Şu HASH işini halledelim.
        user.setPasswordEncodedHash(newPassword);
        user.setPasswordSalt(newPassword);
        
        userRepository.save(user);
        
        FacesMessages.info("passwordEditor.message.Success");

    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    
}
