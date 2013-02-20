/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.event;

import java.util.EventObject;
import javax.swing.text.JTextComponent;
import org.dts.spell.swing.JTextComponentSpellChecker;

/**
 *
 * @author personal
 */
public class TextComponentSpellCheckerEvent extends EventObject {
  
  private JTextComponent txtCmp ;
  
  public TextComponentSpellCheckerEvent(JTextComponentSpellChecker source, JTextComponent txtCmp)
  { 
    super(source) ;
    this.txtCmp = txtCmp ;
  }
  
  public JTextComponentSpellChecker getTextComponentSpellChecker()
  {
    return (JTextComponentSpellChecker) getSource() ;
  }
  
  public JTextComponent getTextComponent()
  {
    return txtCmp ;
  }
}
