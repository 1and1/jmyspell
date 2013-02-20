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
public class NoErrorSolutionPanel extends SimpleTextSolutionPanel {

    public NoErrorSolutionPanel() {
        super(Messages.getString("JRealTimeSpellPanel.NO_ERROR"), Messages.getString("JRealTimeSpellPanel.NO_ERROR_TITLE"), "NO_ERROR_PANEL") ;
    }

    @Override
    public boolean isForError(ErrorInfo errorInfo) {
        return null == errorInfo ;
    }
}
