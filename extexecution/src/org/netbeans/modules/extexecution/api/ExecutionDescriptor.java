/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.extexecution.api;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.print.LineConvertor;
import org.openide.windows.InputOutput;

/**
 * Descriptor for the execution environment. Describes the runtime attributes
 * of the {@link ExecutionService}.
 * <p>
 * <i>Thread safety</i> of this class depends on type of objects passed to its
 * configuration methods. If these objects are immutable, resulting descriptor
 * is immutable as well. It these objects are thread safe, resulting descriptor
 * is thread safe as well.
 *
 * @author Petr Hejl
 * @see ExecutionService
 */
public final class ExecutionDescriptor {

    private final Runnable preExecution;

    private final Runnable postExecution;

    private final boolean suspend;

    private final boolean progress;

    private final boolean front;

    private final boolean input;

    private final boolean controllable;

    private final LineConvertorFactory outConvertorFactory;

    private final LineConvertorFactory errConvertorFactory;

    private final InputProcessorFactory outProcessorFactory;

    private final InputProcessorFactory errProcessorFactory;

    private final InputOutput inputOutput;

    private final RerunCondition rerunCondition;

    private final String optionsPath;

    /**
     * Creates the new descriptor. All properties are initalized to
     * <code>null</code> or <code>false</code>.
     */
    public ExecutionDescriptor() {
        this(new DescriptorData());
    }

    private ExecutionDescriptor(DescriptorData data) {
        this.preExecution = data.preExecution;
        this.postExecution = data.postExecution;
        this.suspend = data.suspend;
        this.progress = data.progress;
        this.front = data.front;
        this.input = data.input;
        this.controllable = data.controllable;
        this.outConvertorFactory = data.outConvertorFactory;
        this.errConvertorFactory = data.errConvertorFactory;
        this.outProcessorFactory = data.outProcessorFactory;
        this.errProcessorFactory = data.errProcessorFactory;
        this.inputOutput = data.inputOutput;
        this.rerunCondition = data.rerunCondition;
        this.optionsPath = data.optionsPath;
    }

    /**
     * Returns a descriptor with configured <i>custom</i> io. When configured
     * to <code>null</code> it means that client is fine with infrustructure
     * provided io (visible as tab in output pane).
     * <p>
     * If configured value is not <code>null</code> values configured via
     * methods {@link #controllable(boolean)}, {@link #rerunCondition(RerunCondition)}
     * and {@link #getOptionsPath()} and are ignored by {@link ExecutionService}.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param io custom input output, <code>null</code> allowed
     * @return new descriptor with configured custom io
     */
    public ExecutionDescriptor inputOutput(InputOutput io) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.inputOutput(io));
    }

    InputOutput getInputOutput() {
        return inputOutput;
    }

    /**
     * Returns a descriptor with configured controllable flag. When
     * <code>true</code> the control buttons (rerun, stop) will be available
     * io tab created by {@link ExecutionService}.
     * <p>
     * Note that this property has no meaning when custom io is used
     * (see {@link #inputOutput(org.openide.windows.InputOutput)}).
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param controllable controllable flag
     * @return new descriptor with configured controllable flag
     */
    public ExecutionDescriptor controllable(boolean controllable) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.controllable(controllable));
    }

    boolean isControllable() {
        return controllable;
    }

    /**
     * Returns a descriptor with configured front window flag. When
     * <code>true</code> the io tab will be selected before the execution
     * invoked by {@link ExecutionService#run()}.
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param frontWindow front window flag
     * @return new descriptor with configured front window flag
     */
    public ExecutionDescriptor frontWindow(boolean frontWindow) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.frontWindow(frontWindow));
    }

    boolean isFrontWindow() {
        return front;
    }

    /**
     * Returns a descriptor with configured input visible flag. When configured
     * value is <code>true</code> the input from user will be allowed.
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param inputVisible input visible flag
     * @return new descriptor with configured input visible flag
     */
    public ExecutionDescriptor inputVisible(boolean inputVisible) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.inputVisible(inputVisible));
    }

    boolean isInputVisible() {
        return input;
    }

    /**
     * Returns a descriptor with configured show progress flag. When configured
     * value is <code>true</code> the progress bar will be visible.
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param showProgress show progress flag
     * @return new descriptor with configured show progress flag
     */
    public ExecutionDescriptor showProgress(boolean showProgress) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.showProgress(showProgress));
    }

    boolean showProgress() {
        return progress;
    }

    /**
     * Returns a descriptor with configured show suspend flag. When configured
     * value is <code>true</code> the progress bar will be suspended to just
     * "running" message.
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param showSuspended show suspended flag
     * @return new descriptor with configured show suspended flag
     */
    public ExecutionDescriptor showSuspended(boolean showSuspended) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.showSuspended(showSuspended));
    }

    boolean showSuspended() {
        return suspend;
    }

    /**
     * Returns a descriptor with configured factory for standard output
     * processor. The factory is used by {@link ExecutionService} to create
     * additional processor for standard output.
     * <p>
     * Note that {@link ExecutionService} always automatically uses
     * the printing processor created by
     * {@link org.netbeans.modules.extexecution.api.input.InputProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.modules.extexecution.api.print.LineConvertor, boolean)}.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param outProcessorFactory factory for standard output processor,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for additional
     *             processor to use for standard output
     */
    public ExecutionDescriptor outProcessorFactory(InputProcessorFactory outProcessorFactory) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.outProcessorFactory(outProcessorFactory));
    }

    InputProcessorFactory getOutProcessorFactory() {
        return outProcessorFactory;
    }

    /**
     * Returns a descriptor with configured factory for standard error output
     * processor. The factory is used by {@link ExecutionService} to create
     * additional processor for standard error output.
     * <p>
     * Note that {@link ExecutionService} always automatically uses
     * the printing processor created by
     * {@link org.netbeans.modules.extexecution.api.input.InputProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.modules.extexecution.api.print.LineConvertor, boolean)}.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param errProcessorFactory factory for standard error output processor,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for additional
     *             processor to use for standard error output
     */
    public ExecutionDescriptor errProcessorFactory(InputProcessorFactory errProcessorFactory) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.errProcessorFactory(errProcessorFactory));
    }

    InputProcessorFactory getErrProcessorFactory() {
        return errProcessorFactory;
    }

    /**
     * Returns a descriptor with configured factory for convertor for standard
     * output. The factory is used by {@link ExecutionService} to create
     * convertor to use with processor printing the standard output.
     * <p>
     * Note that {@link ExecutionService} always uses the printing processor
     * for the standard output. Convertor created by the returned factory will
     * be passed to this default printing processor. See
     * {@link #getOutProcessorFactory()} too.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param convertorFactory factory for convertor for standard output,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for converter for
     *             standard output
     */
    public ExecutionDescriptor outConvertorFactory(LineConvertorFactory convertorFactory) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.outConvertorFactory(convertorFactory));
    }

    LineConvertorFactory getOutConvertorFactory() {
        return outConvertorFactory;
    }

    /**
     * Returns a descriptor with configured factory for convertor for standard
     * error output. The factory is used by {@link ExecutionService} to create
     * convertor to use with processor printing the standard error output.
     * <p>
     * Note that {@link ExecutionService} always uses the printing processor
     * for the standard error output. Convertor created by the returned
     * factory will be passed to this default printing processor. See
     * {@link #getErrProcessorFactory()} too.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param convertorFactory factory for convertor for standard error output,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for converter for
     *             standard error output
     */
    public ExecutionDescriptor errConvertorFactory(LineConvertorFactory convertorFactory) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.errConvertorFactory(convertorFactory));
    }

    LineConvertorFactory getErrConvertorFactory() {
        return errConvertorFactory;
    }

    /**
     * Returns a descriptor with configured pre execution runnable. This
     * runnable is executed <i>before</i> the external execution itself
     * (when invoked by {@link ExecutionService#run()}).
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param preExecution pre execution runnable, <code>null</code> allowed
     * @return new descriptor with configured pre execution runnable
     */
    public ExecutionDescriptor preExecution(Runnable preExecution) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.preExecution(preExecution));
    }

    Runnable getPreExecution() {
        return preExecution;
    }

    /**
     * Returns a descriptor with configured post execution runnable. This
     * runnable is executed <i>after</i> the external execution itself
     * (when invoked by {@link ExecutionService#run()}).
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param postExecution post execution runnable, <code>null</code> allowed
     * @return new descriptor with configured post execution runnable
     */
    public ExecutionDescriptor postExecution(Runnable postExecution) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.postExecution(postExecution));
    }

    Runnable getPostExecution() {
        return postExecution;
    }

    /**
     * Returns a descriptor with configured rerun condition. The condition
     * is used by {@link ExecutionService} to control the possibility of the
     * rerun action.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param rerunCondition rerun condition, <code>null</code> allowed
     * @return new descriptor with configured rerun condition
     */
    public ExecutionDescriptor rerunCondition(ExecutionDescriptor.RerunCondition rerunCondition) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.rerunCondition(rerunCondition));
    }

    RerunCondition getRerunCondition() {
        return rerunCondition;
    }

    /**
     * Returns a descriptor with configured options path. If not configured
     * value is not <code>null</code> the {@link ExecutionService} will
     * display the button in the output tab displaying the proper options
     * when pressed.
     * <p>
     * Format of the parameter is described in
     * {@link org.netbeans.api.options.OptionsDisplayer#open(java.lang.String)}.
     * <p>
     * Note that this property has no meaning when custom io is used
     * (see {@link #inputOutput(org.openide.windows.InputOutput)}).
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param optionsPath options path, <code>null</code> allowed
     * @return this descriptor with configured options path
     */
    public ExecutionDescriptor optionsPath(String optionsPath) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.optionsPath(optionsPath));
    }

    String getOptionsPath() {
        return optionsPath;
    }

    /**
     * Represents the possibility of reruning the action.
     */
    public interface RerunCondition {

        /**
         * Adds a listener to listen for the change in rerun possibility state.
         *
         * @param listener listener that will listen for changes in rerun possibility
         */
        void addChangeListener(ChangeListener listener);

        /**
         * Removes previously registered listener.
         *
         * @param listener listener to remove
         */
        void removeChangeListener(ChangeListener listener);

        /**
         * Returns <code>true</code> if it is possible to execute the action again.
         *
         * @return <code>true</code> if it is possible to execute the action again
         */
        boolean isRerunPossible();

    }

    /**
     * Factory creating the input processor.
     */
    public interface InputProcessorFactory {

        /**
         * Creates and returns new input processor.
         *
         * @return new input processor
         */
        InputProcessor newInputProcessor();

    }

    /**
     * Factory creating the line covertor.
     */
    public interface LineConvertorFactory {

        /**
         * Creates and returns new line convertor.
         *
         * @return new line convertor
         */
        LineConvertor newLineConvertor();

    }

    private static final class DescriptorData {

        private Runnable preExecution;

        private Runnable postExecution;

        private boolean suspend;

        private boolean progress;

        private boolean front;

        private boolean input;

        private boolean controllable;

        private LineConvertorFactory outConvertorFactory;

        private LineConvertorFactory errConvertorFactory;

        private InputProcessorFactory outProcessorFactory;

        private InputProcessorFactory errProcessorFactory;

        private InputOutput inputOutput;

        private ExecutionDescriptor.RerunCondition rerunCondition;

        private String optionsPath;

        public DescriptorData() {
            super();
        }

        public DescriptorData(ExecutionDescriptor descriptor) {
            this.preExecution = descriptor.preExecution;
            this.postExecution = descriptor.postExecution;
            this.suspend = descriptor.suspend;
            this.progress = descriptor.progress;
            this.front = descriptor.front;
            this.input = descriptor.input;
            this.controllable = descriptor.controllable;
            this.outConvertorFactory = descriptor.outConvertorFactory;
            this.errConvertorFactory = descriptor.errConvertorFactory;
            this.outProcessorFactory = descriptor.outProcessorFactory;
            this.errProcessorFactory = descriptor.errProcessorFactory;
            this.inputOutput = descriptor.inputOutput;
            this.rerunCondition = descriptor.rerunCondition;
            this.optionsPath = descriptor.optionsPath;
        }

        public DescriptorData inputOutput(InputOutput io) {
            this.inputOutput = io;
            return this;
        }

        public DescriptorData controllable(boolean controllable) {
            this.controllable = controllable;
            return this;
        }

        public DescriptorData frontWindow(boolean frontWindow) {
            this.front = frontWindow;
            return this;
        }

        public DescriptorData inputVisible(boolean inputVisible) {
            this.input = inputVisible;
            return this;
        }

        public DescriptorData showProgress(boolean showProgress) {
            this.progress = showProgress;
            return this;
        }

        public DescriptorData showSuspended(boolean showSuspended) {
            this.suspend = showSuspended;
            return this;
        }

        public DescriptorData outProcessorFactory(InputProcessorFactory outProcessorFactory) {
            this.outProcessorFactory = outProcessorFactory;
            return this;
        }

        public DescriptorData errProcessorFactory(InputProcessorFactory errProcessorFactory) {
            this.errProcessorFactory = errProcessorFactory;
            return this;
        }

        public DescriptorData outConvertorFactory(LineConvertorFactory convertorFactory) {
            this.outConvertorFactory = convertorFactory;
            return this;
        }

        public DescriptorData errConvertorFactory(LineConvertorFactory convertorFactory) {
            this.errConvertorFactory = convertorFactory;
            return this;
        }

        public DescriptorData preExecution(Runnable preExecution) {
            this.preExecution = preExecution;
            return this;
        }

        public DescriptorData postExecution(Runnable postExecution) {
            this.postExecution = postExecution;
            return this;
        }

        public DescriptorData rerunCondition(ExecutionDescriptor.RerunCondition rerunCondition) {
            this.rerunCondition = rerunCondition;
            return this;
        }

        public DescriptorData optionsPath(String optionsPath) {
            this.optionsPath = optionsPath;
            return this;
        }

    }
}
