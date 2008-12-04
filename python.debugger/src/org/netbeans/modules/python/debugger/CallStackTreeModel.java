/*
 * CallStackTreeModel.java
 *
 * Created on January 30, 2006, 10:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.python.debugger;

import java.io.File;
import java.util.Vector;
import javax.swing.Action;
import org.netbeans.modules.python.debugger.backend.DebuggerContextChangeListener;
import org.netbeans.modules.python.debugger.backend.StackInfo;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
 *
 * @author jean-yves
 */
public class CallStackTreeModel
implements TreeModel ,
           TableModel ,
           NodeModel ,
           NodeActionsProvider ,
           DebuggerContextChangeListener
{

  private static final String _STACK_CURRENT_ =
        "org/netbeans/modules/python/debugger/resources/CurrentFrame";
  private static final String _STACK_NON_CURRENT_ =
        "org/netbeans/modules/python/debugger/resources/NonCurrentFrame";
    
  private static final String _SHORT_DESCRIPTION_ =  "Python stack" ;  
  
  private PythonDebugger  _debugger;
  private ContextProvider _lookupProvider;
  private Vector<ModelListener>          _listeners = new Vector<ModelListener>();
  private StackInfo       _selectedStack = null ; 
  
  /** Creates a new instance of CallStackTreeModel */
  public CallStackTreeModel( ContextProvider lookupProvider )
  {
    _debugger       =  lookupProvider.lookupFirst( null, PythonDebugger.class );
    _lookupProvider = lookupProvider;
  }
  
  /**
   * Returns the translated root node of the tree or null, if the tree is empty.
   *
   * @return the translated root node of the tree or null
   */
  public Object getRoot()
  {
    return ROOT;
  }

  
  /**
   * Registers given listener.
   *
   * @param l the listener to add
   */
  public void addModelListener( ModelListener l )
  {
    _listeners.add( l );
    // provide a way to get called back by Python debugger
    _debugger.addStackListChangeListener(this) ;
  }


  /**
   * Unregisters given listener.
   *
   * @param l the listener to remove
   */
  public void removeModelListener( ModelListener l )
  {
    _listeners.remove( l );
    _debugger.removeStackListChangeListener(this) ;
  }
  
  public void fireContextChanged () 
  {
  Object[] ls;
    synchronized (_listeners) 
    {
      ls = _listeners.toArray();
    }
    ModelEvent ev = new ModelEvent.TreeChanged(this);
    for (int i = 0; i < ls.length; i++) 
    {
      ((ModelListener) ls[i]).modelChanged (ev);
    }
  }
  
  /**
   * Returns number of children for given node.
   *
   * @param  node the parent node
   *
   * @return 0 if node is leaf or number of threads from debugger instance
   *
   * @throws UnknownTypeException if this TreeModel implementation is not able
   *                              to resolve children for given node type
   */
  public int getChildrenCount( Object node ) throws UnknownTypeException
  {
    if (node.equals( ROOT ))
    {
      return _debugger.getStackSize() ;
    }

    return 0;
  }
  
  /**
  * Returns set of actions for given node.
  *
  * @throws  UnknownTypeException if this NodeActionsProvider implementation 
  *          is not able to resolve actions for given node type
  * @return  display name for given node
  */
  public Action[] getActions (Object node) 
  throws UnknownTypeException 
  {
    return new Action [] {};
  }
  
  class _PARSED_STACK_
  {
      private String _fileName ; 
      private File   _file     ;
      private int    _line = -1     ; 
      private boolean _fileExists = false ; 
      
      public _PARSED_STACK_( String cur )
      {
      int lineStart = cur.indexOf('(') ; 
      int lineEnd   = cur.indexOf(')') ;
        if ( ( lineStart != -1 ) &&
              ( lineEnd != -1 )
           )
        {
          _fileName = cur.substring(0,lineStart) ; 
          String lineStr = cur.substring(lineStart+1,lineEnd) ;
          _line = Integer.parseInt(lineStr)-1 ; 
          _file = new File(_fileName) ;
          _fileExists = _file.exists() ; 
        }  
        else
          _fileName = cur ;  
      }
      
      public boolean exists()
      { return _fileExists ; }
      
      public String getFilePath()
      { return _file.getAbsolutePath() ; }
      
      public int getLine()
      { return _line ; }
  }
  
  /**
  * Performs default action for given node.
  *
  * @throws  UnknownTypeException if this NodeActionsProvider implementation 
  *          is not able to resolve actions for given node type
  * @return  display name for given node
  */
  public void performDefaultAction (Object node) 
  throws UnknownTypeException 
  {
    if ( node instanceof StackInfo )
    {
      _selectedStack = (StackInfo) node ; 
      _PARSED_STACK_ parsedStack = new _PARSED_STACK_(_selectedStack.get_name()) ;
      if ( parsedStack.exists() ) 
        Utils.gotoLine( parsedStack.getFilePath() , parsedStack.getLine() ) ;    
    }
    else 
      throw new UnknownTypeException (node);
  }
  
  /**
   * Returns true if node is leaf.
   *
   * @return true if node is leaf
   *
   */
  public boolean isLeaf( Object node ) 
  {
    if (node == ROOT) return false;
    return true;
  }

  /**
   * Returns translated children for given parent on given indexes.
   *
   * @param  parent a parent of returned nodes
   *
   * @return translated children for given parent on given indexes
   *
   * @throws NoInformationException if the set of children can not be resolved
   * @throws ComputingException     if the children resolving process is time
   *                                consuming, and will be performed off-line
   * @throws UnknownTypeException   if this TreeModel implementation is not able
   *                                to resolve dchildren for given node type
   */
  public Object[] getChildren( Object parent, int from, int to )
                       throws UnknownTypeException
  {
    if (parent.equals( ROOT ))
    {
      return _debugger.getStack();
    }

    return null;
  }
  
  /** unused */
  public void setValueAt( Object node , String ColumnID , Object value )
  {}
  
  public boolean isReadOnly( Object node , String columnID )
  { return true ; }
  
  public Object getValueAt( Object node , String columnID )
  throws UnknownTypeException 
  {
    if (columnID == Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID) 
    {
      if ( node instanceof StackInfo )  
        return ((StackInfo)node).get_name() ;
      throw new UnknownTypeException(node) ;
    }
    return "" ; 
  }    
  
 public String getShortDescription( Object node )
 {
   return _SHORT_DESCRIPTION_ ;
 }
 
  /**
   * Returns display name for given node.
   *
   * @throws  ComputingException if the display name resolving process 
   *          is time consuming, and the value will be updated later
   * @throws  UnknownTypeException if this NodeModel implementation is not
   *          able to resolve display name for given node type
   * @return  display name for given node
   */
  public String getDisplayName (Object node) 
  throws UnknownTypeException 
  {
    if (node == ROOT) 
      return ROOT.toString();
    if ( node instanceof StackInfo )  
      return ((StackInfo)node).get_name() ;
    throw new UnknownTypeException(node) ;
  }
  
  /**
   * Returns icon for given node.
   *
   * @throws  ComputingException if the icon resolving process 
   *          is time consuming, and the value will be updated later
   * @throws  UnknownTypeException if this NodeModel implementation is not
   *          able to resolve icon for given node type
   * @return  icon for given node
   */
  public String getIconBase (Object node) 
  throws UnknownTypeException {
    if ( node == ROOT )
        return null ; 
    if ( node instanceof StackInfo ) 
    {
    StackInfo cur = (StackInfo) node ; 
      if ( cur.is_current()  )
          return _STACK_CURRENT_ ;
      return  _STACK_NON_CURRENT_ ;   
    }    
    throw new UnknownTypeException (node);
  }
  
  
}
