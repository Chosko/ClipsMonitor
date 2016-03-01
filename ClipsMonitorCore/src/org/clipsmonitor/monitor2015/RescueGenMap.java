/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.monitor2015;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.clipsmonitor.core.MonitorGenMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.clipsmonitor.core.MonitorImages;
import org.clipsmonitor.monitor2015.RescueFacts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openide.util.Exceptions;

/*
 * Classe che definisce il concetto di scena all'interno del progetto e tutti i metodi per accedervi e
 * modificarla. Inoltre permette la possibilità di salvare e caricare le mappe per poter essere poi
 * successiviamente utilizzate dal simulatore
 *
 * @author Tobia Giani, Alessandro Basile, Marco Corona
 */
public class RescueGenMap extends MonitorGenMap {

    private static RescueGenMap instance;

    private String loaded; // stato iniziale del robot

    /*
     La classe viene definita singleton 
     */
    public static RescueGenMap getInstance() {
        if (instance == null) {
            instance = new RescueGenMap();
            instance.init();
        }
        return instance;
    }

    private RescueGenMap() {

    }

    @Override
    public void init() {
        this.img = MonitorImages.getInstance();
        this.log="";
        log("Inizializzazione del Map generator in corso...");
        this.NumCellX = 0;
        this.NumCellY = 0;
        this.MapWidth = 0;
        this.MapHeight = 0;
        this.CellHeight = 0;
        this.CellWidth = 0;
        this.direction = "north";
        this.loaded = "unloaded";
        this.maxduration = 300;
        this.mode = "scene";
        this.personName = "person";
        this.defaulagentcondition = "gate+agent_north_unloaded";
        this.defaultagentposition = new int[2];
        this.defaultagentposition[0] = 3;
        this.defaultagentposition[1] = 2;
        this.agentposition = new int[2];
        this.agentposition[0] = this.defaultagentposition[0];
        this.agentposition[1] = this.defaultagentposition[1];
        this.NumPerson = 0;
        this.Persons = new LinkedList<Person>();
        this.setKeyMap = new String[]
        { "agent_north_unloaded", 
          "agent_north_loaded",
          "agent_west_unloaded",
          "agent_west_loaded", 
          "agent_east_unloaded",
          "agent_east_loaded",
          "agent_south_unloaded", 
          "agent_south_loaded", 
          "gate", 
          "empty", 
          "outdoor", 
          "wall", 
          "debris", 
          "debris_injured"
        };
        this.setKeyColor=new String[]
        {
          "green",
          "blue",
          "red",
          "yellow",
          "black",
          "magenta",
          "orange"
        };
        
        this.MaxNumPerson = this.setKeyColor.length;
        log("Map generator inizializzato correttamente");
    }

    /**
     * Inizializzazione della scena eseguita mettendo nel perimetro della scena
     * l'outdoor e riempiendo il resto con le celle empty. Il metodo risulta
     * chiaramente adattato ai vincoli del progetto e a come si dovrebbe
     * presentare di default uno scenario
     *
     * @param scene matrice di stringe che dovrà essere riempita
     */
    
    
   /**
    * Inizializzazione della scena eseguita mettendo nel perimetro della scena
    * l'outdoor e riempiendo il resto con le celle empty. Il metodo risulta
    * chiaramente adattato ai vincoli del progetto e a come si dovrebbe
    * presentare di default uno scenario
    *
    * @param scene matrice di stringe che dovrà essere riempita
    */


    @Override
    public void initScene(String[][] scene) {

        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                if (i == 0 || i == scene.length - 1 || j == 0 || j == scene[0].length - 1) {
                    scene[i][j] = "outdoor";
                } else if (i == 1 || i == scene.length - 2 || j == 1 || j == scene[0].length - 2) {
                    scene[i][j] = "wall";
                } else {

                    scene[i][j] = "empty";
                }
            }
        }

        scene[this.agentposition[0]][this.agentposition[1]] = "gate" + "+" + "agent_" + direction + "_" + loaded;
        this.move = this.clone(scene);
    }

    
  
    /**
     * Genera una stringa rappresentante la history da scrivere successivamante
     * su un file di testo Il testo prodotto contiene le informazioni
     * riguardanti l'agente nel suo stato iniziale, la durata massima messa a
     * disposizione dell'agente per eseguire le sue operazioni e l'elenco
     * completo di tutti i path che sono stati definiti per i vari agenti
     *
     * @return la stringa rappresentante la history
     */
    @Override
    public String exportHistory() {
        String history = "";
        history += "(maxduration " + this.maxduration + ") \n\n";
        int[] agentPos = GenMapToMap(this.agentposition);
        
        // posizione iniziale dell'agente
        history += "(initial_agentposition ( pos-r " + agentPos[0]  + ")";
        history += "( pos-c " + agentPos[1] + ")";
        history += "(direction " + this.direction + ")) \n\n";
        
        for(Person elem : Persons){
            String personName = elem.getColor();
            Path firstPath = getPathByName(personName + "_0");
            StepMove firstMove = firstPath.getMoves().getFirst();
            int[] firstMovePos = GenMapToMap(firstMove.getX(), firstMove.getY());
            int step =firstMove.getStep();
            int r = firstMovePos[0];
            int c = firstMovePos[1];
            history += RescueFacts.PersonStatus.getPersonStatus(personName,step , r, c);
        }
        history += "\n";
        
        Path[] paths = this.getPaths(-1);
        
        for(Path elem : paths){
            String name = elem.getName();
            String person = name.substring(0,name.indexOf("_"));
            name = getNewPathName(name);
            int step = elem.getStartStep()+1;
            history += RescueFacts.PersonMove.getPersonMove(step, person,name);
        }
        
        history +="\n";
        
        
        
        for(Path elem : paths){
            String name = elem.getName();
            String person = name.substring(0,name.indexOf("_"));
            name = getNewPathName(name);
            int step = elem.getStartStep();
            LinkedList<StepMove> slist = elem.getMoves(); 
            for( StepMove s : slist){
                int idstep = s.getStep() - step +1;
                int[] stepPos = GenMapToMap(s.getX(), s.getY());
                history += "( move-path " + name + " " + idstep + " " + person 
                            + " " + stepPos[0] + " " + stepPos[1] + " ) \n"; 
            
            }
        
        }
        
        return history;
    }

    /**
     * Genera il testo contenente i fatti che descrivono una possibile mappa. Il
     * metodo sfrutta un metodo ausiliario getEnvCell per costruire le stringhe
     * dei fatti che descrivono le varie celle
     *
     * @return la stringa con la descrizione di tutte le celle da salvare su un
     * file di testo
     */
    @Override
    public String exportScene() {

        //variabili per impostare la posizione delle componenti
        String s = "";
        
        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                int[] realCellPos = GenMapToMap(i,j);
                boolean testInjured = scene[i][j].contains("injured");
                s += RescueFacts.RealCell.getRealCell(realCellPos[0],realCellPos[1], scene[i][j], testInjured);
            }
        }
        return s;
    }

    /**
     * Verifica se la posizione dell'agente richiesta risulta essere compatibile
     * rispetto ai vincoli del progetto. Ritorna true se la condizione è
     * rispettata
     *
     * @param mapPos stringa da valutare come effettivamente valida
     * @return true se risulta una posizione valida per il robot, false
     * altrimenti
     */
    @Override
    public boolean RobotPositionIsValid(String mapPos) {

        return !mapPos.contains("debris") && !mapPos.contains("wall") && !mapPos.contains("outdoor");

    }

    /*
     *  Verifica se la posizione dell'agente richiesta risulta essere compatibile
     *  rispetto ai vincoli del progetto. Ritorna true se la condizione è rispettata
     */
    @Override
    public boolean PersonPositionIsValid(String mapPos) {

        return !mapPos.contains("debris") && !mapPos.contains("wall");

    }

    /**
     * Setta tutti i paramentri del modello relativi al robot, la sua posizione
     * e eventualmente se risulta essere carico o scarico
     *
     * @param state stringa rappresentante il robot
     * @param x riga della cella
     * @param y colonna della cella
     */
    @Override
    public void SetRobotParams(String state, int x, int y) {

        this.agentposition[0] = x;
        this.agentposition[1] = y;

        String[] params = state.split("_");
        String AgentDirection = params[1];
        String AgentLoaded = params[2];

        this.direction = AgentDirection;
        this.loaded = AgentLoaded;

    }
    
    

    /**
     * Metodo per la creazione della matrice di icone da disegnare sulla mappa
     * del generatore quando questo è impostato in modalità scene.Il metodo si occupa 
     * di creare le icone con l'overlap.
     *
     * @return la matrice di icone da disegnare sul pannello
     */
    
    
    
    public boolean getEmptyPerson() {
    
      return this.Persons.isEmpty();
    }    

    
    public void error(String message){
      this.AppendLogMessage(message,"error");
    }
    
    
    public void log(String message){
      this.AppendLogMessage(message, "log");
    }

  public void clear() {
     instance=null;
  }
  
  
  
 public int[] GetJsonMapDimension(File jsonMap){
 
   try {
        //converto il file in un oggetto JSON
        FileReader jsonreader = new FileReader(jsonMap);
        char[] chars = new char[(int) jsonMap.length()];
        jsonreader.read(chars);
        String jsonstring = new String(chars);
        jsonreader.close();
        JSONObject json = new JSONObject(jsonstring);
        //leggo il numero di celle dalla radice del JSON

        int NumCellX = Integer.parseInt(json.get("cell_x").toString());
        int NumCellY = Integer.parseInt(json.get("cell_y").toString());

        return new int[]{NumCellX,NumCellY};
   }
   catch (JSONException ex) {

        AppendLogMessage(ex.getMessage(),"error");
    } catch (IOException ex) {
        AppendLogMessage(ex.getMessage(),"error");
    } catch (NumberFormatException ex) {
        AppendLogMessage(ex.getMessage(),"error");
    }
 
    return null;
 } 
  
 public void createJsonScene(File map){
      try {
        BufferedReader mapread = new BufferedReader(new FileReader(map));
        String l = "";
        log(map.getName());
        String regex = "\\s*(\\(real_cell\\s*(\\(pos-r (\\d+)\\))\\s*(\\(pos-c (\\d+)\\))\\s*(\\(contains ([a-z]+)\\))\\s*(\\(injured (yes|no)\\))\\)\\s*)";
        Pattern p = Pattern.compile(regex);
        int maxR=0 ;
        int maxC=0 ;
        String posR = "";
        String posC = "";
        String contains = "";
        String injured = "";
        String state = "";
        JSONObject Info = new JSONObject();
        
        while((l=mapread.readLine())!=null ){
          Matcher m = p.matcher(l);
          boolean match = m.find();
          if(match){
            posR = m.group(3);
            posC = m.group(5);
            int r = Integer.parseInt(posR);
            int c = Integer.parseInt(posC);
            if(maxR<r){
              maxR=r;
            }
            if(maxC<c){
              maxC=c;
            }
            
         }
       }
        
        mapread = new BufferedReader(new FileReader(map));
        Info.put("cell_x", maxC);
        Info.put("cell_y", maxR);
        JSONArray ArrayCells = new JSONArray();
        
        while((l=mapread.readLine())!=null ){
          Matcher m = p.matcher(l);
          boolean match = m.find();
          if(match){
            posR = m.group(3);
            posC = m.group(5);
            contains = m.group(7);
            injured = m.group(9);
            int r = Integer.parseInt(posR);
            int c = Integer.parseInt(posC);
            JSONObject cell = new JSONObject();
            int[] GenToMap = this.MapToGenMap(r,c,maxR);
            
            cell.put("x", GenToMap[0]);
            cell.put("y", GenToMap[1]);
            state = contains;
            if(contains.contains("debris")){
              if(injured.contains("yes")){
                state="debris_injured";
              }
              else{
                state="debris";
              }
            }
            cell.put("state",state);
            //salvo solo l'intero dello stato che viene
            //scenepato internamente;
            ArrayCells.put(cell);
         }
       }
        Info.put("cells", ArrayCells);
        File jsonfile = new File(map.getParent()+File.separator+"infoMap.json");
        Files.write(Paths.get(jsonfile.getAbsolutePath()), Info.toString(2).getBytes());
      }
      catch (FileNotFoundException ex) {
        Exceptions.printStackTrace(ex);
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      } catch (JSONException ex) {
        Exceptions.printStackTrace(ex);
      }
 }
 
 public void createJsonHistory(File history,File jsonMap){
   try {
        JSONObject Info = new JSONObject();
        BufferedReader mapread = new BufferedReader(new FileReader(history));
        String l = "";
        String timeRegex = "\\s*(\\(maxduration\\s*(\\d+)\\s*\\)\\s*)";
        String agentPosRegex = 	"\\s*(\\(initial_agentposition\\s*"
                + "(\\(pos-r (\\d+)\\))\\s*"
                + "(\\(pos-c (\\d+)\\))\\s*"
                + "(\\(direction ([a-z]+)\\))\\s*\\))";
        
        String perStatusRegex = "\\s*(\\(\\s*personstatus\\s*"
                + "(\\(step (\\d+)\\))"
                + "\\s*(\\(time (\\d+)\\))\\s*"
                + "(\\(ident [A-Za-z0-9]+\\))\\s*"
                + "(\\(pos-r (\\d+)\\))\\s*"
                + "(\\(pos-c (\\d+)\\))\\s*"
                + "(\\(activity out\\))\\s*\\))";
        
        String perMoveRegex = "\\s*(\\(\\s*personmove\\s*"
                + "(\\(step (\\d+)\\))\\s*"
                + "(\\(ident ([A-Za-z0-9]+)\\))\\s*"
                + "(\\(path-id ([A-Za-z0-9]+)\\))\\s*\\))";
        
        String movePathRegex = 	"\\s*(\\(\\s*move\\-path\\s+"
                + "([A-Za-z0-9]+)\\s+"
                + "(\\d+)\\s+"
                + "([A-Za-z0-9]+)\\s+"
                + "(\\d+)\\s+(\\d+)\\s*\\))";
        
        
        // recupero dal json della mappa la dimensione in celle della mappa
        // per eseguire le operazioni di trasformazione delle coordinate 
        // anche sulle move degli agenti
        
        int [] mapDimension = GetJsonMapDimension(jsonMap);
        
        Pattern timeDuration = Pattern.compile(timeRegex);
        Pattern agent = Pattern.compile(agentPosRegex);
        Pattern personStatus = Pattern.compile(perStatusRegex);
        Pattern personMove = Pattern.compile(perMoveRegex);
        Pattern moves = Pattern.compile(movePathRegex);
        
        while((l=mapread.readLine())!=null){
          Matcher tdm = timeDuration.matcher(l);
          Matcher agm = agent.matcher(l);
          Matcher psm = personStatus.matcher(l);
          Matcher pmm = personMove.matcher(l);
          Matcher mm = moves.matcher(l);
          
          // inserisco la maxduration 
          if(tdm.find()){
            String timeDur = tdm.group(2);
            int t = Integer.parseInt(timeDur);
            Info.put("time",t);
          }
          // inserisco le informazioni sull'agent
          if(agm.find()){
            String posR = agm.group(3);
            String posC = agm.group(5);
            String direction = agm.group(7);
            int r= Integer.parseInt(posR);
            int c= Integer.parseInt(posC);
            int [] InitAgentPos = MapToGenMap(r,c,mapDimension[1]);
            Info.put("robot_x", InitAgentPos[0]);
            Info.put("robot_y", InitAgentPos[1]);
            Info.put("robot_direction", direction);
            Info.put("robot_loaded", "unloaded");
          }
          if(psm.find()){
            
          }
          if(pmm.find()){
          
          }
          if(mm.find()){
          
          }
        }
        JSONArray PersonsArray = new JSONArray();
        Info.put("personList", PersonsArray);
        File jsonfile = new File(history.getParent()+File.separator+"infoMove.json");
        Files.write(Paths.get(jsonfile.getAbsolutePath()), Info.toString(2).getBytes());
       
      } catch (FileNotFoundException ex) {
        Exceptions.printStackTrace(ex);
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      } catch (JSONException ex) {
        Exceptions.printStackTrace(ex);
      }
 }
 
    public void LoadJsonRobotParams(File jsonFile)
    {
      try {
            //converto il file in un oggetto JSON
            FileReader jsonreader = new FileReader(jsonFile);
            char[] chars = new char[(int) jsonFile.length()];
            jsonreader.read(chars);
            String jsonstring = new String(chars);
            jsonreader.close();
            JSONObject json = new JSONObject(jsonstring);
            agentposition = new int[]{json.getInt("robot_x"), json.getInt("robot_y")};
            defaultagentposition = new int[]{json.getInt("robot_x_default"), json.getInt("robot_y_default")};
            
            direction = json.getString("robot_direction");
            loaded = json.getString("robot_loaded");
            scene[agentposition[0]][agentposition[1]] +="+"+ "agent_"+ direction + "_" + loaded;
            defaulagentcondition=scene[agentposition[0]][agentposition[1]];
      
      }
      
      catch (JSONException ex) {
            AppendLogMessage(ex.getMessage(),"error");
        } catch (IOException ex) {
            AppendLogMessage(ex.getMessage(),"error");
        } catch (NumberFormatException ex) {
            AppendLogMessage(ex.getMessage(),"error");
       }
    }
            
    /**
     * Salva sul json passato in input le informazioni relative all'agente 
     * da utilizzare per ricaricare la scena
     * @param json 
     */
    
    public boolean SaveJsonRobotParams(File json)
    {
      try{
        JSONObject Info = new JSONObject();
        Info.put("robot_x", agentposition[0]);
        Info.put("robot_y", agentposition[1]);
        Info.put("robot_x_default", agentposition[0]);
        Info.put("robot_y_default", agentposition[1]);
        Info.put("robot_direction", direction);
        Info.put("robot_loaded", loaded);
        Files.write(Paths.get(json.getAbsolutePath()), Info.toString(2).getBytes(),StandardOpenOption.APPEND);
        return true;
      }
      catch (JSONException ex) {
        AppendLogMessage(ex.getMessage(),"error");
        return false;
        
        
      } 
      catch (NumberFormatException ex) {
        AppendLogMessage(ex.getMessage(),"error");
        return false;
      } 
      catch (IOException ex) {
        Exceptions.printStackTrace(ex);
        return false;
      }
    }
}
