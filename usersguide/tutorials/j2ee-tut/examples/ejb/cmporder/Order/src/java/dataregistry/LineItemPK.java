/*
 * Copyright (c) 2004 Sun Microsystems, Inc.  All rights reserved.  U.S.
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
 * Copyright (c) 2004 Sun Microsystems, Inc. Tous droits reserves.
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

import javax.ejb.*;

/**
 * This is the bean class for the LineitemBean enterprise bean.
 */
public final class LineItemPK implements java.io.Serializable {
    
    public java.lang.Integer orderId;
    public java.math.BigDecimal itemId;
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(java.lang.Object otherOb) {
        
        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof dataregistry.LineItemPK)) {
            return false;
        }
        dataregistry.LineItemPK other = (dataregistry.LineItemPK) otherOb;
        return (
                
                (orderId==null?other.orderId==null:orderId.equals(other.orderId))
                &&
                (itemId==null?other.itemId==null:itemId.equals(other.itemId))
                
                );
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (
                
                (orderId==null?0:orderId.hashCode())
                ^
                (itemId==null?0:itemId.hashCode())
                
                );
    }
    
}
