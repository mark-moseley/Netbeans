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
package org.netbeans.modules.mercurial.util;

import java.io.File;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 * A class to encapsulate a Repository and allow us to cache some values
 *
 * @author John Rice
 */
public class HgRepositoryContextCache {
    private static boolean hasHistory;
    private static boolean hasHeads;
    private static String pushDefault;
    private static String pullDefault;
    private static File root;
    
    private static VCSContext rootCtx;
    private static VCSContext historyCtx;
    private static VCSContext headsCtx;
    private static VCSContext pushCtx;
    private static VCSContext pullCtx;

    public static boolean hasHistory(VCSContext ctx) {
        if(ctx == historyCtx && ctx != null && hasHistory){
            return hasHistory;
        }else{
            root = getRoot(ctx);
            hasHistory = HgCommand.hasHistory(root);
            historyCtx = ctx;
            return hasHistory;
        }
    }
    
    public static void resetHasHeads() {
        headsCtx = null;
    }

    public static boolean hasHeads(VCSContext ctx) {
        
        if(ctx == headsCtx && ctx != null){
            return hasHeads;
        }else{
            root = getRoot(ctx);
            hasHeads = HgCommand.isMergeRequired(root);
            headsCtx = ctx;
            return hasHeads;
        }
    }
    
    public static void resetPullDefault() {
        pullCtx = null;
    }

    public static String getPullDefault(VCSContext ctx) {
        if(ctx == pullCtx && ctx != null){
            return pullDefault;
        }else{
            root = getRoot(ctx);
            pullDefault = HgCommand.getPullDefault(root);
            pullCtx = ctx;
            return pullDefault;
        }
    }
    
    public static void resetPushDefault() {
        pushCtx = null;
    }

    public static String getPushDefault(VCSContext ctx) {
        if(ctx == pushCtx && ctx != null){
            return pushDefault;
        }else{
            root = getRoot(ctx);
            pushDefault = HgCommand.getPushDefault(root);
            pushCtx = ctx;
            return pushDefault;
        }
    }
    
    private static File getRoot(VCSContext ctx){
        if(ctx == rootCtx && root != null){
            return root;
        }else{
            root = HgUtils.getRootFile(ctx);
            rootCtx = ctx;
            return root;
        }

    }
}

