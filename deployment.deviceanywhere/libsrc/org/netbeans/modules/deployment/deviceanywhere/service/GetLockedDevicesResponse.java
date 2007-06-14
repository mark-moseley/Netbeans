
package org.netbeans.modules.deployment.deviceanywhere.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="getLockedDevicesReturn" type="{http://services.mc.com}ApplicationAPI_GetLockedDevicesReturn"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getLockedDevicesReturn"
})
@XmlRootElement(name = "getLockedDevicesResponse")
public class GetLockedDevicesResponse {

    @XmlElement(required = true)
    protected ApplicationAPIGetLockedDevicesReturn getLockedDevicesReturn;

    /**
     * Gets the value of the getLockedDevicesReturn property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationAPIGetLockedDevicesReturn }
     *     
     */
    public ApplicationAPIGetLockedDevicesReturn getGetLockedDevicesReturn() {
        return getLockedDevicesReturn;
    }

    /**
     * Sets the value of the getLockedDevicesReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationAPIGetLockedDevicesReturn }
     *     
     */
    public void setGetLockedDevicesReturn(ApplicationAPIGetLockedDevicesReturn value) {
        this.getLockedDevicesReturn = value;
    }

}
