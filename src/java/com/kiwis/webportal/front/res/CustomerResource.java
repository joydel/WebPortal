/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kiwis.webportal.front.res;

import com.kiwis.webportal.domain.servicefacade.CustomerJpaControllerFacade;
import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aybeh
 */

    
/**
 * Topic REST resource controller.
 */
@Path("/res/")
@ManagedBean
public class CustomerResource {

    private static Logger log=LoggerFactory.getLogger(CustomerResource.class);

    @Inject CustomerJpaControllerFacade customerFacade;

    /**
     * Creates a new topic proposal record.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/topics")
    public void newTopic(@Context HttpServletRequest request, ProposalEntity proposal) {

    	log.info("POST topic: IP {}",request.getRemoteAddr());
        log.debug("POST topic:"+proposal.getTitle()+"/"+proposal.getType());

        try {
            topicFacade.newTopic(proposal);
        }
        catch (ValidationException ex) {
            // That's for later
            log.warn("{} Validation errors! ",ex.getIssues().size());
        }
        catch (SystemException ex) {
            // Something went wrong. Cannot be handled gravefully.
            log.error("Trouble!",ex);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
