package controllers.limitdepthfirst;

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
import ontology.avatar.MovingAvatar;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Agent extends AbstractPlayer {

    protected Random randomGenerator;

    protected ArrayList<Observation> grid[][];


    protected int block_size,numstep=0,MaxScore=-10000000;
    private boolean OK=false,Has_Init=false,KKK=false;

    private final int MaxDepth=6;



    private ArrayList<StateObservation> Visited= new ArrayList<StateObservation>();
    private ArrayList<Types.ACTIONS>  DFSActions= new ArrayList<Types.ACTIONS>(),MaxAction= new ArrayList<Types.ACTIONS>();
    private Vector2d goalpos,keypos;
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
        ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = so.getMovablePositions();
        goalpos = fixedPositions[1].get(0).position; //目标的坐标
        keypos = movingPositions[0].get(0).position ;//钥匙的坐标
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */

    private boolean CheckifVisited(StateObservation stataObs) {
        for (int i = 0; i < Visited.size(); i++) {
            if (stataObs.equalPosition(Visited.get(i))) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Vector2d> GetBox(StateObservation stataObs){
        ArrayList<Observation>[][] observationGrid= stataObs.getObservationGrid();
        ArrayList<Vector2d> Box= new ArrayList<tools.Vector2d>();
        for(int i=0;i<observationGrid.length;i++){
            for(int j=0;j<observationGrid[i].length;j++){
                if(observationGrid[i][j]!=null){
                    for(int k=0;k<observationGrid[i][j].size();k++){
                        if(observationGrid[i][j].get(k).itype==8){
                            Box.add(observationGrid[i][j].get(k).position);
                        }
                    }
                }
            }
        }
        return Box;
    }

    private ArrayList<Vector2d> Getkey(StateObservation stataObs){
        ArrayList<Observation>[][] observationGrid= stataObs.getObservationGrid();
        ArrayList<Vector2d> Key= new ArrayList<tools.Vector2d>();
        for(int i=0;i<observationGrid.length;i++){
            for(int j=0;j<observationGrid[i].length;j++){
                if(observationGrid[i][j]!=null){
                    for(int k=0;k<observationGrid[i][j].size();k++){
                        if(observationGrid[i][j].get(k).itype==6){
                            Key.add(observationGrid[i][j].get(k).position);
                        }
                    }
                }
            }
        }
        return Key;
    }
    int GetDistance(Vector2d a,Vector2d b){
        return (int)Math.abs(a.x-b.x)/50+(int)Math.abs(a.y-b.y)/50;
    }

    private int heuristic(StateObservation stateObs){
        int score=0;
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        if(stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS){
            System.out.println("Winnnnnn");
            for(int i=0;i<Visited.size();i++){
               System.out.println("Visited x=" + (Visited.get(i).getAvatarPosition().x/50+1) + " y=" + (Visited.get(i).getAvatarPosition().y/50+1));
            }

            score+=1000000;
        }
        if(stateObs.getGameWinner()==Types.WINNER.PLAYER_LOSES){
            System.out.println("Loseeeee");
            for(int i=0;i<Visited.size();i++){
                System.out.println("Visited x=" + (Visited.get(i).getAvatarPosition().x/50+1) + " y=" + (Visited.get(i).getAvatarPosition().y/50+1));
            }
            score-=1000000;
        }
        score+=(int)stateObs.getGameScore()*100;
      //  System.out.println("score= " + score);

        boolean flag=false;
        for(int i=0;i<Visited.size();i++){
            if(keypos.equals(stateObs.getAvatarPosition())){
                flag=true;
            }
        }
        if(keypos.equals(stateObs.getAvatarPosition())) flag=true;


       // System.out.println(stateObs.getAvatarPosition() + " " + keypos);

        if(flag&&KKK) score+=500;

        if(Getkey(stateObs).isEmpty()){
            score-=GetDistance(stateObs.getAvatarPosition(),goalpos);
        }
        else{
            if(GetBox(stateObs).contains(keypos)) score-=100000;

            score-=GetDistance(stateObs.getAvatarPosition(),keypos);
        }
       // System.out.println("score= " + score);

        return score;
    }
    void LFS(StateObservation stataObs, ElapsedCpuTimer elapsedTimer,int dep){

        //System.out.println("LFS x=" + (stataObs.getAvatarPosition().x/50+1) + " y=" +(stataObs.getAvatarPosition().y/50+1 + " dep=" + dep));

        /*for(int i=0;i<Visited.size();i++){
            System.out.println("Visited x=" + (Visited.get(i).getAvatarPosition().x/50+1) + " y=" + (Visited.get(i).getAvatarPosition().y/50+1));
        }*/

        /*ArrayList<Observation>[][] observationGrid= stataObs.getObservationGrid();
        for(int i=0;i<observationGrid.length;i++){
            for(int j=0;j<observationGrid[i].length;j++){
                if(observationGrid[i][j]!=null){
                    for(int k=0;k<observationGrid[i][j].size();k++){
                        System.out.println("grid x=" + (observationGrid[i][j].get(k).position.x/50+1) + " y=" + (observationGrid[i][j].get(k).position.y/50+1 + " type=" + observationGrid[i][j].get(k).itype));
                    }
                }
            }
        }*/

       // System.out.println("H()= " + heuristic(stataObs));

        if(dep==MaxDepth||stataObs.getGameWinner()==Types.WINNER.PLAYER_WINS){
            int score=heuristic(stataObs);
            System.out.println("LFS x=" + (stataObs.getAvatarPosition().x/50+1) + " y=" +(stataObs.getAvatarPosition().y/50+1 + " dep=" + dep));

            for(int i=0;i<Visited.size();i++){
                System.out.println("Visited x=" + (Visited.get(i).getAvatarPosition().x/50+1) + " y=" + (Visited.get(i).getAvatarPosition().y/50+1));
            }
            System.out.println("########score= " + score + " MaxScore= " + MaxScore);
            if(score>MaxScore){
                MaxScore=score;
                MaxAction= (ArrayList<Types.ACTIONS>) DFSActions.clone();
            }
            System.out.println("!!!!!!!!!score= " + score + " MaxScore= " + MaxScore);
            return;
        }

        if(Has_Init){
            if(CheckifVisited(stataObs)){
                return;
            }
        }
        else{
            Visited.clear();
            DFSActions.clear();
            MaxAction.clear();
            Has_Init=true;
        }

        Visited.add(stataObs);
        StateObservation stCopy = stataObs.copy();
        ArrayList<Types.ACTIONS> actions = stataObs.getAvailableActions();
      //  StateObservation stCopy = stataObs.copy(),re=null;
        for(Types.ACTIONS action:actions) {
            stCopy.advance(action);
            DFSActions.add(action);

            if(CheckifVisited(stCopy)){
                stCopy=stataObs.copy();
                DFSActions.remove(DFSActions.size()-1);
                continue;
            }

            if (stCopy.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                System.out.println("win//////////////////////" + MaxScore + " ; " + DFSActions.size());
                int score=heuristic(stCopy);
                if(score>MaxScore){
                    MaxScore=score;
                    MaxAction= (ArrayList<Types.ACTIONS>) DFSActions.clone();
                }

                for(int i=0;i<MaxAction.size();i++){
                    System.out.println("action -+ " + MaxAction.get(i));
                }

                stCopy=stataObs.copy();
                DFSActions.remove(DFSActions.size()-1);
                Has_Init=true;
                return;
            }
            else{
                LFS(stCopy,elapsedTimer,dep+1);
                Visited.remove(stCopy);
                stCopy=stataObs.copy();
                DFSActions.remove(DFSActions.size()-1);

                continue;
            }
        }

    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if(Has_Init){
            numstep++;int now=numstep;
            if(numstep>MaxAction.size()-1){
                Has_Init=false;numstep=0;MaxScore=-100000;
            }
            else{
                return MaxAction.get(now);
            }
        }

          //  System.out.println(stateObs.getAvatarPosition() + "nowpos");
        if(!Getkey(stateObs).isEmpty()) KKK=true;
        else KKK=false;MaxScore=-10000000;
        System.out.println(stateObs.getAvatarPosition());
        LFS(stateObs,elapsedTimer,0);
        System.out.println("MaxScore= " + MaxScore + " " +  MaxAction.size());
        for(int i=0;i<MaxAction.size();i++){
            System.out.println("action " + MaxAction.get(i));
        }

        if(MaxAction.size()==0){
            System.out.println("action nil");
            return Types.ACTIONS.ACTION_NIL;
        }
        else{
            return MaxAction.get(0);
        }



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