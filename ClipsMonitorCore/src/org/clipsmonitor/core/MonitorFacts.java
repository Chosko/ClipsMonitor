/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

/**
 *
 * @author Marco Corona
 */
public abstract class MonitorFacts {
    
        protected interface Fact{
        public int index();
        public String slot();
    }
    
}
