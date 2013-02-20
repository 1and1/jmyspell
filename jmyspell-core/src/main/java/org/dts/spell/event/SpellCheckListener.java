package org.dts.spell.event;

import java.util.EventListener;

/**
 * This is the event based listener interface.
 *
 * @author Jason Height (jheight@chariot.net.au)
 */
public interface SpellCheckListener extends EventListener
{
  /**
   * Se llama cuando se empieza a realizar una corrección ortográfica.
   * Se puede cancelar la correción llamando a
   * <code>{@link org.dts.spell.event.SpellCheckEvent#cancel() SpellCheckEvent.cancel()}</code> 
   * 
   * @param event
   */
  public void beginChecking(SpellCheckEvent event) ;
  
  /**
   * Se llama cuando se ha detectado un error en la corrección ortográfica.
   * Se puede cancelar la correción llamando a
   * <code>{@link org.dts.spell.event.SpellCheckEvent#cancel() SpellCheckEvent.cancel()}</code>
   *  
   * @param event
   */
  public void spellingError(SpellCheckEvent event) ;

  /**
   * Se ha dectectado un error en el uso de mayúsculas/minúsculas, por ejemplo
   * porque la palabra está al principio de la frase y debe de ir en mayúsculas.
   * @param event
   */
  public void badCaseError(SpellCheckEvent event) ;
  
  
  /**
   * Se ha dectectado un error, al tener dos palabras iguales seguidas.
   * @param event
   */
  public void repeatWordError(SpellCheckEvent event) ;
  
  /**
   * Se llama cuando se termina una corrección ortográfica.
   * Se puede cancelar la correción llamando a
   * <code>{@link org.dts.spell.event.SpellCheckEvent#cancel() SpellCheckEvent.cancel()}</code> 
   * 
   * @param event
   */
  public void endChecking(SpellCheckEvent event) ;  
}
