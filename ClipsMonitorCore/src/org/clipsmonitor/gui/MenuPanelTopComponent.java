package org.clipsmonitor.gui;


import java.awt.Component;
import java.io.File;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.json.simple.parser.ParseException;
import org.mapgenerator.MapGeneratorLoader;
import org.mapgenerator.MapGeneratorSceneModel;
import org.openide.util.Exceptions;

/*
 * Classe che definisce la grafica e le interazioni del pannello laterale destro con i suoi menu
 *
 * @author Tobia Giani, Alessandro Basile
 */
public class MenuPanelTopComponent extends JPanel {

    private javax.swing.JRadioButton drinkButton;
    private javax.swing.JRadioButton emptyButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JRadioButton foodButton;
    private javax.swing.JButton loadButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField num_col_field;
    private javax.swing.JTextField num_row_field;
    private javax.swing.JRadioButton outdoorButton;
    private javax.swing.JRadioButton personButton;
    private javax.swing.JRadioButton gateButton;
    private javax.swing.JRadioButton debrisInjuredButton;
    private javax.swing.JRadioButton debrisNoInjuredButton;
    private javax.swing.JButton updateButton;
    private javax.swing.JRadioButton wallButton;
    private ScenePanelTopComponent scenePanel;
    private JFileChooser fc;
    private JFileChooser save_fc;

    //variabile che tiene in memoria lo stato di selezione dei radiobutton per
    //capire il significato dei numeri memorizzati all'interno vedere a inizio
    //file scene
    private String state;
    private Component frame;

    public MenuPanelTopComponent() {
        initComponents();
        state = "";
    }

    private void initComponents() {
        fc = new JFileChooser();
        fc.setCurrentDirectory(new File("./"));
        fc.setFileFilter(new JSONFilter());

        save_fc = new JFileChooser();
        save_fc.setCurrentDirectory(new File("./"));
        //save_fc.setFileFilter(new CLIPSFilter());
        save_fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        jLabel1 = new javax.swing.JLabel();
        num_row_field = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        num_col_field = new javax.swing.JTextField();
        updateButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        exportButton = new javax.swing.JButton();
        emptyButton = new javax.swing.JRadioButton();
        wallButton = new javax.swing.JRadioButton();
        gateButton = new javax.swing.JRadioButton();
        personButton = new javax.swing.JRadioButton();
        outdoorButton = new javax.swing.JRadioButton();
        debrisInjuredButton = new javax.swing.JRadioButton();
        debrisNoInjuredButton = new javax.swing.JRadioButton();
        loadButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(280, 720));
        setSize(new java.awt.Dimension(280, 720));

        jLabel1.setText("Dimensioni griglia");

        num_row_field.setText("10");
        num_row_field.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                num_row_fieldActionPerformed(evt);
            }
        });

        jLabel2.setText("x");

        num_col_field.setText("10");
        num_col_field.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                num_col_fieldActionPerformed(evt);
            }
        });

        updateButton.setText("Aggiorna");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Inserisci");

        exportButton.setText("Salva");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        emptyButton.setText("Empty");
        emptyButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emptyButtonActionPerformed(evt);
            }
        });

        wallButton.setText("Wall");
        wallButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wallButtonActionPerformed(evt);
            }
        });
        
        wallButton.setText("Gate");
        wallButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gateButtonActionPerformed(evt);
            }
        });
        
        personButton.setText("outdoor");
        personButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outdoorButtonActionPerformed(evt);
            }
        });

        outdoorButton.setText("Person");
        outdoorButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                personButtonActionPerformed(evt);
            }
        });
        
        debrisInjuredButton.setText("Debris_Injured");
        debrisInjuredButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debrisInjuredButtonActionPerformed(evt);
            }
        });
        
        debrisNoInjuredButton.setText("Debris");
        debrisNoInjuredButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debrisNoInjuredButtonActionPerformed(evt);
            }
        });

        loadButton.setText("Carica");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    loadButtonActionPerformed(evt);
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        //raggruppo i radio button per avere una mutua esclusione sulla selezione
        ButtonGroup gruppo = new ButtonGroup();
        gruppo.add(personButton);
        gruppo.add(outdoorButton);
        gruppo.add(debrisInjuredButton);
        gruppo.add(debrisNoInjuredButton);
        gruppo.add(gateButton);
        gruppo.add(wallButton);
        gruppo.add(emptyButton);

        emptyButton.setSelected(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(41, 41, 41)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                                .addComponent(jLabel1)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(num_row_field, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(jLabel2)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(num_col_field, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(updateButton)
                                                .addComponent(jLabel3)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.CENTER, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                                                .addComponent(exportButton)
                                                                .addComponent(loadButton))))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(emptyButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(wallButton))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(debrisNoInjuredButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(debrisInjuredButton))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(gateButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(outdoorButton) 
                                        )
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(personButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        ))
                        .addGap(58, 58, 58))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(num_col_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(num_row_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(updateButton)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(emptyButton)
                                .addComponent(wallButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(gateButton)
                                .addComponent(outdoorButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(debrisInjuredButton)
                                .addComponent(debrisNoInjuredButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(personButton))
                        .addGap(41, 41, 41)
                        .addGap(18, 18, 18)
                        .addComponent(exportButton)
                        .addGap(62, 62, 62)
                        .addComponent(loadButton)
                        .addContainerGap(188, Short.MAX_VALUE))
        );
    }

    private void num_row_fieldActionPerformed(java.awt.event.ActionEvent evt) {

    }

    private void num_col_fieldActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //controllo che l'input sia intero
        //leggo le nuove dimensioni della scena e le comunico al metodo
        //resizeScene che si preoccuperà di ridimensionare la matrice mantenendo
        // i vecchi dati all'interno
        try {
            int num_row = Integer.parseInt(num_row_field.getText());
            int num_col = Integer.parseInt(num_col_field.getText());
            if (num_row > 0 && num_col > 0) {
                scenePanel.resizeScene(num_row, num_col);
            }
        } catch (NumberFormatException e) {
            this.errorMsg("Formato numero richiesto: interi positivi");
        }
    }

    private void emptyButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setState("empty");
    }

    private void wallButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setState("wall");
    }

    private void gateButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setState("gate");
    }
    private void personButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setState("person");
    }

    private void outdoorButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setState("outdoor");
    }

    private void debrisInjuredButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setState("debris_injured");
    }
    
    private void debrisNoInjuredButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setState("debris");
    }
    
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) throws ParseException {
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            MapGeneratorSceneModel s = MapGeneratorLoader.read_mappa(file);
            scenePanel.updateScene(s);
        }
    }

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int retrival = save_fc.showSaveDialog(this);
        if (retrival == JFileChooser.APPROVE_OPTION) {
            scenePanel.exportScene(save_fc.getSelectedFile().toString());
        }
    }

    void init(ScenePanelTopComponent scenePanel) {
        this.scenePanel = scenePanel;
    }

    String getState() {
        return state;
    }

    void setState(String i) {
        state = i;
    }

    void errorMsg(String error) {
        JOptionPane.showMessageDialog(frame,
                error,
                "Input Error",
                JOptionPane.WARNING_MESSAGE);
    }

    void printMsg(String Msg) {
        JOptionPane.showMessageDialog(frame,
                Msg,
                "Message",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static class CLIPSFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".clp") || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "CLIPS files (*.clp)";
        }
    }

    private class JSONFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".json") || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "JSON files (*.json)";
        }
    }
}
