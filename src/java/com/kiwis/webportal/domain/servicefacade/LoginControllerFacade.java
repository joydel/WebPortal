/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kiwis.webportal.domain.servicefacade;

import com.kiwis.webportal.domain.entity.Customer;
import com.kiwis.webportal.domain.service.CustomerJpaController;
import javax.inject.Inject;

/**
 *
 * @author aybeh
 */
public class LoginControllerFacade {
    @Inject CustomerJpaController customerController;
    public boolean login(String username, String password) {
    boolean valid = customerController.findByAuthInfo(username, password);
    
    }
}
