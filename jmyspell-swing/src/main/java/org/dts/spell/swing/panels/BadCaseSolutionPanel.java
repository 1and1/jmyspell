/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.swing.panels;

import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.actions.ReplaceWordAction;
import org.dts.spell.swing.utils.Messages;

/**
 *
 * @author dreamtangerine
 */
public class BadCaseSolutionPanel extends SimpleSolutionPanel {

    public BadCaseSolutionPanel() {
        super("BAD_CASE_ERROR_PANEL");
    }

    @Override
    public void calcSolution(ErrorInfo errorInfo) {
        setTitle(Messages.getString("JRealTimeSpellPanel.BAD_CASE_ERROR_TITLE", errorInfo.getBadWord()));
        setDescription(errorInfo.getDescription());
    }

    @Override
    public void initFrom(JTextComponentSpellChecker txtCmpSpellChecker) {
        super.initFrom(txtCmpSpellChecker);
        setAction(txtCmpSpellChecker.getReplaceWordAction());
    }

    @Override
    protected ReplaceWordAction getAction() {
        return (ReplaceWordAction) super.getAction();
    }



    @Override
    public void activePanel(ErrorInfo errorInfo) {
        getAction().setSuggestion(errorInfo.getFirstSuggestion());
        super.activePanel(errorInfo);
    }

    @Override
    public boolean isForError(ErrorInfo errorInfo) {
        return errorInfo.isBadCaseError();
    }
}
