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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class Unit {
    UpdateUnit updateUnit = null;
    private boolean isVisible;
    private String filter;
    
    protected abstract UpdateElement getRelevantElement();
    public abstract boolean isMarked();
    public abstract void setMarked(boolean marked);
    public abstract int getCompleteSize ();

    public String getDisplayName() {
        return getRelevantElement().getDisplayName();
    }
    
    public final boolean isVisible(final String filter) {
        if (this.filter != null && this.filter.equals(filter)) {
            return isVisible;
        } 
        this.filter = filter;
        Iterable<String> iterable = details();
        for (String detail : iterable) {
            isVisible = filter.length() == 0 || detail.toLowerCase().contains(filter);    
            if (isVisible) break;
        }        
        return isVisible;
    }
    
    private Iterable<String> details() {
        Iterable<String> retval = new Iterable<String>(){
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    int step = 0;
                    public boolean hasNext() {
                        return step <= 6;
                    }
                    
                    public String next() {
                        String next = null;
                        switch(step++) {
                        case 0:
                            next = getDisplayName();break;
                        case 1:
                            next = getDescription();break;
                        case 2:
                            next = updateUnit.getCodeName();break;
                        case 3:
                            next = getDisplayVersion();break;
                        case 4:
                            next = getAuthor();break;
                        case 5:
                            next = getHomepage();break;
                        case 6:
                            next = getSource();break;
                        }
                        return next != null ? next : "";//NOI18N
                    }
                    
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        };        
        return retval;
    }
    
    public String annotate(String toAnnotate) {
        if (isVisible && filter.length() != 0) {
            int startIdx = toAnnotate.toLowerCase().indexOf(filter);
            if (startIdx > -1) {
                StringBuffer sb = new StringBuffer();
                sb.append(toAnnotate.substring(0, startIdx));
                sb.append("<font bgcolor=\"yellow\">"+toAnnotate.substring(startIdx,startIdx+filter.length())+"</font>");
                sb.append(toAnnotate.substring(startIdx+filter.length()));
                return sb.toString();
            }
        }
        return toAnnotate;
    }
    
    public String getDescription() {
        return getRelevantElement().getDescription();
    }
    
    public String getAuthor() {
        return getRelevantElement().getAuthor();
    }
    
    public String getHomepage() {
        return getRelevantElement().getHomepage();
    }
    
    public String getSource() {
        return getRelevantElement().getSource();
    }
    
    public String getDisplayVersion() {
        return getRelevantElement().getSpecificationVersion().toString();
    }    
    
    public static int compareDisplayNames(Unit unit1, Unit unit2) {
        return Collator.getInstance().compare(unit1.getDisplayName(), unit2.getDisplayName());
    }

    public static int compareDisplayVersions(Unit unit1, Unit unit2) {
        return new SpecificationVersion(unit1.getDisplayVersion()).compareTo (new SpecificationVersion (unit2.getDisplayVersion()));
    }

    public static int compareCompleteSizes(Unit unit1, Unit unit2) {
        return Integer.valueOf(unit1.getCompleteSize()).compareTo(unit2.getCompleteSize());
    }
        
    public static class Installed extends Unit {
        
        private UpdateElement installEl = null;
        private UpdateElement backupEl = null;
        private boolean isUninstallAllowed ;
        
        public static boolean isOperationAllowed(UpdateUnit uUnit, UpdateElement element, OperationContainer<OperationSupport> container) {
            ModuleInfo mInfo = ModuleProvider.getInstalledModules().get(element.getCodeName());
            return (mInfo != null && container.canBeAdded(uUnit, element));
        }
        public boolean isModuleEnabled() {
            ModuleInfo mInfo = ModuleProvider.getInstalledModules().get(installEl.getCodeName());
            return mInfo != null ? mInfo.isEnabled() : false;
        }
        public Installed(UpdateUnit unit) {
            this.updateUnit = unit;
            this.installEl = unit.getInstalled();
            assert installEl != null : "Installed UpdateUnit " + unit + " has Installed UpdateElement.";
            this.backupEl = unit.getBackup();
            this.isUninstallAllowed = isOperationAllowed(this.updateUnit, installEl, Containers.forUninstall());
        }
        
        public boolean isMarked() {
            return Containers.forUninstall().contains(installEl);
        }
        
        public void setMarked(boolean marked) {
            assert marked != isMarked();
            if (marked) {
                Containers.forUninstall().add(updateUnit, installEl);
            } else {
                Containers.forUninstall().remove(installEl);
            }
        }
        
        public static int compareInstalledVersions(Unit u1, Unit u2) {
            if (u1 instanceof Unit.Installed && u2 instanceof Unit.Installed) {
                Unit.Installed unit1 = (Unit.Installed )u1;
                Unit.Installed unit2 = (Unit.Installed )u2; 
                return new SpecificationVersion(unit1.getInstalledVersion()).compareTo(new SpecificationVersion(unit2.getInstalledVersion()));
            } 
            return Unit.compareDisplayVersions(u1, u2);
        }
        
        public boolean isUninstallAllowed() {
            return isUninstallAllowed ;
        }
        
        public String getInstalledVersion() {
            assert installEl.getSpecificationVersion() != null : installEl + " has specification version.";
            return installEl.getSpecificationVersion().toString();
        }
        
        public String getBackupVersion() {
            return backupEl == null ? "-" : backupEl.getSpecificationVersion().toString();
        }
        
        public Integer getMyRating() {
            return null;
        }
        
        public UpdateElement getRelevantElement() {
            return installEl;
        }

        public int getCompleteSize() {
            return -1;
        }
        
    }
    
    public static class Update extends Unit {
        private UpdateElement installEl = null;
        private UpdateElement updateEl = null;
        private boolean isNbms;
        private int size = -1;
        
        public Update(UpdateUnit unit, boolean isNbms) {
            this.isNbms = isNbms;
            this.updateUnit = unit;
            this.installEl = unit.getInstalled();
            assert installEl != null : "Updateable UpdateUnit " + unit + " has Installed UpdateElement.";
            // XXX: find highest version
            this.updateEl = unit.getAvailableUpdates().get(unit.getAvailableUpdates().size()-1);
            assert updateEl != null : "Updateable UpdateUnit " + unit + " has UpdateElement for update.";
        }
        
        public boolean isMarked() {
            OperationContainer<InstallSupport> container = (isNbms) ? Containers.forUpdateNbms() : Containers.forUpdate();            
            return container.contains(updateEl);
        }
        
        public void setMarked(boolean marked) {
            assert marked != isMarked();
            OperationContainer<InstallSupport> container = (isNbms) ? Containers.forUpdateNbms() : Containers.forUpdate();
            if (marked) {
                container.add(updateUnit, updateEl);
            } else {
                container.remove(updateEl);
            }
        }

        public static int compareInstalledVersions(Unit u1, Unit u2) {
            if (u1 instanceof Unit.Update && u2 instanceof Unit.Update) {
                Unit.Update unit1 = (Unit.Update)u1;
                Unit.Update unit2 = (Unit.Update)u2; 
                return new SpecificationVersion(unit1.getInstalledVersion()).compareTo(new SpecificationVersion(unit2.getInstalledVersion()));
            } 
            return Unit.compareDisplayVersions(u1, u2);
        }
        
        public static int compareAvailableVersions(Unit u1, Unit u2) {
            if (u1 instanceof Unit.Update && u2 instanceof Unit.Update) {
                Unit.Update unit1 = (Unit.Update)u1;
                Unit.Update unit2 = (Unit.Update)u2; 
                return new SpecificationVersion(unit1.getAvailableVersion()).compareTo(new SpecificationVersion(unit2.getAvailableVersion()));
            } 
            return Unit.compareDisplayVersions(u1, u2);
        }
        
        
        public String getInstalledVersion() {
            return installEl.getSpecificationVersion().toString();
        }
        
        public String getAvailableVersion() {
            return updateEl.getSpecificationVersion().toString();
        }
        
        public String getSize() {
            return Utilities.getDownloadSizeAsString (updateEl.getDownloadSize());
        }
        
        public UpdateElement getRelevantElement() {
            return updateEl;
        }

        public int getCompleteSize() {
            if (size == -1) {
                size = 0;
                OperationContainer<OperationSupport> c = OperationContainer.createForDirectUpdate ();
                OperationInfo<OperationSupport> i = c.add (getRelevantElement ());
                Set<UpdateElement> elems = i.getRequiredElements ();
                for (UpdateElement el : elems) {
                    size += el.getDownloadSize();
                }
                size += getRelevantElement ().getDownloadSize ();
                c.removeAll ();
            }
            return size;
        }
        
    }
    
    public static class Available extends Unit {
        private UpdateElement updateEl = null;
        private boolean isNbms;
        private int size = -1;
        
        public Available(UpdateUnit unit, boolean isNbms) {
            this.isNbms = isNbms;
            this.updateUnit = unit;
            // XXX: find highest version
            this.updateEl = unit.getAvailableUpdates().get(0);
            assert updateEl != null : "Updateable UpdateUnit " + unit + " has UpdateElement for update.";
        }

        public boolean isMarked() {
            OperationContainer<InstallSupport> container = (isNbms) ? Containers.forAvailableNbms() : Containers.forAvailable();            
            return container.contains(updateEl);
        }
        
        public void setMarked(boolean marked) {
            assert marked != isMarked();
            OperationContainer<InstallSupport> container = (isNbms) ? Containers.forAvailableNbms() : Containers.forAvailable();
            
            if (marked) {
                container.add(updateUnit, updateEl);
            } else {
                container.remove(updateEl);
            }
        }

        public static int compareAvailableVersion(Unit u1, Unit u2) {
            if (u1 instanceof Unit.Available && u2 instanceof Unit.Available) {
                Unit.Available unit1 = (Unit.Available)u1;
                Unit.Available unit2 = (Unit.Available)u2; 
                return new SpecificationVersion(unit1.getAvailableVersion()).compareTo(new SpecificationVersion(unit2.getAvailableVersion()));
            } 
            return Unit.compareDisplayVersions(u1, u2);
        }
        
        public String getAvailableVersion() {
            return updateEl.getSpecificationVersion().toString();
        }
        
        public Integer getMyRating() {
            return null;
        }
        
        public String getSize() {
            return Utilities.getDownloadSizeAsString (updateEl.getDownloadSize());
        }
        
        public UpdateElement getRelevantElement() {
            return updateEl;
        }
        
        public int getCompleteSize() {
            if (size == -1) {
                size = 0;
                OperationContainer<OperationSupport> c = OperationContainer.createForDirectInstall ();
                OperationInfo<OperationSupport> i = c.add (getRelevantElement ());
                Set<UpdateElement> elems = i.getRequiredElements ();
                for (UpdateElement el : elems) {
                    size += el.getDownloadSize();
                }
                size += getRelevantElement ().getDownloadSize ();
                c.removeAll ();
            }
            return size;
        }
        
    }
    
    public static class DummyUnit extends Unit {
        private UpdateElement element;
        public DummyUnit(UpdateElement el) {
            element = el;
        }
        
        protected UpdateElement getRelevantElement() {
            return element;
        }
        
        public boolean isMarked() {
            return false;
        }
    
        public void setMarked(boolean marked) {}

        public int getCompleteSize() {
            throw new UnsupportedOperationException ("Not supported yet.");
        }
}
    
}
