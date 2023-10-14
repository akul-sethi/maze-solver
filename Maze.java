import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import javalib.worldimages.*;

//to represent a Node in the Maze
class Node {
  private final ArrayList<Edge> outEdges;
  private final int row;
  private final int col;
  // color method changes this Node's render color for animation, user control
  private Color renderColor;

  Node(ArrayList<Edge> outEdges, int row, int col, Color renderColor) {
    this.outEdges = outEdges;
    this.row = row;
    this.col = col;
    this.renderColor = renderColor;
  }

  Node(int row, int col) {
    this.outEdges = new ArrayList<Edge>();
    this.row = row;
    this.col = col;
    this.renderColor = Color.LIGHT_GRAY;
  }

  // adds the given MazeNode to be included in this MazeNode's outEdges
  // EFFECT; creates a new MazeEdge object, with this MazeNode and the given
  // MazeNode; adds this MazeEdge object to this MazeNode's outEdges and the given
  // MazeNode's outEdges
  void addOutEdge(Node to, int weight) {
    Edge edge = new Edge(this, to, weight);
    this.outEdges.add(edge);
    to.outEdges.add(edge);
  }

  void addOutEdge(Edge edge) {
    this.outEdges.add(edge);
    edge.getOtherNode(this).outEdges.add(edge);
  }

  // initializes the given list with the edges of this MazeNode
  // EFFECT: adds the edges of this MazeNode to the given list
  void addEdgesToList(ArrayList<Edge> edgeList) {
    for (Edge me : this.outEdges) {
      if (!edgeList.contains(me)) {
        edgeList.add(me);
      }
    }
  }

  // adds the connected nodes to this MazeNode
  // EFFECT: adds the nodes present in validPaths to the given nodeList
  void addConnectedNodes(ArrayList<Node> nodeList, ArrayList<Node> alreadySeen,
      HashMap<Node, Node> prev, ArrayList<Edge> validPaths, boolean addAtTail) {
    for (Edge e : this.outEdges) {
      if (validPaths.contains(e)) {
        Node connectedNode = e.getOtherNode(this);
        if (!alreadySeen.contains(connectedNode)) {
          if (addAtTail) {
            nodeList.add(connectedNode);
          }
          else {
            nodeList.add(0, connectedNode);
          }
          prev.put(connectedNode, this);
        }
      }
    }
  }

  // returns whether the given MazeNode is the sameNode as this MazeNode given a
  // shift and direction
  boolean sameNodeShift(Node other, int shift, boolean row) {
    if (row) {
      return this.row == other.row + shift;
    }
    else {
      return this.col == other.col + shift;
    }
  }

  // returns whether this MazeNode connects to the given MazeNode
  boolean connectsToNode(Node mn, ArrayList<Edge> validPaths) {
    for (Node cmn : this.connectedNodes(validPaths)) {
      if (mn == cmn) {
        return true;
      }
    }

    return false;
  }

  // renders this MazeNode as an rectangular square, with borders if necessary
  WorldImage render(int width, int height, ArrayList<Edge> validPaths, int nodeDim) {
    WorldImage nodeImage = new RectangleImage(nodeDim, nodeDim, OutlineMode.SOLID,
        this.renderColor);

    for (Edge me : this.outEdges) {
      if (!validPaths.contains(me)) {
        nodeImage = me.renderBorder(nodeImage, this, nodeDim);
      }
    }
    return nodeImage;
  }

  // initializes this Node's color field
  // EFFECT: sets this Node's render color to the given color
  void color(Color color) {
    this.renderColor = color;
  }

  void resetColor() {
    if (this.row == 0 && this.col == 0) {
      this.color(Color.GREEN);
    }
    else {
      this.color(Color.LIGHT_GRAY);
    }
  }

  // returns the MazeNodes connected to this Node given the valid MazeEdges that
  // indicated which edges can connect two MazeNodes and which can't
  ArrayList<Node> connectedNodes(ArrayList<Edge> validPaths) {
    ArrayList<Node> res = new ArrayList<Node>();
    for (Edge me : this.outEdges) {
      if (validPaths.contains(me)) {
        res.add(me.getOtherNode(this));
      }
    }

    return res;
  }
}

//to represent a connection between two MazeNodes
class Edge {
  private final Node firstNode;
  private final Node secondNode;
  private final int weight;

  Edge(Node firstNode, Node secondNode, int weight) {
    this.firstNode = firstNode;
    this.secondNode = secondNode;
    this.weight = weight;
  }

  // returns 1 if this MazeEdge's weight > the given MazeEdge's weight, 0 if they
  // are the same, and -1 if the given MazeEdge's weight > this weight
  int compareTo(Edge me) {
    return this.weight - me.weight;
  }

  // returns whether this MazeEdge's firstNode and secondNode have the same
  // representatives in the given MazeUnionFind data structure
  boolean sameRepresentatives(UnionFind uf) {
    return uf.find(this.firstNode) == uf.find(this.secondNode);
  }

  // unions this Edge's firstNode and secondNode - placing it as part of the same
  // tree
  // EFFECT: maps this MazeEdge's firstNode to this MazeEdge's secondNode in the
  // MazeUnionFind data structure
  void unionNodes(UnionFind uf) {
    uf.union(this.firstNode, this.secondNode);
  }

  // renders this MazeEdge's border onto the given nodeImage of the given node
  // EFFECT: places a LineImage on above or beside the given nodeImage if
  // appropriate
  WorldImage renderBorder(WorldImage nodeImage, Node node, int nodeDim) {
    Node other = this.getOtherNode(node);

    if (other.sameNodeShift(node, -1, true)) {
      nodeImage = new AboveImage(new LineImage(new Posn(nodeDim, 0), Color.gray), nodeImage);
    }
    else if (other.sameNodeShift(node, 1, true)) {
      nodeImage = new AboveImage(nodeImage, new LineImage(new Posn(nodeDim, 0), Color.gray));
    }
    else if (other.sameNodeShift(node, 1, false)) {
      nodeImage = new BesideImage(nodeImage, new LineImage(new Posn(0, nodeDim), Color.gray));
    }
    else if (other.sameNodeShift(node, -1, false)) {
      nodeImage = new BesideImage(new LineImage(new Posn(0, nodeDim), Color.gray), nodeImage);
    }

    return nodeImage;
  }

  // returns whether this edge contains the given node
  boolean containsNode(Node mn) {
    return this.firstNode == mn || this.secondNode == mn;
  }

  // gets the other node given a MazeNode
  Node getOtherNode(Node mn) {
    if (mn == this.firstNode) {
      return this.secondNode;
    }
    else {
      return this.firstNode;
    }
  }
}

// to represent the Union-Find data structure for Kruskal's algorithm
class UnionFind {
  private final HashMap<Node, Node> representatives;

  UnionFind(HashMap<Node, Node> representatives) {
    this.representatives = representatives;
  }

  UnionFind() {
    this.representatives = new HashMap<Node, Node>();
  }

  // initializes the representatives HashMap
  // EFFECT: maps every MazeNode in the given nodes list to itself into
  // representatives
  void initRepresentatives(ArrayList<ArrayList<Node>> nodes) {
    for (int i = 0; i < nodes.size(); i += 1) {
      for (int j = 0; j < nodes.get(i).size(); j += 1) {
        Node node = nodes.get(i).get(j);
        this.representatives.put(node, node);
      }
    }
  }

  // finds the representative of the given MazeNode
  Node find(Node mn) {
    if (mn == this.representatives.get(mn)) {
      return mn;
    }
    else {
      return this.find(this.representatives.get(mn));
    }
  }

  // unions the first given MazeNode with the second given MazeNode
  // EFFECT: maps the representative of the first given MazeNode to the second
  // given MazeNode in representatives
  void union(Node mn1, Node mn2) {
    this.representatives.put(this.find(mn1), this.find(mn2));
  }

  // returns the amount of trees in this Union-Find data structure
  int treeCount() {
    ArrayList<Node> uniqueReps = new ArrayList<Node>();
    for (Node mn : this.representatives.keySet()) {
      if (!uniqueReps.contains(this.find(mn))) {
        uniqueReps.add(this.find(mn));
      }
    }

    return uniqueReps.size();
  }
}

//to represent a comparator to sort Edges by their weights
class SortByWeight implements Comparator<Edge> {
  public int compare(Edge me1, Edge me2) {
    return me1.compareTo(me2);
  }
}

// to represent the Maze
class Maze extends World {
  // ArrayList representing all the nodes in the Maze
  // not final since nodes can be reinitialized to create a different maze
  private ArrayList<ArrayList<Node>> nodes;

  // ArrayList of Edge representing all edges that can be traversed (connections
  // between Nodes that aren't walls)
  // validPaths must be reinitialized for knocking down walls, or constructing the
  // maze
  private ArrayList<Edge> validPaths;

  // width and height of the maze
  // dimensions of nodes of the maze can be reinitialized since width and height
  // can be changed
  private int width;
  private int height;
  private int sceneWidth;
  private int sceneHeight;
  private int nodeDim;
  private Node topLeft;
  private Node bottomRight;

  // searchPath and visitedNodes change based on BFS or DFS
  private ArrayList<Node> searchPath;
  private ArrayList<Node> visitedNodes;

  private final Random rand;

  // states of the Maze
  // states are mutated to allow for functionality of the program
  private boolean bfs;
  private boolean mazeConstructionControl;
  private boolean userControl;
  private boolean searchControl;
  private boolean pathAnimationControl;
  private boolean finishedPathAnimation;
  private boolean viewVisitedPaths;
  private boolean viewColorGradient;
  private int pathAnimationIndex;
  private int searchAnimationIndex;
  private boolean horizBias;
  private boolean vertBias;
  private int mazeAnimationIndex;
  private int userRow;
  private int userCol;
  private String title;
  private int wrongMoves;
  private int steps;

  Maze(int width, int height, Random rand) {
    this.nodes = new ArrayList<ArrayList<Node>>();
    this.rand = rand;
    this.nodeDim = 20;

    this.genNewMaze(width, height);
  }

  Maze(int width, int height) {
    this(width, height, new Random());
  }

  // test constructor
  Maze(ArrayList<ArrayList<Node>> nodes, ArrayList<Edge> validPaths, int width, int height,
      Random rand) {
    this.width = width;
    this.height = height;
    this.rand = rand;
    this.nodeDim = 20;
    this.sceneWidth = this.width * (this.nodeDim + 10);
    this.sceneHeight = this.height * (this.nodeDim + 10);

    this.nodes = nodes;
    this.validPaths = validPaths;

    this.topLeft = this.nodes.get(0).get(0);
    this.bottomRight = this.nodes.get(this.height - 1).get(this.width - 1);

    this.searchPath = new ArrayList<Node>();
    this.visitedNodes = new ArrayList<Node>();

    this.findPath(this.topLeft, this.bottomRight, false);

    this.resetBoardColor();
    this.resetStates();

    this.viewVisitedPaths = true;
  }

  // resets the states for this Maze
  // EFFECT: sets every state variable for this Maze world to false
  void resetStates() {
    this.bfs = false;
    this.userControl = false;
    this.searchControl = false;
    this.pathAnimationControl = false;
    this.finishedPathAnimation = false;
    this.viewColorGradient = false;
    this.horizBias = false;
    this.vertBias = false;
    this.userCol = 0;
    this.userRow = 0;
    this.pathAnimationIndex = 0;
    this.searchAnimationIndex = 0;
    this.mazeAnimationIndex = 0;
    this.topLeft.color(Color.GREEN);
    this.bottomRight.color(Color.MAGENTA);
    this.visitedNodes = new ArrayList<Node>();
    this.wrongMoves = 0;
    this.steps = 0;
  }

  // reinitializes the board color
  // EFFECT: sets all Nodes to light gray except the topLeft and bottomRight nodes
  void resetBoardColor() {
    for (int i = 0; i < this.height; i += 1) {
      for (int j = 0; j < this.width; j += 1) {
        this.nodes.get(i).get(j).resetColor();
      }
    }
  }

  // initializes this Maze's nodes with the given width and height
  // EFFECT: sets nodes to a 2d ArrayList of Nodes with given width and height and
  // connects them to their adjacent neighbors
  void initNodes(int width, int height) {
    this.nodes = new ArrayList<ArrayList<Node>>();
    int horizBiasVal = 1;

    if (this.horizBias) {
      horizBiasVal *= 50;
    }

    int vertBiasVal = 1;

    if (this.vertBias) {
      vertBiasVal *= 50;
    }

    for (int i = 0; i < height; i += 1) {
      this.nodes.add(new ArrayList<Node>());
      for (int j = 0; j < width; j += 1) {
        this.nodes.get(i).add(new Node(i, j));
      }
    }

    for (int i = 0; i < height; i += 1) {
      for (int j = 0; j < width; j += 1) {
        if (i < this.height - 1) {
          this.nodes.get(i).get(j).addOutEdge(this.nodes.get(i + 1).get(j),
              this.rand.nextInt(100) * horizBiasVal);
        }

        if (j < this.width - 1) {
          this.nodes.get(i).get(j).addOutEdge(this.nodes.get(i).get(j + 1),
              this.rand.nextInt(100) * vertBiasVal);
        }
      }
    }
  }

  // generates a new Maze
  // EFFECT: connects new Nodes, sets topLeft and bottomRight, finds the valid
  // paths, and sets the state as user controlled
  void genNewMaze(int width, int height) {
    this.width = width;
    this.height = height;
    this.sceneWidth = this.width * (this.nodeDim + 10);
    this.sceneHeight = this.height * (this.nodeDim + 10);

    this.initNodes(this.width, this.height);

    this.topLeft = this.nodes.get(0).get(0);
    this.bottomRight = this.nodes.get(this.height - 1).get(this.width - 1);

    this.validPaths = this.findValidPaths();

    this.searchPath = new ArrayList<Node>();
    this.visitedNodes = new ArrayList<Node>();

    this.findPath(this.topLeft, this.bottomRight, false);

    this.setMazeAnimationState();

    this.viewVisitedPaths = true;
  }

  // sets the state for maze construction animation
  // EFFECT: sets the state variables appropriate for maze construction
  void setMazeAnimationState() {
    this.resetStates();
    this.title = "Maze Construction";
    this.mazeConstructionControl = true;
    this.validPaths = new ArrayList<Edge>();
  }

  // sets the state for DFS animation
  // EFFECT: sets the state variables appropriate for DFS
  void setDFSState() {
    this.resetBoardColor();
    this.resetStates();
    this.bfs = false;
    this.searchControl = true;
    this.title = "Depth-First Search";
    this.findPath(this.topLeft, this.bottomRight, this.bfs);
  }

  // sets the state for BFS animation
  // EFFECT: sets the state variables appropriate for BFS
  void setBFSState() {
    this.resetBoardColor();
    this.resetStates();
    this.bfs = true;
    this.searchControl = true;
    this.title = "Breadth-First Search";
    this.findPath(this.topLeft, this.bottomRight, this.bfs);
  }

  // sets the state for path animation
  // EFFECT: sets the state variables appropriate for path animation
  void setPathAnimationState() {
    this.pathAnimationControl = true;
  }

  // sets the state for user control
  // EFFECT: sets the state variables appropriate for user control
  void setUserState() {
    this.resetBoardColor();
    this.resetStates();
    this.userControl = true;
    this.title = "User";
    this.topLeft.color(Color.yellow);
  }

  // sets the state for viewing visited paths
  // EFFECT: sets the state variables appropriate for viewing the visited paths
  void setViewVisitedPathsState() {
    this.viewVisitedPaths = !this.viewVisitedPaths;

    for (Node mn : this.visitedNodes) {
      if (this.viewVisitedPaths && mn != this.topLeft) {
        mn.color(new Color(145, 184, 242, 255));
      }
      else {
        mn.resetColor();
      }
    }

    if (this.userControl) {
      this.nodes.get(this.userRow).get(this.userCol).color(Color.YELLOW);
    }

    if (this.pathAnimationControl || this.finishedPathAnimation) {
      for (Node mn : this.searchPath) {
        mn.color(Color.BLUE);
      }
    }
  }

  // colors every square with a gradient of colors indicating how far it is from
  // the given Node
  // EFFECT: sets each Node in the maze to a color representing how far it is from
  // the given Node
  void colorGradient(Node to) {
    this.resetBoardColor();
    this.resetStates();
    this.viewColorGradient = true;
    this.title = "Color Gradient";

    this.findPath(this.topLeft, this.bottomRight, bfs);

    int maxSearchLength = this.searchPath.size();

    for (int i = 0; i < this.height; i += 1) {
      for (int j = 0; j < this.width; j += 1) {
        Node node = this.nodes.get(i).get(j);
        this.findPath(node, to, bfs);
        double lengthToNode = this.searchPath.size();
        double percentageOfMax = (lengthToNode / maxSearchLength) * 100;
        if (percentageOfMax <= 20) {
          node.color(Color.RED);
        }
        else if (percentageOfMax <= 40) {
          node.color(Color.ORANGE);
        }
        else if (percentageOfMax <= 60) {
          node.color(Color.YELLOW);
        }
        else if (percentageOfMax <= 80) {
          node.color(Color.GREEN);
        }
        else {
          node.color(Color.BLUE);
        }
      }
    }
  }

  // finds the cheapest path in this Maze that would connect all the MazeNodes
  ArrayList<Edge> findValidPaths() {
    UnionFind uF = new UnionFind();
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    ArrayList<Edge> worklist = new ArrayList<Edge>();

    uF.initRepresentatives(this.nodes);

    for (int i = 0; i < this.height; i += 1) {
      for (int j = 0; j < this.width; j += 1) {
        Node node = this.nodes.get(i).get(j);
        node.addEdgesToList(worklist);
      }
    }

    worklist.sort(new SortByWeight());

    while (uF.treeCount() > 1) {
      Edge edge = worklist.remove(0);
      if (edge.sameRepresentatives(uF)) {
        // do nothing
      }
      else {
        edgesInTree.add(edge);
        edge.unionNodes(uF);
      }
    }

    return edgesInTree;
  }

  // find the path from the first given MazeNode to the second given MazeNode
  // using
  // EFFECT: sets this Maze's searchPath and visitedNodes to the path generated
  // from the given from Node to the given to Node using the given search method
  // and the total Nodes visited, respectively
  void findPath(Node from, Node to, boolean bfs) {
    ArrayList<Node> alreadySeen = new ArrayList<Node>();
    ArrayList<Node> worklist = new ArrayList<Node>();
    HashMap<Node, Node> prevMap = new HashMap<Node, Node>();

    prevMap.put(from, from);
    worklist.add(from);

    while (!worklist.isEmpty()) {
      Node next = worklist.remove(0);

      if (next == to) {
        this.searchPath = new ArrayList<Node>();
        this.backtrack(next, prevMap, this.searchPath);

        for (int i = alreadySeen.size() - 1; i >= 0; i -= 1) {
          this.visitedNodes.add(alreadySeen.get(i));
        }
        return;
      }
      else if (alreadySeen.contains(next)) {
        // do nothing
      }
      else {
        next.addConnectedNodes(worklist, alreadySeen, prevMap, this.validPaths, bfs);
        alreadySeen.add(0, next);
      }
    }
  }

  // backtracks through the given HashMap to get the path from the given node to
  // the start
  // EFFECT: adds the nodes found by backtracking through the HashMap to the given
  // path ArrayList
  void backtrack(Node node, HashMap<Node, Node> prev, ArrayList<Node> path) {
    if (prev.get(node) == node) {
      path.add(node);
      return;
    }
    else {
      path.add(node);
      this.backtrack(prev.get(node), prev, path);
    }
  }

  // renders this Maze as an image grid
  WorldImage render() {
    WorldImage rows = new EmptyImage();
    for (ArrayList<Node> nodes : this.nodes) {
      WorldImage row = new EmptyImage();
      for (Node mn : nodes) {
        row = new BesideImage(row,
            mn.render(this.width, this.height, this.validPaths, this.nodeDim));
      }
      rows = new AboveImage(rows, row);
    }
    return rows;
  }

  // animates the maze construction, search, or path animation based on state
  // EFFECT: animates the state and incremenets animation indexes based on state
  public void onTick() {
    if (this.mazeConstructionControl) {
      if (this.mazeAnimationIndex > this.findValidPaths().size() - 1) {
        this.mazeConstructionControl = false;
        this.setUserState();
      }

      if (this.mazeAnimationIndex <= this.findValidPaths().size() - 1) {
        this.validPaths.add(this.findValidPaths().get(mazeAnimationIndex));
        mazeAnimationIndex += 1;
      }
    }

    else if (this.searchControl) {
      if (!this.viewVisitedPaths || this.searchAnimationIndex > this.visitedNodes.size() - 1) {
        this.searchControl = false;
        this.setPathAnimationState();
      }

      if (this.searchAnimationIndex <= this.visitedNodes.size() - 1) {
        this.visitedNodes.get(searchAnimationIndex).color(new Color(145, 184, 242, 255));
        if (!this.searchPath.contains(this.visitedNodes.get(searchAnimationIndex))) {
          this.wrongMoves += 1;
        }
        this.steps += 1;
        this.searchAnimationIndex += 1;
      }

    }
    else if (this.pathAnimationControl) {
      if (this.pathAnimationIndex > this.searchPath.size() - 1) {
        this.pathAnimationControl = false;
        this.finishedPathAnimation = true;
      }
      if (this.pathAnimationIndex <= this.searchPath.size() - 1) {
        this.searchPath.get(this.pathAnimationIndex).color(Color.blue);
        this.pathAnimationIndex += 1;
      }
    }
  }

  // handles key input to change states for this Maze
  // EFFECT: sets the state 
  public void onKeyEvent(String key) {
    if (this.mazeConstructionControl) {
      return;
    }

    if (key.equals("d")) {
      this.setDFSState();
    }

    if (key.equals("b")) {
      this.setBFSState();
    }

    if (key.equals("u")) {
      this.setUserState();
    }

    if (key.equals("p") && !this.searchControl && !this.viewColorGradient) {
      this.setViewVisitedPathsState();
    }

    if (key.equals("v")) {
      this.vertBias = true;

      if (this.vertBias) {
        this.horizBias = false;
      }

      this.genNewMaze(this.width, this.height);
    }

    if (key.equals("h")) {
      this.horizBias = true;

      if (this.horizBias) {
        this.vertBias = false;
      }

      this.genNewMaze(this.width, this.height);
    }

    if (key.equals("r")) {
      this.horizBias = false;
      this.vertBias = false;

      this.genNewMaze(this.width, this.height);
    }

    if (key.equals("s")) {
      this.colorGradient(this.topLeft);
    }

    if (key.equals("e")) {
      this.colorGradient(this.bottomRight);
    }

    if (this.userControl) {
      Node currNode = this.nodes.get(this.userRow).get(this.userCol);
      boolean validMove = false;

      if (key.equals("left")) {
        if (this.userCol > 0 && currNode
            .connectsToNode(this.nodes.get(this.userRow).get(this.userCol - 1), this.validPaths)) {
          this.userCol -= 1;
          validMove = true;
        }
      }

      if (key.equals("right")) {
        if (this.userCol < this.width - 1 && currNode
            .connectsToNode(this.nodes.get(this.userRow).get(this.userCol + 1), this.validPaths)) {
          this.userCol += 1;
          validMove = true;
        }
      }

      if (key.equals("up")) {
        if (this.userRow > 0 && currNode
            .connectsToNode(this.nodes.get(this.userRow - 1).get(this.userCol), this.validPaths)) {
          this.userRow -= 1;
          validMove = true;
        }
      }

      if (key.equals("down")) {
        if (this.userRow < this.height - 1 && currNode
            .connectsToNode(this.nodes.get(this.userRow + 1).get(this.userCol), this.validPaths)) {
          this.userRow += 1;
          validMove = true;
        }
      }

      if (validMove) {
        Node newNode = this.nodes.get(this.userRow).get(this.userCol);
        newNode.color(Color.YELLOW);

        if (this.viewVisitedPaths) {
          currNode.color(new Color(145, 184, 242, 255));
        }
        else {
          currNode.resetColor();
        }

        this.visitedNodes.add(newNode);

        if (!this.searchPath.contains(newNode)) {
          wrongMoves += 1;
        }

        this.steps += 1;

        if (this.nodes.get(this.height - 1).get(this.width - 1) == newNode) {
          this.setPathAnimationState();
        }
      }

    }
  }

  // handles mosue click
  // EFFECT: adds or subtracts width and height based on click location
  // not tested since not part of regular
  public void onMouseClicked(Posn pos) {
    // increase width
    if (pos.x >= this.sceneWidth / 8 - 5 && pos.x <= this.sceneWidth / 8 + 5
        && pos.y >= (27 * this.sceneHeight / 30) - 5 && pos.y <= (27 * this.sceneHeight / 30) + 5) {
      this.width += 1;
      this.genNewMaze(this.width, this.height);
    }

    // decrease width
    if (pos.x >= this.sceneWidth / 8 - 5 && pos.x <= this.sceneWidth / 8 + 5
        && pos.y >= (29 * this.sceneHeight / 30) - 5 && pos.y <= (29 * this.sceneHeight / 30) + 5) {
      this.width -= 1;

      if (this.width < 5) {
        this.width = 5;
      }
      this.genNewMaze(this.width, this.height);
    }

    // increase height
    if (pos.x >= (7 * this.sceneWidth / 8) - 5 && pos.x <= (7 * this.sceneWidth / 8) + 5
        && pos.y >= (27 * this.sceneHeight / 30 - 5) && pos.y <= (27 * this.sceneHeight / 30 + 5)) {
      this.height += 1;
      this.genNewMaze(this.width, this.height);
    }

    // decrease height
    if (pos.x >= (7 * this.sceneWidth / 8) - 5 && pos.x <= (7 * this.sceneWidth / 8) + 5
        && pos.y >= (29 * this.sceneHeight / 30 - 5) && pos.y <= (29 * this.sceneHeight / 30 + 5)) {
      this.height -= 1;

      if (this.width < 5) {
        this.height = 5;
      }
      this.genNewMaze(this.width, this.height);
    }
  }

  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.sceneWidth, this.sceneHeight);

    scene.placeImageXY(new TextImage(this.title, Color.black), this.sceneWidth / 2,
        this.sceneHeight / 14);

    if (!this.mazeConstructionControl) {
      scene.placeImageXY(new TextImage("Steps: " + this.steps, Color.black), this.sceneWidth / 2,
          this.sceneHeight / 8);
      scene.placeImageXY(new TextImage("Wrong moves: " + this.wrongMoves, Color.black),
          this.sceneWidth / 2, (7 * this.sceneHeight) / 8);
    }

    scene.placeImageXY(new TextImage("^", Color.black), this.sceneWidth / 8,
        (27 * this.sceneHeight) / 30);
    scene.placeImageXY(new TextImage("Width: " + this.width, Color.black), this.sceneWidth / 8,
        (28 * this.sceneHeight) / 30);
    scene.placeImageXY(new TextImage("v ", Color.black), this.sceneWidth / 8,
        (29 * this.sceneHeight) / 30);

    scene.placeImageXY(new TextImage("^", Color.black), (7 * sceneWidth / 8),
        (27 * sceneHeight) / 30);
    scene.placeImageXY(new TextImage("Height: " + this.height, Color.black),
        (7 * this.sceneWidth) / 8, (28 * this.sceneHeight) / 30);
    scene.placeImageXY(new TextImage("v ", Color.black), (7 * this.sceneWidth / 8),
        (29 * this.sceneHeight) / 30);

    scene.placeImageXY(this.render(), this.sceneWidth / 2, this.sceneHeight / 2);

    return scene;
  }
}

class ExamplesMazeWorld {
  Maze maze = new Maze(10, 10);

  void testBigBang(Tester t) {
    this.maze.bigBang(1000, 1000, 0.01);
  }
}

class ExamplesMaze {
  Maze testMaze;
  Node topLeft;
  Node topRight;
  Node botLeft;
  Node botRight;
  Edge topEdge;
  Edge botEdge;
  Edge leftEdge;
  Edge rightEdge;
  ArrayList<Edge> validPathsTest;

  Node nodeEdgeNull;

  Node nodeNoEdge;

  Node top;

  Node middle;

  Node right;

  Node bot;

  Node left;

  Node node0;

  Node node1;

  Node node2;

  Node node3;

  Node A;

  Node B;

  Node C;

  Node D;

  Node E;

  Node F;

  Node G;

  Edge AB;

  Edge BC;

  Edge CD;

  Edge AG;

  Edge edgeNull;

  Edge middleTop;

  Edge middleRight;

  Edge middleBot;

  Edge middleLeft;

  Edge edgeTop;

  Edge edgeLeft;

  Edge edgeRight;

  Edge edgeBot;

  ArrayList<Edge> empty;

  ArrayList<Edge> hasEdgeNull;

  ArrayList<Edge> outEdges;

  ArrayList<Edge> validPaths;

  HashMap<Node, Node> representatives;

  Random rand = new Random();

  UnionFind unionFind;

  Maze maze = new Maze(5, 5);

  void initData() {
    this.topLeft = new Node(0, 0);
    this.topRight = new Node(0, 1);
    this.botLeft = new Node(1, 0);
    this.botRight = new Node(1, 1);

    this.topEdge = new Edge(this.topLeft, this.topRight, 5);

    this.botEdge = new Edge(this.botLeft, this.botRight, 5);

    this.leftEdge = new Edge(this.topLeft, this.botLeft, 5);

    this.rightEdge = new Edge(this.topRight, this.botRight, 5);

    this.topLeft.addOutEdge(leftEdge);
    this.topLeft.addOutEdge(topEdge);
    this.botLeft.addOutEdge(botEdge);
    this.topRight.addOutEdge(rightEdge);

    this.validPathsTest = new ArrayList<Edge>();

    this.validPathsTest.add(leftEdge);
    this.validPathsTest.add(topEdge);
    this.validPathsTest.add(rightEdge);

    ArrayList<ArrayList<Node>> testNodes = new ArrayList<ArrayList<Node>>();

    ArrayList<Node> testNodeRow1 = new ArrayList<Node>();
    testNodeRow1.add(this.topLeft);
    testNodeRow1.add(this.topRight);

    testNodes.add(testNodeRow1);

    ArrayList<Node> testNodeRow2 = new ArrayList<Node>();
    testNodeRow2.add(this.botLeft);
    testNodeRow2.add(this.botRight);

    testNodes.add(testNodeRow2);

    this.testMaze = new Maze(testNodes, this.validPathsTest, 2, 2, new Random(1));

    this.A = new Node(0, 0);

    this.B = new Node(0, 1);

    this.C = new Node(0, 2);

    this.D = new Node(0, 3);

    this.E = new Node(0, 4);

    this.F = new Node(0, 5);

    this.G = new Node(0, 6);

    this.representatives = new HashMap<Node, Node>();

    representatives.put(A, E);

    representatives.put(B, A);

    representatives.put(C, E);

    representatives.put(D, E);

    representatives.put(E, E);

    representatives.put(F, E);

    representatives.put(G, G);

    this.unionFind = new UnionFind(representatives);

    this.AB = new Edge(this.A, this.B, 4);

    this.AG = new Edge(this.A, this.G, 4);

    edgeNull = new Edge(null, null, 0);

    empty = new ArrayList<Edge>();

    hasEdgeNull = new ArrayList<Edge>();

    hasEdgeNull.add(edgeNull);

    nodeEdgeNull = new Node(hasEdgeNull, 0, 0, Color.RED);

    nodeNoEdge = new Node(empty, 0, 0, Color.RED);

    node0 = new Node(0, 0);

    node1 = new Node(1, 0);

    node2 = new Node(0, 1);

    this.top = new Node(this.empty, 1, 0, Color.RED);

    this.middle = new Node(this.empty, 1, 1, Color.LIGHT_GRAY);

    this.right = new Node(this.empty, 2, 1, Color.RED);

    this.bot = new Node(this.empty, 1, 2, Color.RED);

    this.left = new Node(this.empty, 0, 1, Color.RED);

    this.middleRight = new Edge(this.middle, this.right, 5);

    this.middleTop = new Edge(this.middle, this.top, 4);

    this.middleBot = new Edge(this.middle, this.bot, 3);

    this.middleLeft = new Edge(this.middle, this.left, 2);

    this.validPaths = new ArrayList<Edge>();

    this.validPaths.add(this.middleTop);

    this.validPaths.add(this.middleRight);
  }

  // Tests addEdgesToList
  // in Node
  void testAddEdgesToList(Tester t) {

    this.initData();

    ArrayList<Edge> list = new ArrayList<Edge>();

    nodeNoEdge.addEdgesToList(list);

    t.checkExpect(list, empty);

    nodeEdgeNull.addEdgesToList(list);

    t.checkExpect(list, hasEdgeNull);

  }

  // Tests addOutEdges in Node

  void testAddOutEdges(Tester t) {

    this.initData();

    ArrayList<Edge> node0Edges = new ArrayList<Edge>();

    ArrayList<Edge> node1Edges = new ArrayList<Edge>();

    ArrayList<Edge> node2Edges = new ArrayList<Edge>();

    ArrayList<Edge> targetList = new ArrayList<Edge>();

    ArrayList<Edge> node2TargetList = new ArrayList<Edge>();

    targetList.add(new Edge(node0, node1, 0));

    node0.addOutEdge(node1, 0);

    node0.addEdgesToList(node0Edges);

    node1.addEdgesToList(node1Edges);

    t.checkExpect(node0Edges, targetList);

    t.checkExpect(node1Edges, targetList);

    targetList.add(new Edge(node0, node2, 1));

    node2TargetList.add(new Edge(node0, node2, 1));

    node0Edges = new ArrayList<Edge>();

    node0.addOutEdge(node2, 1);

    node0.addEdgesToList(node0Edges);

    node2.addEdgesToList(node2Edges);

    t.checkExpect(node0Edges, targetList);

    t.checkExpect(node2Edges, node2TargetList);

    ArrayList<Edge> middleEdges = new ArrayList<Edge>();

    ArrayList<Edge> topEdges = new ArrayList<Edge>();

    ArrayList<Edge> target = new ArrayList<Edge>();

    target.add(this.middleTop);

    this.middle.addOutEdge(this.middleTop);

    this.middle.addEdgesToList(middleEdges);

    this.top.addEdgesToList(topEdges);

    t.checkExpect(middleEdges, target);

    t.checkExpect(topEdges, target);

  }

  void testSameRepresentatives(Tester t) {

    this.initData();

    t.checkExpect(this.AB.sameRepresentatives(this.unionFind), true);

    t.checkExpect(this.AG.sameRepresentatives(this.unionFind), false);

  }

  void testUnion(Tester t) {

    this.initData();

    t.checkExpect(this.unionFind.find(this.A), this.E);

    t.checkExpect(this.unionFind.find(this.G), this.G);

    t.checkExpect(this.unionFind.treeCount(), 2);

    this.AB.unionNodes(this.unionFind);

    t.checkExpect(this.unionFind.find(this.A), this.E);

    t.checkExpect(this.unionFind.find(this.G), this.G);

    t.checkExpect(this.unionFind.treeCount(), 2);

    this.AG.unionNodes(this.unionFind);

    t.checkExpect(this.unionFind.find(this.A), this.G);

    t.checkExpect(this.unionFind.find(this.G), this.G);

    t.checkExpect(this.unionFind.treeCount(), 1);

  }

  void testContainsNode(Tester t) {

    this.initData();

    t.checkExpect(this.AB.containsNode(this.C), false);

    t.checkExpect(this.AB.containsNode(this.A), true);

    t.checkExpect(this.AB.containsNode(this.B), true);

  }

  void testGetOtherNode(Tester t) {

    this.initData();

    t.checkExpect(this.AB.getOtherNode(this.A), this.B);

    t.checkExpect(this.AB.getOtherNode(this.B), this.A);

  }

  void testBacktrack(Tester t) {

    this.initData();

    ArrayList<Node> targetPath = new ArrayList<Node>();

    ArrayList<Node> path = new ArrayList<Node>();

    targetPath.add(this.B);

    targetPath.add(this.A);

    targetPath.add(this.E);

    this.maze.backtrack(this.B, this.representatives, path);

    t.checkExpect(path, targetPath);

  }

  void testCompareTo(Tester t) {

    this.initData();

    t.checkExpect(this.middleTop.compareTo(this.middleRight), -1);

    t.checkExpect(this.middleRight.compareTo(this.middleRight), 0);

    t.checkExpect(this.middleRight.compareTo(this.middleTop), 1);

  }

  // Tests shift method in Node

  void testShift(Tester t) {

    this.initData();

    t.checkExpect(this.middle.sameNodeShift(this.left, 1, true), true);

    t.checkExpect(this.middle.sameNodeShift(this.right, -1, true), true);

    t.checkExpect(this.middle.sameNodeShift(this.middle, 0, true), true);

    t.checkExpect(this.middle.sameNodeShift(this.right, 1, true), false);

    t.checkExpect(this.middle.sameNodeShift(this.bot, -1, false), true);

    t.checkExpect(this.middle.sameNodeShift(this.top, 1, false), true);

    t.checkExpect(this.middle.sameNodeShift(this.top, -1, false), false);

  }

  // Tests union/find methods

  void testUF(Tester t) {

    this.initData();

    // test UF find all referring to E

    t.checkExpect(this.unionFind.find(A), this.E);

    t.checkExpect(this.unionFind.find(B), this.E);

    t.checkExpect(this.unionFind.find(C), this.E);

    t.checkExpect(this.unionFind.find(D), this.E);

    t.checkExpect(this.unionFind.find(E), this.E);

    t.checkExpect(this.unionFind.find(F), this.E);

    // test UF numTrees - since there are two unique parents

    t.checkExpect(this.unionFind.treeCount(), 2);

    // test find Vertex referring to itself

    t.checkExpect(this.unionFind.find(this.G), this.G);

    // test union G with A

    this.unionFind.union(this.G, this.A);

    // test find Vertex G with new representative

    t.checkExpect(this.unionFind.find(this.G), this.E);

    // test numTrees after union

    t.checkExpect(this.unionFind.treeCount(), 1);

  }

  // test Node render
  void testNodeRender(Tester t) {
    this.initData();

    t.checkExpect(this.topLeft.render(2, 2, this.validPathsTest, 20),
        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN));
    t.checkExpect(this.topRight.render(2, 2, this.validPathsTest, 20),
        new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY));
    t.checkExpect(this.botLeft.render(2, 2, this.validPathsTest, 20),
        new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
            new LineImage(new Posn(0, 20), Color.GRAY)));
    t.checkExpect(this.botRight.render(2, 2, this.validPathsTest, 20),
        new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
            new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)));
  }

  // test Edge renderBorder
  void testEdgeRenderBorder(Tester t) {
    this.initData();
    WorldImage nodeImage = new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY);

    t.checkExpect(this.botEdge.renderBorder(nodeImage, this.botLeft, 20),
        new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
            new LineImage(new Posn(0, 20), Color.GRAY)));

    t.checkExpect(this.botEdge.renderBorder(nodeImage, this.botRight, 20),
        new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
            new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY)));
  }

  // test Maze render
  void testMazeRender(Tester t) {
    this.initData();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        this.topLeft.render(2, 2, this.validPathsTest, 20)),
                    this.topRight.render(2, 2, this.validPathsTest, 20))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    this.botLeft.render(2, 2, this.validPathsTest, 20)),
                this.botRight.render(2, 2, this.validPathsTest, 20))));
  }

  // test reset board color
  void testResetBoardColor(Tester t) {
    this.initData();

    this.topRight.color(Color.BLUE);

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.resetBoardColor();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY)))));
  }

  // test resetStates
  void testResetStates(Tester t) {
    this.initData();

    // set in DFS state, where each tick plays one index of the DFS animation
    this.testMaze.setDFSState();

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // checking for changes in the Maze coloring
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, new Color(145, 184, 242, 255)))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // testing reset states stops onTick changes, but doesn't reset the board
    // completely
    this.testMaze.resetStates();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, new Color(145, 184, 242, 255)))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // tries moving animations forward, but Maze coloring stays the same
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, new Color(145, 184, 242, 255)))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));
  }

  // test setDFSState
  void testSetDFSState(Tester t) {
    this.initData();

    // test initial Maze coloring before DFS
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // set state to DFS
    this.testMaze.setDFSState();

    // checks that correct Nodes are colored for each tick
    this.testMaze.onTick();

    // check correctly colored after first tick
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // check correctly colored final product
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

  }

  // test setBFSState
  void testSetBFSState(Tester t) {
    this.initData();

    // test initial Maze coloring before BFS
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // set state to BFS
    this.testMaze.setBFSState();

    // checks that correct Nodes are colored for each tick
    this.testMaze.onTick();

    // check correctly colored after first tick
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // check correctly colored final product
    // In BFS, the bottomLeft Node is colored because it searched all adjacent nodes
    // first instead of going down one Node's edges
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));
  }

  // test setUserState
  void testUserState(Tester t) {
    this.initData();

    // test initial Maze coloring before User control
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // set user control
    this.testMaze.setUserState();

    // test Maze coloring after initial maze control
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.onKeyEvent("down");

    // test maze coloring after moving down
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // test unable to move right into a wall
    this.testMaze.onKeyEvent("right");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // test moving to final position, where it will animate the final path
    this.testMaze.onKeyEvent("up");
    this.testMaze.onKeyEvent("right");
    this.testMaze.onKeyEvent("down");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, new Color(145, 184, 242, 255)))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW)))));

    // test animation of final path
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));
  }

  // test setViewVisitedPathsState
  void testSetViewVisitedPathsState(Tester t) {
    this.initData();

    // test view paths as true with BFS

    // set state to BFS
    this.testMaze.setBFSState();

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // check correctly colored final product
    // In BFS, the bottomLeft Node is colored because it searched all adjacent nodes
    // first instead of going down one Node's edges
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // set view paths to false
    this.testMaze.setViewVisitedPathsState();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // set view paths to true
    this.testMaze.setViewVisitedPathsState();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // test view paths with user

    // set state to user control
    this.testMaze.setUserState();

    this.testMaze.onKeyEvent("down");
    this.testMaze.onKeyEvent("up");
    this.testMaze.onKeyEvent("right");
    this.testMaze.onKeyEvent("down");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, new Color(145, 184, 242, 255)))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW)))));

    // set view paths to false
    this.testMaze.setViewVisitedPathsState();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));
  }

  // test colorGradient
  void testColorGradient(Tester t) {
    this.initData();

    // test initial maze without color gradient
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // test color gradient for distance to start
    this.testMaze.colorGradient(this.topLeft);

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, new Color(255, 200, 0))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // test color gradient for distance to end
    this.testMaze.colorGradient(this.botRight);

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, new Color(255, 200, 0))))));
  }

  // test findValidPaths
  void testFindValidPaths(Tester t) {
    this.initData();

    ArrayList<Edge> validPathsExpectedRes = new ArrayList<>();

    validPathsExpectedRes.add(this.leftEdge);
    validPathsExpectedRes.add(this.topEdge);
    validPathsExpectedRes.add(this.rightEdge);

    t.checkExpect(this.testMaze.findValidPaths(), validPathsExpectedRes);
  }

  // test findPath
  void testFindPath(Tester t) {
    this.initData();

    // find path with DFS
    this.testMaze.findPath(this.topLeft, this.botRight, false);

    // this does not initialize the path
    this.testMaze.setPathAnimationState();

    // test initial state
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // check correctly colored final product with DFS path and visited nodes
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // find path with BFS
    this.testMaze.resetStates();
    this.testMaze.resetBoardColor();
    this.testMaze.findPath(this.topLeft, this.botRight, true);
    this.testMaze.setPathAnimationState();

    // test initial state
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY)))));

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));
  }

  // test setMazeAnimationState
  void testMazeAnimationState(Tester t) {
    this.initData();

    this.testMaze.setMazeAnimationState();

    // initial all walls up, remove until only one wall

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

  }

  // test onTick
  void testOnTick(Tester t) {
    this.initData();

    // maze construction
    this.testMaze.setMazeAnimationState();

    // initial all walls up, remove until only one wall
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // search control
    this.initData();
    this.testMaze.setBFSState();

    // test initial
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // checks that correct Nodes are colored for each tick
    this.testMaze.onTick();

    // check correctly colored after first tick
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // check correctly colored final product
    // In BFS, the bottomLeft Node is colored because it searched all adjacent nodes
    // first instead of going down one Node's edges
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // path control
    this.initData();

    // find path with DFS
    this.testMaze.findPath(this.topLeft, this.botRight, false);

    // this does not initialize the path
    this.testMaze.setPathAnimationState();

    // test initial state
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // check correctly colored final product with DFS path and visited nodes
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));
  }

  // test onKeyEvent
  void testOnKeyEvent(Tester t) {
    this.initData();

    // check user
    // test initial Maze coloring before User control
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // set user control
    this.testMaze.setUserState();

    // test Maze coloring after initial maze control
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.onKeyEvent("down");

    // test maze coloring after moving down
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // test unable to move right into a wall
    this.testMaze.onKeyEvent("right");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // test moving to final position, where it will animate the final path
    this.testMaze.onKeyEvent("up");
    this.testMaze.onKeyEvent("right");
    this.testMaze.onKeyEvent("down");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, new Color(145, 184, 242, 255)))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW)))));

    // test DFS
    this.initData();

    // test initial Maze coloring before DFS
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // set state to DFS
    this.testMaze.onKeyEvent("d");

    // checks that correct Nodes are colored for each tick
    this.testMaze.onTick();

    // check correctly colored after first tick
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // check correctly colored final product
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // test BFS
    this.initData();

    // test initial Maze coloring before BFS
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // set state to BFS
    this.testMaze.onKeyEvent("b");

    // checks that correct Nodes are colored for each tick
    this.testMaze.onTick();

    // check correctly colored after first tick
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // check correctly colored final product
    // In BFS, the bottomLeft Node is colored because it searched all adjacent nodes
    // first instead of going down one Node's edges
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // test show paths
    this.initData();

    // test view paths as true with BFS

    // set state to BFS
    this.testMaze.setBFSState();

    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();
    this.testMaze.onTick();

    // check correctly colored final product
    // In BFS, the bottomLeft Node is colored because it searched all adjacent nodes
    // first instead of going down one Node's edges
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // set view paths to false
    this.testMaze.onKeyEvent("p");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // set view paths to true
    this.testMaze.onKeyEvent("p");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // test view paths with user

    // set state to user control
    this.testMaze.setUserState();

    this.testMaze.onKeyEvent("down");
    this.testMaze.onKeyEvent("up");
    this.testMaze.onKeyEvent("right");
    this.testMaze.onKeyEvent("down");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, new Color(145, 184, 242, 255)))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(
                        new RectangleImage(20, 20, OutlineMode.SOLID,
                            new Color(145, 184, 242, 255)),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.YELLOW)))));

    // set view paths to false
    this.testMaze.onKeyEvent("p");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // test color gradient
    this.initData();

    // test initial maze without color gradient
    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.MAGENTA)))));

    // test color gradient for distance to start
    this.testMaze.onKeyEvent("s");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, new Color(255, 200, 0))),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)))));

    // test color gradient for distance to end
    this.testMaze.onKeyEvent("e");

    t.checkExpect(this.testMaze.render(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE)),
                    new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN))),
            new BesideImage(
                new BesideImage(new EmptyImage(),
                    new BesideImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE),
                        new LineImage(new Posn(0, 20), Color.GRAY))),
                new BesideImage(new LineImage(new Posn(0, 20), Color.GRAY),
                    new RectangleImage(20, 20, OutlineMode.SOLID, new Color(255, 200, 0))))));
  }
}