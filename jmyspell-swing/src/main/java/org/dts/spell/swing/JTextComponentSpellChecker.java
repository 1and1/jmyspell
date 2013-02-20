/*
 * Created on 26/02/2005
 *
 */
package org.dts.spell.swing;

import org.dts.spell.swing.event.TextComponentSpellCheckerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.dts.spell.ErrorInfo;
import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.dictionary.SpellDictionaryException;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.actions.ActionManager;
import org.dts.spell.swing.actions.AddWordAction;
import org.dts.spell.swing.actions.DefaultActionManager;
import org.dts.spell.swing.actions.DeleteWordAction;
import org.dts.spell.swing.actions.IgnoreWordAction;
import org.dts.spell.swing.actions.RealTimeSpellCheckAction;
import org.dts.spell.swing.actions.ReplaceAllWordsAction;
import org.dts.spell.swing.actions.ReplaceWordAction;
import org.dts.spell.swing.actions.SpellCheckAction;
import org.dts.spell.swing.event.ErrorMarkerListener;
import org.dts.spell.swing.event.TextComponentSpellCheckerEvent;
import org.dts.spell.swing.event.UIErrorMarkerListener;
import org.dts.spell.swing.finder.DocumentWordFinder;
import org.dts.spell.swing.utils.ErrorMsgBox;
import org.dts.spell.utils.EventMulticaster;

/**
 * @author DreamTangerine
 *
 */
public class JTextComponentSpellChecker
{
  private SpellChecker spellChecker ;
  private UIErrorMarkerListener uiErrorListener = new UIErrorMarkerListener() ;
  private Map<JTextComponent, ErrorMarkerListener> errorMarkers = new HashMap<JTextComponent, ErrorMarkerListener>() ;
  private DocumentWordFinder defaultDocumentWordFinder = new DocumentWordFinder() ; //new DocumentWordFinder(new FilteredTokenizer(new DumpFilter())) ;
  private RealTimeSpellChecker realTimeSpellChecker = null ;
  private ErrorToolTips errorToolTips = new ErrorToolTips() ;
  private ErrorPopUpMenu errorPopUpMenu = new ErrorPopUpMenu(this) ;
  private ActionManager actionManager = new DefaultActionManager() ;

  private EventMulticaster<TextComponentSpellCheckerListener> eventMulticaster = new EventMulticaster<TextComponentSpellCheckerListener>(TextComponentSpellCheckerListener.class) ;
  
  private static final String DESTROY_PROPERTY = "ancestor" ;
  private static final String DOCUMENT_PROPERTY = "document" ;
  
  private PropertyChangeListener automaticStopListener = new PropertyChangeListener()
  {
     public void propertyChange(PropertyChangeEvent evt) {
      JTextComponent cmp = (JTextComponent) evt.getSource() ;

      // TODO : May be more correct evt.getNewValue() == null
      if (!cmp.isDisplayable())
        stopRealtimeMarkErrors(cmp) ;
    }
  } ;

  private PropertyChangeListener docChangeListener = new PropertyChangeListener()
  {
     public void propertyChange(PropertyChangeEvent evt) {
      JTextComponent cmp = (JTextComponent) evt.getSource() ;
      Document oldDoc = (Document) evt.getOldValue() ;
      
      if (isRealtimeMarkErrors(oldDoc, cmp))
        startRealtimeMarkErrors(cmp, stopRealtimeMarkErrors(oldDoc, cmp, false)) ;
    }
  } ;
  
  public JTextComponentSpellChecker(SpellChecker checker)
  {
    spellChecker = checker ;
  }
  
  public void addListener(TextComponentSpellCheckerListener listener)
  {
    eventMulticaster.addListener(listener);
  }

  public void removeListener(TextComponentSpellCheckerListener listener)
  {
    eventMulticaster.removeListener(listener) ;
  }
  
  public SpellChecker getSpellChecker()
  {
    return spellChecker ;
  }
  
  private ErrorMarkerListener getErrorMarkerListener(JTextComponent textComponent)
  {
    return errorMarkers.get(textComponent) ;
  }
  
  public void markErrors(JTextComponent textComponent)
  {
    markErrors(textComponent, defaultDocumentWordFinder) ;
  }
  
  /**
   * Mark current errors (not while you are typing) of the JTextComponent.
   * You must call unMarkErrors to free resources when you don't need the marks.
   *
   * @param textComponent
   * @param wordFinder
   */
  public void markErrors(JTextComponent textComponent, DocumentWordFinder wordFinder)
  {
    ErrorMarkerListener listener = getErrorMarkerListener(textComponent) ;
    
    if (null == listener)
    {
      listener = new ErrorMarkerListener() ;
      errorMarkers.put(textComponent, listener) ;
    }
    
    Document doc = textComponent.getDocument() ;
    
    listener.setTextComponent(textComponent) ;
    wordFinder.setDocument(doc) ;
    
    spellChecker.check(wordFinder, listener) ;
    
    if (wordFinder == defaultDocumentWordFinder)
      wordFinder.quitDocument() ;
    
    markInManagers(listener.getErrorMarker()) ;
  }
  
  /**
   * Allow quit all marked error of the textComponent. You must call it when you
   * don't want the errors marks. You must first call markErrors or a
   * IllegalArgumentException will be throw.
   *
   * @param textComponent
   */
  public void unMarkErrors(JTextComponent textComponent)
  {
    ErrorMarkerListener listener = getErrorMarkerListener(textComponent) ;
    
    if (null != listener)
    {
      listener.setTextComponent(null) ;
      errorMarkers.remove(textComponent) ;
      
      unMarkInManagers(listener.getErrorMarker()) ;
    }
    else
      throw new IllegalArgumentException() ;
  }
  
  public boolean spellCheck(JTextComponent textComponent)
  {
    return spellCheck(textComponent, defaultDocumentWordFinder) ;
  }
  
  
  /**
   * Chek the current component showing a Graphical Dialog.
   * If the realtime spell check is activated for textComponent, the realtime spell checking
   * is stoped and started when the spellcheck is started and finished respectively.
   *
   */
  public boolean spellCheck(JTextComponent textComponent,  DocumentWordFinder documentWordFinder)
  {
    Document doc = textComponent.getDocument() ;
    DocumentWordFinder wordFinder = null ;
    
    boolean isRealTime = isRealtimeMarkErrors(textComponent) ;
    
    if (isRealTime)
    {
      wordFinder = realTimeSpellChecker.getWordFinder(textComponent) ;
      stopRealtimeMarkErrors(textComponent) ;
    }
    
    uiErrorListener.setTextComponent(textComponent) ;
    documentWordFinder.setDocument(doc) ;
    
    boolean result = spellChecker.check(documentWordFinder, uiErrorListener) ;
    
    uiErrorListener.quitTextComponent() ;
    
    if (!isRealTime)
      documentWordFinder.quitDocument() ;
    else
      startRealtimeMarkErrors(textComponent, wordFinder) ;
    
    return result ;
  }
  
  public boolean isRealtimeMarkErrors(JTextComponent textComponent) 
  {
    return null != realTimeSpellChecker && realTimeSpellChecker.isRealTimeSpellChecking(textComponent) ;
  }

  boolean isRealtimeMarkErrors(Document doc, JTextComponent textComponent) 
  {
    return null != realTimeSpellChecker && realTimeSpellChecker.isRealTimeSpellChecking(doc, textComponent) ;
  }
  
  public ErrorMarker getErrorMarker(JTextComponent textComponent)
  {
    if (null != realTimeSpellChecker)
      return realTimeSpellChecker.getErrorMarker(textComponent) ;
    else
      return null ;
  }
  
  public void startRealtimeMarkErrors(JTextComponent textComponent)
  {
    startRealtimeMarkErrors(textComponent, new DocumentWordFinder()) ;
  }
  
  /**
   * Mark current errors (not while you are typing) of the JTextComponent.
   * You must call unMarkErrors to free resources when you don't need the marks.
   *
   * @param textComponent
   * @param wordFinder
   */
  public void startRealtimeMarkErrors(JTextComponent textComponent, DocumentWordFinder wordFinder)
  {
    Document doc = textComponent.getDocument() ;
    
    wordFinder.setDocument(doc) ;
    
    if (null == realTimeSpellChecker)
    {
      realTimeSpellChecker = new RealTimeSpellChecker(spellChecker) ;
      realTimeSpellChecker.start() ;
    }
    
    realTimeSpellChecker.addTextComponent(textComponent, wordFinder) ;
    markInManagers(realTimeSpellChecker.getErrorMarker(textComponent)) ;
    
    textComponent.addPropertyChangeListener(DESTROY_PROPERTY, automaticStopListener);
    textComponent.addPropertyChangeListener(DOCUMENT_PROPERTY, docChangeListener) ;
    
    eventMulticaster.getMulticaster().realTimeStart(new TextComponentSpellCheckerEvent(this, textComponent)) ;
  }
  
  public void stopRealtimeMarkErrors(JTextComponent textComponent)
  {
    stopRealtimeMarkErrors(textComponent.getDocument(), textComponent, true) ;
  }
  
  DocumentWordFinder stopRealtimeMarkErrors(Document doc, JTextComponent textComponent, boolean stopIfNoEditor)
  {
    DocumentWordFinder result = null ;
    
    if (isRealtimeMarkErrors(doc, textComponent))
    {
      TextComponentSpellCheckerEvent evt = new TextComponentSpellCheckerEvent(this, textComponent) ;
      eventMulticaster.getMulticaster().realTimeWillStop(evt) ;
      
      unMarkInManagers(realTimeSpellChecker.getErrorMarker(doc, textComponent)) ;
      result = realTimeSpellChecker.removeTextComponent(doc, textComponent) ;
      textComponent.removePropertyChangeListener(DESTROY_PROPERTY, automaticStopListener);      
      textComponent.removePropertyChangeListener(DOCUMENT_PROPERTY, docChangeListener) ;
      
      eventMulticaster.getMulticaster().realTimeStop(evt) ;
      
      if (stopIfNoEditor && realTimeSpellChecker.isEmpty())
      {
        realTimeSpellChecker.stop();
        realTimeSpellChecker = null ;
      }
    }
    
    return result ;
  }
  
  public void stopRealtimeMarkErrors()
  {
    if (null != realTimeSpellChecker)
    {
      for (ErrorMarker errorMarker : realTimeSpellChecker.getErrorMarkers())
      {
        JTextComponent textComponent = errorMarker.getTextComponent() ;
        TextComponentSpellCheckerEvent evt = new TextComponentSpellCheckerEvent(this, textComponent) ;        
        eventMulticaster.getMulticaster().realTimeWillStop(evt) ;
        
        unMarkInManagers(errorMarker) ;
        realTimeSpellChecker.removeTextComponent(textComponent) ;
        
        eventMulticaster.getMulticaster().realTimeStop(evt) ;        
      }

      realTimeSpellChecker.stop() ;
      realTimeSpellChecker = null ;
    }
  }

  protected void markInManagers(ErrorMarker errorMarker)
  {
    if (null != errorToolTips)
      errorToolTips.addErrorMarker(errorMarker) ;
    
    if (null != errorPopUpMenu)
      errorPopUpMenu.addErrorMarker(errorMarker) ;
  }
  
  protected void unMarkInManagers(ErrorMarker errorMarker)
  {
    if (null != errorToolTips)
      errorToolTips.removeErrorMarker(errorMarker) ;
    
    if (null != errorPopUpMenu)
      errorPopUpMenu.removeErrorMarker(errorMarker) ;
  }
  
  
  public ErrorToolTips getErrorToolTips()
  {
    return errorToolTips;
  }
  
  /**
   * Change the tooltip to show when mouse is over an error. It try to preserve original ones.
   * 
   * @param errorToolTips It can be null, in that case no tooltip is show.
   */
  public void setErrorToolTips(ErrorToolTips errorToolTips)
  {
    this.errorToolTips = errorToolTips;
  }

  public ErrorPopUpMenu getErrorPopUpMenu()
  {
    return errorPopUpMenu;
  }

  /**
   * Change the popupmenu to show when mouse is over an error. It try to preserve original ones.
   * 
   * @param errorToolTips It can be null, in that case no popupmenu is show.
   */
  public void setErrorPopUpMenu(ErrorPopUpMenu errorPopUpMenu)
  {
    this.errorPopUpMenu = errorPopUpMenu;
  }


  public ActionManager getActionManager() {
    return actionManager ;
  }

  public void setActionManager(ActionManager actionManager) {
    this.actionManager = actionManager ;
  }



  public void recheckAll(JTextComponent textComponent)  
  {
    if (isRealtimeMarkErrors(textComponent))
      realTimeSpellChecker.addTextRange(textComponent.getDocument()) ;
  }
  
  public void recheckErrors(JTextComponent textComponent)
  {
    if (null != realTimeSpellChecker)
    {
      ErrorMarker errorMarker = realTimeSpellChecker.getErrorMarker(textComponent) ;
      
      if (null != errorMarker)
      {
        Document doc = errorMarker.getTextComponent().getDocument() ;
        
        for (ErrorInfo error : errorMarker.getAllErrorInfo()) 
        {
          Word badWord = error.getBadWord() ;
          realTimeSpellChecker.addTextRange(doc, badWord.getStart(), badWord.length()) ;
        }
      }
    }
  }
  
  public void addWordToDictionary(JTextComponent textComponent, String badWord, SpellDictionary dictionary)
  {
    try 
    {
      dictionary.addWord(badWord);
      recheckErrors(textComponent) ;
    }
    catch (SpellDictionaryException ex) 
    {
      ErrorMsgBox.show(ex) ;
    }
  }

  public void ignoreWord(JTextComponent textComponent, String badWord)
  {
    spellChecker.addIgnore(badWord) ;
    recheckErrors(textComponent) ;       
  }

  // NOTE : this functions only replace in current errors.
  public void replaceAllBadWords(JTextComponent textComponent, String badWord, String newWord)
  {
    spellChecker.addReplace(badWord, newWord) ;
    recheckErrors(textComponent) ;       
  }
  
  public void replaceBadWord(JTextComponent textComponent, Word badWord, String suggestion)
  {
    int end = badWord.getEnd() ;
    int start = badWord.getStart() ;

    if (suggestion.isEmpty())
      --start ;

    textComponent.select(start, end) ;
    textComponent.replaceSelection(suggestion) ;
  }

  public void deleteBadWord(JTextComponent textComponent, Word badWord)
  {
    int end = badWord.getEnd() ;
    int start = badWord.getStart() - 1 ;

    textComponent.select(start, end) ;
    textComponent.replaceSelection("") ;
  }



  /**
   * @return the addWordAction
   */
  public AddWordAction getAddWordAction() {
    return actionManager.getAddWordAction(this) ;
  }

  /**
   * @return the ignoreWordAction
   */
  public IgnoreWordAction getIgnoreWordAction() {
    return actionManager.getIgnoreWordAction(this) ;
  }

  /**
   * @return the deleteWordAction
   */
  public DeleteWordAction getDeleteWordAction() {
    return actionManager.getDeleteWordAction(this) ;
  }

  /**
   * @return the spellCheckAction
   */
  public SpellCheckAction getSpellCheckAction() {
    return actionManager.getSpellCheckAction(this) ;
  }

  /**
   * @return the realTimeSpellCheckAction
   */
  public RealTimeSpellCheckAction getRealTimeSpellCheckAction() {
    return actionManager.getRealTimeSpellCheckAction(this) ;
  }

  public ReplaceWordAction getReplaceWordAction() {
    return actionManager.getReplaceWordAction(this) ;
  }

  public ReplaceAllWordsAction getReplaceAllWordsAction() {
    return actionManager.getReplaceAllWordsAction(this) ;
  }

  public ReplaceWordAction getReplaceWordAction(String sugestion) {
    return actionManager.getReplaceWordAction(this, sugestion) ;
  }
}