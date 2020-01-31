package ready;

import java.io.*;
import java.util.*;

public class ReadySetUp
{
    static final String MAIN_THREAD_NAME = "Main Program";
    static AppletRunner appletRunner;
    static Vector suspendedThreads = new Vector ();
   
    public static void initialize ()
    {
	InputStream in;
	PrintStream out;

	in = new ReadyInputStream ();
	System.setIn (in);

	out = new PrintStream (new ReadyOutputStream ());
	System.setOut (out);
	System.setErr (out);

	Thread.currentThread ().setName (MAIN_THREAD_NAME);
    } // initialize method


    public static void runApplet (java.applet.Applet applet, int appletWidth,
	    int appletHeight, String [] appletParams, String codeBase,
	    String documentBase)
    {
	appletRunner = new AppletRunner (applet, appletParams, appletWidth, 
					 appletHeight, codeBase, documentBase);
    } // runApplet method


    public static void pauseJavaThreads ()
    {
	ThreadGroup rootGroup;

	rootGroup = Thread.currentThread ().getThreadGroup ();
	while (rootGroup.getParent () != null)
	{
	    rootGroup = rootGroup.getParent ();
	}

	// Get a list of all the threads
	Thread [] threads = new Thread [rootGroup.activeCount ()];
	int count = rootGroup.enumerate (threads, true);

	// Kill all the non-daemon threads in the list except itself
	for (int i = 0 ; i < count ; i++)
	{
	    Thread t = threads [i];

	    if (t.getName ().equals (MAIN_THREAD_NAME))
	    {
		suspendedThreads.addElement (t);
		t.suspend ();
	    }
	}

	// All active threads should be paused now...
    } // pausedJavaThreads method


    public static void resumeJavaThreads ()
    {
	for (int cnt = 0 ; cnt < suspendedThreads.size () ; cnt++)
	{
	    Thread t = (Thread) suspendedThreads.elementAt (cnt);
	    t.resume ();
	}

	suspendedThreads = new Vector ();

	// All active threads should be resumed now...
    } // resumeJavaThreads method
} // ReadySetUp class
