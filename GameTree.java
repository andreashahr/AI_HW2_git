import java.util.*;

public class GameTree {

  public GameState gstate;
  public int reachableXwin, reachableOwin;
  public GameTree parent;
  public List<GameTree> children;

  public GameTree(GameState gstate) {
    this.gstate = gstate;
    this.children = new LinkedList<GameTree>();
    this.reachableXwin = 0;
    this.reachableOwin = 0;

    Vector<GameState> nextStates = new Vector<GameState>();
    gstate.findPossibleMoves(nextStates);

    for(int g = 0; g < nextStates.size(); g++) {
      addChild(nextStates.elementAt(g));
    }

    if(gstate.isEOG()) {
      GameTree upWards = this.parent;
      if(gstate.isXWin()) {
        this.reachableXwin++;
        while(upWards != null) {
          upWards.reachableXwin++;
          upWards = upWards.parent;
        }
      }
      else if(gstate.isOWin()) {
        this.reachableOwin++;
        while(upWards != null) {
          upWards.reachableOwin++;
          upWards = upWards.parent;
        }
      }
    }
  }

  public GameTree addChild(GameState child) {
    GameTree childNode = new GameTree(child);
    childNode.parent = this;
    this.children.add(childNode);
    return childNode;
  }

}
