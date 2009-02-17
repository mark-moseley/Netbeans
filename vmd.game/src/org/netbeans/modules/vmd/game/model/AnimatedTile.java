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
package org.netbeans.modules.vmd.game.model;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.game.dialog.NewSequenceDialog;
import org.netbeans.modules.vmd.game.dialog.RenameAnimatedTileDialog;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceContainerEditor;
import org.netbeans.modules.vmd.game.editor.sequece.SequenceContainerNavigator;
import org.netbeans.modules.vmd.game.preview.SequenceContainerPreview;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class AnimatedTile extends Tile implements SequenceContainer, Editable, Identifiable {

	private long id = Identifiable.ID_UNKNOWN;

	public static final boolean DEBUG = false;
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private SequenceContainerImpl sequenceContainer;
	
	private SequenceContainerEditor editor;
	private JComponent navigator;

	private String name;
	
	
	AnimatedTile(String name, ImageResource imageResource, int index, Sequence sequence, int width, int height) {
		super(imageResource, index, width, height);
		this.sequenceContainer = new SequenceContainerImpl(this, null, this.propertyChangeSupport, imageResource, width, height, false);
		this.name = name;
		this.setDefaultSequence(sequence);
	}
	
	AnimatedTile(String name, ImageResource imageResource, int index, int width, int height) {
		super(imageResource, index, width, height);
		this.sequenceContainer = new SequenceContainerImpl(this, null, this.propertyChangeSupport, imageResource, width, height, false);
		this.name = name;
		String seqName = this.getNextSequenceName(this.name + "seq"); // NOI18N
		Sequence sequence = this.createSequence(seqName, 1, width, height);
		this.setDefaultSequence(sequence);
	}

	
	public String getNextSequenceName(String prefix) {
		return this.sequenceContainer.getNextSequenceName(prefix);
	}

	public GlobalRepository getGameDesign() {
		return this.getImageResource().getGameDesign();
	}	
	
	public void addSequenceContainerListener(SequenceContainerListener listener) {
		this.sequenceContainer.addSequenceContainerListener(listener);
	}

	public void removeSequenceContainerListener(SequenceContainerListener listener) {
		this.sequenceContainer.removeSequenceContainerListener(listener);
	}

	public void setName(String name) {
		if (name == null) {
			return;
		}
		if (this.getName().equals(name)) {
			return;
		}
		if (!this.getGameDesign().isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("AnimatedTile cannot be renamed because component name '" + name + "' already exists."); // NOI18N
		}
		String oldName = this.name;
		this.name = name;
		//System.out.println("old name: " + oldName + ", new name: " + name);
		this.propertyChangeSupport.firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	//------SequenceContainer-------
	
	public String getName() {
		return this.name;
	}

	public Sequence createSequence(String name, int numberFrames, int frameWidth, int frameHeight) {
		return this.sequenceContainer.createSequence(name, numberFrames, frameWidth, frameHeight);
	}
	
	public Sequence createSequence(String name, Sequence s) {
		return this.sequenceContainer.createSequence(name, s);
	}
	
	public boolean append(Sequence sequence) {
		return this.sequenceContainer.append(sequence);
	}
	
	public boolean insert(Sequence sequence, int index) {
		return this.sequenceContainer.insert(sequence, index);
	}
	
	public boolean remove(Sequence sequence) {
		return this.sequenceContainer.remove(sequence);
	}
	
	public void move(Sequence sequence, int newIndex) {
		this.sequenceContainer.move(sequence, newIndex);
	}
	
	public List<Sequence> getSequences() {
		return this.sequenceContainer.getSequences();
	}

	public int getSequenceCount() {
		return this.sequenceContainer.getSequenceCount();
	}
	
	public Sequence getSequenceByName(String name) {
		return this.sequenceContainer.getSequenceByName(name);
	}
	
	public void setDefaultSequence(Sequence defaultSequence) {
		this.sequenceContainer.setDefaultSequence(defaultSequence);
	}
	
	public Sequence getDefaultSequence() {
		return this.sequenceContainer.getDefaultSequence();
	}
	
	public int indexOf(Sequence sequence) {
		return this.sequenceContainer.indexOf(sequence);
	}
	
	public Sequence getSequenceAt(int index) {
		return this.sequenceContainer.getSequenceAt(index);
	}
	
    public List<Action> getActionsForSequence(Sequence sequence) {
        return this.sequenceContainer.getActionsForSequence(sequence);
    }
	
	public JComponent getEditor() {
		return this.editor == null ? this.editor = new SequenceContainerEditor(this) : this.editor;
	}

    public ImageResourceInfo getImageResourceInfo() {
    	return new ImageResourceInfo(this.getImageResource(), this.getWidth(), this.getHeight(), false);
    }
	
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>();
		actions.add(new RenameAction());
		actions.add(new AddSequenceAction());
		actions.add(new DeleteAction());
		return Collections.unmodifiableList(actions);
	}
	
	public class AddSequenceAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.AddSequenceAction.name")); // NOI18N
		}
		public void actionPerformed(ActionEvent e) {
			NewSequenceDialog dialog = new NewSequenceDialog(AnimatedTile.this, getWidth(), getHeight());
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.AddSequenceAction.name")); // NOI18N
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}

	public class RenameAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.RenameAction.name")); // NOI18N
		}
		public void actionPerformed(ActionEvent e) {
			RenameAnimatedTileDialog dialog = new RenameAnimatedTileDialog(AnimatedTile.this);
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.RenameAction.name")); // NOI18N
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}

	public class DeleteAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.DeleteAction.name")); // NOI18N
		}
		public void actionPerformed(ActionEvent e) {
            Object response = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.DeleteDialog.text", getName()),
                    NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.DeleteAnimatedTile.text"),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION},
                    NotifyDescriptor.YES_OPTION));
            if (response == NotifyDescriptor.YES_OPTION) {
                if (DEBUG) System.out.println("said YES to delete AnimatedTile"); // NOI18N
                getImageResource().removeAnimatedTile(getIndex());
            }
		}
	}

	public JComponent getPreview() {
		return new SequenceContainerPreview(NbBundle.getMessage(AnimatedTile.class, "AnimatedTile.previewLabel.text"), this); // NOI18N
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}

	//--------PropertyChangeListener
	
	public void propertyChange(PropertyChangeEvent evt) {
		if (DEBUG) System.out.println(this.getClass() + "unimplemented propertyChange() from " + evt.getSource()); // NOI18N
	}

    public void paint(Graphics2D g, int x, int y) {
		this.getDefaultSequence().getFrame(0).paint(g, x, y);
    }

    public void paint(Graphics2D g, int x, int y, int scaledWidth, int scaledHeight) {
		this.getDefaultSequence().getFrame(0).paint(g, x, y, scaledWidth, scaledHeight);
    }

	public JComponent getNavigator() {
		return this.navigator == null ? this.navigator = new SequenceContainerNavigator(this) : this.navigator;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
