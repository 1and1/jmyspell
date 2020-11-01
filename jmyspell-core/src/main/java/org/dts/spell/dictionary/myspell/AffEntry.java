/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * <b>Appendix</b>:  Understanding Affix Code
 * <p>
 * An affix is either a  prefix or a suffix attached to root words to make
 * other words.
 * <p>
 * Basically a Prefix or a Suffix is set of AffEntry objects
 * which store information about the prefix or suffix along
 * with supporting routines to check if a word has a particular
 * prefix or suffix or a combination.
 * <p>
 * The structure affentry is defined as follows:
 * <p>
 * struct affentry
 * {
 *   unsigned char achar;   // char used to represent the affix
 *  char * strip;          // string to strip before adding affix
 *  char * appnd;          // the affix string to add
 *  short  stripl;         // length of the strip string
 *  short  appndl;         // length of the affix string
 *  short  numconds;       // the number of conditions that must be met
 *  short  xpflg;          // flag: XPRODUCT- combine both prefix and suffix
 *  char   conds[SETSIZE]; // array which encodes the conditions to be met
 * };
 *
 *
 * Here is a suffix borrowed from the en_US.aff file.  This file
 * is whitespace delimited.
 * <p>
 * SFX D Y 4
 * SFX D   0     e          d
 * SFX D   y     ied        [^aeiou]y
 * SFX D   0     ed         [^ey]
 * SFX D   0     ed         [aeiou]y
 *
 * This information can be interpreted as follows:
 *
 * In the first line has 4 fields
 *
 * Field
 * -----
 * 1     SFX - indicates this is a suffix
 * 2     D   - is the name of the character flag which represents this suffix
 * 3     Y   - indicates it can be combined with prefixes (cross product)
 * 4     4   - indicates that sequence of 4 affentry structures are needed to
 *             properly store the affix information
 *
 * The remaining lines describe the unique information for the 4 SfxEntry
 * objects that make up this affix.  Each line can be interpreted
 * as follows: (note fields 1 and 2 are as a check against line 1 info)
 *
 * Field
 * -----
 * 1     SFX         - indicates this is a suffix
 * 2     D           - is the name of the character flag for this affix
 * 3     y           - the string of chars to strip off before adding affix
 *                     (a 0 here indicates the NULL string)
 * 4     ied         - the string of affix characters to add
 * 5     [^aeiou]y   - the conditions which must be met before the affix
 *                     can be applied
 *
 * Field 5 is interesting.  Since this is a suffix, field 5 tells us that
 * there are 2 conditions that must be met.  The first condition is that
 * the next to the last character in the word must *NOT* be any of the
 * following "a", "e", "i", "o" or "u".  The second condition is that
 * the last character of the word must end in "y".
 *
 * So how can we encode this information concisely and be able to
 * test for both conditions in a fast manner?  The answer is found
 * but studying the wonderful ispell code of Geoff Kuenning, et.al.
 * (now available under a normal BSD license).
 *
 * If we set up a conds array of 256 bytes indexed (0 to 255) and access it
 * using a character (cast to an unsigned char) of a string, we have 8 bits
 * of information we can store about that character.  Specifically we
 * could use each bit to say if that character is allowed in any of the
 * last (or first for prefixes) 8 characters of the word.
 *
 * Basically, each character at one end of the word (up to the number
 * of conditions) is used to index into the conds array and the resulting
 * value found there says whether the that character is valid for a
 * specific character position in the word.
 *
 * For prefixes, it does this by setting bit 0 if that char is valid
 * in the first position, bit 1 if valid in the second position, and so on.
 *
 * If a bit is not set, then that char is not valid for that postion in the
 * word.
 *
 * If working with suffixes bit 0 is used for the character closest
 * to the front, bit 1 for the next character towards the end, ...,
 * with bit numconds-1 representing the last char at the end of the string.
 *
 * Note: since entries in the conds[] are 8 bits, only 8 conditions
 * (read that only 8 character positions) can be examined at one
 * end of a word (the beginning for prefixes and the end for suffixes.
 *
 * So to make this clearer, lets encode the conds array values for the
 * first two affentries for the suffix D described earlier.
 *
 *
 *   For the first affentry:
 *      numconds = 1             (only examine the last character)
 *
 *      conds['e'] =  (1 << 0)   (the word must end in an E)
 *      all others are all 0
 *
 *   For the second affentry:
 *      numconds = 2             (only examine the last two characters)
 *
 *      conds[X] = conds[X] | (1 << 0)     (aeiou are not allowed)
 *          where X is all characters *but* a, e, i, o, or u
 *
 *
 *      conds['y'] = (1 << 1)     (the last char must be a y)
 *      all other bits for all other entries in the conds array are zero
 *
 *
 *
 * @author DreamTangerine
 *
 *  This is great solution for byte-handling function, but it will not work for non-latin charset 
 *  in char-handling function, because we have characters with codes more than 256 !
 *  
 *  We can use regexp for check conditions, because regexp has almost the same syntax.
 *  It should not be much slower, because regexp also optimized for performance.
 *  
 *  So, we can convert "[^aeiou]y" for suffix into Pattern.compile(".*[^aeiou]y") regexp.
 *  
 *  @author Alex Buloichik <alex73mail@gmail.com> 
 *
 *
 */
public abstract class AffEntry
{
  public AffEntry(AffixMgr pmyMgr, AffixHeader header, String line) throws IOException
  {
    this.pmyMgr = pmyMgr ;
    xpflg = header.ff ;
    achar = header.achar;
    
    readFrom(line, header.type) ;
    build_list() ;
  }
  
  /** parent Affix manager */
  protected AffixMgr pmyMgr;

  /** the affix string to append */
  protected String appnd ; 
  
  /** string to strip before append */
  protected String strip;  

  /** Flags. */
  protected short xpflg;
  
  /** Affix name. */  
  protected char achar;
  
  /** conditions to match. */  
  Conditions conds;
  
  public void readFrom(String line, char type) throws IOException
  {
    StringTokenizer tp = new StringTokenizer(line, " ") ;
    
    // split line into pieces
    try
    {
      // piece 1 - is type
      if (tp.nextToken().charAt(0) != type)
        Utils.throwIOException("ERROR_AFFIX_HEADER_CORRUPT", achar, line) ;
      
      // piece 2 - is affix char
      if (tp.nextToken().charAt(0) != achar)
        Utils.throwIOException("ERROR_AFFIX_HEADER_CORRUPT", achar, line) ;
      
      // piece 3 - is string to strip or 0 for null
      strip = tp.nextToken() ;
      
      if (strip.equals("0"))
        strip = "" ;
      
      // piece 4 - is affix string or 0 for null
      appnd = tp.nextToken() ;
      
      if (appnd.equals("0"))
        appnd = "" ;
      
      // piece 5 - is the conditions descriptions
      conds = createConditions(tp.nextToken()) ;
    }
    catch (NoSuchElementException ex)
    {
      Utils.throwIOException("ERROR_AFFIX_HEADER_CORRUPT_COUNT", achar, line) ;
    }
  }
  
  protected abstract Conditions createConditions(String cs) ;
 
  protected abstract void build_list() ;
  
  public char getName()
  {
    return achar ;
  }
}