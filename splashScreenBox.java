/*
Justin Zhu
Ms.Krasteva
14/01/20
Thread used in splashScreen, because the box runs at the same time as the grid animation
*/
import java.awt.*;
import hsa.Console;
import java.util.*;
import java.lang.*;     // to access Thread class

public class splashScreenBox extends Thread
{
    private Console c2;

    public splashScreenBox (Console con)
    {
	c2 = con;
    }


    public void run ()
    {
	for (int i = -390 ; i <= 150 ; i++)
	{
	    synchronized (c2)
	    {
		c2.setColor (JustinZhuISP.greyBlue);
		c2.fillRect (i - 21, 99, 382, 302);
		c2.setColor (Color.black);
		c2.drawRect (i - 20, 100, 380, 300);
		c2.setFont (new Font ("Helvetica", Font.PLAIN, 20));
		c2.drawString ("1. Play", i, 150);
		c2.drawString ("2. Instructions", i, 200);
		c2.drawString ("3. High Scores", i, 250);
		c2.drawString ("4. Exit", i, 300);
		c2.drawString ("5. Clear High Scores", i, 350);
	    }
	    try
	    {
		Thread.sleep (5);
	    }
	    catch (Exception e)
	    {
	    }
	}
	JustinZhuISP.finished = true;
    }
}

