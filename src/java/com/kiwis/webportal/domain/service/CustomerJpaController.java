/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kiwis.webportal.domain.service;

import com.kiwis.webportal.domain.entity.Customer;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.kiwis.webportal.domain.entity.MicroMarket;
import com.kiwis.webportal.domain.entity.DiscountCode;
import com.kiwis.webportal.domain.entity.PurchaseOrder;
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
public class CustomerJpaController implements Serializable {
    
    public CustomerJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Customer customer) throws PreexistingEntityException, Exception {
        if (customer.getPurchaseOrderCollection() == null) {
            customer.setPurchaseOrderCollection(new ArrayList<PurchaseOrder>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MicroMarket zip = customer.getZip();
            if (zip != null) {
                zip = em.getReference(zip.getClass(), zip.getZipCode());
                customer.setZip(zip);
            }
            DiscountCode discountCode = customer.getDiscountCode();
            if (discountCode != null) {
                discountCode = em.getReference(discountCode.getClass(), discountCode.getDiscountCode());
                customer.setDiscountCode(discountCode);
            }
            Collection<PurchaseOrder> attachedPurchaseOrderCollection = new ArrayList<PurchaseOrder>();
            for (PurchaseOrder purchaseOrderCollectionPurchaseOrderToAttach : customer.getPurchaseOrderCollection()) {
                purchaseOrderCollectionPurchaseOrderToAttach = em.getReference(purchaseOrderCollectionPurchaseOrderToAttach.getClass(), purchaseOrderCollectionPurchaseOrderToAttach.getOrderNum());
                attachedPurchaseOrderCollection.add(purchaseOrderCollectionPurchaseOrderToAttach);
            }
            customer.setPurchaseOrderCollection(attachedPurchaseOrderCollection);
            em.persist(customer);
            if (zip != null) {
                zip.getCustomerCollection().add(customer);
                zip = em.merge(zip);
            }
            if (discountCode != null) {
                discountCode.getCustomerCollection().add(customer);
                discountCode = em.merge(discountCode);
            }
            for (PurchaseOrder purchaseOrderCollectionPurchaseOrder : customer.getPurchaseOrderCollection()) {
                Customer oldCustomerIdOfPurchaseOrderCollectionPurchaseOrder = purchaseOrderCollectionPurchaseOrder.getCustomerId();
                purchaseOrderCollectionPurchaseOrder.setCustomerId(customer);
                purchaseOrderCollectionPurchaseOrder = em.merge(purchaseOrderCollectionPurchaseOrder);
                if (oldCustomerIdOfPurchaseOrderCollectionPurchaseOrder != null) {
                    oldCustomerIdOfPurchaseOrderCollectionPurchaseOrder.getPurchaseOrderCollection().remove(purchaseOrderCollectionPurchaseOrder);
                    oldCustomerIdOfPurchaseOrderCollectionPurchaseOrder = em.merge(oldCustomerIdOfPurchaseOrderCollectionPurchaseOrder);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findCustomer(customer.getCustomerId()) != null) {
                throw new PreexistingEntityException("Customer " + customer + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Customer customer) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Customer persistentCustomer = em.find(Customer.class, customer.getCustomerId());
            MicroMarket zipOld = persistentCustomer.getZip();
            MicroMarket zipNew = customer.getZip();
            DiscountCode discountCodeOld = persistentCustomer.getDiscountCode();
            DiscountCode discountCodeNew = customer.getDiscountCode();
            Collection<PurchaseOrder> purchaseOrderCollectionOld = persistentCustomer.getPurchaseOrderCollection();
            Collection<PurchaseOrder> purchaseOrderCollectionNew = customer.getPurchaseOrderCollection();
            List<String> illegalOrphanMessages = null;
            for (PurchaseOrder purchaseOrderCollectionOldPurchaseOrder : purchaseOrderCollectionOld) {
                if (!purchaseOrderCollectionNew.contains(purchaseOrderCollectionOldPurchaseOrder)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PurchaseOrder " + purchaseOrderCollectionOldPurchaseOrder + " since its customerId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (zipNew != null) {
                zipNew = em.getReference(zipNew.getClass(), zipNew.getZipCode());
                customer.setZip(zipNew);
            }
            if (discountCodeNew != null) {
                discountCodeNew = em.getReference(discountCodeNew.getClass(), discountCodeNew.getDiscountCode());
                customer.setDiscountCode(discountCodeNew);
            }
            Collection<PurchaseOrder> attachedPurchaseOrderCollectionNew = new ArrayList<PurchaseOrder>();
            for (PurchaseOrder purchaseOrderCollectionNewPurchaseOrderToAttach : purchaseOrderCollectionNew) {
                purchaseOrderCollectionNewPurchaseOrderToAttach = em.getReference(purchaseOrderCollectionNewPurchaseOrderToAttach.getClass(), purchaseOrderCollectionNewPurchaseOrderToAttach.getOrderNum());
                attachedPurchaseOrderCollectionNew.add(purchaseOrderCollectionNewPurchaseOrderToAttach);
            }
            purchaseOrderCollectionNew = attachedPurchaseOrderCollectionNew;
            customer.setPurchaseOrderCollection(purchaseOrderCollectionNew);
            customer = em.merge(customer);
            if (zipOld != null && !zipOld.equals(zipNew)) {
                zipOld.getCustomerCollection().remove(customer);
                zipOld = em.merge(zipOld);
            }
            if (zipNew != null && !zipNew.equals(zipOld)) {
                zipNew.getCustomerCollection().add(customer);
                zipNew = em.merge(zipNew);
            }
            if (discountCodeOld != null && !discountCodeOld.equals(discountCodeNew)) {
                discountCodeOld.getCustomerCollection().remove(customer);
                discountCodeOld = em.merge(discountCodeOld);
            }
            if (discountCodeNew != null && !discountCodeNew.equals(discountCodeOld)) {
                discountCodeNew.getCustomerCollection().add(customer);
                discountCodeNew = em.merge(discountCodeNew);
            }
            for (PurchaseOrder purchaseOrderCollectionNewPurchaseOrder : purchaseOrderCollectionNew) {
                if (!purchaseOrderCollectionOld.contains(purchaseOrderCollectionNewPurchaseOrder)) {
                    Customer oldCustomerIdOfPurchaseOrderCollectionNewPurchaseOrder = purchaseOrderCollectionNewPurchaseOrder.getCustomerId();
                    purchaseOrderCollectionNewPurchaseOrder.setCustomerId(customer);
                    purchaseOrderCollectionNewPurchaseOrder = em.merge(purchaseOrderCollectionNewPurchaseOrder);
                    if (oldCustomerIdOfPurchaseOrderCollectionNewPurchaseOrder != null && !oldCustomerIdOfPurchaseOrderCollectionNewPurchaseOrder.equals(customer)) {
                        oldCustomerIdOfPurchaseOrderCollectionNewPurchaseOrder.getPurchaseOrderCollection().remove(purchaseOrderCollectionNewPurchaseOrder);
                        oldCustomerIdOfPurchaseOrderCollectionNewPurchaseOrder = em.merge(oldCustomerIdOfPurchaseOrderCollectionNewPurchaseOrder);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = customer.getCustomerId();
                if (findCustomer(id) == null) {
                    throw new NonexistentEntityException("The customer with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Customer customer;
            try {
                customer = em.getReference(Customer.class, id);
                customer.getCustomerId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The customer with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<PurchaseOrder> purchaseOrderCollectionOrphanCheck = customer.getPurchaseOrderCollection();
            for (PurchaseOrder purchaseOrderCollectionOrphanCheckPurchaseOrder : purchaseOrderCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Customer (" + customer + ") cannot be destroyed since the PurchaseOrder " + purchaseOrderCollectionOrphanCheckPurchaseOrder + " in its purchaseOrderCollection field has a non-nullable customerId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            MicroMarket zip = customer.getZip();
            if (zip != null) {
                zip.getCustomerCollection().remove(customer);
                zip = em.merge(zip);
            }
            DiscountCode discountCode = customer.getDiscountCode();
            if (discountCode != null) {
                discountCode.getCustomerCollection().remove(customer);
                discountCode = em.merge(discountCode);
            }
            em.remove(customer);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Customer> findCustomerEntities() {
        return findCustomerEntities(true, -1, -1);
    }

    public List<Customer> findCustomerEntities(int maxResults, int firstResult) {
        return findCustomerEntities(false, maxResults, firstResult);
    }

    private List<Customer> findCustomerEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Customer.class));
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

    public Customer findCustomer(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Customer.class, id);
        } finally {
            em.close();
        }
    }

    public int getCustomerCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Customer> rt = cq.from(Customer.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
