package DataLoaderTests.DataObjectTest.data.Package;/*
 * SwingFormObject.java
 *
 * Created on December 20, 1999, 12:19 PM
 */
 


/** 
 *
 * @author  pknakal
 * @version 
 */
public class SwingFormObject extends javax.swing.JFrame {

  /** Creates new form SwingFormObject */
  public SwingFormObject() {
    initComponents ();
    pack ();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    jButton1 = new javax.swing.JButton ();
    jLabel1 = new javax.swing.JLabel ();
    addWindowListener (new java.awt.event.WindowAdapter () {
      public void windowClosing (java.awt.event.WindowEvent evt) {
        exitForm (evt);
      }
    }
    );

    jButton1.setText ("Close");
    jButton1.addMouseListener (new java.awt.event.MouseAdapter () {
      public void mouseClicked (java.awt.event.MouseEvent evt) {
        jButton1MouseClicked (evt);
      }
    }
    );


    getContentPane ().add (jButton1, java.awt.BorderLayout.SOUTH);

    jLabel1.setText ("dataObjectsInPkg/SwingFormObject");
    jLabel1.setHorizontalAlignment (javax.swing.SwingConstants.CENTER);


    getContentPane ().add (jLabel1, java.awt.BorderLayout.CENTER);

  }//GEN-END:initComponents

private void jButton1MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
    this.dispose();
    this.exitForm(null);
  }//GEN-LAST:event_jButton1MouseClicked

  /** Exit the Application */
  private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
    System.exit (0);
  }//GEN-LAST:event_exitForm

  /**
  * @param args the command line arguments
  */
  public static void main (String args[]) {
    new SwingFormObject ().show ();
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel1;
  // End of variables declaration//GEN-END:variables

}