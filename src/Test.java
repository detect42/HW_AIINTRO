import java.lang.annotation.Repeatable;
import java.util.Random;

import core.ArcadeMachine;
import core.competition.CompetitionParameters;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */

/*
Visited x=3.0 y=2.0
Visited x=3.0 y=3.0
Visited x=4.0 y=3.0
Visited x=4.0 y=4.0
Visited x=3.0 y=4.0
Visited x=4.0 y=4.0
Visited x=4.0 y=3.0
Visited x=3.0 y=3.0
Visited x=3.0 y=2.0
Visited x=3.0 y=5.0
Visited x=3.0 y=4.0
Visited x=4.0 y=4.0
Visited x=4.0 y=3.0
Visited x=3.0 y=3.0
 */

public class Test
{

    public static void main(String[] args)
    {
        String sampleGA = "controllers.dls.Agent";

        boolean visuals = true; // set to false if you don't want to see the game
        int seed = new Random().nextInt(); // seed for random
        CompetitionParameters.ACTION_TIME = 10000;
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl4.txt", true, sampleGA, null, seed, false);
      //  ArcadeMachine.playOneGame( "examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl1.txt", null, new Random().nextInt());
    }
}
