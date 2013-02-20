/*
 * Created on 24/02/2005
 *
 */
package org.dts.spell.examples;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipFile;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.PlainDocument;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.openoffice.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.finder.DocumentWordFinder;

/**
 * @author DreamTangerine
 *
 */
public class Example7
{
  public static void main(String[] args)
  {
    try
    {
      // Allow paint while resize :D
      Toolkit.getDefaultToolkit().setDynamicLayout(true) ;
      
	    SpellDictionary dict = new OpenOfficeSpellDictionary(new ZipFile(args[0])) ;
	    SpellChecker checker = new SpellChecker(dict) ;
	    
	    final JFrame frame = new JFrame("Check Speller") ;
	    final JTextArea textArea = new JTextArea() ;
	    
	    textArea.setWrapStyleWord(true) ;
	    textArea.setLineWrap(true) ;
	
      JPanel buttonPanel = new JPanel() ; 
      
	    JButton checkButton = new JButton("Check") ;
	    final JTextComponentSpellChecker textSpellChecker = 
	      new JTextComponentSpellChecker(checker) ; 
	    
	    checkButton.setMnemonic('C') ;
	    checkButton.addActionListener(
	        new ActionListener()
	        {
            public void actionPerformed(ActionEvent e)
            {
              if (textSpellChecker.spellCheck(textArea))
                JOptionPane.showMessageDialog(textArea, "Text is OK") ;

        	    textArea.requestFocusInWindow() ;              
            }
	        }
	    ) ;
	    
      JButton checkWord = new JButton("Get Word") ;
      
      checkWord.setMnemonic('W') ;
      checkWord.addActionListener(
          new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              try
              {
                DocumentWordFinder wf = 
                  new DocumentWordFinder(textArea.getDocument()) ;
                
                Word word = wf.getWordAt(textArea.getCaretPosition()) ;

                JOptionPane.showMessageDialog(textArea, "#" + word + "#") ;
              }
              catch(Exception ex)
              {
                JOptionPane.showMessageDialog(textArea, ex.getLocalizedMessage()) ;
              }

              textArea.requestFocusInWindow() ;
            }
          }
      ) ;
      
      
      JButton newButton = new JButton("New") ;      

      newButton.setMnemonic('N') ;
      newButton.addActionListener(
          new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              textArea.setDocument(new PlainDocument()) ;
              frame.setTitle("Check Speller") ;
              System.gc() ;
              textArea.requestFocusInWindow() ;              
            }
          }) ;
      
      JButton openButton = new JButton("Open...") ;      
      
      openButton.setMnemonic('O') ;
      openButton.addActionListener(
          new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              JFileChooser fileChooser = new JFileChooser() ;
              
              if (fileChooser.showOpenDialog(textArea) == 
                  JFileChooser.APPROVE_OPTION)
              {
                BufferedReader reader = null ;
                
                try
                {
                  reader = new BufferedReader( 
                    new FileReader(fileChooser.getSelectedFile())) ;
                
                  textArea.read(reader, fileChooser.getSelectedFile()) ;
                  frame.setTitle("Check Speller - " + fileChooser.getSelectedFile()) ;
                }
                catch (IOException ex)
                {
                  JOptionPane.showMessageDialog(textArea, ex.getLocalizedMessage()) ;
                  ex.printStackTrace();
                }
                finally
                {
                  if (null != reader)
                    try
                    {
                      reader.close() ;
                    }
                    catch (IOException e1)
                    {
                      e1.printStackTrace();
                    }
                  
                  System.gc() ;
                  textArea.requestFocusInWindow() ;
                }
              }
            }
          }) ;
      
      buttonPanel.add(openButton) ;
      buttonPanel.add(newButton) ;
      buttonPanel.add(checkButton) ;
      buttonPanel.add(checkWord) ;
      
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE) ;
	    frame.add(buttonPanel, BorderLayout.NORTH) ;
	    frame.add(new JScrollPane(textArea), BorderLayout.CENTER) ;
      
	    frame.setSize(640, 480) ;
	    frame.setVisible(true) ;
	    
	    textArea.requestFocusInWindow() ;
    }
    catch(Exception ex)
    {
      ex.printStackTrace() ;
    }
  }
}
