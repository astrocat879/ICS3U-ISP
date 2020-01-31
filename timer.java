/*
Justin Zhu
Ms.Krasteva
14/01/20
Timer - A seperate class is needed because it must run simultaneously alongside the game
*/
import java.awt.*;
import hsa.Console;
import java.util.*;
import java.lang.*;     // to access Thread class

public class timer extends Thread
{
    private Console c2;
    private Timer t;

    public timer (Console con)
    {
	c2 = con;
    }


    public void run ()
    {
	t = new Timer ();
	long startTime = System.currentTimeMillis ();   // takes current time
	JustinZhuISP.elapsedTime = 0L;  // keeps track of elapsed time

	while (!(JustinZhuISP.won)) // run until user hasn't won
	{
	    try
	    {
		Thread.sleep (50);
	    }
	    catch (Exception e)
	    {
	    }
	    JustinZhuISP.elapsedTime = ((new Date ()).getTime () - startTime) / 10;     // gets current time in seconds
	    synchronized (c2)
	    {   // draws timer to console
		c2.setColor (Color.white);
		c2.fillRect (420, 50, 218, 40);
		c2.setColor (Color.black);
		c2.setFont (new Font ("Helvetica", Font.PLAIN, 20));    
		c2.drawString ("Timer: " + JustinZhuISP.elapsedTime / 100.0, 450, 75);
	    }
	}
    }
}

