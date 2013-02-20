/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.swing.panels;

import java.lang.reflect.InvocationTargetException;
import org.dts.spell.event.ProgressEvent;
import org.dts.spell.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.Expression;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.Statement;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.dts.spell.ErrorInfo;
import org.dts.spell.dictionary.DictionaryManager;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.event.ProgressListener;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.actions.AbstractSpellCheckerAction;
import org.dts.spell.swing.event.TextComponentSpellCheckerAdapter;
import org.dts.spell.swing.event.TextComponentSpellCheckerEvent;
import org.dts.spell.swing.event.TextComponentSpellCheckerListener;
import org.dts.spell.swing.utils.Messages;
import org.dts.spell.swing.utils.SeparatorLineBorder;

/**
 *
 * @author personal
 */
public class JRealTimeSpellPanel extends JPanel {

    private JTextComponentSpellChecker textComponentSpellChecker;
    private ErrorMarker activeErrorMarker;
    private FindFocusAction focusFinderAction;
    private PropertyChangeListener currentErrorInfoListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            updateErrorInfo((ErrorMarker) evt.getSource(), (ErrorInfo) evt.getNewValue());
        }
    };
    private PropertyChangeListener firstLastErrorInfoListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            ErrorMarker errorMarker = (ErrorMarker) evt.getSource();

            if (evt.getPropertyName().equals(ErrorMarker.NEXT_ERROR_PROPERTY)) {
                goNextAction.update(errorMarker, (ErrorInfo) evt.getNewValue());
            } else {
                goPreviousAction.update(errorMarker, (ErrorInfo) evt.getNewValue());
            }

            goLastAction.update(errorMarker, errorMarker.getLastError());
            goFirstAction.update(errorMarker, errorMarker.getFirstError());
        }
    };
    private PropertyChangeListener destroyListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            if (!isDisplayable()) {
                onDestroy();
            }
        }
    };
    private TextComponentSpellCheckerListener cmpChangeListener = new TextComponentSpellCheckerAdapter() {

        @Override
        public void realTimeStart(TextComponentSpellCheckerEvent evt) {
            JRealTimeSpellPanel.this.realTimeStart(evt);
        }

        @Override
        public void realTimeWillStop(TextComponentSpellCheckerEvent evt) {
            JRealTimeSpellPanel.this.realTimeWillStop(evt);
        }
    };
    

    private ProgressListener loadDictionary = new ProgressListener() {

        private void callInSwingTrhead(Runnable run) {
            try {
                if (!SwingUtilities.isEventDispatchThread())
                  SwingUtilities.invokeAndWait(run);
                else
                  run.run() ;
            } catch (InterruptedException ex) {
                Logger.getLogger(JRealTimeSpellPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(JRealTimeSpellPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void beginProgress(final ProgressEvent evt) {
            callInSwingTrhead(new Runnable() {
                public void run() {
                    solutionPanel.getLoadDictionaryPanelAsListener().beginProgress(evt);
                }
            });
        }

        public void nextStep(final ProgressEvent evt) {
            callInSwingTrhead(new Runnable() {
                public void run() {
                    solutionPanel.getLoadDictionaryPanelAsListener().nextStep(evt);
                }
            });
        }

        public void endProgress(final ProgressEvent evt) {
            callInSwingTrhead(new Runnable() {
                public void run() {
                    solutionPanel.getLoadDictionaryPanelAsListener().endProgress(evt);
                }
            });

            if (!evt.hasError()) {
              SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                      resetErrorMarker() ;
                  }
              }) ;
            }
        }
    };

    public JRealTimeSpellPanel(JTextComponentSpellChecker textComponentSpellChecker) {
        this(textComponentSpellChecker, true);
    }

    public JRealTimeSpellPanel(JTextComponentSpellChecker textComponentSpellChecker, boolean auto) {
        this.textComponentSpellChecker = textComponentSpellChecker;
        setAutomatic(auto);
        init();
        onShowPanel();
        addPropertyChangeListener("ancestor", destroyListener);
    }

    private class FindFocusAction extends AbstractSpellCheckerAction {

        public FindFocusAction() {
            super("");
        }

        public JTextComponent getTextComponent() {
            return getLastSelected();
        }

        @Override
        public void onFocusedTextComponentChanged(JTextComponent textCmp) {
            if (JRealTimeSpellPanel.this.isVisible()) {
                JRealTimeSpellPanel.this.onFocusedTextComponentChanged(textCmp);
            }
        }

        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public void setAutomatic(boolean value) {
        if (isAutomatic() != value) {
            if (value) {
                focusFinderAction = new FindFocusAction();
            } else {
                focusFinderAction.release();
                focusFinderAction = null;
            }
        }
    }

    public boolean isAutomatic() {
        return null != focusFinderAction;
    }

    @Override
    public void setVisible(boolean aFlag) {

        if (isVisible() != aFlag) {
            super.setVisible(aFlag);

            if (aFlag) {
                onShowPanel();
            } else {
                onHidePanel();
            }
        }
    }

    protected void onDestroy() {
        onHidePanel();
        setAutomatic(false);
        calcExecutor.shutdown();
    }

    protected void onHidePanel() {
        textComponentSpellChecker.removeListener(cmpChangeListener);
        DictionaryManager.get().removeProgressListener(loadDictionary);
        setErrorMarker(null);
    }

    protected void onShowPanel() {
        textComponentSpellChecker.addListener(cmpChangeListener);
        DictionaryManager.get().addProgressListener(loadDictionary);
        ErrorMarker errorMarker = null;

        if (null != focusFinderAction) {
            JTextComponent txtCmp = focusFinderAction.getTextComponent();

            if (null != txtCmp) {
                errorMarker = textComponentSpellChecker.getErrorMarker(txtCmp);
            }
        }

        setErrorMarker(errorMarker);
    }

    protected void realTimeStart(TextComponentSpellCheckerEvent evt) {
        if (null != focusFinderAction) {
            JTextComponent txtCmp = evt.getTextComponent();

            if (focusFinderAction.getTextComponent() == txtCmp) {
                setErrorMarker(evt.getTextComponentSpellChecker().getErrorMarker(txtCmp));
            }
        }
    }

    public void realTimeWillStop(TextComponentSpellCheckerEvent evt) {
        if (getCurrentTextComponent() == evt.getTextComponent()) {
            setErrorMarker(null);
        }
    }

    protected void onFocusedTextComponentChanged(JTextComponent textCmp) {
        if (null != textCmp) {
            setErrorMarker(textComponentSpellChecker.getErrorMarker(textCmp));
        } else {
            setErrorMarker(null);
        }
    }

    protected void quitErrorMarker(ErrorMarker errorMarker) {
        if (null != errorMarker) {
            errorMarker.removePropertyChangeListener(ErrorMarker.FIRST_ERROR_PROPERTY, goFirstAction);
            errorMarker.removePropertyChangeListener(ErrorMarker.LAST_ERROR_PROPERTY, goLastAction);
            errorMarker.removePropertyChangeListener(ErrorMarker.NEXT_ERROR_PROPERTY, firstLastErrorInfoListener);
            errorMarker.removePropertyChangeListener(ErrorMarker.PREVIOUS_ERROR_PROPERTY, firstLastErrorInfoListener);

            errorMarker.removePropertyChangeListener(ErrorMarker.CURRENT_ERROR_PROPERTY, currentErrorInfoListener);
        }
    }

    protected void putErrorMarker(ErrorMarker errorMarker) {
        if (null != errorMarker) {
            errorMarker.addPropertyChangeListener(ErrorMarker.FIRST_ERROR_PROPERTY, goFirstAction);
            errorMarker.addPropertyChangeListener(ErrorMarker.LAST_ERROR_PROPERTY, goLastAction);
            errorMarker.addPropertyChangeListener(ErrorMarker.NEXT_ERROR_PROPERTY, firstLastErrorInfoListener);
            errorMarker.addPropertyChangeListener(ErrorMarker.PREVIOUS_ERROR_PROPERTY, firstLastErrorInfoListener);

            errorMarker.addPropertyChangeListener(ErrorMarker.CURRENT_ERROR_PROPERTY, currentErrorInfoListener);
        }
    }

    public void setErrorMarker(ErrorMarker errorMarker) {
      setErrorMarker(errorMarker, false) ;
    }

    public void resetErrorMarker() {
      setErrorMarker(activeErrorMarker, true) ;
    }

    protected void setErrorMarker(ErrorMarker errorMarker, boolean force) {
        if (activeErrorMarker != errorMarker || force) {
            quitErrorMarker(activeErrorMarker);
            activeErrorMarker = errorMarker;
            putErrorMarker(activeErrorMarker);

            updateActionsState(errorMarker);
            updateErrorInfo(errorMarker);
        }
    }

    public void updateFirstAction(Action action, ErrorMarker errorMarker, ErrorInfo firstErrorInfo) {
        action.setEnabled(null != firstErrorInfo && errorMarker.getCurrentError() != firstErrorInfo);
    }

    public void updateLastAction(Action action, ErrorMarker errorMarker, ErrorInfo lastErrorInfo) {
        action.setEnabled(null != lastErrorInfo && errorMarker.getCurrentError() != lastErrorInfo);
    }

    public void updateNextAction(Action action, ErrorMarker errorMarker, ErrorInfo nextErrorInfo) {
        action.setEnabled(null != nextErrorInfo);
    }

    public void updatePreviousAction(Action action, ErrorMarker errorMarker, ErrorInfo previousErrorInfo) {
        action.setEnabled(null != previousErrorInfo);
    }

    public void updateActionsState(ErrorMarker errorMarker) {
        goFirstAction.update(errorMarker);
        goLastAction.update(errorMarker);
        goNextAction.update(errorMarker);
        goPreviousAction.update(errorMarker);
    }

    protected void init() {
        createPanels();
        updateActionsState(activeErrorMarker);
        updateErrorInfo(activeErrorMarker);
    }

    protected class GoAction extends AbstractAction implements PropertyChangeListener {

        public GoAction(String name, String actionMethodName, String updateMethodName, String errorMarkerMethod) {
            super(name);
            this.actionMethodName = actionMethodName;
            this.updateMethodName = updateMethodName;
            this.errorMarkerMethod = errorMarkerMethod;
        }

        public void actionPerformed(ActionEvent e) {
            Statement st = new Statement(JRealTimeSpellPanel.this, actionMethodName, new Object[]{activeErrorMarker, getErrorInfo(activeErrorMarker)});

            try {
                st.execute();
            } catch (Exception ex) {
                Logger.getLogger(JRealTimeSpellPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private ErrorInfo getErrorInfo(ErrorMarker errorMarker) {
            ErrorInfo result = null;

            if (null != errorMarker) {
                Expression st = new Expression(errorMarker, errorMarkerMethod, null);

                try {
                    result = (ErrorInfo) st.getValue();
                } catch (Exception ex) {
                    Logger.getLogger(JRealTimeSpellPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return result;
        }

        public void update(ErrorMarker errorMarker) {
            update(errorMarker, getErrorInfo(errorMarker));
        }

        public void update(ErrorMarker errorMarker, ErrorInfo errorInfo) {
            Statement st = new Statement(JRealTimeSpellPanel.this, updateMethodName, new Object[]{this, errorMarker, errorInfo});

            try {
                st.execute();
            } catch (Exception ex) {
                Logger.getLogger(JRealTimeSpellPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            update((ErrorMarker) evt.getSource(), (ErrorInfo) evt.getNewValue());
        }
        String actionMethodName;
        String updateMethodName;
        String errorMarkerMethod;
    }

    protected Action createAction(String name, String methodName) {
        final ActionListener listener = EventHandler.create(ActionListener.class, this, methodName);

        Action result = new AbstractAction(name) {

            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        };

        return result;
    }

    protected void createPanels() {
        setLayout(new GridBagLayout());
        GridBagConstraints cnt = new GridBagConstraints();

        cnt.anchor = GridBagConstraints.NORTH;
        cnt.fill = GridBagConstraints.HORIZONTAL;
        cnt.weightx = 1.0;
        cnt.insets = new Insets(5, 5, 5, 5);

        add(createNavigationPanel(), cnt);

        cnt.gridy = 1;
        cnt.anchor = GridBagConstraints.CENTER;
        cnt.fill = GridBagConstraints.HORIZONTAL;

        add(createErrorInfoPanel(), cnt);

        cnt.gridy = 2;
        cnt.anchor = GridBagConstraints.CENTER;
        cnt.fill = GridBagConstraints.BOTH;
        cnt.weighty = 1.0;

        add(createErrorSolutionPanel(), cnt);
    }

    protected void updateErrorInfo(ErrorMarker errorMarker) {
        if (null != errorMarker) {
            updateErrorInfo(errorMarker, errorMarker.getCurrentError());
        } else {
            updateErrorInfo(errorMarker, null);
        }
    }

    private class SolutionUpdater implements Runnable {

        private SolutionPanel panel;

        public SolutionUpdater(SolutionPanel panel) {
            this.panel = panel;
        }

        public void run() {
            if (null != activeErrorMarker && panel.tryActivatePanel(activeErrorMarker.getCurrentError())) {
                solutionPanel.showSolutionPanel(panel);
                setErrorInfoText(panel.getTitle());
            }
        }
    }

    protected void dalayCalc(long startTimestamp) {
        try {
            long currentTimestamp = System.currentTimeMillis();
            long delay = 1000 - (currentTimestamp - startTimestamp);

            if (delay > 0) {
                Thread.sleep(delay);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(JRealTimeSpellPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void calcSolution(final SolutionPanel panel, final ErrorInfo errorInfo) {
        calcExecutor.execute(new Runnable() {

            public void run() {
                long startTimestamp = System.currentTimeMillis();

                panel.doCalcSolution(errorInfo);

                dalayCalc(startTimestamp);

                SwingUtilities.invokeLater(new SolutionUpdater(panel));
            }
        });
    }

    protected void updateErrorInfo(ErrorMarker errorMarker, ErrorInfo errorInfo) {
        SolutionPanel activePanel;
        SpellDictionary dict = textComponentSpellChecker.getSpellChecker().getDictionary();

        if (null == dict) {
          activePanel = solutionPanel.getNoDictionaryPanel();
        }
        else if (!dict.isLoad()) {
            activePanel = solutionPanel.getLoadDictionaryPanel();
        } else if (null != errorInfo) {
            activePanel = solutionPanel.getSearchErrorPanel();
            calcSolution(solutionPanel.getSolutionPanelFor(errorInfo), errorInfo);
        } else {
            activePanel = solutionPanel.getNoErrorPanel();
        }

        solutionPanel.showSolutionPanel(activePanel);
        setErrorInfoText(activePanel.getTitle());

        errorInfoLabel.setEnabled(null != errorMarker);
        solutionPanel.setEnabled(null != errorMarker);
    }

    public void setErrorInfoText(String txt) {
        errorInfoLabel.setText(txt);
    }

    protected JLabel createErrorInfoLabel() {
        JLabel label = new JLabel("X");

        label.setPreferredSize(new Dimension(0, label.getPreferredSize().height));

        return label;
    }

    protected JPanel createErrorInfoPanel() {
        JPanel result = new JPanel(new BorderLayout());
        errorInfoLabel = createErrorInfoLabel();

        result.setBorder(BorderFactory.createTitledBorder(SeparatorLineBorder.get(), Messages.getString("JRealTimeSpellPanel.ERROR_TITLE_STRING")));
        result.add(errorInfoLabel, BorderLayout.CENTER);

        return result;
    }

    public JTextComponent getCurrentTextComponent() {
        if (null != activeErrorMarker) {
            return activeErrorMarker.getTextComponent();
        } else {
            return null;
        }
    }

    public void gotoError(ErrorInfo info) {
        Word word = info.getBadWord();
        JTextComponent textComponent = getCurrentTextComponent();

        textComponent.setCaretPosition(word.getEnd());
        textComponent.moveCaretPosition(word.getStart());
        textComponent.requestFocusInWindow();
    }

    public void gotoFirstError(ErrorMarker errorMaker, ErrorInfo errorInfo) {
        gotoError(errorInfo);
    }

    public void gotoPreviousError(ErrorMarker errorMaker, ErrorInfo errorInfo) {
        gotoError(errorInfo);
    }

    public void gotoNextError(ErrorMarker errorMaker, ErrorInfo errorInfo) {
        gotoError(errorInfo);
    }

    public void gotoLastError(ErrorMarker errorMaker, ErrorInfo errorInfo) {
        gotoError(errorInfo);
    }

    protected GoAction createFirstAction() {
        return new GoAction(Messages.getString("JRealTimeSpellPanel.GO_FIRST_BUTTON"), "gotoFirstError", "updateFirstAction", "getFirstError");
    }

    protected GoAction createPreviousAction() {
        return new GoAction(Messages.getString("JRealTimeSpellPanel.GO_PREVIOUS_BUTTON"), "gotoPreviousError", "updatePreviousAction", "getPreviousError");
    }

    protected GoAction createNextAction() {
        return new GoAction(Messages.getString("JRealTimeSpellPanel.GO_NEXT_BUTTON"), "gotoNextError", "updateNextAction", "getNextError");
    }

    protected GoAction createLastAction() {
        return new GoAction(Messages.getString("JRealTimeSpellPanel.GO_LAST_BUTTON"), "gotoLastError", "updateLastAction", "getLastError");
    }

    protected void createNavigationActions() {
        goFirstAction = createFirstAction();
        goPreviousAction = createPreviousAction();
        goNextAction = createNextAction();
        goLastAction = createLastAction();
    }

    protected JPanel createNavigationPanel() {
        JPanel result = new JPanel();

        result.setBorder(BorderFactory.createTitledBorder(SeparatorLineBorder.get(), Messages.getString("JRealTimeSpellPanel.NAVIGATION_TITLE_STRING")));

        createNavigationActions();

        result.add(new JButton(goFirstAction));
        result.add(new JButton(goPreviousAction));
        result.add(new JButton(goNextAction));
        result.add(new JButton(goLastAction));

        return result;
    }

    protected JPanel createErrorSolutionPanel() {
        solutionPanel = new SolutionContainerPanel(textComponentSpellChecker);
        solutionPanel.setBorder(BorderFactory.createTitledBorder(SeparatorLineBorder.get(), Messages.getString("JRealTimeSpellPanel.SOLUTION_TITLE_STRING")));

        return solutionPanel;
    }
    private GoAction goFirstAction;
    private GoAction goPreviousAction;
    private GoAction goNextAction;
    private GoAction goLastAction;
    private JLabel errorInfoLabel;
    private SolutionContainerPanel solutionPanel;
    private final ThreadPoolExecutor calcExecutor =
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1),
            new ThreadPoolExecutor.DiscardOldestPolicy());
}
