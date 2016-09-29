import java.util.*;

public class Player {

  private HashMap<Integer, GameState> statesGlobal;
  int myMarks, opMarks;
  long myTime;
  final int tictacDepth = 2;

  public Player() {
    statesGlobal = new HashMap<Integer, GameState>();
  }
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

    System.err.println("here");
    Vector<GameState> nextStates = new Vector<GameState>();
    gameState.findPossibleMoves(nextStates);

    if (nextStates.size() == 0) {
      // Must play "pass" move if there are no other moves possible.
      return new GameState(gameState, new Move());
    }

    // determine global player vars from gamestate
    if(gameState.getNextPlayer() == Constants.CELL_O) {
      myMarks = Constants.CELL_X;
      opMarks = Constants.CELL_O;
    } else {
      myMarks = Constants.CELL_O;
      opMarks = Constants.CELL_X;
    }
    myTime = deadline.timeUntil();
    if(!statesGlobal.containsKey(gameState.hashCode())) {
      statesGlobal.put(gameState.hashCode(), gameState);
    }

    // create make setup gametree
    GameTree tictacTree = new GameTree(gameState);
    deepenGameTree(tictacTree, nextStates, deadline, tictacDepth); // (, , , depth)
    int bestMove = -Integer.MAX_VALUE;
    int bestChild = 0;
    for(int i = 0; i < tictacTree.children.size(); i++) {
      int tempBest = bestMove(tictacTree.children.get(i), -Integer.MAX_VALUE, Integer.MAX_VALUE, tictacDepth);
      if(tempBest > bestMove) {
        bestMove = tempBest;
        bestChild = i;
      }
    }
    return tictacTree.children.get(bestChild).gameState;
  }

  private int bestMove(GameTree tictacTreeNode, int alpha, int beta, int depth) {
    // end recursion
    if(tictacTreeNode.children.size() == 0) return stateGoodness(tictacTreeNode.gameState);
    if(depth == 0) return stateGoodness(tictacTreeNode.gameState);
    int v;
    if((tictacDepth-depth)%2 == 0) {
      v = -Integer.MAX_VALUE;
      for(int i = 0; i < tictacTreeNode.children.size(); i++) {
        int tempv = bestMove(tictacTreeNode.children.get(i), alpha, beta, depth-1);
        if(tempv > v) v = tempv;
        if(v > alpha) alpha = v;
        if(beta <= alpha) break;
      }
    } else {
      v = Integer.MAX_VALUE;
      for(int i = 0; i < tictacTreeNode.children.size(); i++) {
        int tempv = bestMove(tictacTreeNode.children.get(i), alpha, beta, depth-1);
        if(tempv < v) v = tempv;
        if(v < alpha) alpha = v;
        if(beta <= alpha) break;
      }
    }
    return v;
  }

  private void deepenGameTree(GameTree tictacTreeNode, Vector<GameState> nextStates, Deadline deadline, int depth) {
    if(nextStates.size() == 0) return;
    if(deadline.timeUntil() < myTime * 0.3 || depth < 1) return;

    for(int i = 0; i < nextStates.size(); i++) {
      if(!statesGlobal.containsKey(nextStates.get(i).hashCode())) {
        statesGlobal.put(nextStates.get(i).hashCode(), nextStates.get(i));
      }
      tictacTreeNode.addChild(statesGlobal.get(nextStates.get(i).hashCode()));
    }

    for(int i = 0; i < tictacTreeNode.children.size(); i++) {
      tictacTreeNode.children.get(i).gameState.findPossibleMoves(nextStates);
      deepenGameTree(tictacTreeNode.children.get(i), nextStates, deadline, depth-1);
    }
  }

  private int stateGoodness(GameState gState) {

    if(gState.isXWin()) {
      if(myMarks == Constants.CELL_X) return Integer.MAX_VALUE;
      if(myMarks == Constants.CELL_O) return -Integer.MAX_VALUE;
    }
    if(gState.isOWin()) {
      if(myMarks == Constants.CELL_X) return -Integer.MAX_VALUE;
      if(myMarks == Constants.CELL_O) return Integer.MAX_VALUE;
    }
    if(gState.isEOG()) {
      return 0;
    }

    // evaluate state
    int goodness = 0;
    // points for rows
    for(int r = 0; r < gState.BOARD_SIZE; r++) {
      int rowPoints = 0;
      for(int c = 0; c < gState.BOARD_SIZE; c++) {
        if(gState.at(r, c) == myMarks) {
          if(rowPoints < 0) {
            rowPoints = 0;
            break;
          } else {
            rowPoints = (rowPoints+1) * 2;
          }
        } else if(gState.at(r, c) == opMarks) {
          if(rowPoints > 0) {
            rowPoints = 0;
            break;
          } else {
            rowPoints = (rowPoints-1) * 2;
          }
        }
      }
      goodness += rowPoints;
    }
    // points for columns
    for(int c = 0; c < gState.BOARD_SIZE; c++) {
      int colPoints = 0;
      for(int r = 0; r < gState.BOARD_SIZE; r++) {
        if(gState.at(r, c) == myMarks) {
          if(colPoints < 0) {
            colPoints = 0;
            break;
          } else {
            colPoints = (colPoints+1) * 2;
          }
        } else if(gState.at(r, c) == opMarks) {
          if(colPoints > 0) {
            colPoints = 0;
            break;
          } else {
            colPoints = (colPoints-1) * 2;
          }
        }
      }
      goodness += colPoints;
    }
    // points for diagonals
    int diag1pts = 0;
    int diag2pts = 0;
    boolean diag1 = true;
    boolean diag2 = true;
    for(int i = 0; i < gState.BOARD_SIZE; i++) {
      // diagonal 1
      if(gState.at(i, i) == myMarks && diag1) {
        if(diag1pts < 0) {
          diag1pts = 0;
          diag1 = false;
        } else {
          diag1pts = (diag1pts+1) * 2;
        }
      } else if(gState.at(i, i) == opMarks && diag1) {
        if(diag1pts > 0) {
          diag1pts = 0;
          diag1 = false;
        } else {
          diag1pts = (diag1pts-1) * 2;
        }
      }
      // diagonal 2
      if(gState.at(i, gState.BOARD_SIZE-1-i) == opMarks && diag2) {
        if(diag2pts < 0) {
          diag2pts = 0;
          diag2 = false;
        } else {
          diag2pts = (diag2pts+1) * 2;
        }
      } else if(gState.at(i, gState.BOARD_SIZE-1-i) == opMarks && diag2) {
        if(diag2pts > 0) {
          diag2pts = 0;
          diag2 = false;
        } else {
          diag2pts = (diag2pts-1) * 2;
        }
      }
      if(!(diag1 || diag2)) break;
    }
    goodness += diag1pts + diag2pts;
    return goodness;
  }

}
