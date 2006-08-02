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

package org.netbeans.modules.debugger.jpda.util;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.ThreadReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.ErrorManager;

/**
 * Listens for events coming from a remove VM and notifies registered objects.
 * <P>
 * Any object implementing interface {@link Executor} can bind itself
 * with an {@link EventRequest}. Each time an {@link Event} corresponding
 * to the request comes from the virtual machine the <TT>Operator</TT>
 * notifies the registered object by calling its <TT>exec()</TT> method.
 * <P>
 * The only exceptions to the above rule are <TT>VMStartEvent</TT>,
 * <TT>VMDeathEvent</TT> and <TT>VMDisconnectEvent</TT> that cannot be
 * bound to any request. To listen for these events, specify <EM>starter</EM>
 * and <EM>finalizer</EM> in the constructor.
 * <P>
 * The operator is not active until it is started - use method <TT>start()</TT>.
 * The operator stops itself when either <TT>VMDeathEvent</TT> or <TT>VMDisconnectEvent</TT>
 * is received; it can be started again.
 * <P>
 * Use method {@link #register} to bind a requst with an object.
 * The object can be unregistered - use method {@link #unregister}.
 * <P>
 * There should be only one <TT>Operator</TT> per remote VM.
*
* @author Jan Jancura
*/
public class Operator {
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdievents"); // NOI18N

    private Thread            thread;
    private boolean           breakpointsDisabled;
    private List              staledEvents = new ArrayList();
    private List              staledRequests = new ArrayList();
    private boolean           stop;

    /**
     * Creates an operator for a given virtual machine. The operator will listen
     * to the VM's event queue.
     *
     * @param  virtualMachine  remote VM this operator will listen to
     * @param  starter  thread to be started upon start of the remote VM
     *                  (may be <TT>null</TT>)
     * @param  finalizer  thread to be started upon death of the remote VM
     *                    or upon disconnection from the VM
     *                    (may be <TT>null</TT>)
    */
    public Operator (
        VirtualMachine virtualMachine,
        Executor starter,
        Runnable finalizer,
        final Object resumeLock
    ) {
        EventQueue eventQueue = virtualMachine.eventQueue ();
        if (eventQueue == null) 
            throw new NullPointerException ();
        final Object[] params = new Object[] {eventQueue, starter, finalizer};
        thread = new Thread (new Runnable () {
        public void run () {
            EventQueue eventQueue = (EventQueue) params [0];
            Executor starter = (Executor) params [1];
            Runnable finalizer = (Runnable) params [2];
            params [0] = null;
            params [1] = null;
            params [2] = null;
            boolean processStaledEvents = false;
            
             try {
                 for (;;) {
                     EventSet eventSet = null;
                     if (processStaledEvents) {
                         synchronized (Operator.this) {
                             if (staledEvents.size() == 0) {
                                 processStaledEvents = false;
                             } else {
                                eventSet = (EventSet) staledEvents.remove(0);
                                while (staledRequests.size() > 0) {
                                    EventRequest request = (EventRequest) staledRequests.remove(0);
                                    request.virtualMachine().eventRequestManager().deleteEventRequest(request);
                                }
                                //eventSet.virtualMachine.suspend();
                             }
                         }
                     }
                     if (eventSet == null) {
                        try {
                            eventSet = eventQueue.remove ();
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("HAVE EVENT(s) in the Queue: "+eventSet);
                            }
                        } catch (InterruptedException iexc) {
                            synchronized (Operator.this) {
                                if (stop) {
                                    break;
                                }
                            }
                            processStaledEvents = true;
                            continue;
                        }
                     }
                     synchronized (Operator.this) {
                         if (breakpointsDisabled) {
                             if (eventSet.suspendPolicy() == EventRequest.SUSPEND_ALL) {
                                staledEvents.add(eventSet);
                                eventSet.resume();
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine("RESUMING "+eventSet);
                                }
                             }
                             continue;
                         }
                     }
                     boolean resume = true, startEventOnly = true;
                     EventIterator i = eventSet.eventIterator ();
                     if (logger.isLoggable(Level.FINE)) {
                         switch (eventSet.suspendPolicy ()) {
                             case EventRequest.SUSPEND_ALL:
                                 logger.fine("JDI new events (suspend all)=============================================");
                                 break;
                             case EventRequest.SUSPEND_EVENT_THREAD:
                                 logger.fine("JDI new events (suspend one)=============================================");
                                 break;
                             case EventRequest.SUSPEND_NONE:
                                 logger.fine("JDI new events (suspend none)=============================================");
                                 break;
                             default:
                                 logger.fine("JDI new events (?????)=============================================");
                                 break;
                         }
                     }
                     while (i.hasNext ()) {
                         Event e = i.nextEvent ();
                         if ((e instanceof VMDeathEvent) ||
                                 (e instanceof VMDisconnectEvent)
                            ) {

                             if (logger.isLoggable(Level.FINE)) {
                                 printEvent (e, null);
                             }
//                             disconnected = true;
                             if (finalizer != null) finalizer.run ();
                             //S ystem.out.println ("EVENT: " + e); // NOI18N
                             //S ystem.out.println ("Operator end2"); // NOI18N
                             finalizer = null;
                             eventQueue = null;
                             starter = null;
                             return;
                         }
                         
                         if ((e instanceof VMStartEvent) && (starter != null)) {
                             resume = resume & starter.exec (e);
                             //S ystem.out.println ("Operator.start VM"); // NOI18N
                             if (logger.isLoggable(Level.FINE)) {
                                 printEvent (e, null);
                             }
                             continue;
                         }
                         Executor exec = null;
                         if (e.request () == null) {
                             if (logger.isLoggable(Level.FINE)) {
                                 logger.fine("EVENT: " + e + " REQUEST: null"); // NOI18N
                             }
                         } else
                             exec = (Executor) e.request ().getProperty ("executor");

                         if (logger.isLoggable(Level.FINE)) {
                             printEvent (e, exec);
                         }

                         // safe invocation of user action
                         if (exec != null)
                             try {
                                 startEventOnly = false;
                                 resume = resume & exec.exec (e);
                             } catch (VMDisconnectedException exc) {   
//                                 disconnected = true;
                                 if (finalizer != null) finalizer.run ();
                                 //S ystem.out.println ("EVENT: " + e); // NOI18N
                                 //S ystem.out.println ("Operator end"); // NOI18N
                                 return;
                             } catch (Exception ex) {
                                 ErrorManager.getDefault().notify(ex);
                             }
                     } // while
                     //            S ystem.out.println ("END (" + set.suspendPolicy () + ") ==========================================================================="); // NOI18N
                     if (logger.isLoggable(Level.FINE)) {
                         logger.fine("JDI events dispatched (resume " + (resume && (!startEventOnly)) + ")");
                         logger.fine("  resume = "+resume+", startEventOnly = "+startEventOnly);
                     }
                     if (resume && (!startEventOnly)) {
                         synchronized (resumeLock) {
                            eventSet.resume ();
                         }
                     }
                 }// for
             } catch (VMDisconnectedException e) {   
             //} catch (InterruptedException e) {
             } catch (Exception e) {
                 ErrorManager.getDefault().notify(e);
             }
             if (finalizer != null) finalizer.run ();
             //S ystem.out.println ("Operator end"); // NOI18N
             finalizer = null;
             eventQueue = null;
             starter = null;
         }
     }, "Debugger operator thread"); // NOI18N
    }

    /**
    * Starts checking of JPDA messages.
    */
    public void start () {
        thread.start ();
    }

    /**
     * Binds the specified object with the event request.
     * If the request is already bound with another object,
     * the old binding is removed.
     *
     * @param  req  request
     * @param  e  object to be bound with the request
     *            (if <TT>null</TT>, the binding is removed - the same as <TT>unregister()</TT>)
     * @see  #unregister
     */
    public synchronized void register (EventRequest req, Executor e) {
        req.putProperty ("executor", e); // NOI18N
        if (staledEvents.size() > 0 && req instanceof StepRequest) {
            boolean addAsStaled = false;
            for (Iterator it = staledEvents.iterator(); it.hasNext(); ) {
                EventSet evSet = (EventSet) it.next();
                for (Iterator itSet = evSet.iterator(); itSet.hasNext(); ) {
                    Event ev = (Event) itSet.next();
                    EventRequest evReq = ev.request();
                    if (!(evReq instanceof StepRequest)) {
                        addAsStaled = true;
                        break;
                    } else {
                        ThreadReference evThread = ((StepRequest) evReq).thread();
                        ThreadReference reqThread = ((StepRequest) req).thread();
                        if (reqThread.equals(evThread)) {
                            addAsStaled = true;
                            break;
                        }
                    }
                }
                if (addAsStaled) break;
            }
            // Will be added if there is not a staled step event or if all staled
            // step events are on different threads.
            if (addAsStaled) {
                staledRequests.add(req);
            }
        };
    }

    /**
     * Removes binding between the specified event request and a registered object.
     *
     * @param  req  request
     * @see  #register
     */
    public synchronized void unregister (EventRequest req) {
        req.putProperty ("executor", null); // NOI18N
        staledRequests.remove(req);
    }
    
    /**
     * Stop the operator thread.
     */
    public void stop() {
        synchronized (this) {
            stop = true;
            staledRequests.clear();
            staledEvents.clear();
        }
        thread.interrupt();
    }
    
    /**
     * Notifies that breakpoints were disabled and therefore no breakpoint events should occur
     * until {@link #breakpointsEnabled} is called.
     */
    public synchronized void breakpointsDisabled() {
        breakpointsDisabled = true;
    }
    
    /**
     * Notifies that breakpoints were enabled again and therefore breakpoint events can occur.
     */
    public synchronized void breakpointsEnabled() {
        breakpointsDisabled = false;
    }
    
    public boolean flushStaledEvents() {
        boolean areStaledEvents;
        synchronized (this) {
            areStaledEvents = staledEvents.size() > 0;
            if (areStaledEvents) {
                thread.interrupt();
            }
        }
        return areStaledEvents;
    }

    private void printEvent (Event e, Executor exec) {
        try {
            if (e instanceof ClassPrepareEvent) {
                logger.fine("JDI EVENT: ClassPrepareEvent " + ((ClassPrepareEvent) e).referenceType ()); // NOI18N
            } else
            if (e instanceof ClassUnloadEvent) {
                logger.fine("JDI EVENT: ClassUnloadEvent " + ((ClassUnloadEvent) e).className ()); // NOI18N
            } else
            if (e instanceof ThreadStartEvent) {
                try {
                    logger.fine("JDI EVENT: ThreadStartEvent " + ((ThreadStartEvent) e).thread ()); // NOI18N
                } catch (Exception ex) {
                    logger.fine("JDI EVENT: ThreadStartEvent1 " + e); // NOI18N
                }
            } else
            if (e instanceof ThreadDeathEvent) {
                try {
                    logger.fine("JDI EVENT: ThreadDeathEvent " + ((ThreadDeathEvent) e).thread ()); // NOI18N
                } catch (Exception ex) {
                    logger.fine("JDI EVENT: ThreadDeathEvent1 " + e); // NOI18N
                }
            } else
            if (e instanceof MethodEntryEvent) {
                try {
                    logger.fine("JDI EVENT: MethodEntryEvent " + e);
                } catch (Exception ex) {
                    logger.fine("JDI EVENT: MethodEntryEvent " + e);
                }
            } else
            if (e instanceof BreakpointEvent) {
                logger.fine("JDI EVENT: BreakpointEvent " + ((BreakpointEvent) e).thread () + " : " + ((BreakpointEvent) e).location ()); // NOI18N
            } else
            if (e instanceof StepEvent) {
                logger.fine("JDI EVENT: StepEvent " + ((StepEvent) e).thread () + " : " + ((StepEvent) e).location ()); // NOI18N
            } else
                logger.fine("JDI EVENT: " + e + " : " + exec); // NOI18N
        } catch (Exception ex) {
        }
    }
}
