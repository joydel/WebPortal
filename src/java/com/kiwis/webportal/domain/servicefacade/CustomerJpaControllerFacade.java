/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kiwis.webportal.domain.servicefacade;

import com.kiwis.webportal.domain.entity.Customer;
import com.kiwis.webportal.domain.service.CustomerJpaController;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

/**
 *
 * @author aybeh
 */

    /**
 * Service Facade EJB for Topics related operations.
 * <p/>
 * Facades are thin transaction boundary beans and always start a TX propagated
 * to one or more services. The real power of this architecture becomes clear
 * with more complex business operations, distributed over multiple services.
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
//
public class CustomerJpaControllerFacade {

    @Inject CustomerJpaController customerController;
    
    public List<Customer> findCustomerEntities() {
        return customerController.findCustomerEntities();
    }
    
}

