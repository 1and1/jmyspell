package org.dts.spell.swing ;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import java.util.ListIterator;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent ;

import org.dts.spell.SpellChecker ;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.event.RealTimeSpellCheckerListener;
import org.dts.spell.swing.finder.DocumentWordFinder ;
import org.dts.spell.swing.finder.SynchronizedWordFinder;
import org.dts.spell.swing.utils.TextRange;

// NOTE : All public functions must be called from EDT.
public class RealTimeSpellChecker implements DocumentListener, Runnable
{
  private SpellChecker spellChecker ;

  private static class TextComponentAndFinder
  {
    public TextComponentAndFinder(JTextComponent textComponent, DocumentWordFinder documentFinder)
    {
      errorMarker = ErrorMarker.get(textComponent, false) ;
      finder = new SynchronizedWordFinder(documentFinder) ;
    }
    
    public ErrorMarker getErrorMarker()
    {
      return errorMarker ;
    }
    
    public SynchronizedWordFinder getWordFinder()
    {
      return finder ;
    }
    
    public JTextComponent getTextComponent()
    {
      return errorMarker.getTextComponent() ;
    }
    
    private SynchronizedWordFinder finder ;
    private ErrorMarker errorMarker ;
  }
  
  private List<TextRange> textRanges = new LinkedList<TextRange>() ;
  private Map<Document, List<TextComponentAndFinder>>  activeDocuments = new HashMap<Document, List<TextComponentAndFinder>>() ;
  
  private volatile boolean stopThread ;
  
  private RealTimeSpellCheckerListener listener ;
  
  public RealTimeSpellChecker(SpellChecker checker)
  {
    spellChecker = checker ;
  }
  
  public void addTextComponent(JTextComponent textComponent, DocumentWordFinder finder)
  {
    Document doc = textComponent.getDocument() ;
    
    List<TextComponentAndFinder> list = activeDocuments.get(doc) ;
    
    if (null == list)
    {
      list = new LinkedList<TextComponentAndFinder>() ;
      activeDocuments.put(doc, list) ;
      doc.addDocumentListener(this) ;
    }
    
    list.add(new TextComponentAndFinder(textComponent, finder)) ;
    
    addTextRange(doc) ; 
  }

  public void removeTextComponent(JTextComponent textComponent)
  {
    removeTextComponent(textComponent.getDocument(), textComponent) ;
  }
    
  DocumentWordFinder removeTextComponent(Document doc, JTextComponent textComponent)
  {
    List<TextComponentAndFinder> list = activeDocuments.get(doc) ;
    DocumentWordFinder result = null ;
    
    if (null != list)
    {
      ListIterator<TextComponentAndFinder> it = list.listIterator() ;
      
      while (it.hasNext())
      {
        TextComponentAndFinder txtCmpFnd = it.next() ;
                
        if (txtCmpFnd.getTextComponent() == textComponent)
        {
          it.remove() ;
          txtCmpFnd.getErrorMarker().quitTextComponent(doc) ; 
          result = txtCmpFnd.getWordFinder().getWrapDocumentFinder() ;
        }
      }
      
      if (list.isEmpty())
      {
        activeDocuments.remove(doc) ;
        doc.removeDocumentListener(this) ;
      }
    }
    
    return result ;
  }

  public List<ErrorMarker> getErrorMarkers()
  {
    List<ErrorMarker> list = new LinkedList<ErrorMarker>() ;
    
    for (List<TextComponentAndFinder> txtCmpList : activeDocuments.values())
      for (TextComponentAndFinder txtCmpFnd : txtCmpList)
        list.add(txtCmpFnd.getErrorMarker()) ;
      
    return list ;
  }
  
  public boolean isEmpty()
  {
    return activeDocuments.isEmpty() ;
  }
  
  private TextComponentAndFinder getTextComponentAndFinder(JTextComponent textComponent)
  {
    return getTextComponentAndFinder(textComponent.getDocument(), textComponent) ;
  }
    
  private TextComponentAndFinder getTextComponentAndFinder(Document doc, JTextComponent textComponent)
  {
    List<TextComponentAndFinder> list = activeDocuments.get(doc) ;

    if (null != list)
      for (TextComponentAndFinder txtFnd : list)
        if (txtFnd.getTextComponent() == textComponent)
          return txtFnd ;
    
    return null ;
  }
  
  public ErrorMarker getErrorMarker(JTextComponent textComponent)
  {
    return getErrorMarker(textComponent.getDocument(), textComponent) ;
  }
  
  ErrorMarker getErrorMarker(Document doc, JTextComponent textComponent)          
  {
    TextComponentAndFinder txtCmpFnd = getTextComponentAndFinder(doc, textComponent) ;

    if (null != txtCmpFnd)
      return txtCmpFnd.getErrorMarker() ;
    else
      return null ;
  }
  
  public DocumentWordFinder getWordFinder(JTextComponent textComponent)
  {
    TextComponentAndFinder txtCmpFnd = getTextComponentAndFinder(textComponent) ;

    if (null != txtCmpFnd)
      return txtCmpFnd.getWordFinder().getWrapDocumentFinder() ;
    else
      return null ;
  }

  public boolean isRealTimeSpellChecking(JTextComponent textComponent)
  {
    return isRealTimeSpellChecking(textComponent.getDocument(), textComponent) ;
  }

  boolean isRealTimeSpellChecking(Document doc, JTextComponent textComponent)
  {
    return getTextComponentAndFinder(doc, textComponent) != null ;
  }
  
  private Thread thread ;
  
  public synchronized void start()
  {
    assert null == thread ;
    
    stopThread = false ;
    
    listener = new RealTimeSpellCheckerListener() ;
    
    thread = new Thread(this) ;
    thread.setName("Spell-checker") ;
    
    thread.setPriority(Thread.MIN_PRIORITY) ;
    thread.start() ;
  }
  
  public void stop()
  {
    assert null != thread ;
    
    synchronized (this)
    {
      stopThread = true ;
      notify() ;
    }
    
    try
    {
      // wait the end of the thread.
      thread.join() ;
      thread = null ;
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }
  
  public void addTextRange(Document document)  
  {
    addTextRange(document, 0, document.getLength()) ;
  }
  
  public void addTextRange(final Document document, final int begin, final int length)
  {
    addTextRange(new DocumentEvent() {

      public int getOffset() {
        return begin ;
      }

      public int getLength() {
        return length ;
      }

      public Document getDocument() {
        return document ;
      }

      public EventType getType() {
        return DocumentEvent.EventType.CHANGE ;
      }

      public ElementChange getChange(Element elem) {
        throw new UnsupportedOperationException("Not supported yet.");
      }
    }) ;
  }
  
  public synchronized void addTextRange(DocumentEvent e)
  {
    assert null != thread ;
      
    try
    {
      if (!textRanges.isEmpty())
      {
        TextRange oldTextRange = textRanges.get(textRanges.size() - 1) ;
        TextRange newTextRange = oldTextRange.compactWith(e) ;
        
        if (null != newTextRange)
          textRanges.add(newTextRange) ;
      }
      else
        textRanges.add(new TextRange(e)) ;
      
      if (!textRanges.isEmpty())
        notify() ;
    }
    catch(Exception ex)
    {
      ex.printStackTrace() ;
    }
  }
  
  public void insertUpdate(DocumentEvent e)
  {
    addTextRange(e) ;
  }
  
  public void removeUpdate(DocumentEvent e)
  {
    addTextRange(e) ;
  }
  
  public void changedUpdate(DocumentEvent e)
  {
    // Nothing to do when the style change.
  }
  
  private static final TextComponentAndFinder[] EMPTY_ARRAY = new TextComponentAndFinder[0] ;
  
  private void checkRange(TextRange range, RealTimeSpellCheckerListener listener)
  {
    Document doc = range.getDocument() ;
    TextComponentAndFinder[] list = EMPTY_ARRAY ;
    
    synchronized (this)
    {
      List<TextComponentAndFinder> currentList = activeDocuments.get(doc) ;
      
      if (null != currentList)
        list = currentList.toArray(list) ;
    }

    for (TextComponentAndFinder txtCmpFnd : list)
    {
      SynchronizedWordFinder finder = txtCmpFnd.getWordFinder() ;
      
      listener.setErrorMarker(txtCmpFnd.getErrorMarker()) ;
      
      // NOTE : this word can not be valid, but in the next cycle the correct errors will be marked
      Word lastWord = finder.setTextRange(range) ;

      //System.out.println("LastWord = " + lastWord) ;
      spellChecker.setLastWord(lastWord) ;
      spellChecker.check(finder, listener) ;
    }
  }
  
  public void run()
  {
    while (true)
    {
      try
      {
        TextRange range ;
        
        synchronized(this)
        {
          if (!stopThread && textRanges.isEmpty())
            wait() ;
          
          // Must finish :D
          if (stopThread)
            break ;
          
          range = textRanges.remove(0) ;
        }
        
        checkRange(range, listener) ;
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
}
