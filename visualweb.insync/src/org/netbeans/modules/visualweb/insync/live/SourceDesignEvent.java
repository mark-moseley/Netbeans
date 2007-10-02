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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.insync.live;

import com.sun.rave.designtime.*;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignEvent;

/**
 * Abstract base partial DesignEvent implementation which manages a EventDescriptor and other basic
 * beans and designtime stuff and ties into the rest of the SourceLive* classes.
 *
 * @author Carl Quinn
 */
public abstract class SourceDesignEvent implements DesignEvent {

    public static final DesignEvent[] EMPTY_ARRAY = {};

    protected final EventDescriptor descriptor;
    protected final SourceDesignBean liveBean;

    /**
     * Construct a new SourceDesignEvent for a given descriptor and live bean.
     *
     * @param descriptor The EventDescriptor that defines this event.
     * @param liveBean The SourceDesignBean that owns this event.
     */
    public SourceDesignEvent(EventDescriptor descriptor, SourceDesignBean liveBean) {
        this.descriptor = descriptor;
        this.liveBean = liveBean;
    }

    /*
     * @see com.sun.rave.designtime.DesignEvent#getEventDescriptor()
     */
    public EventDescriptor getEventDescriptor() {
        return descriptor;
    }

    /*
     * @see com.sun.rave.designtime.DesignEvent#getDesignBean()
     */
    public DesignBean getDesignBean() {
        return liveBean;
    }

    /**
     * @return the default event handler method name, same as setHandlerName would use if passed
     *         null
     * @see com.sun.rave.designtime.DesignEvent#getDefaultHandlerName()
     */
    public String getDefaultHandlerName() {
        String handlerName = liveBean.getInstanceName() + "_" +
            getEventDescriptor().getListenerMethodDescriptor().getName();
        return handlerName;
    }

    /**
    * ClipImage for events.
    */
   public static class ClipImage {
       String name;
       Object handler;
       ClipImage(String name, Object handler) { this.name = name; this.handler = handler; }

       public String toString() {
           StringBuffer sb = new StringBuffer();
           toString(sb);
           return sb.toString();
       }

       public void toString(StringBuffer sb) {
           sb.append("[DesignEvent.ClipImage");
           sb.append(" name=" + name);
           sb.append(" value=" + handler);
           sb.append("]");
       }
   }

   /**
    * @return
    */
   public ClipImage getClipImage() {
       return new ClipImage(descriptor.getName(), getHandlerName());
   }
}
