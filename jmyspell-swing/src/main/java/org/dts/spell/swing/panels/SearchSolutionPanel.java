/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.panels;

import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.utils.Messages;

/**
 *
 * @author dreamtangerine
 */
public class SearchSolutionPanel extends SimpleTextSolutionPanel {

    public SearchSolutionPanel() {
        super(Messages.getString("JRealTimeSpellPanel.SEARCH_SOLUTION"), Messages.getString("JRealTimeSpellPanel.SEARCH_SOLUTION_TITLE"), "SEARCH_ERROR_PANEL") ;
    }

    @Override
    public boolean isForError(ErrorInfo errorInfo) {
        return false ;
    }

}
