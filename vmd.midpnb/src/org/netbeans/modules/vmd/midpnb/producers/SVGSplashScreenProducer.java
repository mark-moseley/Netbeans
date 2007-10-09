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

package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.SVGSplashScreenDismissCommandCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGSplashScreenDismissCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGSplashScreenCD;
import org.netbeans.modules.vmd.midpnb.palette.MidpNbPaletteProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class SVGSplashScreenProducer extends MidpComponentProducer {
    
    public SVGSplashScreenProducer() {
        super(SVGSplashScreenCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG, NbBundle.getMessage(SVGSplashScreenProducer.class, "DISP_SVG_Splash_Screen"), NbBundle.getMessage(SVGSplashScreenProducer.class, "TTIP_SVG_Splash_Screen"), SVGSplashScreenCD.ICON_PATH, SVGSplashScreenCD.ICON_LARGE_PATH)); // NOI18N
    }

    @Override
    public Result postInitialize (DesignDocument document, DesignComponent splashScreen) {
        DesignComponent dismissCommand = MidpDocumentSupport.getSingletonCommand(document, SVGSplashScreenDismissCommandCD.TYPEID);
        
        DesignComponent dismissEventSource = document.createComponent(SVGSplashScreenDismissCommandEventSourceCD.TYPEID);
        
        dismissEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(splashScreen));
        dismissEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(dismissCommand));
        
        MidpDocumentSupport.addEventSource(splashScreen, DisplayableCD.PROP_COMMANDS, dismissEventSource);
        
        return new Result(splashScreen, dismissCommand, dismissEventSource);
    }
    
    @Override
    public boolean checkValidity(DesignDocument document) {
            return MidpJavaSupport.checkValidity(document, "javax.microedition.m2g.SVGImage") && // NOI18N
                   MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
    }
}
