/*
 * Created on 05-feb-2004
 */
package org.dts.spell.jedit;

import org.dts.spell.filter.Filter;
import org.dts.spell.finder.Word;
import org.dts.spell.tokenizer.WordTokenizer;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.util.Log;

/**
 * @version 1.0 05-feb-2004
 * @author DreamTangerine
 */
public class TokenModeFilter implements Filter
{
  public TokenModeFilter()
  {
    currentLine = -1 ;
    tokenHandler = new DefaultTokenHandler() ;     
  }
  
  public Word filter(Word word, WordTokenizer tokenizer)
  {
    if (null != word)
    {
      BufferCharSequenceAdapter sequence = getBufferCharSequenceAdapter(tokenizer) ;    
      int offSet = sequence.getStart() + (word.getEnd() + word.getStart()) / 2 ; 
      JEditBuffer buffer = getBuffer(tokenizer) ;
      int wordLine = buffer.getLineOfOffset(offSet) ;

//      Log.log(Log.ERROR, this, "Filter in line " + wordLine + "  Current line " + 
//        currentLine);  
//
      Log.log(Log.ERROR, this, "Current word " + offSet + " #"+ word + "#");  
      
      if (wordLine != currentLine)
      {
        currentLine = wordLine ;

        tokenHandler.init() ;
        buffer.markTokens(currentLine, tokenHandler) ;
      }

//      Log.log(Log.ERROR, this, "Marcando nuevos tokens");  
      
      Token token = tokenHandler.getTokens() ;
//      Token auxToken = token ;

      int lineStart = buffer.getLineStartOffset(currentLine) ;
      
//      while (auxToken.id != Token.END)
//      {
//        Log.log(Log.ERROR, this, "token " 
//            + Token.tokenToString(token.id) 
//            + " = #" 
//            + buffer.getText(lineStart + auxToken.offset, auxToken.length)
//            + "#");
//        auxToken = auxToken.next ; 
//      }
//      
//      Log.log(Log.ERROR, this, "Buscando token");
      
      // Search the token at the middle of the word. 
      token = TextUtilities.getTokenAtOffset(tokenHandler.getTokens(), 
          offSet - lineStart) ;

//      Log.log(Log.ERROR, this, "Token encontrado " + 
//      Token.tokenToString(token.id) + " = #" + 
//      buffer.getText(lineStart + token.offset, token.length)
//      + "#") ;
      
      if (hasToBeParsed(buffer, token))
        return word ;
      else
        return null ;
    }
    else
      return null ;
  }

  public void updateCharSequence(
      WordTokenizer tokenizer,
      int start, 
      int end, 
      int cause)
  {
    if (currentLine == -1)
      currentLine = 0 ;
    
    tokenHandler.init() ;
    getBuffer(tokenizer).markTokens(currentLine, tokenHandler) ;
  }
  
  private BufferCharSequenceAdapter getBufferCharSequenceAdapter(WordTokenizer tokenizer)
  {
    return (BufferCharSequenceAdapter) tokenizer.getCharSequence() ;
  }
  
  private JEditBuffer getBuffer(WordTokenizer tokenizer)
  {
    return getBufferCharSequenceAdapter(tokenizer).getBuffer() ;
  }

  private boolean hasToBeParsed(JEditBuffer buffer, Token token)
  {
    if (token.id < Token.ID_COUNT)
      return ModesOptionPane.hasToBeParsed(buffer.getMode(), token) ;
    else
      return false ;
    
//    boolean result = false ;
//    
//    if (token.id < Token.ID_COUNT)
//      result = ModesOptionPane.hasToBeParsed(buffer.getMode(), token) ;
//
//    Log.log(Log.ERROR, this, "hasToBeParsed " + result);  
//    
//    return result ;
  }
  
  private DefaultTokenHandler tokenHandler ;
  private int currentLine ;
}
