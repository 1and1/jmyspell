/*
 * Created on 17/02/2005
 *
 */
package org.dts.spell.swing.finder;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;
import org.dts.spell.tokenizer.WordTokenizer;

/**
 * Esta clase vale para iterar sobre un documento.
 * 
 * @author DreamTangerine
 *
 */
public class DocumentWordFinder extends CharSequenceWordFinder
{
  public DocumentWordFinder(WordTokenizer tokenizer)
  {
    super(new DocumentCharSequence(), tokenizer) ;
  }

  public DocumentWordFinder()
  {
    super(new DocumentCharSequence()) ;
  }
  
  public DocumentWordFinder(Document text, WordTokenizer tokenizer)
  {
    super(new DocumentCharSequence(text), tokenizer) ;
  }

  protected DocumentWordFinder(CharSequence text, WordTokenizer tokenizer)
  {
    super(text, tokenizer) ;
  }
  
  /**
   * @param text
   */
  public DocumentWordFinder(Document text)
  {
    super(new DocumentCharSequence(text)) ;
  }

  protected void replace(String newWord, Word currentWord)
  {
    int start = currentWord.getStart() ;
    Document document = getDocument() ; 
    
    try
    {
      document.remove(start, currentWord.length()) ;
      updateCharSequence(
          start, 
          start + currentWord.length(), 
          WordTokenizer.DELETE_CHARS) ;
      
      if (newWord.length() > 0)
      {
        document.insertString(start, newWord, null) ;

        updateCharSequence(
            start, 
            start + newWord.length(), 
            WordTokenizer.INSERT_CHARS) ;
      }
    }
    catch (BadLocationException e)
    {
      throw new IndexOutOfBoundsException(e.getLocalizedMessage()) ;
    }
  }
  
  public Word getWordAt(int index)
  {
    return getTokenizer().currentWord(index) ;
  }
  
  public Word getPreviousWord(int index)
  {
    return getTokenizer().previousWord(index) ;    
  }

  public Word getNextWord(int index)
  {
    return getTokenizer().nextWord(index) ;    
  }
  
  
  public void setDocument(Document document)
  {
    getDocumentCharSequence().setDocument(document) ;
    updateCharSequence(0, document.getLength(), WordTokenizer.CHANGE_SEQUENCE) ;
    init() ;
  }
  
  public void quitDocument()
  {
    getDocumentCharSequence().setDocument(null) ;
    updateCharSequence(0, 0, WordTokenizer.CHANGE_SEQUENCE) ;
    init() ;
  }
  
  public DocumentCharSequence getDocumentCharSequence()
  {
    return (DocumentCharSequence) getCharSequence() ;
  }

  public Document getDocument()
  {
    return getDocumentCharSequence().getDocument() ;
  }
  
  protected void updateCharSequence(int start, int end, int cause)
  {
    super.updateCharSequence(start, end, cause) ;
    getDocumentCharSequence().updateFromDocument(getDocument()) ;      
  }
}