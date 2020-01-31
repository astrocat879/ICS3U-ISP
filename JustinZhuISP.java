/*
    Justin Zhu
    14/01/20
    Ms. Krasteva
    This is a fully featured program, which is a crossword game.

    The user is shown a spalshScreen. Then, they are prompted for 5 choices.

    1.  Play

	The program asks if you would like to play timed, or untimed. Then, a game board is displayed, along with a side bar with more information.
	Instructions, timer, and clues are displayed in the sidebar. The main game is on the left and features the black box method, which returns true, if the player wins.

    2. Instructions

	Displays instructions.

    3. High Scores

	Shows top 10 players. If there aren't 10, then the top available players will be shown (no null values). For each player, their score, level, and name is displayed.

    4. Exit

	Leads into goodbye (), which contains my name, program info, and exits. 

    5. Clear Leaderboard

	Resets leaderboard to default values. Accomplished by printing null values into the file.


	Global Variables:

	Constants:
	Name            Type        Description
	green           Color       green, which is used to indicate a correctly guessed square
	blue            Color       blue, used for the cursor
	darkBlue        Color       dark blue, used for cursor when word can be inputed
	greyBlue        Color       grey blue, used for background

	Static variables:
	Name            Type        Description
	won             boolean     if the player won or not
	finished        boolean     if the splashscreen finished
	elapsedTime     long        how much time has elapsed

	Normal variables:
	Name            Type        Description
	mode            int         determines what method called in main method
	puzzleNum       int         determines the puzzle number
	x               int         x value of cursor
	y               int         y value of cursor
	selecting       boolean     if player is currently selecting
	quit            boolean     if player wants to quit
	choice          char        stores decisions
	timed           char        either '1' OR '2' based on what the user inputted
	answers         char[][]    stores answers to puzzle
	numbering       String[][]  stores string of numbering in puzzle
	numbering2      int[][]     stores int of numbering in puzzle
	guesses         char[][]    stores user guesses
	guessedCorrectlyboolean[][] stores if a square is guessed correctly
	across          String[]    stores clues from across
	down            String[]    stores clues down
	LBlevel         int[]       leaderboard level
	LBscore         int[]       leaderboard scores
	LBname          String[]    leaderboard names
	input           BufferedR   instance of bufferedreader
	output          PrintW      instance of printwriter
*/

import java.io.*;
import java.awt.*;                  //Built in java library to help console
import hsa.Console;                 //Imports class to display to a window
import javax.swing.JOptionPane;     //Used to display popup error messages to the user

public class JustinZhuISP
{
    Console c;          //Output console, c
    // All colour declarations (constants because they will not change)
    final static Color green = new Color (45, 250, 59), blue = new Color (69, 134, 255), darkBlue = new Color (54, 0, 230), greyBlue = new Color (154, 193, 217);
    final int gridSize = 11, puzzleCnt = 4;

    int mode = 0, puzzleNum, x, y;
    static long elapsedTime;
    static boolean won = false, finished = false;  // used for splashscreen to communicate if the animation finished
    boolean selecting = true, quit = false;
    // every grid will be gridSize by gridSize
    char choice, timed; // timed = '1' means level is timed, timed = '2' means level isn't timed
    char[] [] answers = new char [gridSize] [gridSize];  // contains the answers to the puzzle, used to check if the player has solved the puzzle
    String[] [] numbering = new String [gridSize] [gridSize];   // displays the board numbering (what the default state of the board looks like)
    int[] [] numbering2 = new int [gridSize] [gridSize];   // stores the integer value of board numbering
    char[] [] guesses = new char [gridSize] [gridSize];  // contains the player's guesses to the puzzle
    boolean[] [] guessedCorrectly = new boolean [gridSize] [gridSize];  // stores if the player guessed a specific cell correctly
    String[] across = new String [27];  // clues
    String[] down = new String [27];    // clues

    int[] LBlevel = new int [12];   // stores leaderboard level data
    int[] LBscore = new int [12];   // stores leaderboard score data
    String[] LBname = new String [12];   // stores leaderboard name data

    BufferedReader input;   // allows for file reading and writing
    PrintWriter output;

    public JustinZhuISP ()
    {
	c = new Console ("Crossword"); //Creates the instance of Console which will be used throughout the program
    }


    /*
	Local Variables
	Name        Type        Description
	i           int         loop iterator
	j           int         loop iterator
    */

    public void splashScreen ()
    {
	splashScreen s = new splashScreen (c);  // the splashScreen is created as a seperate class
	s.start ();
	try
	{
	    s.join ();
	}
	catch (InterruptedException e)
	{
	}
    }


    public void mainMenu ()
    {
	background ();
	readLB ();  // updates leaderboard
	c.setColor (Color.black);
	c.setFont (new Font ("Helvetica", Font.PLAIN, 20));
	c.drawRect (130, 100, 380, 300);
	c.drawString ("1. Play", 150, 150); // printing all the user's options
	c.drawString ("2. Instructions", 150, 200);
	c.drawString ("3. High Scores", 150, 250);
	c.drawString ("4. Exit", 150, 300);
	c.drawString ("5. Clear High Scores", 150, 350);
	while (true)
	{
	    choice = c.getChar ();
	    if (choice == '1' || choice == '2' || choice == '3' || choice == '4' || choice == '5')
		break;
	}
	if (choice == '1')  // changes mode based on what "choice" is
	    mode = 1;
	else if (choice == '2')
	    mode = 2;
	else if (choice == '3')
	    mode = 3;
	else if (choice == '4')
	    mode = 4;
	else if (choice == '5')
	    mode = 5;
    }


    /*
	Local Variables
	Name        Type        Description
	i           int         loop iterator
	where       int         indicator for where to print the clue on the screen
	move        char        stores the player's input
	tmp         char        stores the player's input for specific sections
	in          char        stores the player's input for specific sections
	guess       char        stores the player's guess for a specific square
	idx         int         assists with leaderboard processing, stores where to insert the new entry
	curScore    int         calculates the player's score
	name        String      stores the player's name
    */

    public void processing ()
    {
	won = false;
	c.setColor (Color.black);
	c.setFont (new Font ("Helvetica", Font.PLAIN, 15));
	c.drawString ("Would you like to play timed, or endless?", 150, 440);
	c.drawString ("1. Timed", 150, 460);
	c.drawString ("2. Endless", 150, 480);
	while (true)
	{
	    timed = c.getChar ();
	    if (timed == '1' || timed == '2')
		break;
	}
	puzzleNum = (int) (Math.random () * 1000.0) % puzzleCnt + 1;    // chooses a random puzzle

	c.setColor (greyBlue);  // creates background and grid
	c.fillRect (0, 0, 650, 500);
	c.setColor (Color.black);
	c.fillRect (0, 0, 38 * gridSize, 38 * gridSize);

	c.setFont (new Font ("Helvetica", Font.ITALIC, 20));    // draws beginning text
	c.drawString ("Puzzle No. " + puzzleNum, 450, 25);
	c.setFont (new Font ("Helvetica", Font.PLAIN, 17));

	c.setColor (Color.white);
	c.fillRect (418, 150, 222, 100);
	c.setColor (Color.black);
	c.drawString ("Clues:", 424, 170);
	readFile (puzzleNum);
	y = 0;
	x = 0;
	drawCursor (blue);

	if (timed == '1')
	{
	    timer t = new timer (c);
	    t.start (); // begins the display timer
	}

	synchronized (c)
	{
	    c.setColor (greyBlue);  // displays instructions
	    c.fillRect (420, 280, 250, 125);
	    c.setColor (Color.black);
	    c.setFont (new Font ("Helvetica", Font.PLAIN, 14));
	    c.drawString ("W - move cursor up", 450, 300);
	    c.drawString ("A - move cursor left", 450, 320);
	    c.drawString ("S - move cursor down", 450, 340);
	    c.drawString ("D - move cursor right", 450, 360);
	    c.drawString ("Space - enter a letter", 450, 380);
	    c.drawString ("Q - quit", 450, 400);
	}

	while (!(won))
	{
	    if (numbering2 [y] [x] != -1)
	    {
		c.setColor (Color.white);   // clue display
		c.fillRect (418, 175, 222, 75);

		int where = 200;
		if (!(across [numbering2 [y] [x]].equals ("")))
		{
		    c.setFont (new Font ("Helvetica", Font.PLAIN, 12));
		    c.setColor (Color.black);
		    c.drawString (numbering [y] [x] + " Across: " + across [numbering2 [y] [x]], 424, where);
		    where += 30;
		}
		if (!(down [numbering2 [y] [x]].equals ("")))
		{
		    c.setFont (new Font ("Helvetica", Font.PLAIN, 12));
		    c.setColor (Color.black);
		    c.drawString (numbering [y] [x] + " Down: " + down [numbering2 [y] [x]], 424, where);
		    where += 30;
		}
	    }
	    else
	    {

	    }
	    if (selecting)
	    {
		char move = c.getChar (); // selecting mode
		if (move == '}')    // AUTOMATIC WIN CHEAT
		{
		    won = true;
		    continue;
		}
		if (move == 'q' || move == 'Q') // if user wants to quit
		{
		    synchronized (c)
		    {
			c.setFont (new Font ("Helvetica", Font.PLAIN, 16));
			c.setColor (Color.white);
			c.fillRect (210, 440, 200, 50);
			c.setColor (Color.black);
			c.drawString ("Do you want to quit? (y/n)", 220, 470);  // confirm if user would like to quit
		    }
		    char tmp = c.getChar ();
		    while (tmp != 'y' && tmp != 'Y' && tmp != 'n' && tmp != 'N')
			tmp = c.getChar ();
		    if (tmp == 'y' || tmp == 'Y')
		    {
			won = true;
			quit = true;
			break;
		    }
		    c.setColor (greyBlue);
		    c.fillRect (210, 440, 200, 50);
		    continue;

		}
		if (numbering [y] [x].equals ("#"))
		{
		    c.setColor (Color.black);   // black cell
		    c.fillRect (38 * x + 1, 38 * y + 1, 36, 36);
		}
		else
		{
		    if (guessedCorrectly [y] [x])   // if user guessed the square correctly, set to green
			c.setColor (green);
		    else
			c.setColor (Color.white);
		    c.fillRect (38 * x + 1, 38 * y + 1, 36, 36);    // draws square
		    c.setFont (new Font ("Helvetica", Font.PLAIN, 10));
		    c.setColor (Color.black);
		    c.drawString (numbering [y] [x], 38 * x + 3, 38 * y + 10);
		    c.setFont (new Font ("Helvetica", Font.PLAIN, 27));
		    c.setColor (Color.black);
		    c.drawString ((guesses [y] [x] + ""), 38 * x + 7, 38 * y + 30);
		}
		if ((move == 'w' || move == 'W') && y - 1 >= 0) // depending on input, move the cursor location
		    y--;
		else if ((move == 's' || move == 'S') && y + 1 < gridSize)
		    y++;
		else if ((move == 'd' || move == 'D') && x + 1 < gridSize)
		    x++;
		else if ((move == 'a' || move == 'A') && x - 1 >= 0)
		    x--;
		else if (move == ' ')   // if the user wants to go into selecting mode
		{
		    if (!(numbering [y] [x].equals ("#")))  // user cannot enter a word in an occupied tile
		    {
			selecting = false;
			continue;
		    }
		    else
		    {
			JOptionPane.showMessageDialog (null, "Please choose a valid tile to input a word.", "Error", JOptionPane.ERROR_MESSAGE); // error message
		    }
		}
		drawCursor (blue);
	    }
	    else
	    {
		synchronized (c)
		{
		    c.setColor (greyBlue);  // displays instructions
		    c.fillRect (420, 280, 250, 125);
		    c.setColor (Color.black);
		    c.setFont (new Font ("Helvetica", Font.PLAIN, 14));
		    c.drawString ("Enter your guess!", 450, 300);
		}
		drawCursor (darkBlue);
		char guess = Character.toUpperCase (c.getChar ());  // convert guess to uppercase no matter what
		if (guess == 8)     // deals with backspace, which should have same effect as space
		    guess = ' ';
		if (guess != ' ')
		{
		    while (guess > 90 || guess < 65)
		    {
			JOptionPane.showMessageDialog (null, "Please enter a valid letter.", "Error", JOptionPane.ERROR_MESSAGE); // error message
			guess = Character.toUpperCase (c.getChar ());
		    }
		}
		guesses [y] [x] = guess;
		selecting = true;

		if (guesses [y] [x] == answers [y] [x]) // updates guessedCorrectly
		    guessedCorrectly [y] [x] = true;
		else
		    guessedCorrectly [y] [x] = false;

		if (guessedCorrectly [y] [x])   // shows if user guesses correctly (colour is green or white)
		    c.setColor (green);
		else
		    c.setColor (Color.white);
		c.fillRect (38 * x + 1, 38 * y + 1, 36, 36);    // redraws currently occupied cell and the cursor
		c.setFont (new Font ("Helvetica", Font.PLAIN, 10));
		c.setColor (Color.black);
		c.drawString (numbering [y] [x], 38 * x + 3, 38 * y + 10);
		c.setColor (Color.black);
		c.setFont (new Font ("Helvetica", Font.PLAIN, 27));
		c.drawString ((guesses [y] [x] + ""), 38 * x + 7, 38 * y + 30);
		drawCursor (blue);

		synchronized (c)
		{
		    c.setColor (greyBlue);  // displays instructions
		    c.fillRect (420, 280, 250, 125);
		    c.setColor (Color.black);
		    c.setFont (new Font ("Helvetica", Font.PLAIN, 14));
		    c.drawString ("W - move cursor up", 450, 300);
		    c.drawString ("A - move cursor left", 450, 320);
		    c.drawString ("S - move cursor down", 450, 340);
		    c.drawString ("D - move cursor right", 450, 360);
		    c.drawString ("Space - enter a letter", 450, 380);
		    c.drawString ("Q - quit", 450, 400);
		}
	    }
	    if (check ())
		won = true;
	}
	if (won && !(quit))     // either the user can solve the puzzle or quit
	{
	    synchronized (c)
	    {
		c.setColor (Color.black);
		c.setFont (new Font ("Helvetica", Font.PLAIN, 30));
		c.drawString ("You cleared the level!", 100, 450);
		if (timed == '1')
		{
		    c.setFont (new Font ("Helvetica", Font.PLAIN, 20));
		    c.drawString ("Your final time was " + elapsedTime / 100 + " seconds.", 100, 480);
		}
	    }
	    pauseProgram ();
	    if (timed == '1')
	    {
		background ();
		c.setFont (new Font ("Helvetica", Font.PLAIN, 30));
		c.drawString ("Your final time was " + elapsedTime / 100 + " seconds.", 100, 100);

		String name = "";   // takes in name and saves it for the leaderboard
		c.setFont (new Font ("Helvetica", Font.PLAIN, 20));
		c.drawString ("Enter your name:", 100, 150);
		char in = c.getChar ();
		while (in != '\n')  // continuously accept new characters until the user chooses to stop
		{
		    if (in == 8)
		    {
			if (name.length () != 0)
			    name = name.substring (0, name.length () - 1);
		    }
		    else
		    {
			if (name.length () != 14)
			    name += in + "";
		    }
		    c.setColor (greyBlue);
		    c.fillRect (0, 170, 600, 100);
		    c.setColor (Color.black);
		    c.drawString (name, 100, 200);
		    in = c.getChar ();
		}
		int curScore = (int) elapsedTime / 100;
		int idx = 1;        // leaderboard insertion
		while (idx <= 10 && LBscore [idx] <= curScore)
		    idx++;
		for (int i = 9 ; i >= idx ; i--)
		{ // shift all the entries down to make room for new entry
		    LBname [i + 1] = LBname [i];
		    LBscore [i + 1] = LBscore [i];
		    LBlevel [i + 1] = LBlevel [i];
		}
		LBname [idx] = name;    // insert the new value into the array
		LBscore [idx] = curScore;
		LBlevel [idx] = puzzleNum;

		writeLB ();     // saves the leaderboard to the file
		c.drawString ("Check the high scores to see how you did!", 100, 350);
		pauseProgram ();
	    }
	}
	if (quit)
	    pauseProgram ();
	quit = false;   // reset variables
	won = false;
	selecting = true;
    }

    /*
	Local Variables
	Name        Type        Description
	i           int         loop iterator
    */

    private void drawCursor (Color col)     // draws a thick-outlined blue rectangle, representing the cursor
    {
	synchronized (c)
	{
	    c.setColor (col);
	    for (int i = 1 ; i <= 4 ; i++)
		c.drawRect (38 * x + i, 38 * y + i, 37 - (2 * i), 37 - (2 * i));
	}
    }

    /*
	Local Variables
	Name        Type        Description
	i           int         loop iterator
	j           int         loop iterator
    */

    private boolean check ()  // checks if the game is in a winning state (black box return method)
    {
	for (int i = 0 ; i < gridSize ; i++)
	{
	    for (int j = 0 ; j < gridSize ; j++)
	    {
		if (!(numbering [i] [j].equals ("#")))
		    if (guesses [i] [j] != answers [i] [j])
			return false;
	    }
	}
	return true;
    }


    /*
	Local Variables
	Name        Type        Description
	i           int         loop iterator
	j           int         loop iterator
	tmp         String      stores the line from the input file
	idx         int         stores what number the clue corresponds to
    */

    private void readFile (int n)   // reads in the game grid from a text file and outputs it to the screen
    {
	c.setColor (Color.black);
	c.setFont (new Font ("Helvetica", Font.PLAIN, 10));
	try
	{
	    input = new BufferedReader (new FileReader (puzzleNum + ".txt"));   // opens the puzzle
	    for (int i = 0 ; i < gridSize ; i++)
	    {
		String tmp = input.readLine ();
		for (int j = 0 ; j < gridSize ; j++)
		{
		    numbering2 [i] [j] = -1;    // set numbering2 to default value (meaning unoccupied)
		    guessedCorrectly [i] [j] = false;   // set guessedCorrectly to default value (meaning incorrect)
		    if (tmp.charAt (j) != '#')
		    {
			c.setColor (Color.white);   // draw tile
			c.fillRect (38 * j + 1, 38 * i + 1, 36, 36);
			c.setColor (Color.black);
			if (tmp.charAt (j) != ' ')
			{
			    c.drawString ((tmp.charAt (j) - 96) + "", 38 * j + 3, 38 * i + 10);
			    numbering [i] [j] = (tmp.charAt (j) - 96) + ""; // updates numbering arrays
			    numbering2 [i] [j] = tmp.charAt (j) - 96;
			}
			else
			{ // no number assigned to this position
			    numbering [i] [j] = " ";
			}
		    }
		    else
		    {
			numbering [i] [j] = "#";
		    }
		}
	    }
	    for (int i = 0 ; i < gridSize ; i++)
	    {
		String tmp = input.readLine ();
		for (int j = 0 ; j < gridSize ; j++)
		{
		    answers [i] [j] = tmp.charAt (j);   // reads actual puzzle and copies to an array
		    guesses [i] [j] = ' ';  // reset guesses array
		}
	    }
	    for (int i = 0 ; i <= 26 ; i++)     // reset clue arrays to default values
	    {
		down [i] = "";
		across [i] = "";
	    }
	    int idx = Integer.parseInt (input.readLine ());     // processes all the clues and stores to an array
	    String tmp = input.readLine ();
	    while (!(tmp.equals ("Down:"))) // reading in "Across" clues, saving to array called across
	    {
		across [idx] = tmp;
		idx = Integer.parseInt (input.readLine ());
		tmp = input.readLine ();
	    }
	    idx = Integer.parseInt (input.readLine ());
	    tmp = input.readLine ();
	    while (!(tmp.equals ("end"))) // reading in "Down" clues, saving to array called down
	    {
		down [idx] = tmp;
		idx = Integer.parseInt (input.readLine ());
		tmp = input.readLine ();
	    }
	}

	catch (IOException e)
	{
	}
    }


    public void instructions ()
    {
	background ();  // gives user an idea of how to play the game
	c.setColor (Color.black);
	c.setFont (new Font ("Helvetica", Font.PLAIN, 15));
	c.drawString ("Welcome to this crossword game!", 50, 100);
	c.drawString ("When you start to play, you will be given one of " + puzzleCnt + " random puzzles.", 50, 150);
	c.drawString ("In order to fill in squares, you have to use the \"WASD\" keys to move your cursor.", 50, 200);
	c.drawString ("If you want to insert a character into the square, press space, then select the character.", 50, 250);
	c.drawString ("Cheat: enter '}' while in selecting phase, and you will automatically win.", 50, 300);
	c.drawString ("In addition, all the puzzle solutions are available in different text files.", 50, 350);
	c.drawString ("Happy puzzling!", 50, 400);
	pauseProgram ();
    }


    /*
	Local Variables
	Name        Type        Description
	i           int         loop iterator
	score       int         stores score of player
	lv          int         stores which level the player played
	name        int         stores the player's name
    */

    private void readLB ()  // copy text file to array
    {
	try
	{
	    input = new BufferedReader (new FileReader ("Leaderboard.txt"));
	    for (int i = 1 ; i <= 10 ; i++)
	    {
		int score = Integer.parseInt (input.readLine ());
		int lv = Integer.parseInt (input.readLine ());
		String name = input.readLine ();
		LBlevel [i] = lv;
		LBscore [i] = score;
		LBname [i] = name;
	    }
	}
	catch (IOException e)
	{
	}
    }


    private void writeLB ()  // copy array to text file
    {
	try
	{
	    output = new PrintWriter (new FileWriter ("Leaderboard.txt"));
	    for (int i = 1 ; i <= 10 ; i++)
	    {
		output.println (LBscore [i] + "");
		output.println (LBlevel [i] + "");
		output.println (LBname [i]);
	    }
	    output.close ();
	}
	catch (IOException e)
	{
	}
    }


    public void resetLB ()
    { // resets the leaderboard
	c.setFont (new Font ("Helvetica", Font.PLAIN, 15));
	c.drawString ("Are you sure you want to clear the leaderboard? (y/n)", 150, 450);   // confirms deletion
	choice = c.getChar ();
	while (choice != 'y' && choice != 'Y' && choice != 'n' && choice != 'N')
	    choice = c.getChar ();
	if (choice == 'y' || choice == 'Y')
	{
	    try
	    {
		output = new PrintWriter (new FileWriter ("Leaderboard.txt"));
		for (int i = 1 ; i <= 10 ; i++)
		{
		    output.println ("999999");      // default values
		    output.println ("0");
		    output.println ();
		}
		output.close ();
	    }
	    catch (IOException e)
	    {
	    }
	    readLB ();
	}
    }


    /*
	Local Variables
	Name        Type        Description
	i           int         loop iterator
	score       int         stores score of player
	lv          int         stores which level the player played
	name        int         stores the player's name
    */

    public void highScores ()
    {
	background ();
	c.setColor (Color.black);
	c.setFont (new Font ("Helvetica", Font.PLAIN, 20));
	c.drawString ("High Scores", 270, 100);     // draws high scores chart
	c.drawString ("Name", 150, 140);
	c.drawString ("Lv.", 300, 140);
	c.drawString ("Score", 400, 140);
	try
	{
	    input = new BufferedReader (new FileReader ("Leaderboard.txt"));
	    for (int i = 1 ; i <= 10 ; i++)     // loop over entries in the leaderboard
	    {
		int score = Integer.parseInt (input.readLine ());
		int lv = Integer.parseInt (input.readLine ());
		String name = input.readLine ();
		if (lv == 0)
		{
		    if (i == 1) // case where leaderboard is empty
		    {
			c.drawString ("No one has played yet. Will you be the first?", 100, 175);
		    }
		    break;
		}
		c.drawString (i + ".", 100, 150 + i * 25);  // displays information
		c.drawString (name, 150, 150 + i * 25);
		c.drawString (lv + "", 300, 150 + i * 25);
		c.drawString (score + "", 400, 150 + i * 25);
	    }
	}
	catch (IOException e)   // just in case the leaderboard has been damaged somehow
	{
	    JOptionPane.showMessageDialog (null, "Leaderboard.txt has error in formatting. Please clear the file.", "Error", JOptionPane.ERROR_MESSAGE); // error message
	}
	pauseProgram ();
    }


    public void goodbye ()  // displays goodbye message and exits from console
    {
	background ();
	c.setFont (new Font ("Helvetica", Font.PLAIN, 17));
	c.drawString ("This crossword game was made by Justin Zhu. Thanks for playing!", 70, 200);
	pauseProgram ();
	System.exit (0);
    }


    private void background ()      // displays a default background with the text "CROSSWORD"
    {
	synchronized (c)
	{
	    c.setColor (greyBlue);
	    c.fillRect (0, 0, 750, 600);
	    c.setColor (Color.black);
	    c.setFont (new Font ("TimesRoman", Font.PLAIN, 40));
	    c.drawString ("CROSSWORD", 190, 50);
	}
    }


    private void pauseProgram ()    // tells user to press any key to continue
    {
	synchronized (c)
	{
	    c.setColor (Color.black);
	    c.setFont (new Font ("Helvetica", Font.PLAIN, 15));
	    c.drawString ("Press any key to continue.", 450, 480);
	}
	c.getChar ();
    }


    public static void main (String[] args)
    {
	JustinZhuISP c = new JustinZhuISP ();    //Creates an instance of this class
	c.splashScreen ();
	while (true)
	{
	    c.mainMenu ();  // depending on the choice user enters
	    if (c.mode == 1)
	    {
		c.processing ();
	    }
	    else if (c.mode == 2)
	    {
		c.instructions ();
	    }
	    else if (c.mode == 3)
	    {
		c.highScores ();
	    }
	    else if (c.mode == 4)
	    {
		break;
	    }
	    else if (c.mode == 5)
	    {
		c.resetLB ();
	    }
	}
	c.goodbye ();
    }
}
