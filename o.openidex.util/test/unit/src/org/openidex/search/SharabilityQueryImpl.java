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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.openidex.search;

import java.io.File;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import static org.netbeans.api.queries.SharabilityQuery.MIXED;
import static org.netbeans.api.queries.SharabilityQuery.NOT_SHARABLE;
import static org.netbeans.api.queries.SharabilityQuery.SHARABLE;

/**
 * Primitive implementation of {@link SharabilityQuery}.
 *
 * @author  MarianPetras
 */
public class SharabilityQueryImpl implements SharabilityQueryImplementation {

    private static final String NON_SHARABLE_NAME = "private";

    public int getSharability(File file) {
        boolean sharable = file.isFile() ? isSharable(file.getParentFile())
                                         : isSharable(file);
        return sharable ? (file.isFile() ? SHARABLE : MIXED)
                          : NOT_SHARABLE;
    }

    private static boolean isSharable(File folder) {
        assert folder.isDirectory();

        if (folder.getName().equals(NON_SHARABLE_NAME)) {
            return false;
        } else {
            File parent = folder.getParentFile();
            return (parent != null) ? isSharable(parent) : true;
        }
    }

}
