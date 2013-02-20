/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.actions.ReplaceAllWordsAction;
import org.dts.spell.swing.actions.ReplaceWordAction;
import org.dts.spell.swing.utils.Messages;

/**
 *
 * @author dreamtangerine
 */
public class JSuggestionList extends JList {

    private static final String NO_SUGGESTIONS_STRING = Messages.getString("JSpellPanel.NO_SUGGESTIONS_STRING");
    private ReplaceWordAction replaceAction;
    private ReplaceAllWordsAction replaceAllAction ;

    private static boolean isEmpty(JList list) {
        return list.getFirstVisibleIndex() < 0 || list.getModel().getElementAt(0) == NO_SUGGESTIONS_STRING;
    }

    protected class ListListener extends MouseAdapter implements ListSelectionListener, FocusListener {

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                JList list = (JList) e.getSource();

                String suggestion = null;

                if (!isEmpty(list)) {
                    suggestion = (String) list.getSelectedValue();
                }

                replaceAction.setSuggestion(suggestion);
                replaceAllAction.setSuggestion(suggestion);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                JList list = (JList) e.getSource();

                if (!isEmpty(list)) {
                    int index = list.locationToIndex(e.getPoint());

                    if (index != -1) {
                        replaceAction.doAction(list);
                    }
                }
            }
        }

        public void focusGained(FocusEvent e) {
            if (!e.isTemporary()) {
                JList list = (JList) e.getSource();

                if (list.getSelectedIndex() < 0) {
                    list.setSelectedIndex(0);
                }
            }
        }

        public void focusLost(FocusEvent e) {
        }
    }

    public DefaultListModel getDefaultModel() {
        return (DefaultListModel) super.getModel();
    }

    public void setSuggestionWords(List<String> list, boolean startSentence, boolean selectFirst) {
        if (list.isEmpty()) {
            clearSuggestionWords();
        } else {
            getDefaultModel().clear();

            for (String word : list) {
                if (startSentence) {
                    word = Word.getStartSentenceWordCase(word);
                }

                getDefaultModel().addElement(word);
            }

            if (selectFirst) {
                setSelectedIndex(0);
            }
        }
    }

    public void clearSuggestionWords() {
        getDefaultModel().clear();
        getDefaultModel().addElement(NO_SUGGESTIONS_STRING);
        replaceAction.setSuggestion(null);
        replaceAllAction.setSuggestion(null);
    }

    public JSuggestionList(ReplaceWordAction replace, ReplaceAllWordsAction replaceAll) {

        replaceAction = replace;
        replaceAllAction = replaceAll ;

        ListListener listListener = new ListListener();

        setModel(new DefaultListModel());

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addListSelectionListener(listListener);
        addMouseListener(listListener);
        addFocusListener(listListener);

        clearSuggestionWords();
    }
}
