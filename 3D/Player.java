import java.util.*;

public class Player {

  private HashMap<Integer, GameTree> statesGlobal;
  int myMarks, opMarks;
  long myTime;
  int tictacDepth = 3;

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

    myTime = deadline.timeUntil() - 500000000L;
    statesGlobal = new HashMap<Integer, GameTree>();
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
    /*System.err.println("TIME: " + myTime);*/


    // create make setup gametree
    GameTree tictacTree = new GameTree(gameState);
    deepenGameTree(tictacTree, nextStates, tictacDepth, deadline); // (, , , depth)
    /*System.err.println("AFTER TREE: " + deadline.timeUntil());*/
    int bestMove = -Integer.MAX_VALUE;
    int bestChild = 0;
    for(int i = 0; i < tictacTree.children.size(); i++) {
      int tempBest = bestMove(tictacTree.children.get(i), -Integer.MAX_VALUE, Integer.MAX_VALUE, tictacDepth);
      if(tempBest > bestMove) {
        bestMove = tempBest;
        bestChild = i;
      }
    }
    /*System.err.println("AFTER BESTMOVE: " + deadline.timeUntil());*/
    return tictacTree.children.get(bestChild).gameState;
  }

  private int bestMove(GameTree tictacTreeNode, int alpha, int beta, int depth) {
    // end recursion
    if(tictacTreeNode.children.size() == 0) return stateGoodness(tictacTreeNode.gameState);
    if(depth == 0) return stateGoodness(tictacTreeNode.gameState);
    /*if(deadline.timeUntil() < 10) {
      System.err.println("best move interrupted by deadline");
      return stateGoodness(tictacTreeNode.gameState);
    }*/
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
        if(v < alpha) beta = v;
        if(beta <= alpha) break;
      }
    }
    return v;
  }

  private void deepenGameTree(GameTree tictacTreeNode, Vector<GameState> nextStates, int depth, Deadline dl) {
    if(nextStates.size() == 0 || depth < 1 || dl.timeUntil() < myTime+10000) return;

    for(int i = 0; i < nextStates.size(); i++) {
      if(!statesGlobal.containsKey(nextStates.get(i).hashCode())) {
        statesGlobal.put(nextStates.get(i).hashCode(), new GameTree(nextStates.get(i)));
      }
      tictacTreeNode.addChild(statesGlobal.get(nextStates.get(i).hashCode()));
    }

    for(int i = 0; i < tictacTreeNode.children.size(); i++) {
      tictacTreeNode.children.get(i).gameState.findPossibleMoves(nextStates);
      deepenGameTree(tictacTreeNode.children.get(i), nextStates, depth-1, dl);
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
    // colpoints, rowponts and diags for each layer
    for(int l = 0; l < gState.BOARD_SIZE; l++) {
      // points for rows
      for(int r = 0; r < gState.BOARD_SIZE; r++) {
        int rowPoints = 0;
        for(int c = 0; c < gState.BOARD_SIZE; c++) {
          if(gState.at(r, c, l) == myMarks) {
            if(rowPoints < 0) {
              rowPoints = 0;
              break;
            } else {
              rowPoints = (rowPoints+1) * 2;
            }
          } else if(gState.at(r, c, l) == opMarks) {
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
          if(gState.at(r, c, l) == myMarks) {
            if(colPoints < 0) {
              colPoints = 0;
              break;
            } else {
              colPoints = (colPoints+1) * 2;
            }
          } else if(gState.at(r, c, l) == opMarks) {
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
      // points for diagonals of each layer
      int diag1pts = 0;
      int diag2pts = 0;
      boolean diag1 = true;
      boolean diag2 = true;
      for(int i = 0; i < gState.BOARD_SIZE; i++) {
        // diagonal 1
        if(gState.at(i, i, l) == myMarks && diag1) {
          if(diag1pts < 0) {
            diag1pts = 0;
            diag1 = false;
          } else {
            diag1pts = (diag1pts+1) * 2;
          }
        } else if(gState.at(i, i, l) == opMarks && diag1) {
          if(diag1pts > 0) {
            diag1pts = 0;
            diag1 = false;
          } else {
            diag1pts = (diag1pts-1) * 2;
          }
        }
        // diagonal 2
        if(gState.at(i, gState.BOARD_SIZE-1-i, l) == myMarks && diag2) {
          if(diag2pts < 0) {
            diag2pts = 0;
            diag2 = false;
          } else {
            diag2pts = (diag2pts+1) * 2;
          }
        } else if(gState.at(i, gState.BOARD_SIZE-1-i, l) == opMarks && diag2) {
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
    }

    // layerrows
    for(int r = 0; r < gState.BOARD_SIZE; r++) {
      // points for layerrows
      for(int c = 0; c < gState.BOARD_SIZE; c++) {
        int rowPoints = 0;
        for(int l = 0; l < gState.BOARD_SIZE; l++) {
          if(gState.at(r, c, l) == myMarks) {
            if(rowPoints < 0) {
              rowPoints = 0;
              break;
            } else {
              rowPoints = (rowPoints+1) * 2;
            }
          } else if(gState.at(r, c, l) == opMarks) {
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

      // points for standing diagonals
      int diag1pts = 0;
      int diag2pts = 0;
      boolean diag1 = true;
      boolean diag2 = true;
      for(int i = 0; i < gState.BOARD_SIZE; i++) {
        // diagonal 1
        if(gState.at(r, i, i) == myMarks && diag1) {
          if(diag1pts < 0) {
            diag1pts = 0;
            diag1 = false;
          } else {
            diag1pts = (diag1pts+1) * 2;
          }
        } else if(gState.at(r, i, i) == opMarks && diag1) {
          if(diag1pts > 0) {
            diag1pts = 0;
            diag1 = false;
          } else {
            diag1pts = (diag1pts-1) * 2;
          }
        }
        // diagonal 2
        if(gState.at(r, i, gState.BOARD_SIZE-1-i) == myMarks && diag2) {
          if(diag2pts < 0) {
            diag2pts = 0;
            diag2 = false;
          } else {
            diag2pts = (diag2pts+1) * 2;
          }
        } else if(gState.at(r, i, gState.BOARD_SIZE-1-i) == opMarks && diag2) {
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

      // points lying diagonals
      diag1pts = 0;
      diag2pts = 0;
      diag1 = true;
      diag2 = true;
      for(int i = 0; i < gState.BOARD_SIZE; i++) {
        // diagonal 1
        if(gState.at(i, r, i) == myMarks && diag1) {
          if(diag1pts < 0) {
            diag1pts = 0;
            diag1 = false;
          } else {
            diag1pts = (diag1pts+1) * 2;
          }
        } else if(gState.at(i, r, i) == opMarks && diag1) {
          if(diag1pts > 0) {
            diag1pts = 0;
            diag1 = false;
          } else {
            diag1pts = (diag1pts-1) * 2;
          }
        }
        // diagonal 2
        if(gState.at(i, r, gState.BOARD_SIZE-1-i) == myMarks && diag2) {
          if(diag2pts < 0) {
            diag2pts = 0;
            diag2 = false;
          } else {
            diag2pts = (diag2pts+1) * 2;
          }
        } else if(gState.at(i, r, gState.BOARD_SIZE-1-i) == opMarks && diag2) {
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
    }

    // point for cube main diagonals
    int diag1pts = 0;
    int diag2pts = 0;
    int diag3pts = 0;
    int diag4pts = 0;
    boolean diag1 = true;
    boolean diag2 = true;
    boolean diag3 = true;
    boolean diag4 = true;
    for(int i = 0; i < gState.BOARD_SIZE; i++) {
      // diagonal 1
      if(gState.at(i, i, i) == myMarks && diag1) {
        if(diag1pts < 0) {
          diag1pts = 0;
          diag1 = false;
        } else {
          diag1pts = (diag1pts+1) * 2;
        }
      } else if(gState.at(i, i, i) == opMarks && diag1) {
        if(diag1pts > 0) {
          diag1pts = 0;
          diag1 = false;
        } else {
          diag1pts = (diag1pts-1) * 2;
        }
      }
      // diagonal 2
      if(gState.at(gState.BOARD_SIZE-1-i, i, i) == myMarks && diag2) {
        if(diag2pts < 0) {
          diag2pts = 0;
          diag2 = false;
        } else {
          diag2pts = (diag2pts+1) * 2;
        }
      } else if(gState.at(gState.BOARD_SIZE-1-i, i, i) == opMarks && diag2) {
        if(diag2pts > 0) {
          diag2pts = 0;
          diag2 = false;
        } else {
          diag2pts = (diag2pts-1) * 2;
        }
      }
      // diagonal 3
      if(gState.at(i, gState.BOARD_SIZE-1-i, i) == myMarks && diag3) {
        if(diag3pts < 0) {
          diag3pts = 0;
          diag3 = false;
        } else {
          diag3pts = (diag3pts+1) * 2;
        }
      } else if(gState.at(i, gState.BOARD_SIZE-1-i, i) == opMarks && diag3) {
        if(diag3pts > 0) {
          diag3pts = 0;
          diag3 = false;
        } else {
          diag3pts = (diag3pts-1) * 2;
        }
      }
      // diagonal 4
      if(gState.at(gState.BOARD_SIZE-1-i, gState.BOARD_SIZE-1-i, i) == myMarks && diag4) {
        if(diag4pts < 0) {
          diag4pts = 0;
          diag4 = false;
        } else {
          diag4pts = (diag4pts+1) * 2;
        }
      } else if(gState.at(gState.BOARD_SIZE-1-i, gState.BOARD_SIZE-1-i, i) == opMarks && diag4) {
        if(diag4pts > 0) {
          diag4pts = 0;
          diag4 = false;
        } else {
          diag4pts = (diag4pts-1) * 2;
        }
      }
      if(!(diag1 || diag2 || diag3 || diag4)) break;
    }

    goodness += diag1pts + diag2pts + diag3pts + diag4pts;
    return goodness;
  }

}
