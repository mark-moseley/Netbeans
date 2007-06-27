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

package org.netbeans.spi.editor.completion;

import java.util.Collection;
import javax.swing.JToolTip;
import org.netbeans.modules.editor.completion.CompletionResultSetImpl;
import org.netbeans.modules.editor.completion.CompletionSpiPackageAccessor;

/**
 * Listener interface for passing the query results.
 * @see CompletionProvider#createTask
 *
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.01
 */

public final class CompletionResultSet {

    static {
        CompletionSpiPackageAccessor.register(new SpiAccessor());
    }
    
    /**
     * Sort type returned from {@link #getSortType()}
     * that prefers priority of the item ({@link CompletionItem#getSortPriority()})
     * over the text of the item ({@link CompletionItem#getSortText()}).
     */
    public static final int PRIORITY_SORT_TYPE = 0;
    
    /**
     * Sort type returned from {@link #getSortType()}
     * that prefers text of the item ({@link CompletionItem#getSortText()}).
     * over the priority of the item ({@link CompletionItem#getSortPriority()})
     */
    public static final int TEXT_SORT_TYPE = 1;

    private CompletionResultSetImpl impl;

    CompletionResultSet(CompletionResultSetImpl impl) {
        this.impl = impl;
        impl.setResultSet(this);
    }

    /**
     * Set title that will be assigned to the completion popup window.
     * <br>
     * It's only relevant to set the title when providing completion items
     * for {@link CompletionProvider#COMPLETION_QUERY_TYPE}.
     * <br>
     * If there will be multiple completion providers setting this property
     * for the given mime-type then only the first one
     * (according to the xml-layer registration order)
     * will be taken into account.
     */
    @Deprecated
    public void setTitle(String title) {
        impl.setTitle(title);
    }

    /**
     * Set the document offset to which the returned completion items
     * or documentation or tooltip should be anchored.
     * <br>
     * If there will be multiple completion providers setting this property
     * for the given mime-type then only the first one
     * (according to the xml-layer registration order)
     * will be taken into account.
     */
    public void setAnchorOffset(int anchorOffset) {
        impl.setAnchorOffset(anchorOffset);
    }

    /**
     * Add the completion item to this result set.
     * <br>
     * This method can be called multiple times until
     * all the items have been added to ths result set.
     * <br>
     * After the adding is completed @link #finish()} must be called to confirm
     * that the result set will no longer be modified.
     * 
     * @param item non-null completion item.
     * @return true if adding of the items can continue
     *  or false if there is already too many items
     *  to be practical to display in the listbox so subsequent
     *  adding should preferably be discontinued.
     */
    public boolean addItem(CompletionItem item) {
        return impl.addItem(item);
    }
    
    /**
     * Add the collection of the completion items to this result set.
     * <br>
     * This method can be called multiple times until
     * all the items have been added to ths result set.
     * <br>
     * After the adding is completed @link #finish()} must be called to confirm
     * that the result set will no longer be modified.
     * 
     * @param items collection of items to be added.
     * @return true if adding of the items can continue
     *  or false if there is already too many items
     *  to be practical to display in the listbox so subsequent
     *  adding should preferably be discontinued.
     */
    public boolean addAllItems(Collection<? extends CompletionItem> items) {
        return impl.addAllItems(items);
    }
    
    /**
     * Indicate that adding of the items to this result set
     * will likely need a long time so the resulting number of items
     * and their visual size should be estimated so that
     * the completion infrastructure can estimate the size
     * of the popup window and display the items added subsequently
     * without changing its bound extensively.
     * <br>
     * Without calling of this method the completion infrastructure
     * will wait until {@link #finish()} gets called on this result set
     * before displaying any of the items added to this result set.
     *
     * <p>
     * By calling of this method the task also confirms
     * that the items added by {@link #addItem(CompletionItem)} subsequently
     * are already in the order corresponding to the {@link #getSortType()}.
     *
     * @param estimatedItemCount estimated number of the items that will
     *  be added to this result set by {@link #addItem(CompletionItem)}.
     *  If the estimate is significantly lower than the reality then
     *  the vertical scrollbar granularity may be decreased or the vertical
     *  scrollbar can be removed completely once the result set is finished.
     *  If the estimate is significantly higher than the reality then
     *  the vertical scrollbar granularity may be increased
     *  once the result set is finished.
     * @param estimatedItemWidth estimated maximum visual width of a completion item.
     */
    public void estimateItems(int estimatedItemCount, int estimatedItemWidth) {
        impl.estimateItems(estimatedItemCount, estimatedItemWidth);
    }
    
    
    /**
     * Indicate that additional items could be added to this result set. However,
     * adding of these items will likely need a long time to complete so it is
     * preferred to add them only on the special code completion invocation
     * denoted by {@link CompletionProvider#COMPLETION_ALL_QUERY_TYPE}.
     * <br>
     * Calling this method is relevant only for tasks
     * created by {@link CompletionProvider#createTask(int, javax.swing.text.JTextComponent)}
     * with {@link CompletionProvider#COMPLETION_QUERY_TYPE}.
     */
    public void setHasAdditionalItems(boolean value) {
        impl.setHasAdditionalItems(value);
    }
    
    /**
     * Set the documentation to this result set.
     * <br>
     * Calling this method is only relevant for tasks
     * created by {@link CompletionProvider#createTask(int, javax.swing.text.JTextComponent)}
     * with {@link CompletionProvider#DOCUMENTATION_QUERY_TYPE}
     * or for {@link CompletionItem#createDocumentationTask()}.
     */
    public void setDocumentation(CompletionDocumentation documentation) {
        impl.setDocumentation(documentation);
    }
    
    /**
     * Set the tooltip to this result set.
     * <br>
     * Calling this method is only relevant for tasks
     * created by {@link CompletionProvider#createTask(int, javax.swing.text.JTextComponent)}
     * with {@link CompletionProvider#TOOLTIP_QUERY_TYPE}
     * or for {@link CompletionItem#createToolTipTask()}.
     */
    public void setToolTip(JToolTip toolTip) {
        impl.setToolTip(toolTip);
    }
    
    /**
     * Mark that this result set is finished and there will be no more
     * modifications done to it.
     */
    public void finish() {
        impl.finish();
    }
    
    /**
     * Check whether this result set is finished.
     *
     * @return true if the result set is already finished by previous call
     *  to {@link #finish()}.
     */
    public boolean isFinished() {
        return impl.isFinished();
    }
    
    /**
     * Get the sort type currently used by the code completion.
     * <br>
     * It's one of the {@link #PRIORITY_SORT_TYPE} or {@link #TEXT_SORT_TYPE}.
     */
    public int getSortType() {
        return impl.getSortType();
    }
    
    /**
     * Set the explicit value displayed in a label when the completion results
     * do not get computed during a certain timeout (e.g. 250ms).
     * <br>
     * If not set explicitly the completion infrastructure will use
     * the default text.
     *
     * @param waitText description of what the query copmutation
     *  is currently (doing or waiting for).
     *  <br>
     *  After previous explicit setting <code>null</code> can be passed
     *  to restore using of the default text.
     *  
     * @since 1.5
     */
    public void setWaitText(String waitText) {
        impl.setWaitText(waitText);
    }

    
    private static final class SpiAccessor extends CompletionSpiPackageAccessor {
        
        public CompletionResultSet createCompletionResultSet(CompletionResultSetImpl impl) {
            return new CompletionResultSet(impl);
        }
        
    }
}
