/*
 * Created on 25/02/2005
 *
 */
package org.dts.spell.swing ;

import org.dts.spell.swing.utils.*;
import java.awt.Color ;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.event.CaretEvent ;
import javax.swing.event.CaretListener ;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException ;
import javax.swing.text.Document;
import javax.swing.text.Highlighter ;
import javax.swing.text.JTextComponent ;
import javax.swing.text.Highlighter.Highlight ;
import org.dts.spell.ErrorInfo;

/**
 * This class has all the error marks (the red lines) for a JTextComponent.
 * If all the marks are removed and isAutoQuit() is true (by default is false)
 * the ErrorMarker call quitTextComponent().
 *
 * @author DreamTangerine
 *
 */
public class ErrorMarker
{
  public static final String FULL_PROPERTY = TagList.FULL_PROPERTY ;
  public static final String FIRST_ERROR_PROPERTY = TagList.FIRST_ERROR_PROPERTY ;
  public static final String LAST_ERROR_PROPERTY = TagList.LAST_ERROR_PROPERTY ;
  public static final String CURRENT_ERROR_PROPERTY = TagList.CURRENT_ERROR_PROPERTY ;
  public static final String PREVIOUS_ERROR_PROPERTY = TagList.PREVIOUS_ERROR_PROPERTY ;
  public static final String NEXT_ERROR_PROPERTY = TagList.NEXT_ERROR_PROPERTY ;
  
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this) ;
  
  private JTextComponent textComponent = null ;
  
  private Highlighter highlighter ;
  
  private ErrorHighlighterPainter errorHighlighterPainter = new ErrorHighlighterPainter() ;
  
  /**
   * A list to the current tag returned by highlighter.addHighlight. This tag
   * are in fact Highlighter.Highlight.
   *
   * <b>NOTE</b> : The use of Highlighter.Highlight is not documented in the
   * JDK. It known that highlighter.addHighlight return this class by the
   * inspect of the <em>SUN</em> code. So it may change in the future.
   */
  private TagList errorList = new TagList(propertyChangeSupport) ;
  
  private TagSynchronizer caretListener = new TagSynchronizer() ;
  
  /**
   * This flag, when set to true, allow automatic remove of caretListener if
   * errorList is empty.
   */
  private boolean autoQuitSynchronizer = false ;
  
  public static ErrorMarker get()
  {
    return new ErrorMarker() ;
  }
  
  public static ErrorMarker get(JTextComponent textComp)
  {
    return new ErrorMarker(textComp) ;
  }
  
  public static ErrorMarker get(JTextComponent textComp, boolean selectError)
  {
    return new ErrorMarker(textComp, selectError) ;
  }
  
  private ErrorMarker()
  {
    this(null, true) ;
  }
  
  private ErrorMarker(JTextComponent textComp)
  {
    this(textComp, true) ;
  }
  
  private ErrorMarker(JTextComponent textComp, boolean selectError)
  {
    setTextComponent(textComp) ;
    setSelectError(selectError) ;
  }
  
  public boolean isActive()
  {
    return null != textComponent && !isFull() ;
  }
  
  public void setErrorMarkColor(Color color)
  {
    errorHighlighterPainter.setErrorColor(color) ;
  }
  
  public Color getErrorMarkColor()
  {
    return errorHighlighterPainter.getErrorColor() ;
  }
  
  /**
   * @return Returns the selectError.
   */
  public boolean isSelectError()
  {
    return errorHighlighterPainter.isHighlightBackground() ;
  }
  
  /**
   * @param selectError
   *          The selectError to set.
   */
  public void setSelectError(boolean selectError)
  {
    errorHighlighterPainter.setHighlightBackground(selectError) ;
  }
  
  public void setTextComponent(JTextComponent textComp)
  {
    if (textComponent != textComp)
    {
      quitTextComponent() ;
      textComponent = textComp ;

      if (null != textComponent)
      {
        textComponent.addCaretListener(caretListener) ;
        textComponent.getDocument().addDocumentListener(caretListener) ;

        errorList.updateCurrent(textComponent.getCaretPosition()) ;

        highlighter = textComponent.getHighlighter() ;
      }
      else
        highlighter = null ;
    }
  }
  
  public JTextComponent getTextComponent()
  {
    return textComponent ;
  }

  void quitTextComponent(Document doc)
  {
    if (null != textComponent)
    {
      unMarkAllErrors() ;
      
      textComponent.removeCaretListener(caretListener) ;
      
      textComponent = null ;
      highlighter = null ;
    }
    
    doc.removeDocumentListener(caretListener) ;
  }
  
  public void quitTextComponent()
  {
    if (null != textComponent)
      quitTextComponent(textComponent.getDocument()) ;
  }

  public boolean hasError(int position)
  {
    return errorList.hasTagAt(position) ;
  }
  
  public ErrorInfo getErrorInfo(int position)
  {
    return errorList.getErrorInfoAt(position) ;
  }
  
  public Object markError(int start, int end, ErrorInfo info, boolean scroll) throws BadLocationException
  {
    Object tag = highlighter.addHighlight(start, end, errorHighlighterPainter) ;
    
    errorList.add(tag, info) ;
    
    if (scroll)
      textComponent.setCaretPosition(start) ;
    
    return tag ;
  }
  
  public Object markError(int start, int end, ErrorInfo info) throws BadLocationException
  {
    return markError(start, end, info, false) ;
  }
  
  public void unMarkError(Object tag)
  {
    highlighter.removeHighlight(tag) ;
    errorList.remove(tag) ;
  }
  
  public void unMarkRange(int begin, int end)
  {
    errorList.removeRange(begin, end, highlighter) ;
  }
  
  public void unMarkAllErrors()
  {
    errorList.removeAll(highlighter) ;
    errorList.clear();
  }
  
  public boolean isAutoQuit()
  {
    return autoQuitSynchronizer ;
  }
  
  public void setAutoQuit(boolean auto)
  {
    this.autoQuitSynchronizer = auto ;
  }
  
  public List<ErrorInfo> getAllErrorInfo()
  {
    return errorList.getAllErrorInfo() ;
  }
  public int getErrorCount()
  {
    return errorList.getNumOfErrors() ;
  }
  
  public boolean isFull()
  {
    return errorList.isFull() ;
  }
  
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener(listener) ;    
  }

  public void addPropertyChangeListener(String name, PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener(name, listener) ;    
  }
  
  public void removePropertyChangeListener(PropertyChangeListener listener)  
  {
    propertyChangeSupport.removePropertyChangeListener(listener) ;    
  }

  public void removePropertyChangeListener(String name, PropertyChangeListener listener)  
  {
    propertyChangeSupport.removePropertyChangeListener(name, listener) ;
  }

  public PropertyChangeListener[] getPropertyChangeListeners()
  {
    return propertyChangeSupport.getPropertyChangeListeners() ;
  }
  
  public PropertyChangeListener[] getPropertyChangeListeners(String propertyName)
  {
    return propertyChangeSupport.getPropertyChangeListeners(propertyName) ;
  }

  public ErrorInfo getFirstError()
  {
    return errorList.getFirstError() ;
  }
  
  public ErrorInfo getLastError()
  {
    return errorList.getLastError() ;
  }
  
  public ErrorInfo getCurrentError()
  {
    return errorList.getCurrentError() ;
  }
  
  public ErrorInfo getPreviousError()
  {
    return errorList.getPreviousError() ;
  }

  public ErrorInfo getNextError()
  {
    return errorList.getNextError() ;
  }
  
  @Override
  public String toString()
  {
    Highlight[] hl = highlighter.getHighlights() ;
    
    String result = "Nº Highlight " + hl.length + "\n" ;
    
    for (int i = 0 ; i < hl.length ; ++i)
    {
      if (hl[i].getPainter() == errorHighlighterPainter)
        result += "*" ;
      
      result += hl[i] + "\n" ;
    }
    
    return result ;
  }
  
  private class TagSynchronizer implements CaretListener, DocumentListener
  {
    public void caretUpdate(CaretEvent e)
    {
      errorList.updateCurrent(e.getDot()) ;
    }
    
    public void insertUpdate(DocumentEvent e)
    {
    }
    
    public void removeUpdate(DocumentEvent e)
    {
      int os = e.getOffset() ;
      
      errorList.removeNullRanges(os, os + e.getLength(), highlighter) ;
      
      if (errorList.isEmpty() && isAutoQuit())
        quitTextComponent() ;
    }
    
    public void changedUpdate(DocumentEvent e)
    {
    }
  }
}
