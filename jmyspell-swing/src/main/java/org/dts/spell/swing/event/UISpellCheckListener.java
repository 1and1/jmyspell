/*
 * Created on 24/02/2005
 *
 */
package org.dts.spell.swing.event;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.text.MessageFormat;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.dts.spell.event.SpellCheckAdapter;
import org.dts.spell.event.SpellCheckEvent ;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.JSpellDialog;
import org.dts.spell.swing.utils.ErrorMsgBox;
import org.dts.spell.swing.utils.Messages;

/**
 * @author DreamTangerine
 *  
 */
public class UISpellCheckListener extends SpellCheckAdapter
{
  /**
   * The current JSpellDialog
   */
  private JSpellDialog spellDialog ;

  /**
   * Dispose dialog on endSpell.
   */
  private boolean disposeDialog ;

  /**
   * @return Returns the spellDialog.
   */
  public JSpellDialog getSpellDialog()
  {
    return spellDialog ;
  }

  /**
   * Try to relocate the dialog to allow see the bad word.
   * @param wordRect
   */
  public void relocateDialog(Rectangle wordRect, JDialog dialog)
  {
    Rectangle bounds = dialog.getBounds() ;
    int orgX = bounds.x ;
    int orgY = bounds.y ;

    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment() ; 
    Rectangle screenBounds = env.getMaximumWindowBounds() ;
    
    if (bounds.intersects(wordRect))
    {
      double ratio = 0.95 ;
      
      if (screenBounds.getWidth() - wordRect.getWidth() < bounds.getWidth())
      {
        if (wordRect.getMinY() > (screenBounds.getHeight() - wordRect.getMaxY()))
          orgY = (int) (screenBounds.getMinY() + 
            (wordRect.getMinY() - bounds.getHeight()) * (1.0 - ratio)) ;
        else
          orgY = (int) (wordRect.getMaxY() +
              (screenBounds.getMaxY() - wordRect.getMaxY() - bounds.getHeight()) 
                * ratio) ; 
      }
      else
      {
        if (wordRect.getMinX() > (screenBounds.getWidth() - wordRect.getMaxX()))
          orgX = (int) (screenBounds.getMinX() + 
            (wordRect.getMinX() - bounds.getWidth()) * (1.0 - ratio)) ;
        else
          orgX = (int) (wordRect.getMaxX() +
              (screenBounds.getMaxX() - wordRect.getMaxX() - bounds.getWidth()) 
              * ratio) ;
      }

      if (orgX < 0)
        orgX = 0 ;
      else if (orgX + bounds.width > screenBounds.getMaxX())
        orgX = (int) (screenBounds.getMaxX() - bounds.getWidth()) ; 

      if (orgY < 0)
        orgY = 0 ;
      else if (orgY + bounds.height > screenBounds.getMaxY())
        orgY = (int) (screenBounds.getMaxY() - bounds.getHeight()) ; 
      
      dialog.setLocation(orgX, orgY) ;    
    }
  }
  
  public void initDialogPosition(JDialog dialog)
  {
    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment() ; 
    Rectangle screenBounds = env.getMaximumWindowBounds() ;
    
    dialog.pack() ;
    
    Rectangle dlgRect = dialog.getBounds() ;
    
    int orgX = (int) (screenBounds.getMaxX() - dlgRect.getWidth()) ;
    int orgY = (int) (screenBounds.getMaxY() - dlgRect.getHeight()) ; 

    dialog.setLocation(orgX, orgY) ;
  }
  
  public void relocateDialog(Rectangle wordRect)
  {
    relocateDialog(wordRect, getSpellDialog()) ;
  }
  
  /**
   * @param spellDialog
   *          The spellDialog to set.
   */
  public void setSpellDialog(JSpellDialog spellDialog)
  {
    this.spellDialog = spellDialog ;
    
    if (null != spellDialog)
      initDialogPosition(spellDialog) ;
  }

  /**
   *  
   */
  public UISpellCheckListener()
  {
    this(null) ;
  }

  /**
   * Create a UISpellCheckListener that show a JSpellDialog for each error that was 
   * found. Tou can pass a null JSpellDialog and the UISpellCheckListener will create
   * one for you. 
   * 
   * @param spellDialog The dialog to show it can be null.
   */
  public UISpellCheckListener(JSpellDialog spellDialog)
  {
    this(spellDialog, true) ;
  }

  public UISpellCheckListener(JSpellDialog spellDialog, boolean disposeOnEnd)
  {
    setSpellDialog(spellDialog) ;
    setDisposeDialog(disposeOnEnd);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.event.SpellCheckListener#beginChecking(org.dts.spell.event.SpellCheckEvent)
   */
  @Override
  public void beginChecking(SpellCheckEvent event)
  {
    if (getSpellDialog() == null)
      setSpellDialog(new JSpellDialog()) ;
    
    initDialogPosition(getSpellDialog()) ;    
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.event.SpellCheckListener#spellingError(org.dts.spell.event.SpellCheckEvent)
   */
  @Override
  public void spellingError(SpellCheckEvent event)
  {
    JSpellDialog dlg = getSpellDialog() ;
    
    if (!dlg.showDialog(event.getSpellChecker(), event.getWordFinder()))
      event.cancel() ;
  }

  @Override
  public void badCaseError(SpellCheckEvent event)
  {
    Word word = event.getCurrentWord() ;
    String newWord = word.getStartSentenceWordCase() ;
    String msg = MessageFormat.format(
        Messages.getString("ERROR_CAPITALIZATION_STRING"), 
        new Object[] { word, newWord } ) ;

    int result = ErrorMsgBox.yesNoCancelMsg(Messages.getString("ERROR_CAPITALIZATION_TITLE_STRING"), msg) ;
    
    switch(result)
    {
      case JOptionPane.YES_OPTION :
        event.getWordFinder().replace(newWord) ;
        break ;
      
      case JOptionPane.NO_OPTION :
        // Nothing to do.
        break ;
   
      case JOptionPane.CANCEL_OPTION :
        event.cancel() ;
        break ;
    }
  }

  @Override
  public void repeatWordError(SpellCheckEvent event)
  {
    Word word = event.getCurrentWord() ;
    String msg = MessageFormat.format(Messages.getString("ERROR_REPEAT_WORD_STRING"), word) ;
    int result = ErrorMsgBox.yesNoCancelMsg(Messages.getString("ERROR_REPEAT_WORD_TITLE_STRING"), msg) ;
    
    switch(result)
    {
      case JOptionPane.YES_OPTION :
        event.getWordFinder().replace("") ;
        break ;
      
      case JOptionPane.NO_OPTION :
        // Nothing to do.
        break ;
   
      case JOptionPane.CANCEL_OPTION :
        event.cancel() ;
        break ;
    }
  }

    @Override
    public void endChecking(SpellCheckEvent event) {
        super.endChecking(event);

        if (isDisposeDialog()) {
            getSpellDialog().dispose();
            setSpellDialog(null);
        }
    }



    /**
     * @return the disposeOnEnd
     */
    public boolean isDisposeDialog() {
        return disposeDialog;
    }

    /**
     * @param disposeOnEnd the disposeOnEnd to set
     */
    public void setDisposeDialog(boolean dispose) {
        disposeDialog = dispose;
    }
}
