/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.mapgenerator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author theacid
 */
public class ComboBoxRenderer extends JPanel {
    
    
    private final String [] keyStrings;
    


    public ComboBoxRenderer(HashMap<String, BufferedImage> iconsMap , JComboBox<String> jcomboicons ,JLabel icons) {
        super(new BorderLayout());

        
        Set<String> keySet = iconsMap.keySet();
        System.out.println("Set :" + keySet) ;
        keyStrings = keySet.toArray(new String[keySet.size()]);

        //Create the combo box, select the item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        ComboBoxModel<String> jcombostrings = new DefaultComboBoxModel<String>(keyStrings);
        jcomboicons.setModel(jcombostrings);
        jcomboicons.setSelectedIndex(4);

        //Set up the picture
        icons.setFont(icons.getFont().deriveFont(Font.ITALIC));
        icons.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        //The preferred size is hard-coded to be the width of the
        //widest image and the height of the tallest image + the border.
        //A real program would compute this.
        // icons.setPreferredSize(new Dimension(177, 122+10));

        
    }


}
    
