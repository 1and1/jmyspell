/*
 * Created on 19/02/2005
 *
 */
package org.dts.spell.examples;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.openoffice.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.finder.Word;

/**
 * @author DreamTangerine
 *
 */
public class Example1
{
  private static void test(SpellChecker checker, String txt)
  {
    Word badWord = checker.checkSpell(txt) ;

    if (badWord == null)
      System.out.println("SI Parece correcta") ;
    else
      System.out.println("NO Parece correcta la palabra " + badWord) ;
  }

  public static void main(String[] args) throws IOException
  {
    BufferedReader in = null ;

    try
    {
      if (3 != args.length) {
        System.err.print("correct syntax is:\n");
        System.err.print("java -jar affix_file dictionary_file file_of_words_to_check\n");
        System.exit(1);
      }

      SpellDictionary dict = new OpenOfficeSpellDictionary(new File(args[1]), new File(args[0])) ;
      SpellChecker checker = new SpellChecker(dict) ;

      checker.setCaseSensitive(false) ;

      in = new BufferedReader(new FileReader(args[2])) ;
      String word = in.readLine() ;

      while (null != word) {
        if (checker.isCorrect(word)) {
          System.out.format("\"%s\" is okay%n",word);
          System.out.format("%n");
        }
        else
        {
          System.out.format("\"%s\" is incorrect!%n",word);
          System.out.format("   suggestions:%n");

          List<String> suggestions = dict.getSuggestions(word) ;

          for (String sug : suggestions) {
                        System.out.format("    ...\"%s\"%n",sug);
          }

          System.out.format("%n");
        }

        word = in.readLine() ;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace() ;
    }
    finally
    {
      if (null != in)
        in.close();
    }
  }
}
