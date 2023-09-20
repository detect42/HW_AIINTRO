package controllers.Astar;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

class Node implements Comparable<Node> {
    private double score;
    private StateObservation stateObs;
    private Types.ACTIONS action;
    private int depth;

    public Node(int score, StateObservation stateObs, Types.ACTIONS action, int depth) {
        this.score = score;
        this.stateObs = stateObs;
        this.action = action;
        this.depth = depth;
    }

    public double getScore() {
        return score;
    }
    public StateObservation getStateObs() {
        return stateObs;
    }
    public Types.ACTIONS getAction() {
        return action;
    }
    public int getDepth() {
        return depth;
    }


    @Override
    public int compareTo(Node other) {
        // 降序排列
        return Double.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return Double.toString(score);
    }
}

public class Agent extends AbstractPlayer {

    protected Random randomGenerator;

    protected ArrayList<Observation> grid[][];

    private PriorityQueue<Node> openList = new PriorityQueue<Node>();


    protected int block_size,numstep=0,MaxScore=-10000000;
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



    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

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