/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package demo.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity class MicroMarket
 * 
 */
@Entity
@Table(name = "MICRO_MARKET")
@NamedQueries( {
        @NamedQuery(name = "MicroMarket.findByZipCode", query = "SELECT m FROM MicroMarket m WHERE m.zipCode = :zipCode"),
        @NamedQuery(name = "MicroMarket.findByRadius", query = "SELECT m FROM MicroMarket m WHERE m.radius = :radius"),
        @NamedQuery(name = "MicroMarket.findByAreaLength", query = "SELECT m FROM MicroMarket m WHERE m.areaLength = :areaLength"),
        @NamedQuery(name = "MicroMarket.findByAreaWidth", query = "SELECT m FROM MicroMarket m WHERE m.areaWidth = :areaWidth")
    })
public class MicroMarket implements Serializable {

    @Id
    @Column(name = "ZIP_CODE", nullable = false)
    private String zipCode;

    @Column(name = "RADIUS")
    private Double radius;

    @Column(name = "AREA_LENGTH")
    private Double areaLength;

    @Column(name = "AREA_WIDTH")
    private Double areaWidth;
    
    /** Creates a new instance of MicroMarket */
    public MicroMarket() {
    }

    /**
     * Creates a new instance of MicroMarket with the specified values.
     * @param zipCode the zipCode of the MicroMarket
     */
    public MicroMarket(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Gets the zipCode of this MicroMarket.
     * @return the zipCode
     */
    public String getZipCode() {
        return this.zipCode;
    }

    /**
     * Sets the zipCode of this MicroMarket to the specified value.
     * @param zipCode the new zipCode
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Gets the radius of this MicroMarket.
     * @return the radius
     */
    public Double getRadius() {
        return this.radius;
    }

    /**
     * Sets the radius of this MicroMarket to the specified value.
     * @param radius the new radius
     */
    public void setRadius(Double radius) {
        this.radius = radius;
    }

    /**
     * Gets the areaLength of this MicroMarket.
     * @return the areaLength
     */
    public Double getAreaLength() {
        return this.areaLength;
    }

    /**
     * Sets the areaLength of this MicroMarket to the specified value.
     * @param areaLength the new areaLength
     */
    public void setAreaLength(Double areaLength) {
        this.areaLength = areaLength;
    }

    /**
     * Gets the areaWidth of this MicroMarket.
     * @return the areaWidth
     */
    public Double getAreaWidth() {
        return this.areaWidth;
    }

    /**
     * Sets the areaWidth of this MicroMarket to the specified value.
     * @param areaWidth the new areaWidth
     */
    public void setAreaWidth(Double areaWidth) {
        this.areaWidth = areaWidth;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.zipCode != null ? this.zipCode.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this MicroMarket.  The result is 
     * <code>true</code> if and only if the argument is not null and is a MicroMarket object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MicroMarket)) {
            return false;
        }
        MicroMarket other = (MicroMarket)object;
        if (this.zipCode != other.zipCode && (this.zipCode == null || !this.zipCode.equals(other.zipCode))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "demo.model.MicroMarket[zipCode=" + zipCode + "]";
    }
    
}
