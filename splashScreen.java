/*
Justin Zhu
Ms.Krasteva
14/01/20
Seperate class for splashScreen
*/
import java.awt.*;
import hsa.Console;
import java.util.*;
import java.lang.*;     // to access Thread class

public class splashScreen extends Thread
{
    private Console c;

    public splashScreen (Console con)
    {
	c = con;
    }


    public void run ()
    {
	c.setColor (JustinZhuISP.greyBlue);
	c.fillRect (0, 0, 640, 500);
	c.setColor (Color.black);
	for (int i = 0 ; i < 25 ; i++)      // creates tiled pattern
	{
	    for (int j = 0 ; j < 32 ; j++)
	    {
		c.fillRect ((j * 20) + 1, (i * 20) + 1, 18, 18);
	    }
	}
	c.setFont (new Font ("TimesRoman", Font.PLAIN, 40));

	for (int i = 0 ; i < 25 ; i++)
	{
	    for (int j = 0 ; j < 32 ; j++)   // sequentially fills tiled pattern
	    {
		c.setColor (JustinZhuISP.greyBlue);
		c.fillRect ((j * 20) + 1, (i * 20) + 1, 18, 18);

		if (i == 2)
		{
		    c.setColor (Color.black);
		    c.drawString ("CROSSWORD", 190, 50);
		}
		else if (i == 18 && j == 1)
		{
		    splashScreenBox b = new splashScreenBox (c);
		    b.start (); // begins the box, which runs simultaneously alongside the animation
		}
		try
		{
		    Thread.sleep (5);
		}
		catch (Exception e)
		{
		}
	    }
	}
	while (!(JustinZhuISP.finished)) // waits until the thread is done running
	{

	}
    }
}

