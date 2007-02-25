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
package org.netbeans.modules.vmd.game.dialog;

import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceContainer;

public class NewSequenceDialog extends AbstractNameValidationDialog  {

	private SequenceContainer sequenceContainer;
	private Sequence sequence;
	private int frameWidth;
	private int frameHeight;

	public NewSequenceDialog(SequenceContainer sequenceContainer, int frameWidth, int frameHeight) {
		this(sequenceContainer, null);
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
	}
	
	public NewSequenceDialog(SequenceContainer sequenceContainer, Sequence toCopy) {
		this.sequenceContainer = sequenceContainer;
		this.sequence = toCopy;
	}
	
	protected String getInitialStateDescriptionText() {
		return "Enter a Sequence name.";
	}
	
	protected String getNameLabelText() {
		return "Sequence name:";
	}
	
	protected String getDialogNameText() {
		return "Create a new sequence";
	}
	
	protected String getCurrentStateErrorText() {
		String errMsg = null; 
		String seqName = this.fieldName.getText();
	
		if (seqName.equals("")) {
			return this.getInitialStateDescriptionText();
		}
		if (!GlobalRepository.getInstance().isComponentNameAvailable(seqName)) {
			errMsg = "Component name already exists. Choose a different name.";
		}
		return errMsg;
	}
	
	protected void handleOKButton() {
		if (this.sequence == null) {
			this.sequenceContainer.createSequence(this.fieldName.getText(), Sequence.DEFAULT_FRAMES, this.frameWidth, this.frameHeight);
		}
		else {
			this.sequenceContainer.createSequence(this.fieldName.getText(), this.sequence);
		}
	}
	
}
