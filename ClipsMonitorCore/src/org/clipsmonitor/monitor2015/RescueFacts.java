/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.monitor2015;

/**
 *
 * @author Chosko
 */
public final class RescueFacts{
    
    private interface RescueFact{
        public int index();
        public String slot();
    }
    
    public enum RealCell implements RescueFact{
        POSR (0, "pos-r"),
        POSC (1, "pos-c"),
        CONTAINS (2, "contains"),
        INJURED (3, "injured");

        private static final String FACT_NAME = "real_cell";
        private final int index;
        private final String slot;

        RealCell(int index, String slot){
            this.index = index;
            this.slot = slot;
        }
        
        @Override
        public int index(){
            return index;
        }
        
        @Override
        public String slot(){
            return slot;
        }

        public static String[] slotsArray() {
            RescueFact[] fact = values();
            String[] slots = new String[fact.length];
            for (RescueFact slot : fact) {
                slots[slot.index()] = slot.slot();
            }
            return slots;
        }

        public static String factName() {
            return FACT_NAME;
        }
    }

    public enum Cell implements RescueFact{
        POSR (0, "pos-r"),
        POSC (1, "pos-c"),
        CONTAINS(2, "contains"),
        INJURED (3, "injured"),
        DISCOVERED (4, "discovered"),
        CHECKED (5, "checked"), 
        CLEAR(6, "clear"),
        PREVIOUS(7, "previous");
        
        private static final String FACT_NAME = "cell";
        private final int index;
        private final String slot;
        
        Cell(int index, String slot){
            this.index = index;
            this.slot = slot;
        }
        
        @Override
        public int index(){
            return index;
        }
        
        @Override
        public String slot(){
            return slot;
        }

        public static String[] slotsArray() {
            RescueFact[] fact = values();
            String[] slots = new String[fact.length];
            for (RescueFact slot : fact) {
                slots[slot.index()] = slot.slot();
            }
            return slots;
        }

        public static String factName() {
            return FACT_NAME;
        }
    }

    public enum AgentStatus implements RescueFact{
        STEP(0, "step"),
        TIME(1, "time"),
        POSR (2, "pos-r"),
        POSC (3, "pos-c"),
        DIRECTION(4, "direction"),
        LOADED (5, "loaded");
        
        private static final String FACT_NAME = "agentstatus";
        private final int index;
        private final String slot;
        
        AgentStatus(int index, String slot){
            this.index = index;
            this.slot = slot;
        }
        
        @Override
        public int index(){
            return index;
        }
        
        @Override
        public String slot(){
            return slot;
        }

        public static String[] slotsArray() {
            RescueFact[] fact = values();
            String[] slots = new String[fact.length];
            for (RescueFact slot : fact) {
                slots[slot.index()] = slot.slot();
            }
            return slots;
        }

        public static String factName() {
            return FACT_NAME;
        }
    }
    
    public enum Status implements RescueFact{
        STEP (0, "step"),
        TIME (1, "time"),
        RESULT (2, "result");
        
        private static final String FACT_NAME = "status";
        private final int index;
        private final String slot;
        
        Status(int index, String slot){
            this.index = index;
            this.slot = slot;
        }
        
        @Override
        public int index(){
            return index;
        }
        
        @Override
        public String slot(){
            return slot;
        }

        public static String[] slotsArray() {
            RescueFact[] fact = values();
            String[] slots = new String[fact.length];
            for (RescueFact slot : fact) {
                slots[slot.index()] = slot.slot();
            }
            return slots;
        }

        public static String factName() {
            return FACT_NAME;
        }
    }

    public enum PersonStatus implements RescueFact{
        POSR (0, "pos-r"),
        POSC (1, "pos-c"),
        IDENT(2, "ident");
        
        private static final String FACT_NAME = "personstatus";
        private final int index;
        private final String slot;
        
        PersonStatus(int index, String slot){
            this.index = index;
            this.slot = slot;
        }
        
        @Override
        public int index(){
            return index;
        }
        
        @Override
        public String slot(){
            return slot;
        }

        public static String[] slotsArray() {
            RescueFact[] fact = values();
            String[] slots = new String[fact.length];
            for (RescueFact slot : fact) {
                slots[slot.index()] = slot.slot();
            }
            return slots;
        }

        public static String factName() {
            return FACT_NAME;
        }
    }

    public enum KCell implements RescueFact{
        POSR (0, "pos-r"),
        POSC (1, "pos-c"),
        CONTAINS(2, "contains"),
        INJURED (3, "injured"),
        SOUND (4, "sound"),
        CHECKED (5, "checked"), 
        CLEAR(6, "clear");
        
        private static final String FACT_NAME = "K-cell";
        private final int index;
        private final String slot;
        
        KCell(int index, String slot){
            this.index = index;
            this.slot = slot;
        }
    
        @Override
        public int index(){
            return index;
        }
        
        @Override
        public String slot(){
            return slot;
        }

        public static String[] slotsArray() {
            RescueFact[] fact = values();
            String[] slots = new String[fact.length];
            for (RescueFact slot : fact) {
                slots[slot.index()] = slot.slot();
            }
            return slots;
        }

        public static String factName() {
            return FACT_NAME;
        }
    }
}
