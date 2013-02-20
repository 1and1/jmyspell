/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.event;

import java.util.EventListener;

/**
 * Interface to monitor the load of dictionaries.
 * In any method of this interface evt can tell an error.
 * @author dreamtangerine
 */
public interface ProgressListener extends EventListener {

    public void beginProgress(ProgressEvent evt) ;

    public void nextStep(ProgressEvent evt) ;

    public void endProgress(ProgressEvent evt) ;
}
