/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.dts.spell.swing.JTextComponentSpellChecker;

/**
 *
 * @author dreamtangerine
 */
public class ResetReplaceWordsAction extends AbstractAction {

    private JTextComponentSpellChecker textCmpSpellChecker ;

    public ResetReplaceWordsAction(JTextComponentSpellChecker textCmpSpellChecker) {
      super("Reiniciar reemplazar palabras") ;
        this.textCmpSpellChecker = textCmpSpellChecker ;
    }

    public void actionPerformed(ActionEvent e) {
        textCmpSpellChecker.getSpellChecker().resetReplace();
    }
}
