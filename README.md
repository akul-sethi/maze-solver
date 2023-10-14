# maze-solver
Creates random mazes utilizing Kruskal's algorithm and solve them using DFS and BFS. Includes many additional features such as user mode and the ability to construct mazes with differing corridor biases.

# User Guide
Keybinds:

up - Moves user up in User Mode.

right - Moves user right in User Mode.

left - Moves user left in User Mode.

down - Moves user down in User Mode.

d - Performs a depth-first search.

b - Performs a breadth-first search.

r - Resets the maze.

p - Pathing viewing toggle. When on, shows all visited sqaures in a light blue (defaults to on).

u - User Mode toggle.

s - Shows a gradient indicating how far every square is from the start point assuming the rules of the maze are obeyed. Red indicates close and blue indicates far.

e - Shows a gradient indicating how far every square is from the end point assuming the rules of the maze are obeyed. Red indicates close and blue indicates far.

v - Creates a new maze with a bias for vertical corridors
h - Creates a new Maze with a bias for horizontal corridors;

Running the program creates a 10x10 randomly generated maze in User Mode.
In the testBigBang method, where
Maze maze = new Maze(10, 10) different values can be put in the place of 10 to test other dimensions of the default maze (Testing 100x60).

Any time a new maze is generated an animation plays showing each edge being individually removed.

Additionally, clicking the up and down arrows labeled as width and height allows the user to change the dimenions of the maze without restarting the program.

In any mode, DFS, BFS, or User whenever the end of the maze is reached, a path of blue squares is drawn to indicate the path from the start to finish.

There are two counters in the game. The one labeled "steps" indicates how many steps as been taken by either the algorithm or the user. The counter labeled "wrong moves" indicates how many nodes have been visited that are not on the correct path.
