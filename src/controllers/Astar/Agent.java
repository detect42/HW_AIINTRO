package controllers.Astar;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeSet;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import static java.lang.Double.min;

class Node implements Comparable<Node> {
    private double score;
    private StateObservation stateObs;
    private ArrayList<Types.ACTIONS> Actions;
    private int depth;

    public boolean GetKey;

    public Node(double score, StateObservation stateObs, ArrayList<Types.ACTIONS> Actions, int depth,boolean GetKey) {
        this.score = score;
        this.stateObs = stateObs;
        this.Actions = Actions;
        this.depth = depth;
        this.GetKey=GetKey;
    }
    public Node(Node other) {
        System.out.println(other.stateObs==null);
        this.score = other.score;
        this.stateObs = stateObs.copy(); // 假设stateObs也需要克隆
        this.Actions = (ArrayList<Types.ACTIONS>) other.Actions.clone(); // 假设Actions也需要克隆
        this.depth = other.depth;
        this.GetKey=other.GetKey;
        // 复制其他属性
    }
    public double getScore() {
        return score;
    }
    public StateObservation getStateObs() {
        return stateObs;
    }
    public ArrayList<Types.ACTIONS> getActions() {
        return Actions;
    }
    public int getDepth() {
        return depth;
    }
    @Override
    public int compareTo(Node other) {
        // 降序排列
        int ok = Double.compare(other.score, this.score);
        if(ok==0){
            if(stateObs.equalPosition(other.stateObs)){
                return 0;
            }
            else return 1;

        }
        else return ok;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node other = (Node) o;
        return stateObs.equalPosition(other.stateObs);
    }

    @Override
    public String toString() {
        return Double.toString(score);
    }
};

public class Agent extends AbstractPlayer {

    protected Random randomGenerator;

    protected ArrayList<Observation> grid[][];

    private TreeSet<Node> Todolist = new TreeSet<>();

    private boolean OK=false;
    protected int block_size,numstep=0;
    private ArrayList<StateObservation> Visited= new ArrayList<StateObservation>();
    private ArrayList<Types.ACTIONS>  MaxAction= new ArrayList<Types.ACTIONS>();
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
    int GetDistance(Vector2d a,Vector2d b){
        return (int)Math.abs(a.x-b.x)/50+(int)Math.abs(a.y-b.y)/50;
    }
    private Node contain(TreeSet<Node> Todolist,StateObservation stataObs){
        for(Node node:Todolist){
            if(node.getStateObs().equalPosition(stataObs)){
                return node;
            }
        }
        return null;
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

    private double GetDistanceHole(int x,int y,StateObservation stateObs){
        ArrayList<Observation>[][] observationGrid= stateObs.getObservationGrid();
        int n=observationGrid.length,m=observationGrid[0].length;
        double sum=1e9;
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                for(int k=0;k<observationGrid[i][j].size();k++){
                    if(observationGrid[i][j].get(k).itype==2){
                        sum=min(sum,GetDistance(new Vector2d(x,y),observationGrid[i][j].get(k).position));

                    }
                }
            }
        }
        if(sum==1e9) return 0;
        else return sum;

    }

    private double GetMinDistanceBox(StateObservation stateObs){
        ArrayList<Observation>[][] observationGrid= stateObs.getObservationGrid();
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
        double sum=1e9;
        for(int i=0;i<Box.size();i++){
            sum=min(sum,GetDistance(stateObs.getAvatarPosition(),Box.get(i)));
        }
        if(sum==1e9) return 0;
        else return sum;
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

    private double heuristic(StateObservation stateObs,boolean HasGetKey,int dep){
        double score=0,W_0=1,W_1=100000000,W_2=100,W_3=-5,W_4_1=30,W_4_2=10,W_4_3=100,W_4_4=-10,W_5=-10;
        double Dep,WinorLose=0,Nowscore=0,Distance,Box_1=0,Box_2=0,Box_3=0,Box_4=0,GG=0,MinDistanceBox=0;

        Dep=dep;

        if(stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS){
            WinorLose=1;
        }
        if(stateObs.getGameWinner()==Types.WINNER.PLAYER_LOSES){
            WinorLose=-1;
        }


        Nowscore=stateObs.getGameScore();

        if(HasGetKey){
            Distance=GetDistance(stateObs.getAvatarPosition(),goalpos);
        }
        else{
            Distance=GetDistance(stateObs.getAvatarPosition(),keypos);
        }

        ArrayList<Vector2d> Box=GetBox(stateObs);
        ArrayList<Observation>[][] observationGrid= stateObs.getObservationGrid();
        int[][] Map=new int[observationGrid.length][observationGrid[0].length];
        for(int i=0;i<observationGrid.length;i++){
            for(int j=0;j<observationGrid[i].length;j++){
                if(observationGrid[i][j].size()!=0){
                    for(int k=0;k<observationGrid[i][j].size();k++){
                        Map[i][j]=observationGrid[i][j].get(k).itype;
                     //   System.out.println ("x=" + i + " y=" + j + " type=" + Map[i][j]);
                    }
                }
                else{
                    Map[i][j]=-1;
                   // System.out.println("x=" + i + " y=" + j + " type=" + Map[i][j]);
                }
            }
        }

        int ax=(int)stateObs.getAvatarPosition().x/50,ay=(int)stateObs.getAvatarPosition().y/50;

        for(int i=0;i<Box.size();i++){

            int x=(int)Box.get(i).x/50,y=(int)Box.get(i).y/50;
            int n=observationGrid.length,m=observationGrid[0].length;

            if(x==keypos.x/50&&y==keypos.y/50&& !HasGetKey) GG=1;

            boolean is=false;
            if(ax==x&&(ay==y+1||ay==y-1)) is=true;
            if(ay==y&&(ax==x+1||ax==x-1)) is=true;
            if(ax==x-1&&ay==y-1) is=true;
            if(ax==x+1&&ay==y+1) is=true;
            if(ax==x-1&&ay==y+1) is=true;
            if(ax==x+1&&ay==y-1) is=true;
            if(!is) continue;

            if(x<n-1&&x>0){
                if(Map[x+1][y]==-1&&Map[x-1][y]==-1) Box_1++;
            }
            if(y<m-1&&y>0){
               // System.out.println("x=" + x + " y=" + y + " type=" + Map[x][y+1] + " " + Map[x][y-1]);
                if(Map[x][y+1]==-1&&Map[x][y-1]==-1) Box_1++;
            }
            //System.out.println("Box_1= " + Box_1);

            if(x<n-1) if(Map[x+1][y]==2) Box_2++;
            if(x>0) if(Map[x-1][y]==2) Box_2++;
            if(y<m-1) if(Map[x][y+1]==2) Box_2++;
            if(y>0) if(Map[x][y-1]==2) Box_2++;

            if(x<n-1&&x>0){
                if(Map[x+1][y]==2&&Map[x-1][y]==-1) Box_3++;
                if(Map[x-1][y]==2&&Map[x+1][y]==1) Box_3++;
            }
            if(y<m-1&&y>0){
                if(Map[x][y+1]==2&&Map[x][y-1]==-1) Box_3++;
                if(Map[x][y-1]==2&&Map[x][y+1]==-1) Box_3++;
            }

            Box_4+=GetDistanceHole(x*50,y*50,stateObs);

        }
     //   System.out.println(stateObs.getAvatarPosition().x/50 + ", " + stateObs.getAvatarPosition().y/50 + "---------------");
   //     System.out.println("Dep= " + Dep + " WinorLose= " + WinorLose + " Nowscore= " + Nowscore + " Distance= " + Distance + " Box_1= " + Box_1 + " Box_2= " + Box_2 + " Box_3= " + Box_3 + " Box_4= " + Box_4 + " GG= " + GG);
        if(HasGetKey) GG--;

        MinDistanceBox=GetMinDistanceBox(stateObs);

        score=W_0*Dep+W_1*WinorLose+W_2*Nowscore+W_3*Distance+W_4_1*Box_1+W_4_2*Box_2+W_4_3*Box_3+W_4_4*Box_4+GG*-1000000+W_5*MinDistanceBox;
        System.out.println("score= " + score + " Box_4 " + Box_4 + " MinDistanceBox= " + MinDistanceBox + "!!!!!!!");
        return score;
    }
    ArrayList<Types.ACTIONS> Astar(Node StartNode, ElapsedCpuTimer elapsedTimer,int dep){
        int tot=0;
        Todolist.add(StartNode);
        while(!Todolist.isEmpty()){
           /* for(Node x : Todolist){

                System.out.println("before score= " + x.getScore() + " depth= " + x.getDepth() + " GetKey= " + x.GetKey + "Positon= " + (x.getStateObs().getAvatarPosition().x/50)+","+(x.getStateObs().getAvatarPosition().y/50) + "!!!!!!!!");
            }*/
            Node now=Todolist.pollFirst();
           // heuristic(now.getStateObs(),now.GetKey,now.getDepth(),false);
            System.out.println("score= " + now.getScore() + " depth= " + now.getDepth() + " GetKey= " + now.GetKey + "Positon= " + (now.getStateObs().getAvatarPosition().x/50)+","+(now.getStateObs().getAvatarPosition().y/50)+"-------------");
            tot++;

           /* for(int i=0;i<now.getActions().size();i++){
                System.out.println(now.getActions().get(i));
            }*/
           /* System.out.println("size= "  + Todolist.size());
            for(Node x : Todolist){

                    System.out.println("score= " + x.getScore() + " depth= " + x.getDepth() + " GetKey= " + x.GetKey + "Positon= " + (x.getStateObs().getAvatarPosition().x/50)+","+(x.getStateObs().getAvatarPosition().y/50) + "!!!!!!!!");
            }*/

          //  if(tot==2) return now.getActions();
            StateObservation stataObs=now.getStateObs();
            Visited.add(stataObs);
           /* ArrayList<Types.ACTIONS>  AstarAction= (ArrayList<Types.ACTIONS>) now.getActions().clone();
            for(int i=0;i<AstarAction.size();i++){
                System.out.println(AstarAction.get(i));
            }*/
            ArrayList<Types.ACTIONS> actions = now.getStateObs().getAvailableActions();
            for(Types.ACTIONS action:actions){
              //  System.out.println("action= " + action + " x=" + stataObs.getAvatarPosition().x/50 + " y=" + stataObs.getAvatarPosition().y/50);
                StateObservation stCopy = now.getStateObs().copy();
                stCopy.advance(action);
               // System.out.println("action= " + action + " x=" + stCopy.getAvatarPosition().x/50 + " y=" + stCopy.getAvatarPosition().y/50);
                /*if(stCopy.getAvatarPosition().x/50==2&&stCopy.getAvatarPosition().y/50==4) {
                    System.out.println("now=key!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + heuristic(stCopy,now.GetKey,dep+1));
                }*/
                ArrayList<Types.ACTIONS>  AstarAction= (ArrayList<Types.ACTIONS>) now.getActions().clone();
                AstarAction.add(action);
                if(stCopy.getGameWinner()==Types.WINNER.PLAYER_WINS){
                    OK=true;
                   /* for(int i=0;i<AstarAction.size();i++){
                        System.out.println(AstarAction.get(i));
                    }*/
                    return AstarAction;
                }
                if(stCopy.getGameWinner()==Types.WINNER.PLAYER_LOSES||CheckifVisited(stCopy)){
                    AstarAction.remove(AstarAction.size()-1);
                    continue;
                }

                boolean NowGetKey=false;
                if(Getkey(now.getStateObs()).size()==1&& Getkey(stCopy).isEmpty()){
                    NowGetKey=true;
                }

               /* if(stCopy.getAvatarPosition().x/50==2&&stCopy.getAvatarPosition().y/50==4){
                    System.out.println("now=key!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + heuristic(stCopy,NowGetKey,dep+1));
                }*/

                Node node=contain(Todolist,stCopy);
                if(node!=null){
                    double score0=node.getScore();

                    double score1=heuristic(stCopy,NowGetKey|now.GetKey,dep+1);
                    if(score0>=score1){
                        AstarAction.remove(AstarAction.size()-1);
                    }
                    else{
                        Node Newnode=new Node(score1,stCopy,AstarAction, dep+1,NowGetKey|now.GetKey);
                       // System.out.println("before: " + Todolist.size());
                        Todolist.remove(node);
                        //System.out.println("mid: " + Todolist.size());
                        Todolist.add(Newnode);
                        //System.out.println("after: " + Todolist.size());
                    }
                }
                else{
                    Node Newnode=new Node(heuristic(stCopy,NowGetKey|now.GetKey,dep+1),stCopy,AstarAction, dep+1,NowGetKey|now.GetKey);
                   // System.out.println("Newnode= " + Newnode.getScore() + " depth= " + Newnode.getDepth() + " GetKey= " + Newnode.GetKey + "Positon= " + (Newnode.getStateObs().getAvatarPosition().x/50)+","+(Newnode.getStateObs().getAvatarPosition().y/50));
                    Todolist.add(Newnode);
                }
            }
        }

        System.out.println("GGGGGGGGGGGGGGGGGGGGGGG");
        return (new ArrayList<Types.ACTIONS>());

    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

       /* ArrayList<Observation>[][] observationGrid= stateObs.getObservationGrid();
        for(int i=0;i<observationGrid.length;i++){
            for(int j=0;j<observationGrid[i].length;j++){
                if(observationGrid[i][j]!=null){
                    for(int k=0;k<observationGrid[i][j].size();k++){
                        System.out.println("grid x=" + (observationGrid[i][j].get(k).position.x/50+1) + " y=" + (observationGrid[i][j].get(k).position.y/50+1 + " type=" + observationGrid[i][j].get(k).itype));
                    }
                }
            }
        }*/

        if(OK==false){
            Node StartNode=new Node(heuristic(stateObs,false,0),stateObs.copy(),new ArrayList<Types.ACTIONS>(),0,false);
            MaxAction= Astar(StartNode,elapsedTimer,0);OK=true;
            System.out.println("AstarAction.size()= " + MaxAction.size());
            return MaxAction.get(0);
        }
        else{
            numstep++;
            return MaxAction.get(numstep);
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