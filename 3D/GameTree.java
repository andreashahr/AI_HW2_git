import java.util.*;

public class GameTree {

  public GameState gameState;
  public List<GameTree> children;

  public GameTree() {
    this.gameState = null;
    this.children = new LinkedList<GameTree>();
  }

  public GameTree(GameState gState) {
    this.gameState = gState;
    this.children = new LinkedList<GameTree>();
  }

  public void addChild(GameTree gTreeNode) {
    children.add(gTreeNode);
  }

}
