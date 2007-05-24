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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.queue;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 * @author Vladimir Kvashin
 */
public class RepositoryWritingThread implements Runnable {
    
    private RepositoryWriter writer;
    private RepositoryQueue queue;
    
    public RepositoryWritingThread(RepositoryWriter writer, RepositoryQueue queue) {
	this.writer = writer;
	this.queue = queue;
    }

    private void waitReady() throws IOException, InterruptedException {
	if( Stats.maintenanceInterval > 0 ) {
	    if( Stats.allowMaintenance ) {
		while( ! queue.isReady() ) {
		    if( Stats.queueTrace ) System.err.printf("%s: maintenance %n ms...\n", getName(), Stats.maintenanceInterval); // NOI18N
		    long time = System.currentTimeMillis();
		    if( ! writer.maintenance(Stats.maintenanceInterval) ) {
			time = System.currentTimeMillis() - time;
			if( time < Stats.maintenanceInterval ) {
			    Thread.currentThread().sleep(Stats.maintenanceInterval - time);
			}
			break;
		    }
                    queue.onIdle();
		}
	    }
	    else {
		if( Stats.queueTrace ) System.err.printf("%s: sleeping %n ms...\n", getName(), Stats.maintenanceInterval); // NOI18N
		Thread.currentThread().sleep(Stats.maintenanceInterval);
	    }
	}
	if( Stats.queueTrace ) System.err.printf("%s: waiting...\n", getName()); // NOI18N
	queue.waitReady();
    }
    
    public void run() {
	if( Stats.queueTrace ) System.err.printf("%s: started.\n", getName());
        /*queue.registerMaintenancer(new Maintainer() {
            public boolean maintenance(long timeout) throws IOException {
                return writer.maintenance(timeout);
            }
        });*/
	while( true ) {
	    try {
		RepositoryQueue.Entry entry = queue.poll();
		if( entry == null ) {
		    if( RepositoryThreadManager.proceed() ) {
			 waitReady();
		    }
		    else {
			if( Stats.queueTrace ) System.err.printf("%s: exiting\n", getName()); // NOI18N
			break;
		    }
		} else {
		    if( Stats.queueTrace ) System.err.printf("%s: writing %s\n", getName(), entry.getKey()); // NOI18N
		    writer.write(entry.getKey(), entry.getValue());
		}
	    } catch( InterruptedException e ) {
		if( Stats.queueTrace ) System.err.printf("%s: interrupted\n", getName()); // NOI18N
		break;
	    } catch( IOException e ) {
		e.printStackTrace(System.err);
	    } catch( Exception e ) {
		e.printStackTrace(System.err);
	    }
	}
	
    }
    
    private String getName() {
        return Thread.currentThread().getName();
    }
    
}
