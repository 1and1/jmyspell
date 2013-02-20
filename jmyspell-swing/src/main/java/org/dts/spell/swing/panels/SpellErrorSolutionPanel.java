/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.swing.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.dts.spell.ErrorInfo;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.JSuggestionList;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.utils.Messages;

/**
 *
 * @author personal
 */
public class SpellErrorSolutionPanel extends SolutionPanel {

    private String title;
    private JSuggestionList sugestionList;
    private List<String> suggestions;
    private JTextComponentSpellChecker txtCmpSpellChecker ;

    public SpellErrorSolutionPanel() {
        super("SPELLING_ERROR_PANEL") ;
    }

    protected JSuggestionList createSuggestionList() {
        return new JSuggestionList(txtCmpSpellChecker.getReplaceWordAction(), txtCmpSpellChecker.getReplaceAllWordsAction());
    }

    protected JPanel createButtonsPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(4, 1));

        panel.add(new JButton(txtCmpSpellChecker.getReplaceWordAction()));
        panel.add(new JButton(txtCmpSpellChecker.getReplaceAllWordsAction()));
        panel.add(new JButton(txtCmpSpellChecker.getIgnoreWordAction()));
        panel.add(new JButton(txtCmpSpellChecker.getAddWordAction()));

        return panel;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void calcSolution(ErrorInfo errorInfo) {
        Word badWord = errorInfo.getBadWord();
        title = Messages.getString("JRealTimeSpellPanel.SPELL_ERROR_TITLE", badWord);
        suggestions = errorInfo.getDictionary().getSuggestions(badWord.getText());
    }

    @Override
    public void activePanel(ErrorInfo errorInfo) {
        txtCmpSpellChecker.getAddWordAction().setErrorInfo(errorInfo);
        txtCmpSpellChecker.getIgnoreWordAction().setErrorInfo(errorInfo);
        txtCmpSpellChecker.getReplaceWordAction().setErrorInfo(errorInfo);
        txtCmpSpellChecker.getReplaceAllWordsAction().setErrorInfo(errorInfo);

        sugestionList.setSuggestionWords(suggestions, errorInfo.getBadWord().isStartOfSentence(), true);
    }

    @Override
    public boolean isForError(ErrorInfo errorInfo) {
        if (null != errorInfo)
            return errorInfo.isSpellingError();
        else
            return false ;
    }

    @Override
    public void initFrom(JTextComponentSpellChecker txtCmpSpellChecker) {
        super.initFrom(txtCmpSpellChecker);

        setLayout(new BorderLayout());

        JLabel label = new JLabel("Algunas sugerencias son :");
        add(label, BorderLayout.NORTH);

        this.txtCmpSpellChecker = txtCmpSpellChecker ;

        sugestionList = createSuggestionList();
        add(new JScrollPane(sugestionList), BorderLayout.CENTER);

        JPanel buttonsPanel = createButtonsPanel();

        add(buttonsPanel, BorderLayout.SOUTH);
    }
}
