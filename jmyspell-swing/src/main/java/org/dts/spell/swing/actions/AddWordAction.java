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
 * @author dreamtangerine
 */
public class AddWordAction extends ErrorInfoAction {

  protected AddWordAction(JTextComponentSpellChecker txtCmpSpellChecker) {
    super(txtCmpSpellChecker);
  }

  @Override
  public void setErrorInfo(ErrorInfo errorInfo) {
    super.setErrorInfo(errorInfo);

    putValue(NAME, Messages.getString("MENU_ADD_WORD",  errorInfo.getBadWord().toString()));
  }

  public void actionPerformed(ActionEvent e) {
    JTextComponent txtCmp = getTextComponent(e);
    ErrorInfo info = getErrorInfo() ;
    getTxtCmpSpellChecker().addWordToDictionary(txtCmp, info.getBadWord().getText(), info.getDictionary()) ;
    txtCmp.requestFocusInWindow();
  }
}
