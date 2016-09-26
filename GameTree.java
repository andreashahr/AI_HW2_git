import java.util.*;

public class GameTree {

  public GameState gameState;
  public GameTree parent;
  public List<GameTree> = children;

  public GameTree(GameState gState) {
    this.gameState = gState;
    this.parent = null;
    this.children = new LinkedList<GameTree>();
  }

  public GameTree(GameState gState, GameTree parent) {
    this.gameState = gState;
    this.parent = parent;
    this.children = new LinkedList<GameTree>();
  }

  public addChild(GameState gState) {
    children.add(new GameTree(gState, this));
  }

  public buildTree(HashMap<GameState, GameTree> states, int depth) {
    if(depth == 0) return;

    if(this.children.size() == 0) {
      Vector<GameState> nextStates = new Vector<GameState>();
      for(int i = 0; i < nextStates.size(); i++) {
        if(!states.containsKey(nextStates.get(i))) {
          states.put(nextStates.get(i).clone(), stateGoodness(nextStates.get(i))); // clone for vector garbage collection?
        }
        addChild(states.get(nextStates.get(i)));
      }
      nextStates = null; // for garbage collecting the new vector?
    }

    for(int child = 0; child < children.size(); child++) {
      children.get(child).buildTree(states, depth-1);
    }
  }

  public int stateGoodness(GameState gState) {
    int goodness = 0;
    int myMarks, opMarks;
    if(gState.getNextPlayer() == Constants.CELL_O) {
      myMarks = Constants.CELL_X;
      opMarks = Constants.CELL_O;
    } else {
      myMarks = Constants.CELL_O;
      opMarks = Constants.CELL_X;
    }
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
      if(!diag1 && !diag2) break;
    }
    goodness += diag1pts + diag2pts;
    return goodness;
  }

}
