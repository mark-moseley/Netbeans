/*
 * MicroMarketController.java
 *
 * Created on August 16, 2007, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package demo.web;

import demo.model.MicroMarket;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

/**
 *
 * @author martinadamek
 */
public class MicroMarketController {
    
    /** Creates a new instance of MicroMarketController */
    public MicroMarketController() {
    }

    private MicroMarket microMarket;

    private DataModel model;

    @Resource
    private UserTransaction utx;

    @PersistenceUnit(unitName = "JsfJpaCrudPU")
    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    private int batchSize = 20;

    private int firstItem = 0;

    public MicroMarket getMicroMarket() {
        return microMarket;
    }

    public void setMicroMarket(MicroMarket microMarket) {
        this.microMarket = microMarket;
    }

    public DataModel getDetailMicroMarkets() {
        return model;
    }

    public void setDetailMicroMarkets(Collection<MicroMarket> m) {
        model = new ListDataModel(new ArrayList(m));
    }

    public String createSetup() {
        this.microMarket = new MicroMarket();
        return "microMarket_create";
    }

    public String create() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(microMarket);
            utx.commit();
            addSuccessMessage("MicroMarket was successfully created.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        } finally {
            em.close();
        }
        return "microMarket_list";
    }

    public String detailSetup() {
        setMicroMarketFromRequestParam();
        return "microMarket_detail";
    }

    public String editSetup() {
        setMicroMarketFromRequestParam();
        return "microMarket_edit";
    }

    public String edit() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            microMarket = em.merge(microMarket);
            utx.commit();
            addSuccessMessage("MicroMarket was successfully updated.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        } finally {
            em.close();
        }
        return "microMarket_list";
    }

    public String destroy() {
        EntityManager em = getEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            MicroMarket microMarket = getMicroMarketFromRequestParam();
            microMarket = em.merge(microMarket);
            em.remove(microMarket);
            utx.commit();
            addSuccessMessage("MicroMarket was successfully deleted.");
        } catch (Exception ex) {
            try {
                addErrorMessage(ex.getLocalizedMessage());
                utx.rollback();
            } catch (Exception e) {
                addErrorMessage(e.getLocalizedMessage());
            }
        } finally {
            em.close();
        }
        return "microMarket_list";
    }

    public MicroMarket getMicroMarketFromRequestParam() {
        EntityManager em = getEntityManager();
        try{
            MicroMarket o = null;
            if (model != null) {
                o = (MicroMarket) model.getRowData();
                o = em.merge(o);
            } else {
                String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("microMarketId");
                Integer id = new Integer(param);
                o = em.find(MicroMarket.class, id);
            }
            return o;
        } finally {
            em.close();
        }
    }

    public void setMicroMarketFromRequestParam() {
        MicroMarket microMarket = getMicroMarketFromRequestParam();
        setMicroMarket(microMarket);
    }

    public DataModel getMicroMarkets() {
        EntityManager em = getEntityManager();
        try{
            Query q = em.createQuery("select object(o) from MicroMarket as o");
            q.setMaxResults(batchSize);
            q.setFirstResult(firstItem);
            model = new ListDataModel(q.getResultList());
            return model;
        } finally {
            em.close();
        }
    }

    public static void addErrorMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.addMessage(null, facesMsg);
    }

    public static void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.addMessage("successInfo", facesMsg);
    }

    public MicroMarket findMicroMarket(String id) {
        EntityManager em = getEntityManager();
        try{
            MicroMarket o = (MicroMarket) em.find(MicroMarket.class, id);
            return o;
        } finally {
            em.close();
        }
    }

    public int getItemCount() {
        EntityManager em = getEntityManager();
        try{
            int count = ((Long) em.createQuery("select count(o) from MicroMarket as o").getSingleResult()).intValue();
            return count;
        } finally {
            em.close();
        }
    }

    public int getFirstItem() {
        return firstItem;
    }

    public int getLastItem() {
        int size = getItemCount();
        return firstItem + batchSize > size ? size : firstItem + batchSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public String next() {
        if (firstItem + batchSize < getItemCount()) {
            firstItem += batchSize;
        }
        return "microMarket_list";
    }

    public String prev() {
        firstItem -= batchSize;
        if (firstItem < 0) {
            firstItem = 0;
        }
        return "microMarket_list";
    }
    
}
