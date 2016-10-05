import java.util.*;

public class Player {

  int myMarks, opMarks;
  long myTime;
  int tictacDepth = 4;
  boolean winNextMove;

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

    winNextMove = false;
    LinkedList<Integer> nextMoves = new LinkedList<Integer>(); // free cells
    findPossibleMoves(gameState, nextMoves); // fill list with free cells from gameState
    if (nextMoves.size() == 0) {
      // Must play "pass" move if there are no other moves possible.
      return new GameState(gameState, new Move());
    }
    // determine global player vars
    if(gameState.getNextPlayer() == Constants.CELL_O) {
      myMarks = Constants.CELL_X;
      opMarks = Constants.CELL_O;
    } else {
      myMarks = Constants.CELL_O;
      opMarks = Constants.CELL_X;
    }
    // determine best move
    int bestMove = -Integer.MAX_VALUE;
    int bestChild = -1;
    Integer alpha = new Integer(-Integer.MAX_VALUE); // best stateGoodness @ tictacDepth
    Integer beta = new Integer(Integer.MAX_VALUE); // worst (best for opponent)
    for(int i = 0; i < nextMoves.size(); i++) {
      int tmp = nextMoves.removeFirst(); // save value of free cell
      GameState nextState = new GameState(gameState, new Move(tmp, myMarks)); // set my mark on free cell
      int tempBest = bestMove(nextState, nextMoves, alpha, beta, 0, opMarks); // traverse tree and return move goodness
      nextMoves.addLast(tmp); // put back free cell
      if(tempBest >= bestMove) {
        bestMove = tempBest;
        bestChild = tmp;
      }
    }
    // do win move (for terminal run to finish correctly)
    if(winNextMove) {
      return new GameState(gameState, new Move(bestChild, myMarks, 1));
    }
    return new GameState(gameState, new Move(bestChild, myMarks));
  }

  private void findPossibleMoves(GameState gameState, LinkedList<Integer> nextMoves) {

    for(int i = 0; i < gameState.CELL_COUNT; i++) {
      if(gameState.at(i) == 0) nextMoves.add(i);
    }
  }

  private int bestMove(GameState gameState, LinkedList<Integer> nextMoves, Integer alpha, Integer beta, int depth, int player) {
    // end recursion
    int goodness = stateGoodness(gameState);
    if(goodness == Integer.MAX_VALUE) {
      if(depth == 0) {winNextMove = true;}
      return goodness;
    }
    if(goodness == -Integer.MAX_VALUE) return goodness;
    if(depth == tictacDepth-1) return goodness;

    // traverse subtree
    int v;
    if(player == myMarks) {
      v = -Integer.MAX_VALUE;
      for(int i = 0; i < nextMoves.size(); i++) {
        int tmp = nextMoves.remove(i);
        GameState nextState = new GameState(gameState, new Move(tmp, myMarks));
        int tempv = bestMove(nextState, nextMoves, alpha, beta, depth+1, opMarks);
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
        int tempv = bestMove(nextState, nextMoves, alpha, beta, depth+1, myMarks);
        nextMoves.add(i, tmp);
        if(tempv < v) v = tempv;
        if(v < beta) beta = v;
        if(beta <= alpha) break;
      }
    }
    return v;
  }

  private int stateGoodness(GameState gState) {

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
              rowPoints = rowPoints*10+1;
            }
          } else if(gState.at(r, c, l) == opMarks) {
            if(rowPoints > 0) {
              rowPoints = 0;
              break;
            } else {
              rowPoints = rowPoints*10-1;
            }
          }
        }
        if(rowPoints == 1111) return Integer.MAX_VALUE;
        else if(rowPoints == -1111) return -Integer.MAX_VALUE;
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
              colPoints = colPoints*10+1 ;
            }
          } else if(gState.at(r, c, l) == opMarks) {
            if(colPoints > 0) {
              colPoints = 0;
              break;
            } else {
              colPoints = colPoints*10-1 ;
            }
          }
        }
        if(colPoints == 1111) return Integer.MAX_VALUE;
        else if(colPoints == -1111) return -Integer.MAX_VALUE;
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
            diag1pts = diag1pts*10+1;
          }
        } else if(gState.at(i, i, l) == opMarks && diag1) {
          if(diag1pts > 0) {
            diag1pts = 0;
            diag1 = false;
          } else {
            diag1pts = diag1pts*10-1;
          }
        }
        // diagonal 2
        if(gState.at(i, gState.BOARD_SIZE-1-i, l) == myMarks && diag2) {
          if(diag2pts < 0) {
            diag2pts = 0;
            diag2 = false;
          } else {
            diag2pts = diag2pts*10+1;
          }
        } else if(gState.at(i, gState.BOARD_SIZE-1-i, l) == opMarks && diag2) {
          if(diag2pts > 0) {
            diag2pts = 0;
            diag2 = false;
          } else {
            diag2pts = diag2pts*10-1;
          }
        }
        if(!(diag1 || diag2)) break;
      }
      if(diag1pts == 1111) return Integer.MAX_VALUE;
      else if(diag1pts == -1111) return -Integer.MAX_VALUE;
      if(diag2pts == 1111) return Integer.MAX_VALUE;
      else if(diag2pts == -1111) return -Integer.MAX_VALUE;

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
              rowPoints = rowPoints*10+1;
            }
          } else if(gState.at(r, c, l) == opMarks) {
            if(rowPoints > 0) {
              rowPoints = 0;
              break;
            } else {
              rowPoints = rowPoints*10-1;
            }
          }
        }
        if(rowPoints == 1111) return Integer.MAX_VALUE;
        else if(rowPoints == -1111) return -Integer.MAX_VALUE;
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
            diag1pts = diag1pts*10+1;
          }
        } else if(gState.at(r, i, i) == opMarks && diag1) {
          if(diag1pts > 0) {
            diag1pts = 0;
            diag1 = false;
          } else {
            diag1pts = diag1pts*10-1;
          }
        }
        // diagonal 2
        if(gState.at(r, i, gState.BOARD_SIZE-1-i) == myMarks && diag2) {
          if(diag2pts < 0) {
            diag2pts = 0;
            diag2 = false;
          } else {
            diag2pts = diag2pts*10+1;
          }
        } else if(gState.at(r, i, gState.BOARD_SIZE-1-i) == opMarks && diag2) {
          if(diag2pts > 0) {
            diag2pts = 0;
            diag2 = false;
          } else {
            diag2pts = diag2pts*10-1;
          }
        }
        if(!(diag1 || diag2)) break;
      }
      if(diag1pts == 1111) return Integer.MAX_VALUE;
      else if(diag1pts == -1111) return -Integer.MAX_VALUE;
      if(diag2pts == 1111) return Integer.MAX_VALUE;
      else if(diag2pts == -1111) return -Integer.MAX_VALUE;
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
            diag1pts = diag1pts*10+1;
          }
        } else if(gState.at(i, r, i) == opMarks && diag1) {
          if(diag1pts > 0) {
            diag1pts = 0;
            diag1 = false;
          } else {
            diag1pts = diag1pts*10-1;
          }
        }
        // diagonal 2
        if(gState.at(i, r, gState.BOARD_SIZE-1-i) == myMarks && diag2) {
          if(diag2pts < 0) {
            diag2pts = 0;
            diag2 = false;
          } else {
            diag2pts = diag2pts*10+1;
          }
        } else if(gState.at(i, r, gState.BOARD_SIZE-1-i) == opMarks && diag2) {
          if(diag2pts > 0) {
            diag2pts = 0;
            diag2 = false;
          } else {
            diag2pts = diag2pts*10-1;
          }
        }
        if(!(diag1 || diag2)) break;
      }
      if(diag1pts == 1111) return Integer.MAX_VALUE;
      else if(diag1pts == -1111) return -Integer.MAX_VALUE;
      if(diag2pts == 1111) return Integer.MAX_VALUE;
      else if(diag2pts == -1111) return -Integer.MAX_VALUE;
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
          diag1pts = diag1pts*10+1;
        }
      } else if(gState.at(i, i, i) == opMarks && diag1) {
        if(diag1pts > 0) {
          diag1pts = 0;
          diag1 = false;
        } else {
          diag1pts = diag1pts*10-1;
        }
      }
      // diagonal 2
      if(gState.at(gState.BOARD_SIZE-1-i, i, i) == myMarks && diag2) {
        if(diag2pts < 0) {
          diag2pts = 0;
          diag2 = false;
        } else {
          diag2pts = diag2pts*10+1;
        }
      } else if(gState.at(gState.BOARD_SIZE-1-i, i, i) == opMarks && diag2) {
        if(diag2pts > 0) {
          diag2pts = 0;
          diag2 = false;
        } else {
          diag2pts = diag2pts*10-1;
        }
      }
      // diagonal 3
      if(gState.at(i, gState.BOARD_SIZE-1-i, i) == myMarks && diag3) {
        if(diag3pts < 0) {
          diag3pts = 0;
          diag3 = false;
        } else {
          diag3pts = diag3pts*10+1;
        }
      } else if(gState.at(i, gState.BOARD_SIZE-1-i, i) == opMarks && diag3) {
        if(diag3pts > 0) {
          diag3pts = 0;
          diag3 = false;
        } else {
          diag3pts = diag3pts*10-1;
        }
      }
      // diagonal 4
      if(gState.at(gState.BOARD_SIZE-1-i, gState.BOARD_SIZE-1-i, i) == myMarks && diag4) {
        if(diag4pts < 0) {
          diag4pts = 0;
          diag4 = false;
        } else {
          diag4pts = diag4pts*10+1;
        }
      } else if(gState.at(gState.BOARD_SIZE-1-i, gState.BOARD_SIZE-1-i, i) == opMarks && diag4) {
        if(diag4pts > 0) {
          diag4pts = 0;
          diag4 = false;
        } else {
          diag4pts = diag4pts*10-1;
        }
      }
      if(!(diag1 || diag2 || diag3 || diag4)) break;
    }
    if(diag1pts == 1111) return Integer.MAX_VALUE;
    else if(diag1pts == -1111) return -Integer.MAX_VALUE;
    if(diag2pts == 1111) return Integer.MAX_VALUE;
    else if(diag2pts == -1111) return -Integer.MAX_VALUE;
    if(diag3pts == 1111) return Integer.MAX_VALUE;
    else if(diag3pts == -1111) return -Integer.MAX_VALUE;
    if(diag4pts == 1111) return Integer.MAX_VALUE;
    else if(diag4pts == -1111) return -Integer.MAX_VALUE;

    goodness += diag1pts + diag2pts + diag3pts + diag4pts;
    return goodness;
  }

}
