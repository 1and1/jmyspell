/*
 * Created on 04/03/2005
 *
 */
package org.dts.spell.swing.utils;

import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Highlighter;
import org.dts.spell.ErrorInfo;

/**
 * @author DreamTangerine
 * NOTE : Tags are close at begin and open at end,for example the interval is [6, 11).
 */
public class TagList
{
  public static final String FULL_PROPERTY = "FULL_PROPERTY" ;
  public static final String NUM_ERRORS_PROPERTY = "NUM_ERRORS_PROPERTY" ;
  public static final String FIRST_ERROR_PROPERTY = "FIRST_ERROR_PROPERTY" ;
  public static final String LAST_ERROR_PROPERTY = "LAST_ERROR_PROPERTY" ;
  public static final String CURRENT_ERROR_PROPERTY = "CURRENT_ERROR_PROPERTY" ;
  public static final String PREVIOUS_ERROR_PROPERTY = "PREVIOUS_ERROR_PROPERTY" ;
  public static final String NEXT_ERROR_PROPERTY = "NEXT_ERROR_PROPERTY" ;  
  
  private class StateChecker
  {
    private int nErrors ;
    private ErrorInfo firstError ;
    private ErrorInfo lastError ;
    private ErrorInfo caretError ;
    private ErrorInfo previousError ;
    private ErrorInfo nextError ;
    private boolean full ;
    
    public void init()
    {
      nErrors = TagList.this.nErrors ;
      firstError = getFirstError() ;
      lastError = getLastError() ;
      caretError = getCurrentError() ;
      previousError = getPreviousError() ;
      nextError = getNextError() ;
      full = isFull() ;
    }
    
    public void check()
    {
      if (TagList.this.nErrors < 0)
        throw new IllegalStateException("Number of nodes < 0") ;
      
      propertyChangeSupport.firePropertyChange(NUM_ERRORS_PROPERTY, nErrors,  TagList.this.nErrors) ;
      propertyChangeSupport.firePropertyChange(FULL_PROPERTY, full,  isFull()) ;
      propertyChangeSupport.firePropertyChange(FIRST_ERROR_PROPERTY, firstError, getFirstError()) ;
      propertyChangeSupport.firePropertyChange(LAST_ERROR_PROPERTY, lastError, getLastError()) ;
      propertyChangeSupport.firePropertyChange(CURRENT_ERROR_PROPERTY, caretError, getCurrentError()) ;
      propertyChangeSupport.firePropertyChange(PREVIOUS_ERROR_PROPERTY, previousError, getPreviousError()) ;      
      propertyChangeSupport.firePropertyChange(NEXT_ERROR_PROPERTY, nextError, getNextError()) ;      
    }
  }
  
  private static class TagNode
  {
    public TagNode(Highlighter.Highlight tag, ErrorInfo info)
    {
      this(tag, null, null, info) ;
    }

    public TagNode(Highlighter.Highlight tag, TagNode next, ErrorInfo info)
    {
      this(tag, null, next, info) ;
    }
    
    public TagNode(Highlighter.Highlight tag, TagNode previous, TagNode next, ErrorInfo info)
    {
      this.tag = tag ;
      setNext(next) ;
      setPrevious(previous) ;
      this.errorInfo = info ;
    }
    
    public TagNode getNext()
    {
      return next ;
    }
    
    public void setNext(TagNode node)
    {
      next = node ;
    }
    
    public TagNode getPrevious()
    {
      return prev ;
    }
    
    public void setPrevious(TagNode node)
    {
      prev = node ;
    }
    
    private TagNode next ;
    private TagNode prev ;

    public Highlighter.Highlight getTag()
    {
      return tag ;
    }

    public ErrorInfo getErrorInfo()
    {
      // update word position.
      errorInfo.getBadWord().moveTo(tag.getStartOffset()) ;

      return errorInfo ;
    }
    
    @Override
    public String toString()
    {
     return "(" + tag.getStartOffset() + "," + tag.getEndOffset() + ")" ; 
    }
    
    /**
     * The current Highlighter.Highlight. In fact is the previous iterator. That
     * is iterator.previous() == current.
     * 
     * <b>NOTE</b> : The use of Highlighter.Highlight is not documented in the
     * JDK. It known that highlighter.addHighlight return this class by the
     * inspect of the <em>SUN</em> code. So it may change in the future.
     */
    private Highlighter.Highlight tag ;
    private ErrorInfo errorInfo ;
  }
  
  private PropertyChangeSupport propertyChangeSupport ;
  
  public TagList(PropertyChangeSupport propertyChangeSupport)
  {
    this.propertyChangeSupport = propertyChangeSupport ;
    clear() ;
  }
  
  public boolean isEmpty()
  {
    return null == first ;
  }
  
  public void clear()
  {
    first = null ; 
    last = null ;
    current = null ;
    nErrors = 0 ;
  }

  /**
   * Search the TagNode with that tag.
   * 
   * @param tag the tag to look for.
   * @return The TagNode or null if is not found.
   */
  private TagNode findTagNode(Highlighter.Highlight tag)
  {
    TagNode node = findNearTagNode((tag.getStartOffset() + tag.getEndOffset()) / 2) ;    
    Highlighter.Highlight curTag = node.getTag() ;
      
    if (tag == curTag)
      return node ;
    else 
      return null ;
  }
  
  public void add(Object tag, ErrorInfo info)
  {
    stateChecker.init() ;
    add((Highlighter.Highlight) tag, info) ;
    stateChecker.check() ;
  }
  
  private void addFirstAndLastTagNode(Highlighter.Highlight tag, ErrorInfo info)
  {
    first = new TagNode(tag, info) ;
    last = first ;
    current = first ;
  }

  private void addMiddleTagNode(Highlighter.Highlight tag, ErrorInfo info, TagNode node)
  {
    current = new TagNode(tag, node, node.getNext(), info) ;
    node.getNext().setPrevious(current) ;    
    node.setNext(current) ;
  }
  
  private void addFirstTagNode(Highlighter.Highlight tag, ErrorInfo info)
  {
    current = new TagNode(tag, first, info) ;
    first.setPrevious(current) ;
    first = current ;
  }

  private void addLastTagNode(Highlighter.Highlight tag, ErrorInfo info)
  {
    current = new TagNode(tag, last, null, info) ;
    last.setNext(current) ;
    last = current ;
  }
  
  private void add(Highlighter.Highlight tag, ErrorInfo info)
  {
    if (!isEmpty())
    {
      TagNode node = findPreviousNode((tag.getStartOffset() + tag.getEndOffset()) / 2) ;
      //findNearTagNode((tag.getStartOffset() + tag.getEndOffset()) / 2) ;    
      //Highlighter.Highlight curTag = node.getTag() ;
      
      if (null == node)
        addFirstTagNode(tag, info) ;
      else if (last == node)
        addLastTagNode(tag, info) ;
      else
        addMiddleTagNode(tag, info, node) ;
    }
    else
      addFirstAndLastTagNode(tag, info) ;
    
    ++nErrors ;
  }

  public void remove(Object tag)
  {
    remove((Highlighter.Highlight) tag) ;
  }

  private void removeFirstTagNode(TagNode node)
  {
    first = node.getNext() ;
    first.setPrevious(null) ;
    current = first ;
  }

  private void removeFisrtAndLastTagNode()
  {
    first = null ;
    last = null ;
    current = null ;          
  }

  private void removeLastTagNode(TagNode node)
  {
    last = node.getPrevious() ;
    last.setNext(null) ;
    current = last ;
  }
  
  private void removeTagNode(TagNode node)
  {
    TagNode prev = node.getPrevious() ;

    prev.setNext(node.getNext()) ;
    node.getNext().setPrevious(prev) ;
    current = prev ;
  }
  
  private void remove(Highlighter.Highlight tag)
  {
    if (!isEmpty())
    {
      TagNode node = findTagNode(tag) ;
      
      if (null != node)
      {
        if (first == node)
        {
          if (last != node)
            removeFirstTagNode(node) ;
          else
            removeFisrtAndLastTagNode() ;
        }      
        else if (last == node)
          removeLastTagNode(node) ;
        else
          removeTagNode(node) ;
      }
      
      --nErrors ;
    }
  }

  private boolean isInside(int beginPos, int endPos)
  {
    if (isEmpty())
      return false ;
    else
    {
      return first.getTag().getStartOffset() <= endPos && 
             last.getTag().getEndOffset() >= beginPos ; 
    }
  }
  
  public void removeAll(Highlighter highlighter)
  {
    while (null != first)
    {
      highlighter.removeHighlight(first.getTag()) ;
      first = first.getNext() ; 
    }
    
    last = null ;
    current = null ;
    nErrors = 0 ;
  }

  private TagNode removeNullRange(TagNode begin, Highlighter highlighter)
  {
    // Delete until no null range
    while (null != begin && HighlightUtils.isNullRange(begin.getTag()))
    {
      highlighter.removeHighlight(begin.getTag()) ;
      begin = begin.getNext() ;
      --nErrors ;
    }
    
    return begin ;
  }

  private TagNode removeRangeTo(TagNode begin, Highlighter highlighter, int endPos)
  {
    // Delete until no endPos
    while (null != begin && !HighlightUtils.isMajor(begin.getTag(), endPos))
    {
      highlighter.removeHighlight(begin.getTag()) ;
      begin = begin.getNext() ;
      --nErrors ;
    }

    return begin ;
  }
  
  public void removeRange(int beginPos, int endPos, Highlighter highlighter)
  {
    if (isInside(beginPos, endPos))
    {
      stateChecker.init() ;
      
      if (HighlightUtils.isMajorOrEquals(first.getTag(), beginPos) && HighlightUtils.isMinorOrEquals(last.getTag(), endPos))
        removeAll(highlighter) ;
      else
      {
        TagNode prev = findPreviousNode(beginPos) ;
        TagNode next ;
        
        if (null == prev)
        {
          // Delete the next to first node
          if (HighlightUtils.isInside(first.getTag(), beginPos - 1))
          {
            next = removeRangeTo(first.getNext(), highlighter, endPos) ;
            prev = first ;
          }
          else // Delete first node
          {
            next = removeRangeTo(first, highlighter, endPos) ;
            first = next ;
            first.setPrevious(null) ;
            prev = next ;
            next = prev.getNext() ;
          }
        }
        else
          next = removeRangeTo(prev.getNext(), highlighter, endPos) ;
           
        if (null != next)
        {
          prev.setNext(next) ;
          next.setPrevious(prev) ;
        }
        else
        {
          last = prev ;
          prev.setNext(null) ;
        }
        
        current = prev ;
      }
      
      stateChecker.check();
    }
  }
  
  public void removeNullRanges(int beginPos, int endPos, Highlighter highlighter)
  {
    if (isInside(beginPos, endPos))
    {
      stateChecker.init() ;
      
      if (HighlightUtils.isMajorOrEquals(first.getTag(), beginPos) && HighlightUtils.isMinorOrEquals(last.getTag(), endPos))
        removeAll(highlighter) ;
      else
      {
        TagNode prev = findPreviousNode(beginPos) ;
        TagNode next ;
        
        if (null == prev)
        {
          // Delete the next to first node
          if (HighlightUtils.isInside(first.getTag(), beginPos - 1))
          {
            next = removeNullRange(first.getNext(), highlighter) ;
            prev = first ;
          }
          else // Delete first node
          {
            next = removeNullRange(first, highlighter) ;
            first = next ;
            first.setPrevious(null) ;
            prev = next ;
            next = prev.getNext() ;
          }
        }
        else
          next = removeNullRange(prev.getNext(), highlighter) ;
           
        if (null != next)
        {
          prev.setNext(next) ;
          next.setPrevious(prev) ;
        }
        else
        {
          last = prev ;
          prev.setNext(null) ;
        }
        
        current = prev ;
      }

      stateChecker.check();
    }
  }
  
  public void updateCurrent(int curPos)
  {
    stateChecker.init();
    
    current = findNearTagNode(curPos) ;
    caretPosition = curPos ;

    stateChecker.check();
  }
  
  private TagNode findFirstLastOrCurrentNearNode(int curPos)
  {
    TagNode result = current ;
    
    int delta = HighlightUtils.getDeltaFromStart(result.getTag(), curPos) ;

    if (delta > 0)
    {
      if (HighlightUtils.getDeltaFromEnd(curPos, first.getTag()) < delta) 
        result = first ;
    }
    else 
    {
      delta = HighlightUtils.getDeltaFromEnd(curPos, result.getTag()) ;

      if (HighlightUtils.getDeltaFromStart(last.getTag(), curPos) < delta)
        result = last ;
    }

    return result ;
  }
  
  // Find near node (this don't skip null ranges)
  private TagNode findNearTagNode(int curPos)
  {
    TagNode result = current ;
    
    if (!isEmpty())
    {
      if (HighlightUtils.isMajor(first.getTag(), curPos))
        result = first ;
      else if (HighlightUtils.isMinor(last.getTag(), curPos))
        result = last ;
      else
      {
        result = findFirstLastOrCurrentNearNode(curPos) ;
        
        while (null != result && HighlightUtils.isMinor(result.getTag(), curPos))
          result = result.getNext() ;

        while (null != result && HighlightUtils.isMajor(result.getTag(), curPos))
          result = result.getPrevious() ;
        
        if (null == result)
          result = first ;
      }
    }
    
    return result ;
  }

  // Find previous node (this skip null ranges)
  private TagNode findPreviousNode(int index)
  {
    TagNode result = findFirstLastOrCurrentNearNode(index) ;
    
    if (HighlightUtils.isMajorOrEquals(result.getTag(), index)) 
    {
      while (null != result && HighlightUtils.isMajorOrEquals(result.getTag(), index))
        result = result.getPrevious() ;
    }
    else
    {
      TagNode prev = result ;
      
      while (null != result && HighlightUtils.isMinor(result.getTag(), index))
      {
        prev = result ;
        result = result.getNext() ;
      }
      
      result = prev ;
    }
    
    while (null != result && HighlightUtils.isNullRange(result.getTag()))
      result = result.getPrevious() ; 
    
    return result ;
  }
  
  public boolean hasTagAt(int curPos)
  {
    boolean result = false ;
    
    if (!isEmpty())
    {
      current = findNearTagNode(curPos) ;
      result = HighlightUtils.isUpOpenInside(current.getTag(), curPos) ;
    }
    
    return result ;
  }
  
  public ErrorInfo getErrorInfoAt(int curPos)
  {
    ErrorInfo result = null ;
    
    if (hasTagAt(curPos))
      result = current.getErrorInfo() ;
        
    return result ;
  }

  public List<ErrorInfo> getAllErrorInfo()
  {
    TagNode node = first ;
    List<ErrorInfo> result = new LinkedList<ErrorInfo>() ;
            
    while (null != node)
    {
      result.add(node.getErrorInfo()) ;
      node = node.getNext() ;
    }
    
    return result ;
  }

  public static int getMaxNumOfErrors() {
    return maxNumOfErrors;
  }

  public static void setMaxNumOfErrors(int aMaxNumOfErrors) {
    maxNumOfErrors = aMaxNumOfErrors;
  }
  
  public boolean isFull() 
  {
    return getNumOfErrors() >= getMaxNumOfErrors() ;
  }
  
  public int getNumOfErrors()
  {
    return nErrors ;
  }

  private ErrorInfo getErrorInfo(TagNode node)
  {
    if (node != null)
      return node.getErrorInfo() ;
    else
      return null ;
  }
  
  public ErrorInfo getFirstError()
  {
    return getErrorInfo(first) ;
  }
  
  public ErrorInfo getLastError()
  {
    return getErrorInfo(last) ;
  }
  
  public ErrorInfo getCurrentError()
  {
    return getErrorInfoAt(caretPosition) ;
  }
  
  public ErrorInfo getPreviousError()
  {
    TagNode node = null ;
    
    if (caretPosition >= 0 && !isEmpty())
    {
      node = findNearTagNode(caretPosition) ;
      
      if (!HighlightUtils.isMinorOrEquals(node.getTag(), caretPosition))
        node = node.getPrevious() ;
    }

    return getErrorInfo(node) ;
  }

  public ErrorInfo getNextError()
  {
    TagNode node = null ;
    
    if (caretPosition >= 0 && !isEmpty())
    {
      node = findNearTagNode(caretPosition) ;
      
      if (!HighlightUtils.isMajor(node.getTag(), caretPosition))
        node = node.getNext() ;
    }

    return getErrorInfo(node) ;
  }

  @Override
  public String toString()
  {
    TagNode aux = first ;
    String result = String.format("N nodes %d ", nErrors) ;
    
    while (aux != null)
    {
      result += aux + "\n" ;
      aux = aux.getNext() ;
    }
    
    result += " Current = " + current ;
      
    return result ;
  }
  
  private TagNode first ; 
  private TagNode last ;
  
  /**
   * Recent used node. It is the recent used node, it is updated when add, remove o call
   * updateCurrent. It is a cache node.  
   */
  private TagNode current ;

  private int caretPosition = -1 ;
  
  /**
   * number of nodes to limits numebr of errors.
   */
  private int nErrors ;

  /**
   * Max num of errors by list.
   */
  private static int maxNumOfErrors = 2500 ;
  
  private StateChecker stateChecker = new StateChecker() ;
}
