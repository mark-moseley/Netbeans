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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Future;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.support.InputRedirectorFactory;
import org.openide.windows.InputOutput;

/**
 * An utility class that simplifies usage of Native Execution Support Module
 * for common tasks like files copying.
 */
public final class CommonTasksSupport {

    private CommonTasksSupport() {
    }

    /**
     * Starts <tt>srcFileName</tt> file upload from the localhost to the host,
     * specified by the <tt>dstExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file with the given file mode creation mask. <p>
     * In case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFileName full path to the source file with file name
     * @param dstExecEnv execution environment that describes destination host
     * @param dstFileName destination filename on the host, specified by
     *        <tt>dstExecEnv</tt>
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal)
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion 
     *         of the upload task. The result of this Future is the exit
     *         code of the copying routine. 0 indicates that the file was
     *         successfully uplodaded. Result other than 0 indicates an error.
     */
    public static Future<Integer> uploadFile(
            final String srcFileName,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask, final Writer error) {

        final File localFile = new File(srcFileName);

        if (!localFile.exists()) {
            if (error != null) {
                try {
                    error.append("File " + srcFileName + " not found!"); // NOI18N
                } catch (IOException ex) {
                }
            }
            return null;
        }

        if (!localFile.canRead()) {
            if (error != null) {
                try {
                    error.append("File " + srcFileName + " is not readable!"); // NOI18N
                } catch (IOException ex) {
                }
            }
            return null;
        }

        ChangeListener processListener = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                final Object src = e.getSource();

                if (!(src instanceof NativeProcess)) {
                    return;
                }

                final NativeProcess process = (NativeProcess) src;
                final State newState = process.getState();

                if (newState == State.RUNNING) {
                    new CopyRoutine(
                            process.getOutputStream(),
                            localFile, mask).start();
                }
            }
        };

        CommandLineHelper helper = CommandLineHelper.getInstance(dstExecEnv);
        String dstFileNameEscaped = helper.toShellPath(dstFileName);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(dstExecEnv);
        npb.setCommandLine(String.format("cat >%s && chmod 0%03o %s", dstFileNameEscaped, mask, dstFileNameEscaped)); // NOI18N
        npb.addNativeProcessListener(processListener);

        ExecutionDescriptor descriptor =
                new ExecutionDescriptor().inputOutput(
                InputOutput.NULL);
        if (error != null) {
            descriptor = descriptor.outProcessorFactory(
                    new InputProcessorFactory() {

                        public InputProcessor newInputProcessor(
                                InputProcessor defaultProcessor) {
                            return new FilterInputProcessor(
                                    InputProcessors.copying(error));
                        }
                    });
        }

        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "Upload file " + srcFileName + // NOI18N
                " to " + dstExecEnv.toString() + ":" + dstFileName); // NOI18N

        return execService.run();
    }

    /**
     * Creates a task for removing a file <tt>fname</tt> from the host
     * identified by the <tt>execEnv</tt>.
     * @param execEnv execution environment to delete file from
     * @param fname the file name with the full path to it
     * @param error if not <tt>NULL</tt> and some error occurs during file 
     *        removing, an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the file removing task. The result of this Future indicates
     *         whether the file was removed (0) or not.
     */
    public static Future<Integer> rmFile(
            ExecutionEnvironment execEnv,
            String fname, final Writer error) {
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("rm").setArguments("-f", fname); // NOI18N

        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(
                InputOutput.NULL);

        if (error != null) {
            descriptor = descriptor.errProcessorFactory(
                    new InputRedirectorFactory(error));
        }

        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "Remove file " + fname); // NOI18N
        return execService.run();
    }

    /**
     * Changes file permissions.
     *
     * @param execEnv  execution environment where the file is located
     * @param file  file to change permissions for
     * @param mode  new file permissions in octal form, e.g. <tt>0755</tt>
     * @param error if not <tt>null</tt> and some error occurs,
    an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the chmod task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> chmod(final ExecutionEnvironment execEnv,
            final String file, final int mode, final Writer error) {
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("chmod").setArguments(String.format("0%03o", mode), file); // NOI18N

        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(
                InputOutput.NULL);

        if (error != null) {
            descriptor = descriptor.errProcessorFactory(
                    new InputRedirectorFactory(error));
        }

        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "Changing permissions for " + file); // NOI18N
        return execService.run();
    }

    /**
     * Creates a task for removing a directory <tt>dirname</tt> from the host
     * identified by the <tt>execEnv</tt>.
     * @param execEnv execution environment to delete the directory from
     * @param dirname the file name with the full path to it
     * @param recursively if set to <tt>true</tt> then directory is to be
     *        removed recursively.
     * @param error if not <tt>NULL</tt> and some error occurs during directory
     *        removing, an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the directory removing task. The result of this Future indicates
     *         whether the directory was removed (0) or not.
     */
    public static Future<Integer> rmDir(final ExecutionEnvironment execEnv,
            String dirname, boolean recursively, final Writer error) {
        String cmd = recursively ? "rm" : "rmdir"; // NOI18N

        String[] args = recursively
                ? new String[]{"-rf", dirname} : new String[]{"-f", dirname}; // NOI18N

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable(cmd).setArguments(args);

        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(
                InputOutput.NULL);

        if (error != null) {
            descriptor = descriptor.errProcessorFactory(
                    new InputRedirectorFactory(error));
        }

        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "Remove directory " + dirname); // NOI18N

        return execService.run();
    }

    /**
     * Creates a directory (and parent directories if needed).
     *
     * @param execEnv  execution environment to create directory in
     * @param dirname  absolute path of created directory
     * @param error  if not <tt>null</tt> and some error occurs,
     *        an error message will be written to this <tt>Writer</tt>
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the mkdir task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> mkDir(final ExecutionEnvironment execEnv,
            final String dirname, final Writer error) {
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("/bin/mkdir").setArguments("-p", dirname); // NOI18N

        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(
                InputOutput.NULL);

        if (error != null) {
            descriptor = descriptor.errProcessorFactory(
                    new InputRedirectorFactory(error));
        }

        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "Creating directory " + dirname); // NOI18N
        return execService.run();
    }

    private static final class CopyRoutine extends Thread {

        private final OutputStream outStream;
        private final File srcFile;
        private final int mask;

        public CopyRoutine(OutputStream in, File srcFile, int mask) {
            this.outStream = in;
            this.srcFile = srcFile;
            this.mask = mask;
        }

        @Override
        public void run() {
            WritableByteChannel out = null;
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            boolean interrupted = false;
            ReadableByteChannel in = null;

            try {
                out = Channels.newChannel(outStream);
                long filesize = srcFile.length();
                int workUnitsLimit = 1;
                double workUnitFactor = 1;
                if (filesize > Integer.MAX_VALUE) {
                    workUnitsLimit = Integer.MAX_VALUE;
                    workUnitFactor = Integer.MAX_VALUE / filesize;
                } else {
                    workUnitsLimit = (int) filesize;
                    workUnitFactor = 1;
                }

//                String command = String.format("C0%03o %d %s\n", // NOI18N
//                        mask, filesize, srcFile.getName());
//                buffer.put(command.getBytes(), 0, command.length());

                // send a content of srcFile
                final FileInputStream fis = new FileInputStream(srcFile);
                in = Channels.newChannel(fis);
                long sendBytes = 0;
                int progress = 0;

                while (true) {
                    int len = in.read(buffer);
                    if (len <= 0) {
                        break;
                    }
                    buffer.flip();
                    try {
                        out.write(buffer);
                    } catch (Exception ex) {
                        throw new InterruptedException();
                    }
                    buffer.clear();
                    sendBytes += len;
                    progress = (int) (sendBytes * workUnitFactor);
                }

                // Finally write 0 byte
                buffer.clear();
                buffer.put((byte) 0);
                buffer.flip();
                out.write(buffer);
            } catch (InterruptedException ie) {
                interrupted = true;
            } catch (IOException ex) {
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                    }
                }

                /* Here I have faced with the following problem:
                 * If some error occured (permission denied, as an example),
                 * an attempt to close out leads to JVM crash...
                 * Interesting thing is that out is not null and even is not
                 * closed (isOpen() returns true)...
                 * So this was overcomed using interrupted flag ...
                 *
                 * TODO: I don't like this solution.
                 */

                if (!interrupted && out != null) {
                    try {
                        out.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    private static class FilterInputProcessor implements InputProcessor {

        private final InputProcessor delegate;
        private boolean closed;

        public FilterInputProcessor(InputProcessor delegate) {
            this.delegate = delegate;
        }

        public void processInput(char[] chars) throws IOException {
            if (closed) {
                throw new IllegalStateException("Already closed processor"); // NOI18N
            }

            StringBuilder sb = new StringBuilder(chars.length);
            for (char c : chars) {
                if (c >= 32 || c == 10 || c == 13) {
                    sb.append(c);
                }
            }

            delegate.processInput(sb.toString().toCharArray());
        }

        public void reset() throws IOException {
            if (closed) {
                throw new IllegalStateException("Already closed processor"); // NOI18N
            }
            delegate.reset();
        }

        public void close() throws IOException {
            closed = true;
            delegate.close();
        }
    }
}
