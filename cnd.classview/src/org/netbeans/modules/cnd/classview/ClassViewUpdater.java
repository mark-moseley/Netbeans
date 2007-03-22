/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.classview;

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 * Deals with class view model updates
 * @author vk155633
 */
public class ClassViewUpdater extends Thread {
    
    private static final boolean traceEvents = Boolean.getBoolean("cnd.classview.updater-events"); // NOI18N
    
    private static class BlockingQueue {
        
        private LinkedList data = new LinkedList();
        
        private Object lock = new Object();
        
        public SmartChangeEvent get() throws InterruptedException {
            synchronized( lock ) {
                while( data.isEmpty() ) {
                    lock.wait();
                }
                return (SmartChangeEvent) data.removeFirst();
            }
        }
        
        public void add(SmartChangeEvent event) {
            synchronized( lock ) {
                data.add(event);
                lock.notify();
            }
        }
        
        public SmartChangeEvent peek() throws InterruptedException {
            synchronized( lock ) {
                while( data.isEmpty() ) {
                    lock.wait();
                }
                return (SmartChangeEvent) data.peek();
            }
        }
        
        public boolean isEmpty() throws InterruptedException {
            synchronized( lock ) {
                return data.isEmpty();
            }
        }
    }
    
    private ClassViewModel model;
    private BlockingQueue queue;
    private boolean isStoped = false;
    
    public ClassViewUpdater(ClassViewModel model) {
        super("Class View Updater");
        this.model = model;
        queue = new BlockingQueue();
    }
    
    public void setStop(){
        isStoped = true;
    }
    
    private boolean isSkiped(SmartChangeEvent e){
        if (model.isShowLibs()){
            return false;
        }
        if (e.getChangedProjects().size()==1){
            CsmProject project = (CsmProject)e.getChangedProjects().keySet().iterator().next();
            if (model.isLibProject(project)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * delay before class view update.
     */
    private static final int MINIMAL_DELAY = 500;
    
    /**
     * delay before checking queue in batch mode.
     */
    private static final int BATCH_MODE_DELAY = 1000;
    
    /**
     * stop collect events when batch contains:
     */
    private static final int MAXIMAL_BATCH_SIZE = 50;
    
    /**
     * stop collect events when batch consume time in second:
     */
    private static final int MAXIMAL_BATCH_TIME = 10;
    
    /**
     * delay on user activity.
     */
    private static final int USER_ACTIVITY_DELAY = 1000;
    
    public void run() {
        long start = 0;
        try {
            while( true ) {
                if (isStoped) {
                    return;
                }
                SmartChangeEvent compose = queue.get();
                if (isSkiped(compose)){
                    continue;
                }
                if (queue.isEmpty()) {
                    Thread.sleep(MINIMAL_DELAY);
                }
                int doWait = 0;
                while(true){
                    if (isStoped) {
                        return;
                    }
                    while(!queue.isEmpty()){
                        if (isStoped) {
                            return;
                        }
                        SmartChangeEvent e = queue.peek();
                        if (!isSkiped(e)){
                            if (!compose.addChangeEvent(e)){
                                break;
                            }
                        }
                        queue.get();
                        if (queue.isEmpty() && compose.getCount() < MAXIMAL_BATCH_SIZE && doWait < MAXIMAL_BATCH_TIME) {
                            doWait++;
                            Thread.sleep(BATCH_MODE_DELAY);
                        }
                    }
                    if (model.isUserActivity()){
                        Thread.sleep(USER_ACTIVITY_DELAY);
                        continue;
                    }
                    break;
                }
                if (traceEvents) start = System.nanoTime();
                if (isStoped) {
                    return;
                }
                model.update(compose);
                if (traceEvents) {
                    long end = System.nanoTime();
                    long time = (end-start)/1000000;
                    System.out.println("Compose change event contains "+compose.getCount()+ // NOI18N
                            " events. Time = "+((float)(time)/1000.)); // NOI18N
                    for(Map.Entry<CsmProject, SmartChangeEvent.Storage> entry : compose.getChangedProjects().entrySet()){
                        System.out.println("    Project "+entry.getKey().getName()+ // NOI18N
                                " Nd="+entry.getValue().getNewDeclarations().size()+ // NOI18N
                                ", Rd="+entry.getValue().getRemovedDeclarations().size()+ // NOI18N
                                ", Ud="+entry.getValue().getChangedDeclarations().size()+ // NOI18N
                                ", Nn="+entry.getValue().getNewNamespaces().size()+ // NOI18N
                                ", Rn="+entry.getValue().getRemovedNamespaces().size()); // NOI18N
                    }
                }
            }
        } catch( InterruptedException e ) {
            return;
        } finally {
            model = null;
            queue = null;
        }
    }
    
    public void scheduleUpdate(CsmChangeEvent e) {
        //model.update(e);
        queue.add(new SmartChangeEvent(e));
    }
}
