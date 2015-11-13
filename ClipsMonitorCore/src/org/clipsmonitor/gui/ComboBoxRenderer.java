/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Marco Corona
 * 
 * Questa classe viene utilizzata come supporto per la creazione all'interno del generatore di mappe 
 * per renderizzare il Combo box per la selezione delle opzioni di scelta e le relative icone di preview.
 * Il renderer avviene utilizzando un HashMap contenente le coppie <etichetta,icona>  
 */

public class ComboBoxRenderer extends JPanel {
    
    
    /*
    Costruttore della Classe ComboBoxRenderer
    @args:
      -iconsMap: HashMap per popolare l'oggetto JcomboBox
      -jcomboicons : l'oggetto JcomboBox su cui inserire le opzioni
      -icons : l'oggetto JLabel su cui inserire le icone di preview
    */


    public ComboBoxRenderer(String [] keys, JComboBox<String> jcomboicons ,JLabel icons) {
        super(new BorderLayout());

        // prelevo il set di chiavi dall'hashMap e genero il model che popola il JComboBox
                
        
        Arrays.sort(keys);
        

        ComboBoxModel<String> jcombostrings = new DefaultComboBoxModel<String>(keys);
        jcomboicons.setModel(jcombostrings);
        jcomboicons.setSelectedIndex(0);

        icons.setFont(icons.getFont().deriveFont(Font.ITALIC));
        icons.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));


        
    }


}
    
