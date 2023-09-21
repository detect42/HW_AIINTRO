import core.ArcadeMachine;
import core.competition.CompetitionParameters;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test_human
{

    public static void main(String[] args)
    {
        String sampleGA = "controllers.human.Agent";

        boolean visuals = true; // set to false if you don't want to see the game
        int seed = new Random().nextInt(); // seed for random
        CompetitionParameters.ACTION_TIME = 100;
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl3.txt", true, sampleGA, null, seed, true);
      //  ArcadeMachine.playOneGame( "examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl1.txt", null, new Random().nextInt());
    }
}
