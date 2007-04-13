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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.metadata.model.api;

import java.io.IOException;
import org.netbeans.modules.j2ee.metadata.model.MetadataModelAccessor;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.openide.util.Parameters;

/**
 * Encapsulates a generic metadata model. The kind of metadata and the
 * operation allowed on them is given by the <code>T</code> type parameter,
 * and must be described to the client by the provider of the model.
 *
 * @author Andrei Badea
 * @since 1.2
 */
public final class MetadataModel<T> {

    static {
        MetadataModelAccessor.DEFAULT = new MetadataModelAccessor() {
            public <E> MetadataModel<E> createMetadataModel(MetadataModelImplementation<E> impl) {
                return new MetadataModel<E>(impl);
            }
        };
    }

    private final MetadataModelImplementation<T> impl;

    private MetadataModel(MetadataModelImplementation<T> impl) {
        assert impl != null;
        this.impl = impl;
    }

    /**
     * Executes an action in the context of this model. This method is used to provide
     * the model client with access to the metadata contained in the model.
     *
     * <p>This method provides safe access to the model in the presence of concurrency.
     * It ensures that when the action's {@link MetadataModelAction#run} method
     * is running, no other thread  can be running another action's <code>run()</code> method on the same
     * <code>MetadataModel</code> instance. It also guarantees that the
     * metadata does not change until the action's <code>run()</code> method
     * returns.</p>
     *
     * <p><strong>This method does not, however, guarantee, that any piece of
     * metadata obtained from the model as a result of invoking <code>runReadAction()</code>
     * will still be present in the model when a subsequent invocation of
     * <code>runReadAction()</code> takes place. As a result, clients are forbidden
     * to call any methods on any piece of metadata obtained from the model outside
     * the <code>run()</code> method of an action being executed as a result of an
     * invocation of <code>runReadAction()</code>. In other words, pieces of metadata
     * that are not explicitly documented as immutable are not allowed to escape
     * the action's <code>run()</code> method.</strong></p>
     *
     * @param  action the action to be executed.
     * @return the value returned by the action's <code>run()</code> method.
     * @throws MetadataModelException if a checked exception was thrown by
     *         the action's <code>run()</code> method. That checked exception
     *         will be available as the return value of the {@link MetadataModelException#getCause getCause()}
     *         method. This only applies to checked exceptions; unchecked exceptions
     *         are propagated from the <code>run()</code> method unwrapped.
     * @throws IOException if there was a problem reading the model from its storage (for
     *         example an exception occured while reading the disk files
     *         which constitute the source for the model's metadata).
     * @throws NullPointerException if the <code>action</code> parameter was null.
     */
    public <R> R runReadAction(MetadataModelAction<T, R> action) throws MetadataModelException, IOException {
        Parameters.notNull("action", action); // NOI18N
        return impl.runReadAction(action);
    }
}
