package org.dts.spell.swing.event;

public interface TextComponentSpellCheckerListener {

  public void realTimeStart(TextComponentSpellCheckerEvent evt);
  
  // The realtime will be stopped
  public void realTimeWillStop(TextComponentSpellCheckerEvent evt);
  
  // The realtime was stopped
  public void realTimeStop(TextComponentSpellCheckerEvent evt);
}
