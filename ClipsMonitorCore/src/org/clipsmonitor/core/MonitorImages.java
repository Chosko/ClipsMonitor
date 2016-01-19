/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.paint.Color;
import java.util.Set;
import javax.imageio.ImageIO;
import org.clipsmonitor.clips.ClipsConsole;

/**
 *
 * @author Marco Corona, Ruben Caliandro
 *
 * Questa classe viene utilizzata come supporto per la visualizzazione sia dell'ambiente
 * sia come supporto alla creazione delle mappe mediante il generatore.
 *
 */
public class MonitorImages {

    private static MonitorImages instance;

    private Map<String, BufferedImage> map_img;
    private Map<String,BufferedImage> colors;
    private String [] setKeyMap;
    private String [] setKeyColor;
    private Map<String, String> colorToRgba;

    private ClipsConsole console;

    public static final int DEFAULT_IMG_SIZE = 170;

    /**
     * Private constructor (Singleton)
     */
    private MonitorImages(){}

    public static MonitorImages getInstance(){
        if(instance == null){
            instance = new MonitorImages();
            instance.init();
        }
        return instance;
    }

    /**
     * Initialize the instance. Used in a separate function to avoid infinite
     * recursion when initializing singleton classes
     */
    @SuppressWarnings("empty-statement")
    private void init(){
        console = ClipsConsole.getInstance();
        map_img = new HashMap<String, BufferedImage>();
        setKeyColor = null;
        setKeyMap = null;
        colorToRgba = new HashMap<String, String>(){
         @Override
          public String put(String key, String value) {
            return super.put(key.toLowerCase(), value);
          }

          // not @Override because that would require the key parameter to be of type Object
          public String get(String key) {
            return super.get(key.toLowerCase());
          }
      };  
       NameToRgbMap();
    }
    public Map<String, BufferedImage> getMapImg(){
        return map_img;
    }

    /**
     * Returns an image given its name
     */
    public BufferedImage getImage(String name){
        BufferedImage img = map_img.get(name);

        // Image not found. If 'name' is a color name, create an image with that color and retreive it
        if(img == null){
            Color color = findColorByName(name);
            if(color != null){
                img = createColorImage(name, color);
            }
        }

        // Color image not found, put an error image
        if(img == null){
            img = map_img.get("error_image");
            ClipsConsole.getInstance().warn("Image \""+ name +"\"not found, applying a red image instead");
        }

        return img;
    }

    /**
     * Create a new Buffered image with the given color
     */
    private BufferedImage createColorImage(String name, Color color){
        java.awt.Color c = new java.awt.Color((float)color.getRed(), (float)color.getGreen(), (float)color.getBlue(), (float)color.getOpacity());
        BufferedImage img = new BufferedImage(DEFAULT_IMG_SIZE, DEFAULT_IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        int[] red = new int[DEFAULT_IMG_SIZE * DEFAULT_IMG_SIZE];
        Arrays.fill(red, c.getRGB());
        img.setRGB(0, 0, DEFAULT_IMG_SIZE, DEFAULT_IMG_SIZE, red, 0, 0);
        map_img.put(name, img);
        return img;
    }

    public Map<String, BufferedImage> getMapColor() {
        return colors;
    }

    public void ClearImg(){
        this.map_img=null;
        this.colors=null;
        this.setKeyColor= null;
        this.setKeyMap = null;
    }

    /*
        Carica le immagini per le mappe e il generatore di mappe
    */

    public void loadImages(String path) {
        try {
            createColorImage("error_image", new Color(1, 0, 0, 0.4f));

            File img_dir = new File(path + File.separator + "img");

            File [] imgs = img_dir.listFiles();

            for(File img : imgs)
            {
                if(img.isFile()){ // escludo la directory dei colori
                    String file_name = img.getName(); // recupero il nome dell'immagine
                    int dot_position = file_name.lastIndexOf(".");  // calcolo la posizione del separatore
                    String img_name = file_name.substring(0,dot_position);
                    map_img.put(img_name, ImageIO.read(new File(path + File.separator + "img"+ File.separator + file_name)));
                }

             }

            Set<String> keys = map_img.keySet();
            setKeyMap= keys.toArray(new String[keys.size()]);
            String setMap = "(";
            for(int i=0; i<setKeyMap.length;i++){
                if(i<setKeyMap.length-1){
                     setMap += setKeyMap[i] + ",";
                }
                else{
                    setMap += setKeyMap[i] ;
                }
            }
            setMap +=")";
            console.debug("Chiavi icona registrate :"+ setMap);

        } catch (IOException e) {
            console.error("Load Icons error:");
            console.error(e);
        }
    }


    private Color findColorByName(String name) {
        try{
            return Color.web(name);
        }
        catch(IllegalArgumentException ex){
            return null;
        }
    }

   public String [] getSetKeyMap(){

      return this.setKeyMap;
   }

   public String[] getSetKeyColor(){

       return this.setKeyColor;
   }


   /**
        * Restituisce l'immagine che è la sovrapposizione fra object e background.
        * La dimensione è quella dell'immagine più piccola
        *
        * @param object
        * @param background
        * @return
        */
        public BufferedImage overlapImages(BufferedImage object, BufferedImage background) {

           BufferedImage combined;
           Graphics g;
           // crea una nuova immagine, la dimensione è quella più grande tra le 2 img
           int w = Math.max(background.getWidth(), object.getWidth());
           int h = Math.max(background.getHeight(), object.getHeight());
           combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

           // SOVRAPPONE le immagini, preservando i canali alpha per le trasparenze (figo eh?)
           g = combined.getGraphics();
           g.drawImage(background, 0, 0, null);
           g.drawImage(object, 0, 0, null);

           return combined;
       }


       private void NameToRgbMap(){

        colorToRgba.put("AliceBlue","rgba(240,24,255");
        colorToRgba.put("AntiqueWhite","250,235,215");
        colorToRgba.put("Aqua","0,255,255");
        colorToRgba.put("Aquamarine","127,255, 212");
        colorToRgba.put("Azure","240,255,255");
        colorToRgba.put("Beige","245,245,220");
        colorToRgba.put("Bisque","255,228,196");
        colorToRgba.put("Black","0,0,0");
        colorToRgba.put("BlanchedAlmond","255,235,205");
        colorToRgba.put("Blue","0,0,255");
        colorToRgba.put("BlueViolet","138,43,226");
        colorToRgba.put("Brown","165,42,42");
        colorToRgba.put("BurlyWood","222,184,135");
        colorToRgba.put("CadetBlue","95,158,160");
        colorToRgba.put("Chartreuse","127,255,0");
        colorToRgba.put("Chocolate","210,105,46");
        colorToRgba.put("Coral","255,127,80");
        colorToRgba.put("CornflowerBlue","100,149,237");
        colorToRgba.put("Cornsilk","255,248,220");
        colorToRgba.put("Crimson","220,36,60");
        colorToRgba.put("Cyan","0,255,255");
        colorToRgba.put("DarkBlue","0,0,139");
        colorToRgba.put("DarkCyan","0,139,139");
        colorToRgba.put("DarkGoldenRod","184,134,11");
        colorToRgba.put("DarkGray","169,169,169");
        colorToRgba.put("DarkGreen","0,100,0");
        colorToRgba.put("DarkKhaki","189,183,107");
        colorToRgba.put("DarkMagenta","139,0,139");
        colorToRgba.put("DarkOliveGreen","85,107,47");
        colorToRgba.put("DarkOrange","255,140,0");
        colorToRgba.put("DarkOrchid","153,50,204");
        colorToRgba.put("DarkRed","139,0,0");
        colorToRgba.put("DarkSalmon","233,150,122");
        colorToRgba.put("DarkSeaGreen","142,188,142");
        colorToRgba.put("DarkSlateBlue","72,61,139");
        colorToRgba.put("DarkSlateGray","47,79,79");
        colorToRgba.put("DarkTurquoise","0,206,209");
        colorToRgba.put("DarkViolet","148,0,211");
        colorToRgba.put("DeepPink","255,36,147");
        colorToRgba.put("DeepSkyBlue","0,191,255");
        colorToRgba.put("DimGray","105,105,105");
        colorToRgba.put("DodgerBlue","46,143,255");
        colorToRgba.put("FireBrick","178,34,34");
        colorToRgba.put("FloralWhite","255,250,240");
        colorToRgba.put("ForestGreen","34,139,34");
        colorToRgba.put("Fuchsia","255,0,255");
        colorToRgba.put("Gainsboro","220,220,220");
        colorToRgba.put("GhostWhite","248,248,255");
        colorToRgba.put("Gold","255,215,0");
        colorToRgba.put("GoldenRod","218,165,32");
        colorToRgba.put("Gray","128,128,128");
        colorToRgba.put("Green","0,128,0");
        colorToRgba.put("GreenYellow","173,255,47");
        colorToRgba.put("HoneyDew","240,255,240");
        colorToRgba.put("HotPink","255,105,180");
        colorToRgba.put("IndianRed","205,92,92");
        colorToRgba.put("Indigo","75,0,130");
        colorToRgba.put("Ivory","255,255,240");
        colorToRgba.put("Khaki","240,230,140");
        colorToRgba.put("Lavender","230,230,250");
        colorToRgba.put("LavenderBlush","255,240,245");
        colorToRgba.put("LawnGreen","124,252,0");
        colorToRgba.put("LemonChiffon","255,250,205");
        colorToRgba.put("LightBlue","173,216,230");
        colorToRgba.put("LightCoral","240,128,128");
        colorToRgba.put("LightCyan","224,255,255");
        colorToRgba.put("LightGoldenRodYellow","250,250,210");
        colorToRgba.put("LightGray","211,211,211");
        colorToRgba.put("LightGreen","143,238,143");
        colorToRgba.put("LightPink","255,182,193");
        colorToRgba.put("LightSalmon","255,160,122");
        colorToRgba.put("LightSeaGreen","32,178,170");
        colorToRgba.put("LightSkyBlue","135,206,250");
        colorToRgba.put("LightSlateGray","117,136,153");
        colorToRgba.put("LightSteelBlue","176,196,222");
        colorToRgba.put("LightYellow","255,255,224");
        colorToRgba.put("Lime","0,255,0");
        colorToRgba.put("LimeGreen","50,205,50");
        colorToRgba.put("Linen","250,240,230");
        colorToRgba.put("Magenta","255,0,255");
        colorToRgba.put("Maroon","128,0,0");
        colorToRgba.put("MediumAquaMarine","102,205,170");
        colorToRgba.put("MediumBlue","0,0,205");
        colorToRgba.put("MediumOrchid","186,85,211");
        colorToRgba.put("MediumPurple","147,112,219");
        colorToRgba.put("MediumSeaGreen","60,179,113");
        colorToRgba.put("MediumSlateBlue","123,104,238");
        colorToRgba.put("MediumSpringGreen","0,250,154");
        colorToRgba.put("MediumTurquoise","72,209,204");
        colorToRgba.put("MediumVioletRed","199,37,133");
        colorToRgba.put("MidnightBlue","41,41,112");
        colorToRgba.put("MintCream","245,255,250");
        colorToRgba.put("MistyRose","255,228,225");
        colorToRgba.put("Moccasin","255,228,181");
        colorToRgba.put("NavajoWhite","255,222,173");
        colorToRgba.put("Navy","0,0,128");
        colorToRgba.put("OldLace","253,245,230");
        colorToRgba.put("Olive","128,128,0");
        colorToRgba.put("OliveDrab","107,141,35");
        colorToRgba.put("Orange","255,165,0");
        colorToRgba.put("OrangeRed","255,69,0");
        colorToRgba.put("Orchid","218,112,214");
        colorToRgba.put("PaleGoldenRod","238,232,170");
        colorToRgba.put("PaleGreen","152,251,152");
        colorToRgba.put("PaleTurquoise","175,238,238");
        colorToRgba.put("PaleVioletRed","219,112,147");
        colorToRgba.put("PapayaWhip","255,239,213");
        colorToRgba.put("PeachPuff","255,218,185");
        colorToRgba.put("Peru","205,133,63");
        colorToRgba.put("Pink","255,192,203");
        colorToRgba.put("Plum","221,160,221");
        colorToRgba.put("PowderBlue","176,224,230");
        colorToRgba.put("Purple","128,0,128");
        colorToRgba.put("Red","255,0,0");
        colorToRgba.put("RosyBrown","188,142,142");
        colorToRgba.put("RoyalBlue","65,105,225");
        colorToRgba.put("SaddleBrown","139,69,35");
        colorToRgba.put("Salmon","250,128,114");
        colorToRgba.put("SandyBrown","244,164,96");
        colorToRgba.put("SeaGreen","46,139,87");
        colorToRgba.put("SeaShell","255,245,238");
        colorToRgba.put("Sienna","160,82,45");
        colorToRgba.put("Silver","192,192,192");
        colorToRgba.put("SkyBlue","135,206,235");
        colorToRgba.put("SlateBlue","106,90,205");
        colorToRgba.put("SlateGray","112,128,143");
        colorToRgba.put("Snow","255,250,250");
        colorToRgba.put("SpringGreen","0,255,127");
        colorToRgba.put("SteelBlue","70,130,180");
        colorToRgba.put("Tan","210,180,140");
        colorToRgba.put("Teal","0,128,128");
        colorToRgba.put("Thistle","216,191,216");
        colorToRgba.put("Tomato","255,99,71");
        colorToRgba.put("Turquoise","64,224,208");
        colorToRgba.put("Violet","238,130,238");
        colorToRgba.put("Wheat","245,222,179");
        colorToRgba.put("White","255,255,255");
        colorToRgba.put("WhiteSmoke","245,245,245");
        colorToRgba.put("Yellow","255,255,0");
        colorToRgba.put("YellowGreen","154,205,50");

       }
       
       
       public String creatergbafromName(String name,double transparency){
          String rgba="";
         if(transparency>=0 && transparency<=1){
           rgba = "rgba("+ this.colorToRgba.get(name) + "," + transparency + ")";
         }
         return rgba;
       }        
}
