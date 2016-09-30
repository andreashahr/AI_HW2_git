import java.util.*;

public class GameTree {

  public GameState gameState;
  public GameTree parent;
  public List<GameTree> children;

  public GameTree() {
    this.gameState = null;
    this.parent = null;
    this.children = new LinkedList<GameTree>();
  }

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

  public void addChild(GameTree gTreeNode) {
    children.add(gTreeNode);
  }

}
