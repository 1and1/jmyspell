/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.swing.panels;

import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.dts.spell.ErrorInfo;
import org.dts.spell.event.ProgressEvent;
import org.dts.spell.event.ProgressListener;
import org.dts.spell.swing.JTextComponentSpellChecker;

/**
 *
 * @author dreamtangerine
 */
public class LoadDictionaryPanel extends SolutionPanel implements ProgressListener {

    private JLabel label;
    private JProgressBar progressBar;
    private String description = "Se está cargando el diccionario, por favor espere mientras se carga." ;

    public LoadDictionaryPanel() {
        super("LOAD_DICTIONARY_PANEL");
    }

    @Override
    public void initFrom(JTextComponentSpellChecker txtCmpSpellChecker) {
        super.initFrom(txtCmpSpellChecker);

        label = createNoWidthLabel(getDescription());
        progressBar = new JProgressBar();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(label);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(progressBar);
    }

    @Override
    public String getTitle() {
        return "<html>El diccionario no está cargado.</html>";
    }

    @Override
    public void calcSolution(ErrorInfo errorInfo) {
    }

    @Override
    public void activePanel(ErrorInfo errorInfo) {
    }

    @Override
    public boolean isForError(ErrorInfo errorInfo) {
        return false;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    protected void setProgressData(ProgressEvent evt) {
        String txt = String.format("<html>%s<br><br>%s<br><br></html>", getDescription(), evt.getProgress()) ;

        progressBar.setMinimum(0);
        progressBar.setMaximum(evt.getTotal());
        progressBar.setValue(evt.getStep());
        label.setText(txt) ;

        repaint();
    }

    protected void setError(ProgressEvent evt) {
        String txt = String.format("<html>%s</html>", evt.getError().getLocalizedMessage()) ;

        progressBar.setVisible(false);
        label.setText(txt);

        repaint() ;
    }

    public void beginProgress(ProgressEvent evt) {
        setProgressData(evt);
    }

    public void nextStep(ProgressEvent evt) {
        setProgressData(evt);
    }

    public void endProgress(ProgressEvent evt) {
      if (!evt.hasError())
        setProgressData(evt);
      else
        setError(evt) ;
    }
}
