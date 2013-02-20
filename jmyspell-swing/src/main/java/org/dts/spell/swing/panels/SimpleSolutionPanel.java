/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.panels;

import java.awt.Dimension;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.actions.ErrorInfoAction;

/**
 *
 * @author personal
 */
public abstract class SimpleSolutionPanel extends SolutionPanel {

  private String title ;
  private String description;
  
  private JButton applyButton ;
  private JLabel label ;

  public SimpleSolutionPanel(String name) {
      this((Action) null, name) ;
  }

  public SimpleSolutionPanel(ErrorInfoAction action, String name)
  {
    this((Action) action, name) ;
  }

  public SimpleSolutionPanel(Action action, String name)
  {
    this(action, name, "", "") ;
  }

  public SimpleSolutionPanel(Action action, String name, String title, String description)
  {
    super(name) ;
    this.title = title ;
    this.description = description ;
  
    initFromAction(action) ;
  }
  
  protected void initFromAction(Action action) {
    label = createNoWidthLabel(getDescription()) ;
    applyButton = new JButton(action) ;
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    add(label) ;
    add(Box.createRigidArea(new Dimension(0, 5)));
    add(applyButton) ;
  }

  protected void setAction(Action action) {
      applyButton.setAction(action);
  }

  protected Action getAction() {
      return applyButton.getAction() ;
  }

  @Override
  public String getTitle() {
    return title ;
  }

  @Override
  public void activePanel(ErrorInfo errorInfo) {
    Action action = getAction() ;
    
     if (action instanceof ErrorInfoAction)
        ((ErrorInfoAction) action).setErrorInfo(errorInfo) ;
    
    label.setText(getDescription());
  }

    /**
     * @param title the title to set
     */
    protected void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    protected String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    protected void setDescription(String description) {
        this.description = description;
    }
}
