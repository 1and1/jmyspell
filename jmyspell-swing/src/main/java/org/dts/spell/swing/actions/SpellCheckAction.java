/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.utils.Messages;

/**
 *
 * @author personal
 */
public class SpellCheckAction extends AbstractSpellCheckerAction {

  private JTextComponentSpellChecker textCmpSpellChecker ;
  
  protected SpellCheckAction(JTextComponentSpellChecker textCmpSpellChecker) {
    super(Messages.getString("ACTION_SPELL_CHECK")) ;
    
    this.textCmpSpellChecker = textCmpSpellChecker ;
    
    putValue(Action.SMALL_ICON, new ImageIcon(SpellCheckAction.class.getResource("images/stock_spellcheck.png"))) ;
  }

  public void actionPerformed(ActionEvent e) {
    JTextComponent txtCmp = getTextComponent(e) ;
    
    if (textCmpSpellChecker.spellCheck(txtCmp))
      JOptionPane.showMessageDialog(txtCmp, Messages.getString("ACTION_SPELL_CHECK_OK")) ;
              
    txtCmp.requestFocusInWindow() ;              
  }
}
