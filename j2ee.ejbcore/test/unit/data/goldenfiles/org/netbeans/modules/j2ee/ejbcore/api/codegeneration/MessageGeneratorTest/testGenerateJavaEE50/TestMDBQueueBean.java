/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testGenerateJavaEE50;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author {user}
 */
@MessageDriven(mappedName = "TestMessageDestination", activationConfig =  {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
    })
public class TestMDBQueueBean implements MessageListener {
    
    public TestMDBQueueBean() {
    }

    public void onMessage(Message message) {
    }
    
}
