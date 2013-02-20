/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.utils.Messages;

/**
 *
 * @author personal
 */
public class RealTimeSpellCheckAction extends AbstractSpellCheckerAction {

  private JTextComponentSpellChecker textCmpSpellChecker ;
  
  protected RealTimeSpellCheckAction(JTextComponentSpellChecker textCmpSpellChecker) {
    super(Messages.getString("ACTION_REALTIME_SPELL_CHECK")) ;
    
    this.textCmpSpellChecker = textCmpSpellChecker ;
    
    putValue(Action.SMALL_ICON, new ImageIcon(SpellCheckAction.class.getResource("images/stock_autospellcheck.png"))) ;
  }

  protected JTextComponentSpellChecker getTextComponentSpellChecker() {
    return this.textCmpSpellChecker ;
  }

  protected void onStartRealtimeMarkErrors(JTextComponent txtCmp)
  {
    textCmpSpellChecker.startRealtimeMarkErrors(txtCmp) ;    
  }

  protected void onStopRealtimeMarkErrors(JTextComponent txtCmp)
  {
    textCmpSpellChecker.stopRealtimeMarkErrors(txtCmp) ;
  }

  @Override
  public void onFocusedTextComponentChanged(JTextComponent textCmp) {
    super.onFocusedTextComponentChanged(textCmp);
    
    if (null != textCmp)
      putValue(Action.SELECTED_KEY, textCmpSpellChecker.isRealtimeMarkErrors(textCmp)) ;
  }
  
  public void actionPerformed(ActionEvent e) {
    JTextComponent txtCmp = getTextComponent(e) ;
    
    if (null != txtCmp)
    {
      if (textCmpSpellChecker.isRealtimeMarkErrors(txtCmp))
        onStopRealtimeMarkErrors(txtCmp) ;
      else
        onStartRealtimeMarkErrors(txtCmp) ;
      
      putValue(Action.SELECTED_KEY, textCmpSpellChecker.isRealtimeMarkErrors(txtCmp)) ;
      txtCmp.requestFocusInWindow();
    }
  }
}