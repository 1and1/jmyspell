/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.utils.Messages;

/**
 *
 * @author personal
 */
public class DeleteWordAction extends ErrorInfoAction {

  protected DeleteWordAction(JTextComponentSpellChecker txtCmpSpellChecker)
  {
    super(txtCmpSpellChecker) ;
  }
  
  @Override
  public void setErrorInfo(ErrorInfo errorInfo)
  {
    this.putValue(NAME, Messages.getString("MENU_DELETE_WORD", errorInfo.getBadWord()));
    super.setErrorInfo(errorInfo);
  }
  
  public void actionPerformed(ActionEvent e) {
    JTextComponent txtCmp = getTextComponent(e) ;
    getTxtCmpSpellChecker().replaceBadWord(txtCmp, getErrorInfo().getBadWord(), "") ;
    txtCmp.requestFocusInWindow();
  }
}