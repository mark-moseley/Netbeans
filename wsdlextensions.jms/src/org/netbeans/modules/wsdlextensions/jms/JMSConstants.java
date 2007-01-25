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
package org.netbeans.modules.wsdlextensions.jms;


/**
 * JMSConstants
 */
public class JMSConstants {
            
    // Destination Type (domain)
    public static final String QUEUE = "Queue";
    public static final String TOPIC = "Topic";
    
    // Transaction Support
    public static final String TRANSACTION_NONE  = "NoTransaction";
    public static final String TRANSACTION_LOCAL = "LocalTransaction";
    public static final String TRANSACTION_XA    = "XATransaction";
    
    // Delivery Modes
    public static final String DELIVERYMODE_PERSISTENT = "PERSISTENT";
    public static final String DELIVERYMODE_NON_PERSISTENT = "NON_PERSISTENT";
    
    // Time to live
    public static final long TIME_TO_LIVE_FOREVER = 0;

    // Timeout 
    public static final long TIME_OUT_MSECS_DEFAULT = 5 * 60000;
    
    // Delivery Priority
    public static final int PRIORITY_0 = 0;
    public static final int PRIORITY_1 = 1;
    public static final int PRIORITY_2 = 2;
    public static final int PRIORITY_3 = 3;
    public static final int PRIORITY_4 = 4;
    public static final int PRIORITY_5 = 5;
    public static final int PRIORITY_6 = 6;
    public static final int PRIORITY_7 = 7;
    public static final int PRIORITY_8 = 8;
    public static final int PRIORITY_9 = 9;
    public static final int PRIORITY_DEFAULT = PRIORITY_4;
    
    // Message Types
    public static final String TEXT_MESSAGE = "TextMessage";
    public static final String STREAM_MESSAGE = "StreamMessage";
    public static final String BYTES_MESSAGE = "BytesMessage";
    public static final String MAP_MESSAGE = "MapMessage";
    public static final String OBJECT_MESSAGE = "ObjectMessage";
    public static final String MESSAGE_MESSAGE = "Message";
    
    // Acknowlegement Modes
    public static final String AUTO_ACKNOWLEDGE   = "AUTO_ACKNOWLEDGE";
    public static final String CLIENT_ACKNOWLEDGE = "CLIENT_ACKNOWLEDGE";
    public static final String DUPS_OK_ACKNOWLEGE = "DUPS_OK_ACKNOWLEGE";    
    
    // Subscription Durability Types
    public static final String DURABLE  = "Durable";
    public static final String NON_DURABLE = "NonDurable";
    
    // Some options specific to JMS BC
    public static final String OUTBOUND_MAX_RETRIES = "SendMaxRetries";
    public static final String OUTBOUND_RETRY_INTERVAL = "SendRetryInterval";
    
    // Boolean values
    public static final String BOOLEAN_FALSE = "false";
    public static final String BOOLEAN_TRUE  = "true";
    
    // Batch size
    public static final int BATCH_SIZE_DEFAULT = 0;
    
    public static boolean stringValueIsTrue (String val) {        
        if (val == null || val.equals(BOOLEAN_FALSE) ) {
            return false;
        } else {
            return true;
        }
    }
    
}
