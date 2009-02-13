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

package org.netbeans.modules.maven.execute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.maven.embedder.MavenEmbedderLogger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.output.ContextOutputProcessorFactory;
import org.netbeans.modules.maven.api.output.NotifyFinishOutputProcessor;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputWriter;

/**
 *
 * @author mkleint
 */
public abstract class AbstractOutputHandler {
    protected static final String PRJ_EXECUTE = "project-execute"; //NOI18N
    
    protected HashMap<String, Set<OutputProcessor>> processors;
    protected Set<OutputProcessor> currentProcessors;
    protected Set<NotifyFinishOutputProcessor> toFinishProcessors;
    protected OutputVisitor visitor;
    private RequestProcessor.Task sleepTask;
    private static final int SLEEP_DELAY = 5000;

    protected AbstractOutputHandler() {
        processors = new HashMap<String, Set<OutputProcessor>>();
        currentProcessors = new HashSet<OutputProcessor>();
        visitor = new OutputVisitor();
        toFinishProcessors = new HashSet<NotifyFinishOutputProcessor>();
    }

    protected AbstractOutputHandler(final ProgressHandle hand) {
        this();
        sleepTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                hand.suspend("");
            }
        });
    }

    protected AbstractOutputHandler(final AggregateProgressHandle hand) {
        this();
        sleepTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                hand.suspend("");
            }
        });
    }

    protected void checkSleepiness() {
        RequestProcessor.Task task = sleepTask;
        if (task != null) {
            task.schedule(SLEEP_DELAY);
        }
    }


    protected final void quitSleepiness() {
        RequestProcessor.Task task = sleepTask;
        if (task != null) {
            task.cancel();
            sleepTask = null;
        }
    }
    
    abstract MavenEmbedderLogger getLogger();

    protected final String getEventId(String eventName, String target) {
        if (PRJ_EXECUTE.equals(eventName)) {
            return eventName;
        }
        return eventName + "#" + target; //NOI18N
    }
    
    protected final void initProcessorList(Project proj, RunConfig config) {
        // get the registered processors.
        Lookup.Result<OutputProcessorFactory> result  = Lookup.getDefault().lookup(new Lookup.Template<OutputProcessorFactory>(OutputProcessorFactory.class));
        Iterator<? extends OutputProcessorFactory> it = result.allInstances().iterator();
        while (it.hasNext()) {
            OutputProcessorFactory factory = it.next();
            Set<OutputProcessor> procs = factory.createProcessorsSet(proj);
            if (factory instanceof ContextOutputProcessorFactory) {
                procs = new HashSet<OutputProcessor>(procs);
                procs.addAll(((ContextOutputProcessorFactory)factory).createProcessorsSet(proj, config));
            }
            Iterator it2 = procs.iterator();
            while (it2.hasNext()) {
                OutputProcessor proc = (OutputProcessor)it2.next();
                String[] regs = proc.getRegisteredOutputSequences();
                for (int i = 0; i < regs.length; i++) {
                    String str = regs[i];
                    Set<OutputProcessor> set = processors.get(str);
                    if (set == null) {
                        set = new HashSet<OutputProcessor>();
                        processors.put(str, set);
                    }
                    set.add(proc);
                }
            }
        }
    }
    
    protected final void processStart(String id, OutputWriter writer) {
        checkSleepiness();
        Set<OutputProcessor> set = processors.get(id);
        if (set != null) {
            currentProcessors.addAll(set);
        }
        visitor.resetVisitor();
        Iterator it = currentProcessors.iterator();
        while (it.hasNext()) {
            OutputProcessor proc = (OutputProcessor)it.next();
            proc.sequenceStart(id, visitor);
        }
        if (visitor.getLine() != null) {
            if (visitor.getOutputListener() != null) {
                try {
                    writer.println(visitor.getLine(), visitor.getOutputListener(), visitor.isImportant());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                writer.println(visitor.getLine());
            }
        }
    }
    
    protected final void processEnd(String id, OutputWriter writer) {
        checkSleepiness();
        visitor.resetVisitor();
        Iterator<OutputProcessor> it = currentProcessors.iterator();
        while (it.hasNext()) {
            OutputProcessor proc = it.next();
            proc.sequenceEnd(id, visitor);
            if (proc instanceof NotifyFinishOutputProcessor) {
                toFinishProcessors.add((NotifyFinishOutputProcessor)proc);
            }
        }
        if (visitor.getLine() != null) {
            if (visitor.getOutputListener() != null) {
                try {
                    writer.println(visitor.getLine(), visitor.getOutputListener(), visitor.isImportant());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                writer.println(visitor.getLine());
            }
        }
        Set set = processors.get(id);
        if (set != null) {
            //TODO a bulletproof way would be to keep a list of currently started
            // sections and compare to the list of getRegisteredOutputSequences fo each of the
            // processors in set..
            currentProcessors.removeAll(set);
        }
    }
    
    protected final void processFail(String id, OutputWriter writer) {
        checkSleepiness();
        visitor.resetVisitor();
        Iterator it = currentProcessors.iterator();
        while (it.hasNext()) {
            OutputProcessor proc = (OutputProcessor)it.next();
            if (proc instanceof NotifyFinishOutputProcessor) {
                toFinishProcessors.add((NotifyFinishOutputProcessor)proc);
            }
            proc.sequenceFail(id, visitor);
        }
        if (visitor.getLine() != null) {
            if (visitor.getOutputListener() != null) {
                try {
                    writer.println(visitor.getLine(), visitor.getOutputListener(), visitor.isImportant());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                writer.println(visitor.getLine());
            }
        }
        Set<OutputProcessor> set = processors.get(id);
        if (set != null) {
            Set<OutputProcessor> retain = new HashSet<OutputProcessor>();
            retain.addAll(set);
            retain.retainAll(currentProcessors);
            Set<OutputProcessor> remove = new HashSet<OutputProcessor>();
            remove.addAll(set);
            remove.removeAll(retain);
            currentProcessors.removeAll(remove);
        }
        
    }
    
    protected final void buildFinished() {
        quitSleepiness();
        for (NotifyFinishOutputProcessor proc : toFinishProcessors) {
            proc.buildFinished();
        }
    }
    
    protected final void processMultiLine(String input, OutputWriter writer, String levelText) {
        if (input == null) {
            return;
        }
        //MEVENIDE-637
        for (String s : splitMultiLine(input)) {
            processLine(s, writer, levelText);
        }
    }
    
    protected final void processLine(String input, OutputWriter writer, String levelText) {
        checkSleepiness();
        
        visitor.resetVisitor();
        Iterator it = currentProcessors.iterator();
        while (it.hasNext()) {
            OutputProcessor proc = (OutputProcessor)it.next();
            proc.processLine(input, visitor);
        }
        if (!visitor.isLineSkipped()) {
            String line = visitor.getLine() == null ? input : visitor.getLine();
            if (visitor.getOutputListener() != null) {
                try {
                    writer.println((levelText.length() == 0 ? "" : ("[" + levelText + "]")) + line, visitor.getOutputListener(), visitor.isImportant()); //NOI18N
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                writer.println((levelText.length() == 0 ? "" : ("[" + levelText + "]")) + line); //NOI18N
            }
        }
    }
    
    //MEVENIDE-637   
    public static List<String> splitMultiLine(String input) {
        List<String> list = new ArrayList<String>();
        String[] strs = input.split("\\r|\\n"); //NOI18N
        for (int i = 0; i < strs.length; i++) {
            if(strs[i].length()>0){
              list.add(strs[i]);
            }
        }
        return list;
    }   
}
