/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kiwis.webportal.domain.service;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.kiwis.webportal.domain.entity.Customer;
import com.kiwis.webportal.domain.entity.MicroMarket;
import com.kiwis.webportal.domain.service.exceptions.IllegalOrphanException;
import com.kiwis.webportal.domain.service.exceptions.NonexistentEntityException;
import com.kiwis.webportal.domain.service.exceptions.PreexistingEntityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author aybeh
 */
public class MicroMarketJpaController implements Serializable {

    public MicroMarketJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MicroMarket microMarket) throws PreexistingEntityException, Exception {
        if (microMarket.getCustomerCollection() == null) {
            microMarket.setCustomerCollection(new ArrayList<Customer>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Customer> attachedCustomerCollection = new ArrayList<Customer>();
            for (Customer customerCollectionCustomerToAttach : microMarket.getCustomerCollection()) {
                customerCollectionCustomerToAttach = em.getReference(customerCollectionCustomerToAttach.getClass(), customerCollectionCustomerToAttach.getCustomerId());
                attachedCustomerCollection.add(customerCollectionCustomerToAttach);
            }
            microMarket.setCustomerCollection(attachedCustomerCollection);
            em.persist(microMarket);
            for (Customer customerCollectionCustomer : microMarket.getCustomerCollection()) {
                MicroMarket oldZipOfCustomerCollectionCustomer = customerCollectionCustomer.getZip();
                customerCollectionCustomer.setZip(microMarket);
                customerCollectionCustomer = em.merge(customerCollectionCustomer);
                if (oldZipOfCustomerCollectionCustomer != null) {
                    oldZipOfCustomerCollectionCustomer.getCustomerCollection().remove(customerCollectionCustomer);
                    oldZipOfCustomerCollectionCustomer = em.merge(oldZipOfCustomerCollectionCustomer);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMicroMarket(microMarket.getZipCode()) != null) {
                throw new PreexistingEntityException("MicroMarket " + microMarket + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MicroMarket microMarket) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MicroMarket persistentMicroMarket = em.find(MicroMarket.class, microMarket.getZipCode());
            Collection<Customer> customerCollectionOld = persistentMicroMarket.getCustomerCollection();
            Collection<Customer> customerCollectionNew = microMarket.getCustomerCollection();
            List<String> illegalOrphanMessages = null;
            for (Customer customerCollectionOldCustomer : customerCollectionOld) {
                if (!customerCollectionNew.contains(customerCollectionOldCustomer)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Customer " + customerCollectionOldCustomer + " since its zip field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Customer> attachedCustomerCollectionNew = new ArrayList<Customer>();
            for (Customer customerCollectionNewCustomerToAttach : customerCollectionNew) {
                customerCollectionNewCustomerToAttach = em.getReference(customerCollectionNewCustomerToAttach.getClass(), customerCollectionNewCustomerToAttach.getCustomerId());
                attachedCustomerCollectionNew.add(customerCollectionNewCustomerToAttach);
            }
            customerCollectionNew = attachedCustomerCollectionNew;
            microMarket.setCustomerCollection(customerCollectionNew);
            microMarket = em.merge(microMarket);
            for (Customer customerCollectionNewCustomer : customerCollectionNew) {
                if (!customerCollectionOld.contains(customerCollectionNewCustomer)) {
                    MicroMarket oldZipOfCustomerCollectionNewCustomer = customerCollectionNewCustomer.getZip();
                    customerCollectionNewCustomer.setZip(microMarket);
                    customerCollectionNewCustomer = em.merge(customerCollectionNewCustomer);
                    if (oldZipOfCustomerCollectionNewCustomer != null && !oldZipOfCustomerCollectionNewCustomer.equals(microMarket)) {
                        oldZipOfCustomerCollectionNewCustomer.getCustomerCollection().remove(customerCollectionNewCustomer);
                        oldZipOfCustomerCollectionNewCustomer = em.merge(oldZipOfCustomerCollectionNewCustomer);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = microMarket.getZipCode();
                if (findMicroMarket(id) == null) {
                    throw new NonexistentEntityException("The microMarket with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MicroMarket microMarket;
            try {
                microMarket = em.getReference(MicroMarket.class, id);
                microMarket.getZipCode();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The microMarket with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Customer> customerCollectionOrphanCheck = microMarket.getCustomerCollection();
            for (Customer customerCollectionOrphanCheckCustomer : customerCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This MicroMarket (" + microMarket + ") cannot be destroyed since the Customer " + customerCollectionOrphanCheckCustomer + " in its customerCollection field has a non-nullable zip field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(microMarket);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MicroMarket> findMicroMarketEntities() {
        return findMicroMarketEntities(true, -1, -1);
    }

    public List<MicroMarket> findMicroMarketEntities(int maxResults, int firstResult) {
        return findMicroMarketEntities(false, maxResults, firstResult);
    }

    private List<MicroMarket> findMicroMarketEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MicroMarket.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public MicroMarket findMicroMarket(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MicroMarket.class, id);
        } finally {
            em.close();
        }
    }

    public int getMicroMarketCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MicroMarket> rt = cq.from(MicroMarket.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
