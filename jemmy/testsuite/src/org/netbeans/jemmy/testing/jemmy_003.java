package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.io.PrintWriter;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolTip;

public class jemmy_003 extends JemmyTest {
    public int runIt(Object obj) {

	try {
	    (new ClassReference("org.netbeans.jemmy.testing.Application_003")).startApplication();

	    JemmyProperties.push();

	    JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
	    JemmyProperties.getCurrentTimeouts().print(JemmyProperties.getCurrentOutput().getOutput());

	    EventDispatcher.waitQueueEmpty();

	    JFrame win =JFrameOperator.waitJFrame("Application_003", true, true);
	    JFrameOperator wino = new JFrameOperator(win);

	    Demonstrator.setTitle("jemmy_003 test");

	    for(int i = 0; i < 4; i++) {
		for(int j = 0; j < 4; j++) {
		    String bText = Integer.toString(i) + "-" + Integer.toString(j);
		    Demonstrator.nextStep("Push button " + bText);
		    JButtonOperator bo = new JButtonOperator((JButton)JButtonOperator.findJComponent(win, bText, false, true));
		    AbstractButtonOperator abo = new AbstractButtonOperator(wino, i*4 + j);
		    JButtonOperator bo2 = new JButtonOperator(wino, i*4 + j);
		    if(abo.getSource() != bo.getSource() ||
		       bo2.getSource() != bo.getSource()) {
			getOutput().printError("Wrong");
			getOutput().printErrLine(bo.getSource().toString());
			getOutput().printErrLine(abo.getSource().toString());
			getOutput().printErrLine(bo2.getSource().toString());
			finalize();
			return(1);
		    }
		    JToolTip tt = bo.showToolTip();
		    if(!tt.getTipText().equals(bText + " button")) {
			getOutput().printLine("Wrong tip text: " + tt.getTipText());
			getOutput().printLine("Expected      : " + bText + " button");
			finalize();
			return(1);
		    }
		    bo.push();
		    JLabel lbl = JLabelOperator.waitJLabel(win, "Button \"" + bText + "\" has been pushed", true, true);
		    JLabelOperator lbo = new JLabelOperator(wino);
		    if(lbo.getSource() != lbl) {
			getOutput().printError("Wrong");
			getOutput().printErrLine(lbl.toString());
			getOutput().printErrLine(lbo.getSource().toString());
			finalize();
			return(1);
		    }
		}
	    }

	    Demonstrator.showFinalComment("Test passed");

	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    JemmyProperties.pop();
	    return(1);
	}

	finalize();
	JemmyProperties.pop();
	return(0);
    }

}
