/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.panels;

import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.utils.Messages;

/**
 *
 * @author dreamtangerine
 */
public class RepeatWordSolutionPanel extends SimpleSolutionPanel {

    public RepeatWordSolutionPanel() {
        super("REPEAT_WORD_ERROR_PANEL") ;
    }


    @Override
    public void calcSolution(ErrorInfo errorInfo) {
        setTitle(Messages.getString("JRealTimeSpellPanel.REPEAT_WORD_ERROR_TITLE", errorInfo.getBadWord()));
        setDescription(errorInfo.getDescription());
    }

    @Override
    public void initFrom(JTextComponentSpellChecker txtCmpSpellChecker) {
        super.initFrom(txtCmpSpellChecker);
        setAction(txtCmpSpellChecker.getDeleteWordAction()) ;
    }

    @Override
    public boolean isForError(ErrorInfo errorInfo) {
        return errorInfo.isRepeatWordError() ;
    }

}
