/*
 * ErrorPopUpMenu.java
 *
 * Created on 18 de diciembre de 2006, 04:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dts.spell.swing;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import org.dts.spell.ErrorInfo;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.actions.ErrorInfoAction;
import org.dts.spell.swing.actions.ReplaceWordAction;
import org.dts.spell.swing.utils.Messages;

/**
 *
 * @author DreamTangerine
 */
public class ErrorPopUpMenu
{

  /**
   * @return the textComponentSpellChecker
   */
  protected JTextComponentSpellChecker getTextComponentSpellChecker() {
    return textComponentSpellChecker;
  }
  private class NullPopup extends JPopupMenu
  {
    @Override
    public void setVisible(boolean b)
    {
      if (b)
      {
        mergeWithErrorPopupMenuItems(this) ;
        
        if (getSubElements().length > 0)
          super.setVisible(b) ;
      }
      else
        super.setVisible(b) ;
    }
  }
  
  private JPopupMenu NULL_POPUP = new NullPopup() ;
  
  public class PopupMenuInfo implements PropertyChangeListener
  {
    public PopupMenuInfo(ErrorMarker marker)
    {
      JTextComponent textComponent = marker.getTextComponent() ;
      errorMarker = marker ;
      originalPopup = textComponent.getComponentPopupMenu() ;
      textComponent.addPropertyChangeListener("componentPopupMenu", this) ;
      
      if (null == originalPopup)
        textComponent.setComponentPopupMenu(NULL_POPUP) ;
      
      if (textComponent.getInheritsPopupMenu())
      {
        if (textComponent.getParent() instanceof JComponent)
        {
          JComponent parent = (JComponent) textComponent.getParent() ;
          
          if (originalPopup == parent.getComponentPopupMenu())
            originalPopup = null ;
        }
      }
    }
    
    public void propertyChange(PropertyChangeEvent evt)
    {
      JPopupMenu newPopup = (JPopupMenu) evt.getNewValue() ;
      
      // Avoid put original popup like an error.
      if (newPopup != originalPopup)
        originalPopup = newPopup ;
    }
    
    public ErrorMarker errorMarker ;
    public JPopupMenu originalPopup ;
    public Point position ; 
  }
  
  private class ErrorPopupMenuListener extends MouseMotionAdapter implements PopupMenuListener
  {
    public void popupMenuCanceled(PopupMenuEvent e)
    {
      //deleteErrorPopupMenuItems((JPopupMenu) e.getSource()) ;
    }
    
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
    {
      deleteErrorPopupMenuItems((JPopupMenu) e.getSource()) ;
    }
    
    public void popupMenuWillBecomeVisible(PopupMenuEvent e)
    {
      JPopupMenu popup = (JPopupMenu) e.getSource() ;
      
      if (NULL_POPUP != popup)
        mergeWithErrorPopupMenuItems(popup) ;
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
      PopupMenuInfo popupInfo = currentEditors.get((JTextComponent) e.getSource()) ;
      popupInfo.position = e.getPoint() ;
    }
  } ;
  
  private ErrorPopupMenuListener errorPopupMenuListener = new ErrorPopupMenuListener() ;
  
  private Hashtable<JTextComponent, PopupMenuInfo> currentEditors = new Hashtable<JTextComponent, PopupMenuInfo>() ;
  
  private JTextComponentSpellChecker textComponentSpellChecker ;
  
  /** Creates a new instance of ErrorPopUpMenu */
  public ErrorPopUpMenu(JTextComponentSpellChecker textComponentSpellChecker)
  {
    this.textComponentSpellChecker = textComponentSpellChecker ;
  }
  
  public void addErrorMarker(ErrorMarker errorMarker)
  {
    JTextComponent textComponent = errorMarker.getTextComponent() ;
    currentEditors.put(textComponent, new PopupMenuInfo(errorMarker)) ;
    
    textComponent.addMouseMotionListener(errorPopupMenuListener) ;
    textComponent.getComponentPopupMenu().addPopupMenuListener(errorPopupMenuListener) ;
  }
  
  public void removeErrorMarker(ErrorMarker errorMarker)
  {
    JTextComponent textComponent = errorMarker.getTextComponent() ;
    PopupMenuInfo info = currentEditors.get(textComponent) ;
    
    textComponent.removePropertyChangeListener(info) ;
    currentEditors.remove(textComponent) ;
    
    textComponent.getComponentPopupMenu().removePopupMenuListener(errorPopupMenuListener) ;
    textComponent.removeMouseMotionListener(errorPopupMenuListener) ;
    
    if (null == info.originalPopup)
      textComponent.setComponentPopupMenu(null) ;
  }
  
  public PopupMenuInfo getPopupMenuInfo(JPopupMenu popup)
  {
    return currentEditors.get((JTextComponent) popup.getInvoker()) ;
  }
  
  /**
   * This is the function to override to personalize the menu items. (You can use an instance of ReplaceErrorMenuItem)
   */
  protected void mergeWithErrorPopupMenuItems(JPopupMenu popup)
  {
    JTextComponent textCmp = (JTextComponent) popup.getInvoker() ;
    PopupMenuInfo popupInfo = currentEditors.get(textCmp) ;
    ErrorMarker marker = popupInfo.errorMarker ;
    int pos ;

    if (null != popupInfo.position)
      pos = textCmp.viewToModel(popupInfo.position) ;
    else
      pos = textCmp.getCaretPosition() ;

    mergeWithErrorPopupMenu(marker, pos) ;
  }
  
  /**
   * This is the function to override to delete menu items. If you use instances of ReplaceErrorMenuItem you don't
   * have to override.
   */
  protected void deleteErrorPopupMenuItems(JPopupMenu menu)  
  {
    cleanErrorPopupMenu(menu) ;
  }
    
  protected void mergeWithErrorPopupMenu(ErrorMarker marker, int position)
  {
    ErrorInfo info = marker.getErrorInfo(position) ;
    
    if (null != info)
      mergeWithErrorPopupMenu(info, marker.getTextComponent()) ;
  }
  
  protected JPopupMenu getErrorPopupMenu(ErrorInfo info, JTextComponent textComponent)
  {
    return mergeWithErrorPopupMenu(info, textComponent) ;
  }

  // interface to mark items and easy delete.
  protected interface SpellErrorMenuItem
  {
  }
  
  protected static class SeparatorSpellErrorMenuItem extends JPopupMenu.Separator implements SpellErrorMenuItem
  {
  }

  protected class SpellMenutItem extends JMenuItem implements SpellErrorMenuItem {
    public SpellMenutItem(Action action) {
      super(action) ;
    }
  }

  protected ErrorInfoAction setupErrorInfoAction(ErrorInfoAction action, ErrorInfo info) {
    action.setErrorInfo(info);

    return action ;
  }

  // function to override for delete word.
  protected Action getDeleteWordAction(ErrorInfo info) {
    return setupErrorInfoAction(getTextComponentSpellChecker().getDeleteWordAction(), info) ;
  }

  // function to override for replace word.
  protected Action getReplaceWordAction(ErrorInfo info, String suggestion) {
    return setupErrorInfoAction(getTextComponentSpellChecker().getReplaceWordAction(suggestion), info) ;
  }

  // function to override for add word.
  protected Action getAddWordAction(ErrorInfo info) {
    return setupErrorInfoAction(getTextComponentSpellChecker().getAddWordAction(), info) ;
  }

  // function to override for ignore word.
  protected Action getIgnoreWordAction(ErrorInfo info) {
    return setupErrorInfoAction(getTextComponentSpellChecker().getIgnoreWordAction(), info) ;
  }


  protected JPopupMenu mergeWithErrorPopupMenu(ErrorInfo info, JTextComponent textComponent)
  {
    //Word badWord = info.getBadWord() ;
    String[] suggestions = info.getSuggestions() ;
    JPopupMenu result = textComponent.getComponentPopupMenu() ;
    
    if (null == suggestions)
    {
      if (result.getSubElements().length > 0)
        result.add(new SeparatorSpellErrorMenuItem()) ;

      result.add(new SpellMenutItem(getDeleteWordAction(info))) ;
    }
    else
    {
      if (suggestions.length > 0)
      {
        if (result.getSubElements().length > 0)        
          result.add(new SeparatorSpellErrorMenuItem()) ;
        
        for (String sg : suggestions)
          result.add(new SpellMenutItem(getReplaceWordAction(info, sg))) ;
      }
    }

    if (info.isSpellingError())
    {
      if (result.getSubElements().length > 0)        
        result.add(new SeparatorSpellErrorMenuItem()) ;
      
      result.add(new SpellMenutItem(getIgnoreWordAction(info))) ;
      result.add(new SpellMenutItem(getAddWordAction(info))) ;
    }
    
    return result ;
  }
  
  protected static JPopupMenu cleanErrorPopupMenu(JTextComponent textComponent)
  {
    return cleanErrorPopupMenu(textComponent.getComponentPopupMenu()) ;
  }
  
  protected static JPopupMenu cleanErrorPopupMenu(JPopupMenu menu)
  {
    int current = menu.getComponentCount() - 1 ;
    boolean exit = current < 0 ;
    
    while (!exit)
    {
      Component cmp = menu.getComponent(current) ;
      
      if (cmp instanceof SpellErrorMenuItem)
      {
        menu.remove(cmp) ;
        --current ;
        
        exit = current < 0 ;
      }
      else
        exit = true ;
    }
    
    return menu ;
  }
}