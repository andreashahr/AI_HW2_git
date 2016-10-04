import java.util.*;

public class Player {

  int myMarks, opMarks;
  long myTime;
  int tictacDepth = 5;
  boolean winNextMove = false;

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
    LinkedList<Integer> nextMoves = new LinkedList<Integer>(); // free cells
    findPossibleMoves(gameState, nextMoves); // fill list with free cells from gameState

    if (nextMoves.size() == 0) {
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

    int bestMove = -Integer.MAX_VALUE;
    int bestChild = 0;
    int alpha = -Integer.MAX_VALUE;
    int beta = Integer.MAX_VALUE;
    for(int i = 0; i < nextMoves.size(); i++) {
      int tmp = nextMoves.remove(i); // free cells possible to mark
      GameState nextState = new GameState(gameState, new Move(tmp, myMarks)); // set my mark on free cell
      int tempBest = bestMove(nextState, nextMoves, alpha, beta, 0, opMarks); // evaluate the goodness of this move
      nextMoves.add(i, tmp); // put back free cell for next move
      if(tempBest > bestMove) {
        bestMove = tempBest;
        bestChild = nextMoves.get(i);
      }
    }
    /*System.err.println("bestMove: " + bestMove + " AT cell: " + bestChild);*/
    if(winNextMove) {
      return new GameState(gameState, new Move(bestChild, myMarks, 1));
    }
    /*System.err.println("AFTER BESTMOVE: " + deadline.timeUntil()-myTime);*/
    return new GameState(gameState, new Move(bestChild, myMarks));
  }

  private void findPossibleMoves(GameState gameState, LinkedList<Integer> nextMoves) {
    /*if(gameState.isEOG()) return;*/

    for(int i = 0; i < gameState.CELL_COUNT; i++) {
      if(gameState.at(i) == 0) nextMoves.add(i);
    }
  }

  private int bestMove(GameState gameState, LinkedList<Integer> nextMoves, int alpha, int beta, int depth, int player) {
    // end recursion
    int goodness = stateGoodness(gameState);
    if(goodness == Integer.MAX_VALUE && depth%2 == 0) {
      if(depth == 0) winNextMove = true;
      return goodness;
    }
    if(goodness == -Integer.MAX_VALUE && depth%2 == 1) return goodness;
    if(depth == tictacDepth-1) return goodness;

    int v;
    if(player == myMarks) {
      v = -Integer.MAX_VALUE;
      for(int i = 0; i < nextMoves.size(); i++) {
        int tmp = nextMoves.remove(i);
        GameState nextState = new GameState(gameState, new Move(tmp, myMarks));
        int tempv = bestMove(gameState, nextMoves, alpha, beta, depth+1, opMarks);
        nextMoves.add(i, tmp);
        if(tempv > v) v = tempv;
        if(v > alpha) alpha = v;
        if(beta <= alpha) break;
      }
    } else {
      v = Integer.MAX_VALUE;
      for(int i = 0; i < nextMoves.size(); i++) {
        int tmp = nextMoves.remove(i);
        GameState nextState = new GameState(gameState, new Move(tmp, opMarks));
        int tempv = bestMove(gameState, nextMoves, alpha, beta, depth+1, myMarks);
        nextMoves.add(i, tmp);
        if(tempv < v) v = tempv;
        if(v < beta) beta = v;
        if(beta <= alpha) break;
      }
    }
    return v;
  }

  private int stateGoodness(GameState gState) {

    /*if(gState.isXWin()) {
      if(myMarks == Constants.CELL_X) return Integer.MAX_VALUE;
      if(myMarks == Constants.CELL_O) return -Integer.MAX_VALUE;
    }
    if(gState.isOWin()) {
      if(myMarks == Constants.CELL_X) return -Integer.MAX_VALUE;
      if(myMarks == Constants.CELL_O) return Integer.MAX_VALUE;
    }
    if(gState.isEOG()) {
      return 0;
    }*/

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
        if(rowPoints == 30) return Integer.MAX_VALUE;
        else if(rowPoints == -30) return -Integer.MAX_VALUE;
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
        if(colPoints == 30) return Integer.MAX_VALUE;
        else if(colPoints == -30) return -Integer.MAX_VALUE;
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
      if(diag1pts == 30) return Integer.MAX_VALUE;
      else if(diag1pts == -30) return -Integer.MAX_VALUE;
      if(diag2pts == 30) return Integer.MAX_VALUE;
      else if(diag2pts == -30) return -Integer.MAX_VALUE;

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
        if(rowPoints == 30) return Integer.MAX_VALUE;
        else if(rowPoints == -30) return -Integer.MAX_VALUE;
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
      if(diag1pts == 30) return Integer.MAX_VALUE;
      else if(diag1pts == -30) return -Integer.MAX_VALUE;
      if(diag2pts == 30) return Integer.MAX_VALUE;
      else if(diag2pts == -30) return -Integer.MAX_VALUE;
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
      if(diag1pts == 30) return Integer.MAX_VALUE;
      else if(diag1pts == -30) return -Integer.MAX_VALUE;
      if(diag2pts == 30) return Integer.MAX_VALUE;
      else if(diag2pts == -30) return -Integer.MAX_VALUE;
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
    if(diag1pts == 30) return Integer.MAX_VALUE;
    else if(diag1pts == -30) return -Integer.MAX_VALUE;
    if(diag2pts == 30) return Integer.MAX_VALUE;
    else if(diag2pts == -30) return -Integer.MAX_VALUE;
    if(diag3pts == 30) return Integer.MAX_VALUE;
    else if(diag3pts == -30) return -Integer.MAX_VALUE;
    if(diag4pts == 30) return Integer.MAX_VALUE;
    else if(diag4pts == -30) return -Integer.MAX_VALUE;

    goodness += diag1pts + diag2pts + diag3pts + diag4pts;
    return goodness;
  }

}
