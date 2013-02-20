/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.event;

import java.util.EventObject;

/**
 *
 * @author dreamtangerine
 */
public class ProgressEvent extends EventObject {
    
    private int step ;
    private int total ;
    private String progress ;
    private Throwable error ;

    public ProgressEvent(Object source, Throwable error) {
        super(source) ;
        this.error = error ;
    }

    public ProgressEvent(Object source, String progress, int step, int total)
    {
        super(source) ;

        this.step = step ;
        this.total = total ;
        this.progress = progress ;

        error = null ;
    }

    public int getStep() {
        return step ;
    }

    public int getTotal() {
        return total ;
    }

    public String getProgress() {
        return progress ;
    }

    public boolean hasError() {
        return null != error ;
    }

    public Throwable getError() {
        return error ;
    }
}
