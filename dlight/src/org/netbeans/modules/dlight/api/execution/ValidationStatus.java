/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.execution;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.nativeexecution.api.ObservableAction;

/**
 *
 */
public final class ValidationStatus {

    /**
     * Object that represents VALID status. VALID status means that
     * full validation was performed and passed.
     */
    public static ValidationStatus validStatus =
            new ValidationStatus(true, true, "OK", null); // NOI18N
    /**
     * Object that represents initial status (before any validation)
     */
    public static ValidationStatus initialStatus =
            new ValidationStatus(false, false, "Initial", null); // NOI18N
    private boolean isDefined = false;
    private boolean isValid = false;
    private String reason = "";
    private Collection<ObservableAction> requiredActions =
            new ArrayList<ObservableAction>();

    private ValidationStatus(
            final boolean isDefined,
            final boolean isValid,
            final String reason,
            final Collection<ObservableAction> requiredActions) {
        this.isValid = isValid;
        this.isDefined = isDefined;
        this.reason = reason == null ? "" : reason; // NOI18N
        if (requiredActions != null) {
            this.requiredActions.addAll(requiredActions);
        }
    }

    /**
     * Returns object that represents UNKNOWN validation status. This means that
     * validation was not completed by some reason and some action should be
     * performed in order to perform re-validation.
     *
     * @param reason string that describes why validation was not completed.
     * @param requiredAction an action that should be performed prior to
     * re-validation
     * @return object that represents UNKNOWN validation status.
     */
    public static ValidationStatus unknownStatus(final String reason,
            final ObservableAction requiredAction) {
        if (requiredAction == null) {
            throw new NullPointerException(
                    "requiredAction cannot be NULL"); // NOI18N
        }

        ArrayList<ObservableAction> actions = new ArrayList<ObservableAction>();
        actions.add(requiredAction);
        return new ValidationStatus(false, false, reason, actions);

    }

    /**
     * Returns object that represents INVALID validation status. This means that
     * it is known that validation failed and no additional actions could be
     * performed to perform re-validation.
     *
     * @param reason description of failure reason
     * @return object that represents INVALID validation status.
     */
    public static ValidationStatus invalidStatus(final String reason) {
        return new ValidationStatus(true, false, reason, null);
    }

    /**
     * Returns a string that describes the reason of validation failure or why
     * validation has not been fully performed.
     * @return reason of validation failure.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Merges two validation statuses and returns a new (merged) one.
     *
     * @param status validation status to merge with.
     * @return new <tt>ValidationStatus</tt> object that is constructed
     * from the provided <tt>status</tt> and this one.
     */
    public ValidationStatus merge(final ValidationStatus status) {
        if (this.isInvalid()) {
            // this was validated and is INVALID ->
            //              merged is invalid (just == this)
            // TODO: merge reasons?
            return this;
        }

        if (status.isInvalid()) {
            // status was validated and is INVALID ->
            //              merged is invalid (just == status)
            // TODO: merge reasons?
            return status;
        }

        if (this.isValid() && status.isValid()) {
            return this;
        }

        if (this.isValid()) {
            // here status is not validated -> merge is the status
            return status == initialStatus ? this : status;
        }

        if (status.isValid()) {
            // here this is not validated -> merge is this
            return this == initialStatus ? status : this;
        }

        // here both are not validated... do merge!
        Collection<ObservableAction> mergedActions =
                new ArrayList<ObservableAction>();
        mergedActions.addAll(this.requiredActions);
        mergedActions.addAll(status.requiredActions);

        String mergedReason = null;

        if (this == initialStatus) {
            mergedReason = status.reason;
        } else if (status == initialStatus) {
            mergedReason = this.reason;
        } else {
            mergedReason = this.reason + "; " + status.reason; // NOI18N
        }

        ValidationStatus result = new ValidationStatus(false, false,
                mergedReason.toString(),
                mergedActions);

        return result;
    }

    /**
     * Compares this status with the provided <tt>obj</tt>.
     * @param obj object to compare this status with.
     * @return true if and only if <tt>obj</tt> is instance of
     * <tt>ValidationStatus</tt> and their state (defined/undefined,
     * valid/invalid) are equal and <tt>getReason</tt> returns the same string
     * for both.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValidationStatus)) {
            return false;
        }

        ValidationStatus st = (ValidationStatus) obj;

        if (!st.reason.equals(reason)) {
            return false;
        }

        if (st.isDefined != isDefined) {
            return false;
        }

        if (isDefined == true) {
            if (st.isValid != isValid) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a hash code value for the object that is based on object's
     * sate and validation failure reason.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.isDefined ? 1 : 0);
        hash = 97 * hash + (this.isValid ? 1 : 0);
        hash = 97 * hash + (this.reason != null ? this.reason.hashCode() : 0);
        return hash;
    }

    /**
     * Returns a string representation of the <tt>ValidationStatus</tt>.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        if (!isDefined) {
            return "UNKNOWN: " + reason; // NOI18N
        } else {
            if (isValid) {
                return "OK"; // NOI18N
            } else {
                return "INVALID: " + reason; // NOI18N
            }
        }
    }

    /**
     * Returns actions (usually that require user interaction) needed to be
     * performed to complete full validation.
     *
     * @return Collection of observable actions needed to be performed to
     * complete full validation.
     */
    public Collection<ObservableAction> getRequiredActions() {
        return requiredActions;
    }

    /**
     * Returns <tt>true</tt> if and only if the status is known and is valid.
     * @return
     *          <tt>true</tt> if and only if the status is known and is valid.
     *          <tt>false</tt> otherwise
     */
    public boolean isValid() {
        return isDefined && isValid;
    }

    /**
     * Returns <tt>true</tt> if and only if the status is known and is invalid.
     * @return
     *          <tt>true</tt> if and only if status the is known and is invalid.
     *          <tt>false</tt> otherwise
     */
    public boolean isInvalid() {
        return isDefined && !isValid;
    }

    /**
     * Returns <tt>true</tt> if and only if the status is known.
     * @return
     *          <tt>true</tt> if and only if the status is known.
     *          <tt>false</tt> otherwise
     */
    public boolean isKnown() {
        return isDefined;
    }
}