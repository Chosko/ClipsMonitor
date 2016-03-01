/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.gui;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Marco Corona
 * 
 * Questa classe viene utilizzato dal Map generator per popolare le liste utili
 * per la creazione della history.Il costruttore viene parametrizzato oltre che
 * dal passaggio della Jlist da renderizzare ma anche da una stringa target che
 * determina in che modo si dovrà popolare la lista e come sono etichettati gli
 * agenti esterni al robot nel progetto
 */


public class ListRenderer {
    
    
    
    public ListRenderer(JList list , String [] Listmodel){
    
        DefaultListModel strings;
        strings = new DefaultListModel();
        for (String elem : Listmodel){
            strings.addElement(elem);
        }
        ListSelectionModel modelSelect = list.getSelectionModel();
        modelSelect.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setModel(strings);
    }
    
    
}
