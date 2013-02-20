/*
 * Created on 18/02/2005
 *
 */
package org.dts.spell.swing.finder;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

public class DocumentCharSequence implements CharSequence
{
  private Position begin ;
  private Position end ;
  private Document document ;

  public DocumentCharSequence()
  {
    this(null) ;
  }
  
  public DocumentCharSequence(Document doc, int begin, int end) 
  {
    init(doc, begin, end) ;
  }
  
  public DocumentCharSequence(Document doc) 
  {
    init(doc, 0, null != doc ? doc.getLength() : 0) ;    
  }

  public int length()
  {
    if (null == document)
      return 0 ;
    else if (null != end)
      return end.getOffset() - begin.getOffset() ;
    else
      return document.getLength() - begin.getOffset() ;
  }

  // TODO : Optimize with a cache
  public char charAt(int index)
  {
    try
    {
      return document.getText(begin.getOffset() + index, 1).charAt(0) ;
    }
    catch (BadLocationException e)
    {
      throw new IndexOutOfBoundsException(e.getLocalizedMessage()) ;
    }
  }

  public CharSequence subSequence(int start, int end)
  {
    return new DocumentCharSequence(
        document, 
        begin.getOffset() + start, 
        begin.getOffset() + end) ;
  }
  
  public String toString()
  {
    try
    {
      if (null != document)
        return document.getText(begin.getOffset(), length()) ;
      else
        return "" ;
    }
    catch (BadLocationException e)
    {
      throw new IndexOutOfBoundsException(e.getLocalizedMessage()) ;
    }
  }
  
  public Document getDocument()
  {
    return document ;
  }
  
  public void setDocument(Document doc) 
  {
    init(doc, 0, null != doc ? doc.getLength() : 0) ;
  }
  
  void updateFromDocument(Document doc)
  {
    setDocument(doc) ;    
  }
  
  private void init(Document doc, int begin, int end)
  {
    this.document = doc ;
    
    if (null != doc)
    {
      try
      {
        this.begin = doc.createPosition(begin) ;
        
        if (doc.getLength() != end)        
          this.end = doc.createPosition(end) ;
        else
          this.end = null ;
      }
      catch (BadLocationException e)
      {
        throw new IndexOutOfBoundsException(e.getLocalizedMessage()) ;
      }
    }
    else
    {
      this.begin = null ;
      this.end = null ;
    }
  }
}