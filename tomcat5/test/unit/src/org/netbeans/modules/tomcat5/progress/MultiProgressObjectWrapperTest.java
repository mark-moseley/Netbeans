/*
 * MultiProgressObjectWrapper.java
 * JUnit based test
 *
 * Created on August 14, 2007, 4:13 PM
 */

package org.netbeans.modules.tomcat5.progress;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class MultiProgressObjectWrapperTest extends NbTestCase {


    public MultiProgressObjectWrapperTest(String testName) {
        super(testName);
    }

    public void testCompleted() {
        TargetModuleID module1 = new TargetModuleIDImpl("name1", "description1", "id1", "url1");
        TargetModuleID module2 = new TargetModuleIDImpl("name2", "description2", "id2", "url2");

        ProgressObjectImpl object1 = new ProgressObjectImpl(new Status(ActionType.EXECUTE,
                CommandType.DISTRIBUTE, "start", StateType.RUNNING), module1);
        ProgressObjectImpl object2 = new ProgressObjectImpl(new Status(ActionType.EXECUTE,
                CommandType.DISTRIBUTE, "start", StateType.RUNNING), module2);

        MultiProgressObjectWrapper wrapper = new MultiProgressObjectWrapper(new ProgressObject[] {object1, object2});

        DeploymentStatus module1Status1 = new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE,
                "still running", StateType.RUNNING);
        ProgressEvent event1 = new ProgressEvent("fake", module1, module1Status1);

        DeploymentStatus module1Status2 = new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE,
                "completed", StateType.COMPLETED);
        ProgressEvent event2 = new ProgressEvent("fake", module1, module1Status2);

        DeploymentStatus module2Status1 = new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE,
                "completed", StateType.COMPLETED);
        ProgressEvent event3 = new ProgressEvent("fake", module2, module2Status1);

        ProgressListener listener1 = new ProgressListenerImpl(new ProgressEvent[] {event1, event2, event3},
                wrapper);

        ProgressListener listener2 = new ProgressListenerImpl(new ProgressEvent[] {event1, event2, event3},
                wrapper);

        wrapper.addProgressListener(listener1);
        wrapper.addProgressListener(listener2);

        // test itself
        object1.setDeploymentStatus(module1Status1);
        object1.setDeploymentStatus(module1Status2);
        object2.setDeploymentStatus(module2Status1);

        assertEquals(StateType.COMPLETED, wrapper.getDeploymentStatus().getState());
        assertTrue(wrapper.getDeploymentStatus().isCompleted());
        assertFalse(wrapper.getDeploymentStatus().isRunning());
        assertFalse(wrapper.getDeploymentStatus().isFailed());
    }

    public void testFailed() {
        TargetModuleID module1 = new TargetModuleIDImpl("name1", "description1", "id1", "url1");
        TargetModuleID module2 = new TargetModuleIDImpl("name2", "description2", "id2", "url2");

        ProgressObjectImpl object1 = new ProgressObjectImpl(new Status(ActionType.EXECUTE,
                CommandType.DISTRIBUTE, "start", StateType.RUNNING), module1);
        ProgressObjectImpl object2 = new ProgressObjectImpl(new Status(ActionType.EXECUTE,
                CommandType.DISTRIBUTE, "start", StateType.RUNNING), module2);

        MultiProgressObjectWrapper wrapper = new MultiProgressObjectWrapper(new ProgressObject[] {object1, object2});

        DeploymentStatus module1Status1 = new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE,
                "completed", StateType.COMPLETED);
        ProgressEvent event1 = new ProgressEvent("fake", module1, module1Status1);

        DeploymentStatus module2Status1 = new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE,
                "completed", StateType.FAILED);
        ProgressEvent event2 = new ProgressEvent("fake", module2, module2Status1);

        ProgressListener listener1 = new ProgressListenerImpl(new ProgressEvent[] {event1, event2},
                wrapper);

        ProgressListener listener2 = new ProgressListenerImpl(new ProgressEvent[] {event1, event2},
                wrapper);

        wrapper.addProgressListener(listener1);
        wrapper.addProgressListener(listener2);

        // test itself
        object1.setDeploymentStatus(module1Status1);
        object2.setDeploymentStatus(module2Status1);

        assertEquals(StateType.FAILED, wrapper.getDeploymentStatus().getState());
        assertFalse(wrapper.getDeploymentStatus().isCompleted());
        assertFalse(wrapper.getDeploymentStatus().isRunning());
        assertTrue(wrapper.getDeploymentStatus().isFailed());
    }

    private static class ProgressObjectImpl implements ProgressObject {

        private List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

        private DeploymentStatus status;

        private TargetModuleID targetModuleID;

        public ProgressObjectImpl(DeploymentStatus status, TargetModuleID targetModuleID) {
            this.status = status;
            this.targetModuleID = targetModuleID;
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
            return null;
        }

        public synchronized DeploymentStatus getDeploymentStatus() {
            return status;
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return new TargetModuleID[] { targetModuleID };
        }

        public boolean isCancelSupported() {
            return false;
        }

        public void cancel() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isStopSupported() {
            return false;
        }

        public void stop() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeProgressListener(ProgressListener listener) {
            if (listener != null) {
                listeners.remove(listener);
            }
        }

        public void addProgressListener(ProgressListener listener) {
            if (listener != null) {
                listeners.add(listener);
            }
        }

        public synchronized void setDeploymentStatus(DeploymentStatus status) {
            this.status = status;
            fireProgressEvent(new ProgressEvent(this, targetModuleID, status));
        }

        private void fireProgressEvent(ProgressEvent evt) {
            for (ProgressListener listener : listeners) {
                listener.handleProgressEvent(evt);
            }
        }

    }
}
