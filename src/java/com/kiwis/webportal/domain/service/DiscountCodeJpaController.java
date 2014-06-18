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
import com.kiwis.webportal.domain.entity.DiscountCode;
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
public class DiscountCodeJpaController implements Serializable {

    public DiscountCodeJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(DiscountCode discountCode) throws PreexistingEntityException, Exception {
        if (discountCode.getCustomerCollection() == null) {
            discountCode.setCustomerCollection(new ArrayList<Customer>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Customer> attachedCustomerCollection = new ArrayList<Customer>();
            for (Customer customerCollectionCustomerToAttach : discountCode.getCustomerCollection()) {
                customerCollectionCustomerToAttach = em.getReference(customerCollectionCustomerToAttach.getClass(), customerCollectionCustomerToAttach.getCustomerId());
                attachedCustomerCollection.add(customerCollectionCustomerToAttach);
            }
            discountCode.setCustomerCollection(attachedCustomerCollection);
            em.persist(discountCode);
            for (Customer customerCollectionCustomer : discountCode.getCustomerCollection()) {
                DiscountCode oldDiscountCodeOfCustomerCollectionCustomer = customerCollectionCustomer.getDiscountCode();
                customerCollectionCustomer.setDiscountCode(discountCode);
                customerCollectionCustomer = em.merge(customerCollectionCustomer);
                if (oldDiscountCodeOfCustomerCollectionCustomer != null) {
                    oldDiscountCodeOfCustomerCollectionCustomer.getCustomerCollection().remove(customerCollectionCustomer);
                    oldDiscountCodeOfCustomerCollectionCustomer = em.merge(oldDiscountCodeOfCustomerCollectionCustomer);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findDiscountCode(discountCode.getDiscountCode()) != null) {
                throw new PreexistingEntityException("DiscountCode " + discountCode + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(DiscountCode discountCode) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            DiscountCode persistentDiscountCode = em.find(DiscountCode.class, discountCode.getDiscountCode());
            Collection<Customer> customerCollectionOld = persistentDiscountCode.getCustomerCollection();
            Collection<Customer> customerCollectionNew = discountCode.getCustomerCollection();
            List<String> illegalOrphanMessages = null;
            for (Customer customerCollectionOldCustomer : customerCollectionOld) {
                if (!customerCollectionNew.contains(customerCollectionOldCustomer)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Customer " + customerCollectionOldCustomer + " since its discountCode field is not nullable.");
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
            discountCode.setCustomerCollection(customerCollectionNew);
            discountCode = em.merge(discountCode);
            for (Customer customerCollectionNewCustomer : customerCollectionNew) {
                if (!customerCollectionOld.contains(customerCollectionNewCustomer)) {
                    DiscountCode oldDiscountCodeOfCustomerCollectionNewCustomer = customerCollectionNewCustomer.getDiscountCode();
                    customerCollectionNewCustomer.setDiscountCode(discountCode);
                    customerCollectionNewCustomer = em.merge(customerCollectionNewCustomer);
                    if (oldDiscountCodeOfCustomerCollectionNewCustomer != null && !oldDiscountCodeOfCustomerCollectionNewCustomer.equals(discountCode)) {
                        oldDiscountCodeOfCustomerCollectionNewCustomer.getCustomerCollection().remove(customerCollectionNewCustomer);
                        oldDiscountCodeOfCustomerCollectionNewCustomer = em.merge(oldDiscountCodeOfCustomerCollectionNewCustomer);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = discountCode.getDiscountCode();
                if (findDiscountCode(id) == null) {
                    throw new NonexistentEntityException("The discountCode with id " + id + " no longer exists.");
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
            DiscountCode discountCode;
            try {
                discountCode = em.getReference(DiscountCode.class, id);
                discountCode.getDiscountCode();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The discountCode with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Customer> customerCollectionOrphanCheck = discountCode.getCustomerCollection();
            for (Customer customerCollectionOrphanCheckCustomer : customerCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This DiscountCode (" + discountCode + ") cannot be destroyed since the Customer " + customerCollectionOrphanCheckCustomer + " in its customerCollection field has a non-nullable discountCode field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(discountCode);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<DiscountCode> findDiscountCodeEntities() {
        return findDiscountCodeEntities(true, -1, -1);
    }

    public List<DiscountCode> findDiscountCodeEntities(int maxResults, int firstResult) {
        return findDiscountCodeEntities(false, maxResults, firstResult);
    }

    private List<DiscountCode> findDiscountCodeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(DiscountCode.class));
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

    public DiscountCode findDiscountCode(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(DiscountCode.class, id);
        } finally {
            em.close();
        }
    }

    public int getDiscountCodeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<DiscountCode> rt = cq.from(DiscountCode.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
