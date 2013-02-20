/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.actions;

import javax.swing.text.TextAction;
import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.JTextComponentSpellChecker;

/**
 *
 * @author personal
 */
public abstract class ErrorInfoAction extends TextAction {
  
  public ErrorInfoAction(JTextComponentSpellChecker txtCmpSpellChecker)
  {
    super("") ;
    this.txtCmpSpellChecker = txtCmpSpellChecker ;
  }
  
  private JTextComponentSpellChecker txtCmpSpellChecker ;
  private ErrorInfo errorInfo ;

  public JTextComponentSpellChecker getTxtCmpSpellChecker() {
    return txtCmpSpellChecker;
  }

  public ErrorInfo getErrorInfo() {
    return errorInfo;
  }

  public void setErrorInfo(ErrorInfo errorInfo) {
    this.errorInfo = errorInfo;
  }
}
