/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer.cluster;

import com.installshield.product.SoftwareObject;
import com.installshield.product.SoftwareVersion;
import com.installshield.product.service.registry.RegistryService;
import com.installshield.util.Log;
import com.installshield.wizard.CancelableWizardAction;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.service.ServiceException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.netbeans.installer.Util;

public class NbSearchAction extends CancelableWizardAction {
    
    private static Vector nbHomeList = new Vector();
    
    private String [] nbUIDArray = new String[0];
    
    private static String BUNDLE = "$L(org.netbeans.installer.cluster.Bundle,";
    
    public NbSearchAction() {
    }
    
    public void build(WizardBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
            support.putClass(NbSearchAction.SoftwareObjectComparator.class.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void execute(WizardBeanEvent evt) {
        //Return if nbHomeList is not empty. This is an work around
        //to make the action not to search the NBs if the user click
        //back button.
        if (!nbHomeList.isEmpty()) {
            return;
        }
        
        //Handle silent mode where getUserInterface returns null
        if (evt.getUserInterface() != null) {
            String searchMsg = resolveString(BUNDLE + "NbSearchAction.searchMessage)");
            evt.getUserInterface().setBusy(searchMsg);
        }
        
        initNbUIDArray();
        findNb();
    }
    
    private void initNbUIDArray () {
        int arrLength = 0;
        String s = resolveString(BUNDLE + "NetBeans.UIDLength)");
        try {
            arrLength = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            logEvent(this, Log.ERROR,"Incorrect number for NetBeans.UIDLength: " + s);
        }
        
        //No order is defined.
        if (arrLength == 0) {
            return;
        }
        nbUIDArray = new String[arrLength];
        for (int i = 0; i < arrLength; i++) {
            nbUIDArray[i] = resolveString(BUNDLE + "NetBeans.UID" + i + ")");
            logEvent(this, Log.DBG,"nbUIDArray[" + i + "]: " + nbUIDArray[i]);
        }
    }
    
    private boolean acceptNbUID (String nbUID) {
        for (int i = 0; i < nbUIDArray.length; i++) {
            if (nbUID.equals(nbUIDArray[i])) {
                return true;
            }
        }
        return false;
    }
    
    /** Look for all installation of NetBeans IDE using vpd.properties.
     */
    private void findNb () {
        try {
            // Get the instance of RegistryService
            String jseUID = resolveString(BUNDLE + "JSE.productUID)");
            RegistryService regserv = (RegistryService)getService(RegistryService.NAME);  
            String [] arr = regserv.getAllSoftwareObjectUIDs();
            /*for (int i = 0; i < arr.length; i++) {
               System.out.println("arr[" + i + "]: " + arr[i]);
            }*/
            
            //Look for any profiler installation
            SoftwareObject [] soArr = null;
            //System.out.println("substring:" + nbUID.substring(26,32));
            for (int i = 0; i < arr.length; i++) {
                if (acceptNbUID(arr[i])) {
                //if (arr[i].startsWith(nbUID.substring(0,26))) {
                    soArr = regserv.getSoftwareObjects(arr[i]);
                    System.out.println("so.length:" + soArr.length);
                    for (int j = 0; j < soArr.length; j++) {
                        logEvent(this, Log.DBG,"so[" + j + "]:"
                        + " displayName: " + soArr[j].getDisplayName()
                        + " name: " + soArr[j].getName()
                        + " productNumber: " + soArr[j].getProductNumber()
                        + " installLocation: " + soArr[j].getInstallLocation());
                        nbHomeList.add(soArr[j]);
                    }
                } else if (arr[i].equals(jseUID)) {
                    soArr = regserv.getSoftwareObjects(arr[i]);
                    System.out.println("so.length:" + soArr.length);
                    for (int j = 0; j < soArr.length; j++) {
                        SoftwareVersion version = soArr[j].getKey().getVersion();
                        logEvent(this, Log.DBG,"so[" + j + "]:"
                        + " displayName: " + soArr[j].getDisplayName()
                        + " name: " + soArr[j].getName()
                        + " productNumber: " + soArr[j].getProductNumber()
                        + " installLocation: " + soArr[j].getInstallLocation()
                        + " version:" + version);
                        if (acceptJSEVersion(version)) {
                            nbHomeList.add(soArr[j]);
                        }
                    }
                }
            }
            orderList(nbHomeList);
        } catch (ServiceException exc) {
            logEvent(this, Log.ERROR, exc);
        }
    }
    
    private boolean acceptJSEVersion (SoftwareVersion version) {
        if (version.getMajor().equals("8") && version.getMinor().equals("1")) {
            return true;
        }
        if (version.getMajor().equals("9") && version.getMinor().equals("0") && version.getMaintenance().equals("1")) {
            return true;
        }
        if (version.getMajor().equals("9") && version.getMinor().equals("0") && version.getMaintenance().equals("2")) {
            return true;
        }
        return false;
    }
    
    private static void orderList (Vector nbHomeList) {
        // Sort anagram groups according to size
        Collections.sort(nbHomeList, new SoftwareObjectComparator());
    }
    
    /** Collection of SoftwareObject instances is ordered in ascending order.
     * It means that latest version is last.
     */
    public static int getLatestVersionIndex() {
        return nbHomeList.size() - 1;
    }
    
    public static Vector getNbHomeList () {
        return nbHomeList;
    }
    
    /** Used to sort collection of SoftwareObject instances
     */
    private static class SoftwareObjectComparator implements Comparator {
    
        public int compare(Object o1, Object o2) {
            if ((o1 instanceof SoftwareObject) && (o2 instanceof SoftwareObject)) {
                SoftwareObject so1 = (SoftwareObject) o1;
                SoftwareObject so2 = (SoftwareObject) o2;
                String s1 = so1.getKey().getUID().substring(0,29);
                String s2 = so2.getKey().getUID().substring(0,29);
                return s1.compareTo(s2);
            } else {
                return 0;
            }
        }
    }
 }
