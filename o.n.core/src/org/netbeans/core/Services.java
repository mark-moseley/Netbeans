/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.io.*;
import java.util.*;

import org.openide.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Works with all service types.
*
* @author Jaroslav Tulach
* @deprecated Obsoleted by lookup and new settings system.
*/
public final class Services extends ServiceType.Registry implements LookupListener {
    /** serial */
    static final long serialVersionUID =-7558069607307508327L;
    
    /** Result containing all current services. */
    private Lookup.Result allTypes;
    
    /** Mapping between service name and given ServiceType instance. */
    private Map name2Service;
    
    /** Default instance */
    public static Services getDefault () {
        return (Services)Lookup.getDefault ().lookup (org.openide.ServiceType.Registry.class);
    }
    
    public Services() {
        name2Service = new HashMap();
    }
    
    public ServiceType find(Class clazz) {
        return (ServiceType)Lookup.getDefault().lookup(clazz);
    }
    
    /** Override to specially look up no-op services. */
    public ServiceType find (String name) {
        Map lookupMap = name2Service;
        ServiceType ret;
        synchronized (lookupMap) {
            ret = (ServiceType) lookupMap.get(name);
        }
        
        if (ret == null) {
            ret = super.find(name);
            synchronized (lookupMap) {
                lookupMap.put(name, ret);
            }
        }
        
        return ret;
    }
    
    /** Result containing all current services. */
    private Lookup.Result getTypesResult() {
        boolean init = false;
        synchronized (this) {
            if (allTypes == null) {
                allTypes = Lookup.getDefault().lookup(
                    new Lookup.Template(ServiceType.class)
                );
                allTypes.addLookupListener(this);
                init = true;
            }
        }
        if (init) resultChanged(null);
        return allTypes;
    }
    
    /** A change in lookup occured.
     * @param ev event describing the change
     */
    public void resultChanged(LookupEvent ev) {
        synchronized (name2Service) {
            name2Service.clear();
        }
    }
    
    /** Getter for list of all services types.
    * @return list of ServiceType
    */
    public java.util.List getServiceTypes () {
        return new ArrayList(getTypesResult().allInstances());
    }
    
    /** Setter for list of all services types. This allows to change
    * instaces of the objects but only of the types that are already registered
    * to the system by manifest sections.
    *
    * @param arr list of ServiceTypes 
    */
    public synchronized void setServiceTypes (java.util.List arr) {
        if (arr == null) {
            throw new NullPointerException();
        }
        
        arr = ensureSingleness(arr);
        
        HashMap services = new HashMap(20); // <service type, DataObject>
        searchServices(NbPlaces.getDefault().findSessionFolder("Services").getPrimaryFile(), services); // NOI18N
        
        // storing services
        HashMap order = new HashMap(10); // <parent folder, <file>>
        Iterator it = arr.iterator();
        while (it.hasNext()) {
            ServiceType st = (ServiceType) it.next();
            DataObject dobj = (DataObject) services.get(st);
            
            if (dobj != null) {
                // store existing
                try {
                    dobj = InstanceDataObject.create(dobj.getFolder(), dobj.getPrimaryFile().getName(), st, null);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                services.remove(st);
            } else {
                dobj = storeNewServiceType(st);
            }
            
            // compute order in folders
            if (dobj != null) {
                DataFolder parent = dobj.getFolder();
                List orderedFiles = (List) order.get(parent);
                if (orderedFiles == null) {
                    orderedFiles = new ArrayList(6);
                    order.put(parent, orderedFiles);
                }
                orderedFiles.add(dobj);
            }
        }
        
        // storing order attribute
        it = order.keySet().iterator();
        while (it.hasNext()) {
            DataObject parent = (DataObject) it.next();
            List orderedFiles = (List) order.get(parent);
            if (orderedFiles.size() < 2) continue;
            
            Iterator files = orderedFiles.iterator();
            StringBuffer orderAttr = new StringBuffer(64);
            while (files.hasNext()) {
                DataObject file = (DataObject) files.next();
                orderAttr.append(file.getPrimaryFile().getNameExt()).append('/');
            }
            orderAttr.deleteCharAt(orderAttr.length() - 1);
            try {
                parent.getPrimaryFile().
                    setAttribute("OpenIDE-Folder-Order", orderAttr.toString()); // NOI18N
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        // remove remaining services from default FS
        it = services.values().iterator();
        while (it.hasNext()) {
            DataObject dobj = (DataObject) it.next();
            try {
                dobj.delete();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
    }
    
    private DataObject storeNewServiceType(ServiceType st) {
        Class stype = st.getClass ();
        // finds direct subclass of service type
        while (stype.getSuperclass () != ServiceType.class) {
            stype = stype.getSuperclass();
        }
        
        try{
            String folder = org.openide.util.Utilities.getShortClassName(stype);

            DataFolder dfServices = NbPlaces.getDefault().findSessionFolder("Services"); // NOI18N
            DataFolder dfTarget = DataFolder.create(dfServices, folder);
            
            return InstanceDataObject.create(dfTarget, null, st, null);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }
    
    /** ensure that instance of the service type will be listed just once.
     */
    private List ensureSingleness(List l) {
        List newList = new ArrayList(l.size());
        Iterator it = l.iterator();
        
        while (it.hasNext()) {
            ServiceType stype = (ServiceType) it.next();
            if (newList.contains(stype)) {
                continue;
            } else {
                newList.add(stype);
            }
        }
        
        return newList;
    }
    
    /** search all data objects containing service type instance. */
    private void searchServices(FileObject folder, Map services) {
        FileObject[] fobjs = folder.getChildren();
        for (int i = 0; i < fobjs.length; i++) {
            if (!fobjs[i].isValid()) continue;
            if (fobjs[i].isFolder()) {
                searchServices(fobjs[i], services);
            } else {
                try {
                    DataObject dobj = DataObject.find(fobjs[i]);
                    InstanceCookie inst = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
                    if (inst == null) continue;
                    
                    if (instanceOf(inst, ServiceType.class)) {
                        ServiceType ser = (ServiceType) inst.instanceCreate();
                        services.put(ser, dobj);
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }
    
    /** test if instance cookie is instance of clazz*/
    private static boolean instanceOf(InstanceCookie inst, Class clazz) {
        if (inst instanceof InstanceCookie.Of) {
            return ((InstanceCookie.Of) inst).instanceOf(clazz);
        } else {
            try {
                return clazz.isAssignableFrom(inst.instanceClass());
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return false;
            }
        }
    }
    
    /** all services */
    public Enumeration services () {
        return Collections.enumeration (getServiceTypes ());
    }

    /** Get all available services that are subclass of given class
    * @param clazz the class that all services should be subclass of
    * @return an enumeration of {@link ServiceType}s that are subclasses of
    *    given class
    */
    public Enumeration services (Class clazz) {
        if (clazz == null) new org.openide.util.enum.EmptyEnumeration();
        Collection res = Lookup.getDefault().lookup(new Lookup.Template(clazz)).allInstances();
        return Collections.enumeration(res);
    }
    
    /** Write the object down.
    */
    private void writeObject (ObjectOutputStream oos) throws IOException {
        Enumeration en = services ();
        while (en.hasMoreElements ()) {
            ServiceType s = (ServiceType)en.nextElement ();

            NbMarshalledObject obj;
            try {
                obj = new NbMarshalledObject (s);
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (
                  ErrorManager.INFORMATIONAL,
                  ex
                );
                // skip the object if it cannot be serialized
                obj = null;
            }
            if (obj != null) {
                oos.writeObject (obj);
            }
        }

        oos.writeObject (null);
    }

    /** Read the object.
    */
    private void readObject (ObjectInputStream oos)
    throws IOException, ClassNotFoundException {
        final LinkedList ll = new LinkedList ();
        for (;;) {
            NbMarshalledObject obj = (NbMarshalledObject)oos.readObject ();

            if (obj == null) {
                break;
            }

            try {
                ServiceType s = (ServiceType)obj.get ();
                ll.add (s);
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (
                  ErrorManager.INFORMATIONAL,
                  ex
                );
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault ().notify (
                  ErrorManager.INFORMATIONAL,
                  ex
                );
            }
        }

        getDefault ().setServiceTypes (ll);
    }

    /** Only one instance */
    private Object readResolve () {
        return getDefault ();
    }
}
