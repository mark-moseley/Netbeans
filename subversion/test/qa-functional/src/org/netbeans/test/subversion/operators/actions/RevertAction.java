/*
 * RevertAction.java
 *
 * Created on 18 May 2006, 17:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.operators.actions;

import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 *
 * @author peter
 */
public class RevertAction extends ActionNoBlock{
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";
            
    /** "Revert" menu item. */
    public static final String REVERT_ITEM = "Revert";
    
    /** Creates a new instance of RevertAction */
    public RevertAction() {
        super(SVN_ITEM + "|" + REVERT_ITEM, SVN_ITEM + "|" + REVERT_ITEM);
    }
    
}
