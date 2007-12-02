/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub;

import java.io.*;
import java.util.*;

import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.JID;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubEvent;

/**
 * Interface representing the items streamelement
 * in the pubsub#event namespace
 *
 */
public interface PubSubItems extends StreamElement {
	
    public final NSI NAME = new NSI("items",PubSubEvent.NAMESPACE);

    public StreamElement createPubSubItem(String id);
    
    public void setNodeIdentifier(String nodeId) throws IllegalArgumentException;
    
    public String getNodeIdentifier();
    
    public StreamElement addPubSubItem(String id);
    
    public StreamElement addPubSubItem(String id, String content);

    public StreamElement addPubSubItem(String id, StreamElement el);
    
    public void removePubSubItem(String id);
    
    public StreamElement getPubSubItem(String id);
    
    public List listPubSubItems();
    
    public void clearPubSubItems();
    
    public StreamElement createPubSubRetractItem(String id);
    
    public void addPubSubRetractItem(String id);
    
    public StreamElement getPubSubRetractItem();
    
    public boolean hasPubSubRetractItem();

}

