package controllers.depthfirst;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.awt.*;

import core.game.ForwardModel;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    /**
     * Random generator for the agent.
     */
    protected Random randomGenerator;

    /**
     * Observation grid.
     */
    protected ArrayList<Observation> grid[][];

    /**
     * block size
     */
    protected int block_size,numstep=0;
    private boolean OK=false;

    private ArrayList<StateObservation> Visited= new ArrayList<StateObservation>();
    private ArrayList<Vector2d> Key= new ArrayList<tools.Vector2d>();
    private ArrayList<Types.ACTIONS>  DFSActions= new ArrayList<Types.ACTIONS>();

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */

    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        randomGenerator = new Random();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */

    /*
    Visited x=100.0 y=50.0
    Visited x=100.0 y=100.0
    Visited x=150.0 y=100.0
    Visited x=150.0 y=150.0
    Visited x=100.0 y=150.0
    Visited x=100.0 y=200.0


DFS x=2.0 y=1.0
Visited x=2.0 y=1.0
Visited x=2.0 y=2.0
Visited x=3.0 y=2.0
Visited x=3.0 y=3.0
Visited x=2.0 y=3.0
Visited x=2.0 y=4.0

     */
    private boolean CheckifVisited(StateObservation stataObs) {
        for (int i = 0; i < Visited.size(); i++) {
            if (stataObs.equalPosition(Visited.get(i))) {
                return true;
            }
        }
        return false;
    }
    boolean DFS(StateObservation stataObs, ElapsedCpuTimer elapsedTimer){
       // BufferedImage bufferedImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
       // Graphics2D g2d = bufferedImage.createGraphics();

       // grid = stataObs.getObservationGrid();
      //  draw(g2d);
        //if(stataObs.)
       // System.out.println("DFS x=" + stataObs.getAvatarPosition().x/50 + " y=" + stataObs.getAvatarPosition().y/50);
       /* for(int i=0;i<Visited.size();i++){
            System.out.println("Visited x=" + Visited.get(i).x/50 + " y=" + Visited.get(i).y/50);
        }*/



        if(CheckifVisited(stataObs)){
            return false;
        }
        Visited.add(stataObs);
        StateObservation stCopy = stataObs.copy();
        ArrayList<Types.ACTIONS> actions = stataObs.getAvailableActions();
      //  StateObservation stCopy = stataObs.copy(),re=null;
        for(Types.ACTIONS action:actions) {
            stCopy.advance(action);
            DFSActions.add(action);
            if (stCopy.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                return true;
            } else if (CheckifVisited(stCopy) || stCopy.isGameOver()) {
                stCopy = stataObs.copy();
                DFSActions.remove(DFSActions.size() - 1);
                continue;
            } else if (DFS(stCopy, elapsedTimer)) {
                return true;
            } else {
                stCopy = stataObs.copy();
                DFSActions.remove(DFSActions.size() - 1);
                continue;
            }
        }
        return false;
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if(OK==true){
            numstep++;
            return DFSActions.get(numstep);
        }
        ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions();
        ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions();
        grid = stateObs.getObservationGrid();

        printDebug(npcPositions,"npc");
        printDebug(fixedPositions,"fix");
        printDebug(movingPositions,"mov");
        printDebug(resourcesPositions,"res");
        printDebug(portalPositions,"por");
        System.out.println();

        for(int j=0;j<movingPositions[0].size();j++){
            Key.add(movingPositions[0].get(j).position);
            System.out.println("key x=" + movingPositions[0].get(j).position.x/50 + " y=" + movingPositions[0].get(j).position.y/50);
        }

        System.out.println("act x=" + stateObs.getAvatarPosition().x + " y=" + stateObs.getAvatarPosition().y);
        Visited.clear();
        DFSActions.clear();
        DFS(stateObs,elapsedTimer);
        if(DFSActions.size()==0){
            System.out.println("action nil");
            return Types.ACTIONS.ACTION_NIL;
        }
        else{
            OK=true;
            return DFSActions.get(0);
        }


        /*ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions();
        ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions();
        grid = stateObs.getObservationGrid();

        printDebug(npcPositions,"npc");
        printDebug(fixedPositions,"fix");
        printDebug(movingPositions,"mov");
        printDebug(resourcesPositions,"res");
        printDebug(portalPositions,"por");
        System.out.println();

        Types.ACTIONS action = null;
        StateObservation stCopy = stateObs.copy(),re=null;


        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        re=stCopy.copy();
        while(true){
            int index = randomGenerator.nextInt(actions.size());

            action=actions.get(index);
            stCopy.advance(action);
            if(!stCopy.equalPosition(re)){
                re=stCopy.copy();
                break;
            }
        }
        //
        //action = actions.get(index);
        System.out.println(action);
        return action;*/
    }

    /**
     * Prints the number of different types of sprites available in the "positions" array.
     * Between brackets, the number of observations of each type.
     * @param positions array with observations.
     * @param str identifier to print
     */
    private void printDebug(ArrayList<Observation>[] positions, String str) {
        if (positions != null) {
            System.out.print(str + ":" + positions.length + "(");
            for (int i = 0; i < positions.length; i++) {
                System.out.print(positions[i].size() + ",");
            }
            System.out.print("); ");
            for(int i=0;i<positions.length;i++){
                for(int j=0;j<positions[i].size();j++){
                    System.out.println(str + " x=" + positions[i].get(j).position.x/50 + " y=" + positions[i].get(j).position.y/50);
                }
            }
        } else System.out.print(str + ": 0; ");
    }
}

    /**
     * Gets the player the control to draw something on the screen.
     * It can be used for debug purposes.
     * @param g Graphics device to draw to.
     */
 /*   public void draw(Graphics2D g)
    {
        System.out.println("drawing---------");
        int half_block = (int) (block_size*0.5);
        for(int j = 0; j < grid[0].length; ++j)
        {
            for(int i = 0; i < grid.length; ++i)
            {
                System.out.println("i="+i+" j="+j + grid[i][j].size());
                if(grid[i][j].size() > 0)
                {
                    Observation firstObs = grid[i][j].get(0); //grid[i][j].size()-1
                    //Three interesting options:
                    int print = firstObs.category; //firstObs.itype; //firstObs.obsID;
                    g.drawString(print + "", i*block_size+half_block,j*block_size+half_block);
                }
            }
        }
    }
}*/
