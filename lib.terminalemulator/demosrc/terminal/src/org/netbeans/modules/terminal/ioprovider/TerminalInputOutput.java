/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.terminal.ioprovider;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import org.netbeans.lib.richexecution.program.Program;
import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.ActiveTermListener;
import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.Extent;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.terminal.api.Terminal;
import org.netbeans.modules.terminal.api.TerminalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOPosition;
import org.openide.windows.IOTab;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * An implementation of {@link InputOutput} based on
 * {@link org.netbeans.lib.terminalemulator.Term}.
 * <p>
 * This class is public to allow access to the underlying Term.
 * <p>
 * A note on println()'s with OutputListeners:
 * <ul>
 * <li>
 * outputLineAction() works when hyperlinks are clicked.
 * <p>
 * <li>
 * outputLineCleared() didn't make much sense for output2 because output2 had
 * "infinte" history. However, it did make sense when the buffer was cleared.
 * <p>
 * For us issuing Cleared() when the buffer is cleared makes sense but isn't
 * implemented.
 * <br>
 * Issuing Cleared() when a hyperlink scrolls out of the history window
 * also makes sense and is even more work to implement.
 * <li>
 * outputLineSelected() tracked the "caret" in output2. However output2 was
 * "editor" based whereas we're a terminal and a terminals cursor is not
 * a caret ... it doesn't move around that much. (It can move under the
 * control of a program, like vi, but one doesn't generally use hyperlinks
 * in such situations).
 * <p>
 * Term can in principle notify when the cursor is hovering over a hyperlink
 * and perhaps that is the right time to issue Selected().
 * </ul>
 * @author ivan
 */
public final class TerminalInputOutput implements InputOutput, Lookup.Provider {

    private final IOContainer ioContainer;

    private final Terminal terminal;
    private final StreamTerm term;
    private OutputWriter outputWriter;

    // shadow copies in support of IOTab
    private Icon icon;
    private String toolTipText;

    private final Lookup lookup = Lookups.fixed(new MyIOColorLines(),
                                                new MyIOColors(),
                                                new MyIOPosition(),
                                                new MyIOExecution(),
                                                new MyIOTab()
                                                );


    private final Map<Color, Integer> colorMap = new HashMap<Color, Integer>();
    private int allocatedColors = 0;

    private final Map<IOColors.OutputType, Color> typeColorMap =
        new HashMap<IOColors.OutputType, Color>();

    private int outputColor = 0;

    public Lookup getLookup() {
        return lookup;
    }

    /**
     * Convert a Color to an ANSI Term color index.
     * @param color
     * @return
     */
    private int customColor(Color color) {
        if (color == null)
            return -1;

        if (!colorMap.containsKey(color)) {
            if (allocatedColors >= 8)
                return -1;  // ran out of slots for custom colors
            term().setCustomColor(allocatedColors, color);
            colorMap.put(color, (allocatedColors++)+50);
        }
        int customColor = colorMap.get(color);
        return customColor;
    }

    private void println(CharSequence text, Color color) {
        int customColor = customColor(color);
        if (customColor == -1) {        // ran out of colors
            getOut().println(text);
        } else {
            term().setAttribute(customColor);
            getOut().println(text);
            term().setAttribute(outputColor);
        }
    }

    private void println(CharSequence text, OutputListener listener, boolean important, Color color) {
        if ( !(term instanceof ActiveTerm))
            throw new UnsupportedOperationException("Term is not an ActiveTerm");

        if (color == null) {
            // If color isn't overriden, use default colors.
            if (listener != null) {
                if (important)
                    color = typeColorMap.get(IOColors.OutputType.HYPERLINK_IMPORTANT);
                else
                    color = typeColorMap.get(IOColors.OutputType.HYPERLINK);
            } else {
                // color = typeColorMap.get(IOColors.OutputType.OUTPUT);
            }
        }

        ActiveTerm at = (ActiveTerm) term;
        if (listener != null) {
            ActiveRegion ar = at.beginRegion(true);
            ar.setUserObject(listener);
            ar.setLink(true);
            println(text, color);
            at.endRegion();
        } else {
            println(text, color);
        }
    }

    private void scrollTo(Coord coord) {
        term.possiblyNormalize(coord);
    }

    private class MyIOColorLines extends IOColorLines {
        @Override
        protected void println(CharSequence text, OutputListener listener, boolean important, Color color) {
            TerminalInputOutput.this.println(text, listener, important, color);
        }
    }

    private class MyIOColors extends IOColors {

        @Override
        protected Color getColor(OutputType type) {
            return typeColorMap.get(type);
        }

        @Override
        protected void setColor(OutputType type, Color color) {
            typeColorMap.put(type, color);
            if (type == OutputType.OUTPUT) {
                outputColor = customColor(color);
                if (outputColor == -1)
                    outputColor = 0;
                term.setAttribute(outputColor);
            }
        }
    }

    private static class MyPosition implements IOPosition.Position {
        private final TerminalInputOutput back;
        private final Coord coord;

        MyPosition(TerminalInputOutput back, Coord coord) {
            this.back = back;
            this.coord = coord;
        }

        public void scrollTo() {
            back.scrollTo(coord);
        }
    }

    private class MyIOPosition extends IOPosition {

        @Override
        protected Position currentPosition() {
            return new MyPosition(TerminalInputOutput.this, term.getCursorCoord());
        }
    }

    private class MyIOTab extends IOTab {

        @Override
        protected Icon getIcon() {
            return icon;
        }

        @Override
        protected void setIcon(Icon icon) {
	    TerminalInputOutput.this.icon = icon;
	    ioContainer.setIcon(terminal, icon);
        }

        @Override
        protected String getToolTipText() {
	    return toolTipText;
        }

        @Override
        protected void setToolTipText(String text) {
	    TerminalInputOutput.this.toolTipText = toolTipText;
	    ioContainer.setToolTipText(terminal, toolTipText);
        }
    }

    /* LATER
    private class MyIOColorPrint extends IOColorPrint {

        private final Map<Color, Integer> colorMap = new HashMap<Color, Integer>();
        private int index = 0;

        public MyIOColorPrint() {
            // preset standard colors
            colorMap.put(Color.black, 30);
            colorMap.put(Color.red, 31);
            colorMap.put(Color.green, 32);
            colorMap.put(Color.yellow, 33);
            colorMap.put(Color.blue, 34);
            colorMap.put(Color.magenta, 35);
            colorMap.put(Color.cyan, 36);
            colorMap.put(Color.white, 37);
        }

        private int customColor(Color color) {
            if (!colorMap.containsKey(color)) {
                if (index >= 8)
                    return -1;  // ran out of slots for custom colors
                term().setCustomColor(index, color);
                colorMap.put(color, (index++)+50);
            }
            int customColor = colorMap.get(color);
            return customColor;

        }

        @Override
        protected void print(CharSequence text, Color color) {
            if ( !(term instanceof ActiveTerm))
                throw new UnsupportedOperationException("Term is not an ActiveTerm");

            int customColor = customColor(color);
            if (customColor == -1) {
                outputWriter.print(text);
            } else {
                term().setAttribute(customColor);
                outputWriter.print(text);
                term().setAttribute(0);
            }
        }
    }
    */

    private class MyIOExecution extends IOExecution {

        @Override
        protected void execute(Program program) {
	    terminal.startProgram(program, true);
	    /* OLD
            //
            // Create a pty, handle window size changes
            //
            final Pty pty;
            try {
                pty = Pty.create(Pty.Mode.REGULAR);
            } catch (PtyException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }

            term().addListener(new TermListener() {
                public void sizeChanged(Dimension cells, Dimension pixels) {
                    pty.masterTIOCSWINSZ(cells.height, cells.width,
                                         pixels.height, pixels.width);
                }
            });

            //
            // Create a process
            //
            if (term() != null) {
                Map<String, String> env = program.environment();
                env.put("TERM", term().getEmulation());
            }
            PtyExecutor executor = new PtyExecutor();
            executor.start(program, pty);

            //
            // connect them up
            //

            // Hmm, what's the difference between the PtyProcess io streams
            // and the Pty's io streams?
            // Nothing.
            OutputStream pin = pty.getOutputStream();
            InputStream pout = pty.getInputStream();

	    term.connect(pin, pout, null);
	     */
        }
    }



    /**
     * Delegate prints and writes to a Term via TermWriter.
     */
    private class TermOutputWriter extends OutputWriter {
        TermOutputWriter() {
            super(term.getOut());
        }

        @Override
        public void println(String s, OutputListener l) throws IOException {
            TerminalInputOutput.this.println(s, l, false, null);
        }

        @Override
        public void println(String s, OutputListener l, boolean important) throws IOException {
            TerminalInputOutput.this.println(s, l, important, null);
        }

        @Override
        public void reset() throws IOException {
            term.clearHistory();
        }
    }

    private static class TerminalOutputEvent extends OutputEvent {
        private final String text;

        public TerminalOutputEvent(InputOutput io, String text) {
            super(io);
            this.text = text;
        }

        @Override
        public String getLine() {
            return text;
        }
    }

    TerminalInputOutput(String name, IOContainer ioContainer) {
        this.ioContainer = ioContainer;
        terminal = TerminalProvider.getDefault().createTerminal(name, ioContainer);
        term = terminal.term();

        if (! (term instanceof ActiveTerm))
            return;

        ActiveTerm at = (ActiveTerm) term;

        // Set up to convert clicks on active regions, created by OutputWriter.
        // println(), to outputLineAction notifications.
        at.setActionListener(new ActiveTermListener() {
            public void action(ActiveRegion r, InputEvent e) {
                OutputListener ol = (OutputListener) r.getUserObject();
                if (ol == null)
                    return;
                Extent extent = r.getExtent();
                String text = term.textWithin(extent.begin, extent.end);
                OutputEvent oe =
                    new TerminalOutputEvent(TerminalInputOutput.this, text);
                ol.outputLineAction(oe);
            }
        });

        // preset standard colors
        colorMap.put(Color.black, 30);
        colorMap.put(Color.red, 31);
        colorMap.put(Color.green, 32);
        colorMap.put(Color.yellow, 33);
        colorMap.put(Color.blue, 34);
        colorMap.put(Color.magenta, 35);
        colorMap.put(Color.cyan, 36);
        colorMap.put(Color.white, 37);
    }

    public StreamTerm term() {
        return term;
    }
    
    /**
     * Stream to write to stuff being output by the proceess destined for the
     * terminal.
     * @return the writer.
     */
    public OutputWriter getOut() {
        if (outputWriter == null)
            outputWriter = new TermOutputWriter();
        return outputWriter;
    }

    /**
     * Stream to read from stuff typed into the terminal destined for the process.
     * @return the reader.
     */
    public Reader getIn() {
	return term.getIn();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Output written to this Writer may appear in a different tab (not
     * supported) or different color (easily doable).
     * <p>
     * I'm hesitant to implement this because traditionally separation of
     * stdout and stderr (as done by {@link Process#getErrorStream}) is a dead
     * end. That is why {@link ProcessBuilder}'s redirectErrorStream property is
     * false by default. It is also why
     * {@link org.netbeans.lib.termsupport.TermExecutor#start} will
     * pre-combine stderr and stdout.
     */
    public OutputWriter getErr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void closeInputOutput() {
        terminal.close();
    }

    public boolean isClosed() {
        return terminal.isClosed();
    }

    public void setOutputVisible(boolean value) {
        // no-op in output2
    }

    public void setErrVisible(boolean value) {
        // no-op in output2
    }

    public void setInputVisible(boolean value) {
        // no-op
    }

    public void select() {
        terminal.select();
    }

    public boolean isErrSeparated() {
        return false;
    }

    public void setErrSeparated(boolean value) {
        // no-op in output2
    }

    public boolean isFocusTaken() {
        return false;
    }

    /**
     * output2 considered this to be a "really bad" operation so we will
     * outright not support it.
     */
    public void setFocusTaken(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public Reader flushReader() {
	return term.getIn();
    }
}