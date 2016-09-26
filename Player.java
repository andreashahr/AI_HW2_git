import java.util.*;

public class Player {

  private GameTree gt;
  /**
  * Performs a move
  *
  * @param gameState
  *            the current state of the board
  * @param deadline
  *            time before which we must have returned
  * @return the next state the board is in after our move
  */
  public GameState play(final GameState gameState, final Deadline deadline) {
    Vector<GameState> nextStates = new Vector<GameState>();
    gameState.findPossibleMoves(nextStates);

    if (nextStates.size() == 0) {
      // Must play "pass" move if there are no other moves possible.
      return new GameState(gameState, new Move());
    }

    /**
    * Here you should write your algorithms to get the best next move, i.e.
    * the best next state. This skeleton returns a random move instead.
    */
    GameTree gt = new GameTree(gameState);

    int best = 0;
    int bestIndex = 0;
    if(gameState.getNextPlayer() == 0) {
      for(int g = 0; g < gt.children.size(); g++) {
        if(best < gt.children.get(g).reachableXwin - gt.children.get(g).reachableOwin) {
          best = gt.children.get(g).reachableXwin - gt.children.get(g).reachableOwin;
          bestIndex = 0;
        }
      }
    }
    else {
      for(int g = 0; g < gt.children.size(); g++) {
        if(best < gt.children.get(g).reachableOwin - gt.children.get(g).reachableXwin) {
          best = gt.children.get(g).reachableOwin - gt.children.get(g).reachableXwin;
          bestIndex = 0;
        }
      }
    }
    return gt.children.get(bestIndex).gstate;
    /*Random random = new Random();
    return nextStates.elementAt(random.nextInt(nextStates.size()));*/
  }

}
