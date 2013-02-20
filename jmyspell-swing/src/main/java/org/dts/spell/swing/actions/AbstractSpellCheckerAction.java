/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.actions;

import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

/**
 *
 * @author personal
 */
public abstract class AbstractSpellCheckerAction extends TextAction {

  /**
   * Last Adapter to JTextComponent focused.
   */
  public static JTextComponent lastAdapterFocused ;

  public static void setLastAdapterFocused(JTextComponent lastAdapterFocused) {
      AbstractSpellCheckerAction.lastAdapterFocused = lastAdapterFocused ;
  }

   public static JTextComponent getLastAdapterFocused() {
      return AbstractSpellCheckerAction.lastAdapterFocused ;
  }

  private PropertyChangeListener listener = new PropertyChangeListener() {

    public void propertyChange(PropertyChangeEvent evt) {
      
      if (evt.getNewValue() instanceof JTextComponent)
        onFocusedTextComponentChanged((JTextComponent) evt.getNewValue()) ;
      else
        onFocusedTextComponentChanged(getLastSelected()) ;
    }
  } ;
  
  public AbstractSpellCheckerAction(String name) {
    super(name) ;
    
    KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager() ;
    
    focusManager.addPropertyChangeListener("focusOwner", listener);
  }
  
  public void onFocusedTextComponentChanged(JTextComponent textCmp)
  {
    setEnabled(null != textCmp) ;
  }
  
  public void release()
  {
    KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager() ;
    
    focusManager.removePropertyChangeListener("focusOwner", listener);
  }

  public JTextComponent getLastSelected() {
      if (null != lastAdapterFocused)
          return lastAdapterFocused ;
      else
        return getFocusedComponent() ;
  }
}
