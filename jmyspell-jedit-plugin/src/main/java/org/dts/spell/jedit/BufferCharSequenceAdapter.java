/*
 * Created on 31/08/2005
 *
 */
package org.dts.spell.jedit;

import javax.swing.text.Position;

import org.gjt.sp.jedit.buffer.JEditBuffer;

public class BufferCharSequenceAdapter implements CharSequence
{
  private Position start ;
  private Position end ;
  private JEditBuffer buffer ;
  
  public BufferCharSequenceAdapter(JEditBuffer buffer, int start, int end)
  {
    this.buffer = buffer ;
    this.start = buffer.createPosition(start) ;
    this.end = buffer.createPosition(end) ;
  }

  public int length()
  {
    return end.getOffset() - start.getOffset() ;
  }

  public char charAt(int index)
  {
    // TODO : Create a cache with segments
    return buffer.getText(start.getOffset() + index, 1).charAt(0) ;
  }

  public CharSequence subSequence(int start, int end)
  {
    return new BufferCharSequenceAdapter(buffer, 
        this.start.getOffset() + start, 
        this.start.getOffset() + end) ;
  }
  
  public JEditBuffer getBuffer()
  {
    return buffer ;
  }
  
  public String toString()
  {
    return buffer.getText(start.getOffset(), length()) ;
  }
  
  public int getStart()
  {
    return this.start.getOffset() ;
  }
}