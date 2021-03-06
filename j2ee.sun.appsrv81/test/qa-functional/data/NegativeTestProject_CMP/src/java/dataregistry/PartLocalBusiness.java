/*
 * Copyright (c) 2005 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */

package dataregistry;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;


/**
 * This is the business interface for Part enterprise bean.
 */
public interface PartLocalBusiness {
    
    public abstract String getPartNumber();

    public abstract BigDecimal getRevision();

    public abstract String getDescription();

    public abstract void setDescription(String description);

    public abstract Timestamp getRevisionDate();

    public abstract void setRevisionDate(Timestamp revisionDate);

    public abstract PartLocal getBomPart();

    public abstract void setBomPart(PartLocal partBean);

    public abstract Collection getPartBean1();

    public abstract void setPartBean1(Collection partBean1);

    public abstract VendorPartLocal getVendorPartBean();

    public abstract void setVendorPartBean(VendorPartLocal vendorPartBean);

    Serializable getDrawing();

    void setDrawing(Serializable drawing);

    String getSpecification();

    void setSpecification(String specification);
    
}
