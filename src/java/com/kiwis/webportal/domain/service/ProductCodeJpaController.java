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
import com.kiwis.webportal.domain.entity.Product;
import com.kiwis.webportal.domain.entity.ProductCode;
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
public class ProductCodeJpaController implements Serializable {

    public ProductCodeJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ProductCode productCode) throws PreexistingEntityException, Exception {
        if (productCode.getProductCollection() == null) {
            productCode.setProductCollection(new ArrayList<Product>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Product> attachedProductCollection = new ArrayList<Product>();
            for (Product productCollectionProductToAttach : productCode.getProductCollection()) {
                productCollectionProductToAttach = em.getReference(productCollectionProductToAttach.getClass(), productCollectionProductToAttach.getProductId());
                attachedProductCollection.add(productCollectionProductToAttach);
            }
            productCode.setProductCollection(attachedProductCollection);
            em.persist(productCode);
            for (Product productCollectionProduct : productCode.getProductCollection()) {
                ProductCode oldProductCodeOfProductCollectionProduct = productCollectionProduct.getProductCode();
                productCollectionProduct.setProductCode(productCode);
                productCollectionProduct = em.merge(productCollectionProduct);
                if (oldProductCodeOfProductCollectionProduct != null) {
                    oldProductCodeOfProductCollectionProduct.getProductCollection().remove(productCollectionProduct);
                    oldProductCodeOfProductCollectionProduct = em.merge(oldProductCodeOfProductCollectionProduct);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findProductCode(productCode.getProdCode()) != null) {
                throw new PreexistingEntityException("ProductCode " + productCode + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ProductCode productCode) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ProductCode persistentProductCode = em.find(ProductCode.class, productCode.getProdCode());
            Collection<Product> productCollectionOld = persistentProductCode.getProductCollection();
            Collection<Product> productCollectionNew = productCode.getProductCollection();
            List<String> illegalOrphanMessages = null;
            for (Product productCollectionOldProduct : productCollectionOld) {
                if (!productCollectionNew.contains(productCollectionOldProduct)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Product " + productCollectionOldProduct + " since its productCode field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Product> attachedProductCollectionNew = new ArrayList<Product>();
            for (Product productCollectionNewProductToAttach : productCollectionNew) {
                productCollectionNewProductToAttach = em.getReference(productCollectionNewProductToAttach.getClass(), productCollectionNewProductToAttach.getProductId());
                attachedProductCollectionNew.add(productCollectionNewProductToAttach);
            }
            productCollectionNew = attachedProductCollectionNew;
            productCode.setProductCollection(productCollectionNew);
            productCode = em.merge(productCode);
            for (Product productCollectionNewProduct : productCollectionNew) {
                if (!productCollectionOld.contains(productCollectionNewProduct)) {
                    ProductCode oldProductCodeOfProductCollectionNewProduct = productCollectionNewProduct.getProductCode();
                    productCollectionNewProduct.setProductCode(productCode);
                    productCollectionNewProduct = em.merge(productCollectionNewProduct);
                    if (oldProductCodeOfProductCollectionNewProduct != null && !oldProductCodeOfProductCollectionNewProduct.equals(productCode)) {
                        oldProductCodeOfProductCollectionNewProduct.getProductCollection().remove(productCollectionNewProduct);
                        oldProductCodeOfProductCollectionNewProduct = em.merge(oldProductCodeOfProductCollectionNewProduct);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = productCode.getProdCode();
                if (findProductCode(id) == null) {
                    throw new NonexistentEntityException("The productCode with id " + id + " no longer exists.");
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
            ProductCode productCode;
            try {
                productCode = em.getReference(ProductCode.class, id);
                productCode.getProdCode();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The productCode with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Product> productCollectionOrphanCheck = productCode.getProductCollection();
            for (Product productCollectionOrphanCheckProduct : productCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This ProductCode (" + productCode + ") cannot be destroyed since the Product " + productCollectionOrphanCheckProduct + " in its productCollection field has a non-nullable productCode field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(productCode);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ProductCode> findProductCodeEntities() {
        return findProductCodeEntities(true, -1, -1);
    }

    public List<ProductCode> findProductCodeEntities(int maxResults, int firstResult) {
        return findProductCodeEntities(false, maxResults, firstResult);
    }

    private List<ProductCode> findProductCodeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ProductCode.class));
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

    public ProductCode findProductCode(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ProductCode.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductCodeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ProductCode> rt = cq.from(ProductCode.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
