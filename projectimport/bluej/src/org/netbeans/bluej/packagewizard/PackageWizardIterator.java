/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.bluej.packagewizard;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.bluej.BluejProject;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/**
 * a hacky wrapper around the default package wizard iterator, making sure we create the bluej.pkg file in a package so
 * that it appears in the bluej view.
 * @author mkleint
 */
public class PackageWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private WizardDescriptor.InstantiatingIterator delegate;

    private WizardDescriptor wiz;

    private Set set;
    
    
    public static PackageWizardIterator createWizard() {
        return new PackageWizardIterator();
    }
    
    /** Creates a new instance of PackageWizardIterator */
    private PackageWizardIterator() {
        ClassLoader ldr = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
        Class clazz;
        Method method;
        try {
            clazz = Class.forName("org.netbeans.modules.java.project.NewJavaFileWizardIterator", true, ldr); // NOI18N
            method = clazz.getMethod("packageWizard", null); // NOI18N
            delegate = (WizardDescriptor.InstantiatingIterator)method.invoke(null, null);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public Set instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder( wiz );
        Project project = FileOwnerQuery.getOwner(dir);
        if (project.getLookup().lookup(BluejProject.class) != null) {
            FileSystem fs = dir.getFileSystem();
            
            fs.runAtomicAction(
                    new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    set = delegate.instantiate();
                    FileObject fo = (FileObject)set.iterator().next();
                    fo.createData("bluej.pkg"); // NOI18N
                }
            }
            );
            
        } else {
            set = delegate.instantiate();
            
        }
        return set;
    }

    public void initialize(WizardDescriptor wizard) {
        wiz = wizard;
        delegate.initialize(wizard);
    }

    public void uninitialize(WizardDescriptor wizard) {
        delegate.uninitialize(wizard);
    }

    public WizardDescriptor.Panel current() {
        return delegate.current();
    }

    public String name() {
        return delegate.name();
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public boolean hasPrevious() {
        return delegate.hasPrevious();
    }

    public void nextPanel() {
        delegate.nextPanel();
    }

    public void previousPanel() {
        delegate.previousPanel();
    }

    public void addChangeListener(ChangeListener l) {
        delegate.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        delegate.removeChangeListener(l);
    }
    
}
