/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.util;

import java.lang.ref.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.openide.ErrorManager;
import org.openide.util.Mutex;

// XXX create a specialized subclass with underlying model an EditorCookie.Observable + Document

/**
 * Support for bidirectional construction of a derived model from an underlying model.
 * Based on a mutex which is assumed to control both models.
 * Handles all locking and scheduling associated with such a system.
 * It is possible to "nest" supports so that the derived model of one is the
 * underlying model of another - but they must still share a common mutex.
 *
 * <p>"Derive" means to take the underlying model (not represented explicitly here,
 * but assumed to be "owned" by the subclass) and produce the derived model;
 * typically this will involve parsing or the like. This operates in a read mutex.
 *
 * <p>"Recreate" means to take a new derived model (which may in fact be the same
 * as the old derived model but with different structure) and somehow change the
 * underlying model on that basis.
 *
 * <p>"Initiate" means to start derivation asynchronously, not waiting for the
 * result to be complete; this operation is idempotent, i.e. you can call it
 * whenever you think you might like the value later, but it will not cause
 * gratuitous extra derivations.
 *
 * <p>"Invalidate" means to signal that the underlying model has somehow changed
 * and that if there is any derived model it should be considered stale.
 * Invalidating when there is not yet any derived model is a no-op.
 *
 * <p>There are four different kinds of "values" which are employed by this class
 * and which you should be careful to differentiate:
 *
 * <ol>
 *
 * <li><p>The state of the underlying model. This is <em>not</em> explicitly modeled
 * by this class. Subclasses are expected to use that state as needed in
 * {@link #doDerive} and {@link #doRecreate}.
 *
 * <li><p>The state ("value") of the derived model. This is never null and is the
 * return value of {@link #doDerive}, {@link #doRecreate}, {@link #getValueBlocking},
 * {@link #getValueNonBlocking}, and {@link #getStaleValueNonBlocking} (except
 * where those methods are documented to return null), as well as the first
 * parameter to {@link #doRecreate} and {@link #doDerive} and the parameter to
 * {@link #createReference}.
 *
 * <li><p>Deltas in the underlying model. These may in fact be entire new copies
 * of an underlying model, or some diff-like structure, or an {@link java.util.EventObject},
 * etc. - whatever seems most convenient. These are never null and are the argument
 * type of {@link #invalidate} and the second argument type of {@link #doDerive}
 * as well as both argument types and the return value of {@link #composeUnderlyingDeltas}.
 *
 * <li><p>Deltas in the derived model. Again these may be of the same form as the
 * derived model itself - just replacing the model wholesale - or they may be some
 * kind of diff or event structure. These are again never null and are the argument
 * for {@link #mutate} and the second argument for {@link #doRecreate}.
 *
 * </ol>
 *
 * <p>Setting a new derived value explicitly always sets it immediately.
 * When getting the derived value, you have several choices. You can ask for the
 * exact value, if necessary waiting for it to be derived for the first time, or
 * rederived if it is stale. Or you can ask for the value if it is fresh or accept
 * null if it is missing or stale. Or you can ask for the value if it is fresh or
 * stale and accept null if it is missing. The latter two operations do not block
 * (except to get the read mutex) and so are valuable in views.
 *
 * <p>Derivation is started immediately after an initiate operation if there is
 * no derived model yet. If there is a model but it is stale and you ask to
 * initiate derivation, by default this also starts immediately, but you may
 * instead give a delay before the new derivation starts (assuming no one asks
 * for the exact derived value before then); this is useful for cases where
 * derivation is time-consuming (e.g. a complex parse) and for performance
 * reasons you wish to avoid triggering it too frivolously. For example, you may
 * be invalidating the derived model after every keystroke which changes a text
 * document, but would prefer to wait a few seconds before showing new results.
 *
 * <p>In case a recreate operation is attempted during a delay in which the model
 * is stale, or simply while a derivation is in progress with or without a preceding
 * delay, there is a conflict: the recreated model is probably a modification of
 * the old stale underlying model, and it is likely that setting it as the new derived
 * model and recreating the underlying model would clobber intermediate changes in the
 * underlying model, causing data loss. By default this support will signal an exception
 * if this is attempted, though subclasses may choose to suppress that and forcibly
 * set the new derived model and recreate the underlying model. Subclasses are better advised
 * to use the exception, and ensure that views of the derived model either handle
 * it gracefully (e.g. offering the user an opportunity to retry the modification
 * on the new derived model when it is available, or just beeping), or put the
 * derived view into a read-only mode temporarily while there is a stale underlying
 * model so that such a situation cannot arise.
 *
 * <p>There is a kind of "external clobbering" that can occur if the view does not
 * update itself promptly after a recreation (generally, after a change in the
 * derived model leading to a fresh value) but only with some kind of delay. In
 * that case an attempted change to the derived model may be working with obsolete
 * data. The support does <em>not</em> try to handle this case; the view is
 * responsible for detecting it and reacting appropriately.
 *
 * <p>Another kind of "clobbering" can occur in case the underlying model is not
 * completely controlled by the mutex. For example, it might be the native filesystem,
 * which can change at any time without acquiring a lock in the JVM. In that case
 * an attempted mutation may be operating against a model derived from an older
 * state of the underlying model. Again, this support does <em>not</em> provide a
 * solution for this problem. Subclasses should attempt to detect such a condition
 * and recover from it gracefully, e.g. by throwing an exception from
 * <code>doRecreate</code> or by merging changes. Using TwoWaySupport may not be
 * appropriate for such cases anyway, since derivation could then cause an existing
 * reader to see state changes within its read mutex, which could violate its
 * assumptions about the underlying model.
 *
 * <p>Derivation and recreation may throw checked exceptions. In such cases the
 * underlying and derived models should be left in a consistent state if at all
 * possible. If derivation throws an exception, the derived model will be considered
 * stale, but no attempt to rederive the model will be made unless the underlying
 * model is invalidated; subsequent calls to {@link #getValueBlocking} with the
 * same underlying model will result in the same exception being thrown repeatedly.
 * Views should generally put themselves into a read-only mode in this case.
 * If recreation throws an exception, this is propagated to {@link #mutate} but
 * otherwise nothing is changed.
 *
 * <p>You may not call any methods of this class from within the dynamic scope of
 * {@link #doDerive} or {@link @doRecreate} or a listener callback.
 *
 * <p>You can attach a listener to this class. You will get an event when the
 * status of the support changes. All events are fired as soon as possible in the
 * read mutex.
 *
 * @author Jesse Glick
 */
public abstract class TwoWaySupport {
    
    /** lock used for all static vars */
    private static final Object LOCK = new String("TwoWaySupport");
    
    /** supports which are scheduled to be derived but haven't been yet */
    private static final SortedSet toDerive = new TreeSet(); // SortedSet<DeriveTask>
    
    /** derivation tasks indexed by support */
    private static final Map tasks = new WeakHashMap(); // Map<TwoWaySupport,DeriveTask>
    
    /** derivation thread when it has been started */
    private static boolean startedThread = false;
    
    /** queue of derived model references */
    private static ReferenceQueue queue = null;
    
    /** reverse lookup for model field to support queue collector */
    private static final Map referencesToSupports = new WeakHashMap(); // Map<Reference<Object>,Reference<TwoWaySupport>>
    
    /** associated mutex */
    private final Mutex mutex;
    
    /** listener list */
    private final List listeners; // List<TwoWayListener>
    
    /** current derived model, if any */
    private Reference model = null; // Reference<Object>
    
    /** current derivation problem, if any */
    private Exception problem = null; // XXX should perhaps be Reference<Exception>?
    
    /** if model is not null, whether it is fresh or stale */
    private boolean fresh = false;
    
    /** if true, derivation has been initiated */
    private boolean active = false;
    
    /** underlying delta, if one is being processed thru initiate + doDerive */
    private Object underlyingDelta = null;
    
    /** currently in doRecreate() */
    private boolean mutating = false;
    
    /** currently in doDerive() */
    private boolean deriving = false;
    
    /**
     * Create an uninitialized support.
     * No derivation or recreation is scheduled initially.
     * @param mutex the associated mutex
     */
    protected TwoWaySupport(Mutex mutex) {
        if (mutex == Mutex.EVENT) throw new IllegalArgumentException("Mutex.EVENT can deadlock TwoWaySupport!");
        this.mutex = mutex;
        listeners = new ArrayList();
    }
    
    /**
     * Get the associated mutex.
     * @return the mutex
     */
    public final Mutex getMutex() {
        return mutex;
    }
    
    /**
     * Compute the derived model from the underlying model.
     *
     * <p>This method is called with a read lock held on the mutex.
     * However for derived models with mutable state you may need to acquire an
     * additional simple lock (monitor) on some part of the model to refresh its
     * state - this is not a true write, but other readers should be locked out
     * until it is finished. For purely functional derived models that are
     * replaced wholesale, this is not necessary.
     *
     * <p>Note that derivations never run in parallel, even though they are in a
     * read mutex. In this implementation, all derivations in fact run in a dedicated
     * thread if they are invoked asynchronously using {@link #initiate}, but that
     * may change.
     *
     * <p>{@link TwoWayListener#derived} will be triggered after this method
     * completes. However, in the case of a derived model with internal
     * state with a complex relationship to the underlying model, it may not be
     * apparent from a {@link TwoWayEvent.Derived} what the changes to the derived
     * model were. Therefore, an implementation of this method may wish to fire
     * suitable changes to listeners on the derived model, rather than extracting
     * this information from the derived event.
     *
     * @param oldValue the old value of the derived model, or null if it had
     *                 never been calculated before
     * @param underlyingDelta a change in the underlying model, or null if no
     *                        particular change was signalled
     * @return the new value of the derived model (might be the same object as
     *         the old value)
     * @throws Exception (checked only!) if derivation of the model failed
     */
    protected abstract Object doDerive(Object oldValue, Object underlyingDelta) throws Exception;
    
    /**
     * Compute the effect of two sequential changes to the underlying model.
     * 
     * <p>This method is called with a read lock held on the mutex.
     *
     * @param underlyingDelta1 the older delta
     * @param underlyingDelta2 the newer delta
     * @return a delta representing those two changes applied in sequence
     */
    protected abstract Object composeUnderlyingDeltas(Object underlyingDelta1, Object underlyingDelta2);
    
    /**
     * Recreate the underlying model from the derived model.
     *
     * <p>This method is called with a write lock held on the mutex.
     *
     * <p>It is expected that any changes to the underlying model will be notified
     * to the relevant listeners within the dynamic scope of this method. Normally
     * an implementation will also notify changes to the derived model, unless that
     * has been done by other code already.
     *
     * @param oldValue the old value of the derived model, or null if it was
     *                 never derived
     * @param derivedDelta a change in the derived model
     * @return the new value of the derived model (might be the same object as
     *         the old value)
     * @throws Exception (checked only!) if recreation of the underlying model failed
     */
    protected abstract Object doRecreate(Object oldValue, Object derivedDelta) throws Exception;
    
    private boolean stateConsistent() {
        assert Thread.holdsLock(LOCK);
        if (fresh && model == null) return false;
        if (fresh && problem != null) return false;
        if (fresh && active) return false;
        if (active) {
            if (!tasks.containsKey(this)) return false;
            // XXX check that toDerive and tasks are consistent
        } else {
            if (tasks.containsKey(this)) return false;
        }
        // XXX what else?
        return true;
    }
    
    /**
     * Get the value of the derived model, blocking as needed until it is ready.
     * This method requires the read mutex and may block further for
     * {@link #doDerive}.
     * @return the value of the derived model (never null)
     * @throws InvocationTargetException if <code>doDerive</code> was called
     *                                   and threw an exception (possibly from an
     *                                   earlier derivation run that is still broken)
     */
    public final Object getValueBlocking() throws InvocationTargetException {
        assert mutex.canRead();
        Object old;
        synchronized (LOCK) {
            assert stateConsistent();
            assert !mutating;
            while (deriving) {
                // Another reader is getting the value at the moment, wait for it.
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {/* OK */}
            }
            if (fresh) {
                Object o = model.get();
                if (o != null) {
                    return o;
                }
            } else if (problem != null) {
                throw new InvocationTargetException(problem);
            }
            // Else we need to block for a value.
            old = (model != null) ? model.get() : null;
            deriving = true;
            fresh = false;
        }
        Object result = null;
        Exception resultingProblem = null;
        try {
            result = doDerive(old, null);
            assert result != null;
            fireChange(new TwoWayEvent.Derived(this, old, result, underlyingDelta));
            return result;
        } catch (RuntimeException e) {
            // We don't treat these as model-visible exceptions.
            throw e;
        } catch (Exception e) {
            resultingProblem = e;
            fireChange(new TwoWayEvent.Broken(this, old, underlyingDelta, e));
            throw new InvocationTargetException(e);
        } finally {
            synchronized (LOCK) {
                deriving = false;
                LOCK.notifyAll();
                if (active) {
                    // No longer need to run this.
                    active = false;
                    DeriveTask t = (DeriveTask)tasks.get(this);
                    assert t != null;
                    toDerive.remove(t);
                }
                if (result != null) {
                    setModel(result);
                    fresh = true;
                } else if (resultingProblem != null) {
                    problem = resultingProblem;
                    fresh = false;
                }
            }
        }
    }
    
    private void setModel(Object result) {
        assert Thread.holdsLock(LOCK);
        assert result != null;
        if (model != null) {
            referencesToSupports.remove(model);
        }
        model = createEnqueuedReference(result);
        referencesToSupports.put(model, new WeakReference(this));
    }
    
    /**
     * Get the value of the derived model, if it is ready and fresh.
     * This method requires the read mutex but otherwise does not block.
     * @return the value of the derived model, or null if it is stale or has never
     *         been computed at all
     */
    public final Object getValueNonBlocking() {
        assert mutex.canRead();
        synchronized (LOCK) {
            assert stateConsistent();
            assert !mutating;
            return fresh ? model.get() : null;
        }
    }
    
    /**
     * Get the value of the derived model, if it is ready (fresh or stale).
     * This method requires the read mutex but otherwise does not block.
     * @return the value of the derived model, or null if it has never been
     *         computed at all
     */
    public final Object getStaleValueNonBlocking() {
        assert mutex.canRead();
        synchronized (LOCK) {
            assert stateConsistent();
            assert !mutating;
            return (model != null) ? model.get() : null;
        }
    }
    
    /**
     * Change the value of the derived model and correspondingly update the
     * underlying model.
     * <p>This method requires the write mutex and calls {@link #doRecreate}
     * if it does not throw <code>ClobberException</code>.
     * @param derivedDelta a change to the derived model
     * @return the new value of the derived model
     * @throws ClobberException in case {@link #permitsClobbering} is false and
     *                          the old value of the derived model was stale or
     *                          missing
     * @throws InvocationTargetException if <code>doRecreate</code> throws an
     *                                   exception
     */
    public final Object mutate(Object derivedDelta) throws ClobberException, InvocationTargetException {
        if (derivedDelta == null) throw new NullPointerException();
        assert mutex.canWrite();
        Object oldValue;
        synchronized (LOCK) {
            assert stateConsistent();
            assert !mutating;
            assert !deriving;
            oldValue = (model != null) ? model.get() : null;
            if (!fresh && !permitsClobbering()) {
                throw new ClobberException(this, oldValue, derivedDelta);
            }
            mutating = true;
        }
        try {
            // XXX should also dequeue if necessary to avoid sequence:
            // invalidate -> initiate -> [pause] -> mutate -> [pause] -> invalidate -> [pause] -> derive
            // where the final derivation was not really appropriate (or was it?)
            Object result = doRecreate(oldValue, derivedDelta);
            setModel(result);
            if (fresh) {
                fireChange(new TwoWayEvent.Recreated(this, oldValue, result, derivedDelta));
            } else {
                fireChange(new TwoWayEvent.Clobbered(this, oldValue, result, derivedDelta));
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            synchronized (LOCK) {
                mutating = false;
            }
        }
    }
    
    /**
     * Indicate that any current value of the derived model is invalid and
     * should no longer be used if exact results are desired.
     * <p>This method requires the read mutex but does not block otherwise,
     * except to call {@link #composeUnderlyingDeltas}.
     * @param underlyingDelta a change to the underlying model
     */
    public final void invalidate(Object underlyingDelta) {
        if (underlyingDelta == null) throw new NullPointerException();
        assert mutex.canRead();
        boolean oldFresh;
        Object oldValue;
        synchronized (LOCK) {
            assert stateConsistent();
            assert !mutating;
            if (this.underlyingDelta != null) {
                // XXX don't call this with LOCK held
                // may then need to have an 'invalidating' flag (?)
                this.underlyingDelta = composeUnderlyingDeltas(this.underlyingDelta, underlyingDelta);
            } else {
                this.underlyingDelta = underlyingDelta;
            }
            oldFresh = fresh;
            if (fresh) {
                fresh = false;
            }
            oldValue = (model != null) ? model.get() : null;
        }
        if (oldFresh && oldValue != null) {
            fireChange(new TwoWayEvent.Invalidated(this, oldValue, underlyingDelta));
        }
    }

    /**
     * Initiate creation of the derived model from the underlying model.
     * This is a no-op unless that process has not yet been started or if the
     * value of the derived model is already fresh and needs no rederivation.
     * <p>This method does not require the mutex nor does it block.
     */
    public final void initiate() {
        synchronized (LOCK) {
            assert stateConsistent();
            if (!active && !fresh) {
                DeriveTask t = new DeriveTask(this);
                toDerive.add(t);
                tasks.put(this, t);
                active = true;
                startDerivationThread();
                LOCK.notifyAll();
            }
        }
    }
    
    /**
     * Add a listener to lifecycle changes in the support.
     * <p>A listener may be added multiple times and must be removed once
     * for each add.
     * <p>This method may be called from any thread and will not block.
     * @param l a listener to add
     */
    public final void addTwoWayListener(TwoWayListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    /**
     * Add a listener to lifecycle changes in the support.
     * <p>This method may be called from any thread and will not block.
     * @param l a listener to remove
     */
    public final void removeTwoWayListener(TwoWayListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * Fire an event to all listeners in the read mutex.
     */
    private void fireChange(final TwoWayEvent e) {
        final TwoWayListener[] ls;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            ls = (TwoWayListener[])listeners.toArray(new TwoWayListener[listeners.size()]);
        }
        mutex.readAccess(new Mutex.Action() {
            public Object run() {
                for (int i = 0; i < ls.length; i++) {
                    if (e instanceof TwoWayEvent.Derived) {
                        ls[i].derived((TwoWayEvent.Derived)e);
                    } else if (e instanceof TwoWayEvent.Invalidated) {
                        ls[i].invalidated((TwoWayEvent.Invalidated)e);
                    } else if (e instanceof TwoWayEvent.Recreated) {
                        ls[i].recreated((TwoWayEvent.Recreated)e);
                    } else if (e instanceof TwoWayEvent.Clobbered) {
                        ls[i].clobbered((TwoWayEvent.Clobbered)e);
                    } else if (e instanceof TwoWayEvent.Forgotten) {
                        ls[i].forgotten((TwoWayEvent.Forgotten)e);
                    } else {
                        assert e instanceof TwoWayEvent.Broken;
                        ls[i].broken((TwoWayEvent.Broken)e);
                    }
                }
                return null;
            }
        });
    }
    
    /**
     * Supply an optional delay before rederivation of a model after an invalidation.
     * If zero (the default), there is no intentional delay. The delay is irrelevant
     * in the case of {@link #getValueBlocking}.
     * @return a delay in milliseconds (>= 0)
     */
    protected long delay() {
        return 0L;
    }
    
    /**
     * Indicate whether this support permits changes to the derived model via
     * {@link #mutate} to "clobber" underived changes to the underlying model.
     * If false (the default), such attempts will throw {@link ClobberException}.
     * If true, they will be permitted, though a clobber event will be notified
     * rather than a recreate event.
     * @return true to permit clobbering, false to forbid it
     */
    protected boolean permitsClobbering() {
        return false;
    }
    
    private Reference createEnqueuedReference(Object value) {
        Reference r = createReference(value, queue);
        if (!(r instanceof StrongReference) && queue == null) {
            // Well discard that one; optimistically assumed that
            // createReference is not overridden, in which case we
            // never actually have to make a queue.
            queue = new ReferenceQueue();
            r = createReference(value, queue);
            Thread t = new Thread(new QueuePollingThread(), "TwoWaySupport.QueuePollingThread");
            t.setPriority(Thread.MIN_PRIORITY);
            t.setDaemon(true);
            t.start();
        }
        return r;
    }
    
    private static final class QueuePollingThread implements Runnable {
        
        public void run() {
            while (true) {
                try {
                    Reference r = queue.remove();
                    TwoWaySupport s;
                    synchronized (LOCK) {
                        Reference r2 = (Reference)referencesToSupports.remove(r);
                        s = (r2 != null) ? (TwoWaySupport)r2.get() : null;
                    }
                    if (s != null) {
                        s.fireChange(new TwoWayEvent.Forgotten(s));
                    }
                } catch (InterruptedException e) {
                    assert false : e;
                }
            }
        }
        
    }
    
    /**
     * Create a reference to the derived model.
     * The support will only retain this reference (though event objects will
     * strongly refer to the derived model when appropriate).
     * If the referent is collected, the support returns to an underived state.
     *
     * <p>This implementation always creates a strong reference that will never
     * be collected so long as the support itself is not collected.
     * @param value a derived model object
     * @param q a reference queue supplied by the support
     * @return a reference to the model enqueued on that reference queue
     */
    protected Reference createReference(Object value, ReferenceQueue q) {
        // Does not matter what the queue is.
        return new StrongReference(value);
    }

    /**
     * A strong reference whose referent will not be collected unless the
     * reference is too.
     */
    private static final class StrongReference extends WeakReference {
        private Object value;
        public StrongReference(Object value) {
            super(value);
            assert value != null;
            this.value = value;
        }
        public Object get() {
            return value;
        }
        public void clear() {
            super.clear();
            value = null;
        }
    }
    
    private static final class DeriveTask implements Comparable {
        
        public final Reference support; // Reference<TwoWaySupport>
        
        public final long schedule;
        
        public DeriveTask(TwoWaySupport support) {
            this.support = new WeakReference(support);
            this.schedule = System.currentTimeMillis() + support.delay();
        }
        
        public int compareTo(Object o) {
            DeriveTask t = (DeriveTask)o;
            if (t == this) return 0;
            if (schedule > t.schedule) return 1;
            if (schedule < t.schedule) return -1;
            return hashCode() - t.hashCode();
        }
        
    }
    
    private static void startDerivationThread() {
        synchronized (LOCK) {
            if (!startedThread) {
                Thread t = new Thread(new DerivationThread(), "TwoWaySupport.DerivationThread");
                t.setPriority(Thread.MIN_PRIORITY);
                t.setDaemon(true);
                t.start();
                startedThread = true;
            }
        }
    }
    
    private static final class DerivationThread implements Runnable {
        
        public void run() {
            synchronized (LOCK) {
                while (true) {
                    while (toDerive.isEmpty()) {
                        try {
                            LOCK.wait();
                        } catch (InterruptedException e) {
                            assert false : e;
                        }
                    }
                    Iterator it = toDerive.iterator();
                    DeriveTask t = (DeriveTask)it.next();
                    final TwoWaySupport s = (TwoWaySupport)t.support.get();
                    if (s != null) {
                        long now = System.currentTimeMillis();
                        if (t.schedule <= now) {
                            s.getMutex().readAccess(new Mutex.Action() {
                                public Object run() {
                                    try {
                                        s.getValueBlocking();
                                        // Ignore value and exceptions - gVB is
                                        // enough to cache that info and fire changes.
                                    } catch (InvocationTargetException e) {
                                        // OK, handled separately.
                                    } catch (RuntimeException e) {
                                        // Oops!
                                        ErrorManager.getDefault().notify(e);
                                    } catch (Error e) {
                                        // Oops!
                                        ErrorManager.getDefault().notify(e);
                                    }
                                    return null;
                                }
                            });
                            // Don't explicitly remove it from the queue - if it was
                            // active, then gVB should have done that itself.
                        } else {
                            try {
                                LOCK.wait(t.schedule - now);
                            } catch (InterruptedException e) {
                                assert false : e;
                            }
                            // Try again in next round.
                        }
                    } else {
                        // Dead - support was collected before we got to it.
                        it.remove();
                    }
                }
            }
        }
        
    }
    
}
