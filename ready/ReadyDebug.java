package ready;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import java.io.IOException;

public class ReadyDebug extends Thread
{
    public static final int SUCCESS = 1;
    public static final int NO_APPROPRIATE_CONNECTOR = -11;
    public static final int BAD_LAUNCHING_CONNECTOR = -12;
    public static final int IOEXCEPTION_WHEN_ATTACHING = -13;
    public static final int INTERNAL_ERROR_WHEN_ATTACHING = -14;

    public static final int EXCLUDE_SUN = 1;
    public static final int EXCLUDE_HSA = 2;
    public static final int EXCLUDE_READY = 4;

    // Messages to the environment
    public static final int ENV_MESSAGEBOX = -41;
    public static final int ENV_PAUSE_AT_LOC = -42;
    public static final int ENV_PAUSE_NO_LOC = -43;

    // Running remote VM
    private static VirtualMachine vm;

    // Has a step occurred
    private static boolean connected;
    private static boolean stepOccurred;
    private static boolean stepping;
    private static boolean pausing;

    // Class patterns for which we don't want events
    private static String [] [] excludes =
	{
	    {},                                           // No excludes
	    {"java.*", "javax.*", "sun.*", "com.sun.*"},  // Sun excludes
	    {"hsa.*"},                                    // HSA excludes
	    {"java.*", "javax.*", "sun.*", "hsa.*"},      // Sun and HSA excludes
	    {"ready.*"},                                  // Ready excludes
	    {"java.*", "javax.*", "sun.*", "com.sun.*", "ready.*"},  // Sun and Ready excludes
	    {"hsa.*", "ready.*"},                         // HSA and Ready excludes
	    {"java.*", "javax.*", "sun.*", "hsa.*", "ready.*"}  // Sun and HSA and Ready excludes
	};

    private static String [] [] excludesBackslash;
    private static int excludeList = EXCLUDE_SUN | EXCLUDE_HSA | EXCLUDE_READY;

    // The list of eventRequests, so we can delete them.
    private static Vector eventRequests = new Vector ();

    // Load the DLL that we send all output to
    static
    {
	// Load the system library
	System.loadLibrary ("edjdll");

	// Create the excludesBackslash 2D String array.
	excludesBackslash = new String [excludes.length] [];
	for (int cnt = 0 ; cnt < excludes.length ; cnt++)
	{
	    excludesBackslash [cnt] = new String [excludes [cnt].length];
	    for (int cnt1 = 0 ; cnt1 < excludes [cnt].length ; cnt1++)
	    {
		excludesBackslash [cnt] [cnt1] =
		    excludes [cnt] [cnt1].replaceAll ("\\.", "\\\\").replaceAll ("\\*", "");
	    }
	}
    } // static clause


    // Native methods supplied by edjdll.dll
    private static native void notifyDebuggerDisconnect ();
    private static native void outputBytes (int purpose, int size,
	    byte [] data);

    // Output a string to the outputBytes routine in the DLL
    private static void outputString (int purpose, String s)
    {
	byte [] data = s.getBytes ();
	outputBytes (purpose, data.length, data);
    } // outputString


    /**
     * Attach to non-existent VM in order to load in appropriate classes
     * and prevent a delay later.
     */
    public synchronized static void attachToNothing ()
    {
	AttachingConnector connector = null;
	Map arguments;

	// Find Shared Memory connector
	List connectors = Bootstrap.virtualMachineManager ().allConnectors ();
	Iterator iter = connectors.iterator ();
	while (iter.hasNext ())
	{
	    Connector conn = (Connector) iter.next ();
	    if (conn.name ().equals ("com.sun.jdi.SharedMemoryAttach"))
	    {
		connector = (AttachingConnector) conn;
		break;
	    }
	}

	if (connector == null)
	{
	    return;
	}

	// Set the appropriate arguments
	arguments = connector.defaultArguments ();
	Connector.Argument nameArg =
	    (Connector.Argument) arguments.get ("name");
	if (nameArg == null)
	{
	    return;
	}
	nameArg.setValue ("readyshmem");

	// Attach to the VM
	try
	{
	    connector.attach (arguments);
	}
	catch (Exception exc)
	{
	    return;
	}
    } // attachToNothing method


    /**
     * Attach to VM launched by Ready (Shared memory address = "readyshmem")
     *
     * Return 1  Successful
     *        -1 "Could not find appropriate connector"
     *        -2 "Bad launching connector"
     *        -3 "Unable to attach to target VM: I/O Exception"
     *        -4 "Unable to attach to target VM: Internal Error"
     */
    public synchronized static int attachToTarget ()
    {
	AttachingConnector connector = null;
	Map arguments;

	// Find Shared Memory connector
	List connectors = Bootstrap.virtualMachineManager ().allConnectors ();
	Iterator iter = connectors.iterator ();
	while (iter.hasNext ())
	{
	    Connector conn = (Connector) iter.next ();
	    if (conn.name ().equals ("com.sun.jdi.SharedMemoryAttach"))
	    {
		connector = (AttachingConnector) conn;
		break;
	    }
	}

	if (connector == null)
	{
	    return NO_APPROPRIATE_CONNECTOR;
	}

	// Set the appropriate arguments
	arguments = connector.defaultArguments ();
	Connector.Argument nameArg =
	    (Connector.Argument) arguments.get ("name");
	if (nameArg == null)
	{
	    return BAD_LAUNCHING_CONNECTOR;
	}
	nameArg.setValue ("readyshmem");

	// Attach to the VM
	try
	{
	    vm = connector.attach (arguments);
	}
	catch (IOException exc)
	{
	    return IOEXCEPTION_WHEN_ATTACHING;
	}
	catch (IllegalConnectorArgumentsException exc)
	{
	    return INTERNAL_ERROR_WHEN_ATTACHING;
	}

	connected = true;
	stepping = false;
	pausing = false;

	// Spawn an event listener
	(new ReadyDebug ()).start ();

	return SUCCESS;
    } // attachToTarget method


    public synchronized static void detachFromTarget ()
    {
	try
	{
	    vm.dispose ();
	}
	catch (Exception e)
	{
	    // Don't bother capturing exceptions.  There's already a good
	    // chance that the vm is gone by the time we're trying to sever
	    // the attachment.
	}
    } // detachFromTarget


    /**
     * A VMDisconnectedException has happened while dealing with
     * another event. We need to flush the event queue, dealing only
     * with exit events (VMDeath, VMDisconnect) so that we terminate
     * correctly.
     */
    private static synchronized void handleDisconnectedException ()
    {
	EventQueue queue = vm.eventQueue ();
	while (connected)
	{
	    try
	    {
		EventSet eventSet = queue.remove ();
		EventIterator iter = eventSet.eventIterator ();
		while (iter.hasNext ())
		{
		    Event event = iter.nextEvent ();
		    if ((event instanceof VMDeathEvent) ||
			    (event instanceof VMDisconnectEvent))
		    {
			handleVMDeathOrDisconnectEvent ();
		    }
		}
		eventSet.resume (); // Resume the VM
	    }
	    catch (InterruptedException exc)
	    {
		// ignore
	    }
	}
    } // handleDisconnectedException


    private static void handleStepEvent (StepEvent stepEvent)
    {
	if (pausing)
	{
	    // If the thread we're executing has user code, then pause
	    // at the user code line.
	    Location loc = FindUserFrameInThread (stepEvent.thread ());
	    if (loc != null)
	    {
		// Display where the step took place
		loc = stepEvent.location ();

		try
		{
		    outputString (ENV_PAUSE_AT_LOC,
			    loc.sourcePath () + ":" + loc.lineNumber ());
		}
		catch (com.sun.jdi.AbsentInformationException e)
		{
		    outputString (ENV_PAUSE_NO_LOC, null);
		}
	    }
	    else
	    {
		// Let's look at all the stack frames and see how many have user
		// program frames in them.
		List userLocs = FindUserFrame ();

		if (userLocs.size () == 0)
		{
		    // There is no user code being executed.  Pause with no location.
		    outputString (ENV_PAUSE_NO_LOC, null);
		    return;
		}
		else if (userLocs.size () == 1)
		{
		    // There is one thread where user code is being executed.
		    // Pause with this location.
		    loc = (Location) userLocs.get (0);
		    try
		    {
			outputString (ENV_PAUSE_AT_LOC,
				loc.sourcePath () + ":" + loc.lineNumber ());
		    }
		    catch (com.sun.jdi.AbsentInformationException e)
		    {
			outputString (ENV_PAUSE_NO_LOC, null);
		    }
		    return;
		}
		else
		{
		    // Okay, there are multiple threads where user threads are being
		    // executed.  At this point, choose a random thread.
		    int index = (int) (Math.random () * userLocs.size ());
		    // Pause at the random thread in which user code is
		    // being executed.
		    loc = (Location) userLocs.get (index);
		    try
		    {
			outputString (ENV_PAUSE_AT_LOC,
				loc.sourcePath () + ":" + loc.lineNumber ());
		    }
		    catch (com.sun.jdi.AbsentInformationException e)
		    {
			outputString (ENV_PAUSE_NO_LOC, null);
		    }
		} // if (userLocs.size () >= 2)
	    } // if (loc == null)
	} // if (pausing)

	// Eliminate all the remaining step requests
	EventRequestManager mgr = vm.eventRequestManager ();
	mgr.deleteEventRequests (mgr.stepRequests ());
	mgr.deleteEventRequests (mgr.threadStartRequests ());

	stepping = false;
	pausing = false;
    } // handleStepEvent


    private static void handleThreadStartEvent (ThreadStartEvent stepEvent)
    {
	if (stepping || pausing)
	{
	    EventRequestManager mgr = vm.eventRequestManager ();
	    int stepSize = StepRequest.STEP_LINE;

	    if (pausing)
	    {
		stepSize = StepRequest.STEP_MIN;
	    }

	    // Create a new step request for the new thread
	    ThreadReference thread = stepEvent.thread ();
	    StepRequest sr = mgr.createStepRequest (thread,
		    stepSize, StepRequest.STEP_INTO);
	    if (!pausing)
	    {
		for (int i = 0 ; i < excludes [excludeList].length ; ++i)
		{
		    sr.addClassExclusionFilter (excludes [excludeList] [i]);
		}
	    }
	    sr.setSuspendPolicy (EventRequest.SUSPEND_ALL);
	    sr.addCountFilter (1);
	    sr.enable ();
	}
    } // handleThreadStartEvent


    private static void handleVMDeathOrDisconnectEvent ()
    {
	notifyDebuggerDisconnect ();
	vm.dispose ();
	vm = null;
	connected = false;
    } // handleVMDeathOrDisconnectEvent


    // We pause the VM in the following fashion:
    // First we suspend the VM and look at the stack frames in all extant
    // threads.  If only one thread has a frame in the user's program, then
    // we pause, labelling the lowest Frame found in the user's program as
    // our pause line.
    // If no threads have any frames from any thread are in the user's program
    // then we pause with a message "program paused".
    // If there are multiple threads which are have user program frames,
    // then we take a microstep.  If the frame that stops in the microstep
    // has a user program frame, then we use it.  Otherwise we determine
    // randomly from among the threads with a user program frame.
    public synchronized static void pauseVM ()
    {
	vm.suspend ();

	// Let's look at all the stack frames and see how many have user
	// program frames in them.
	List userLocs = FindUserFrame ();
	if (userLocs.size () == 0)
	{
	    // There is no user code being executed.  Pause with no location.
	    outputString (ENV_PAUSE_NO_LOC, null);
	    return;
	}
	else if (userLocs.size () == 1)
	{
	    // There is one thread where user code is being executed.
	    // Pause with this location.
	    Location loc = (Location) userLocs.get (0);
	    try
	    {
		outputString (ENV_PAUSE_AT_LOC,
			loc.sourcePath () + ":" + loc.lineNumber ());
	    }
	    catch (com.sun.jdi.AbsentInformationException e)
	    {
		outputString (ENV_PAUSE_NO_LOC, null);
	    }
	    return;
	}

	// Okay, there are multiple threads where user threads are being
	// executed.  Execute a single step and see if there is user code
	// in the thread that executes.  If not, then choose a random
	// thread with user code in it.
	pausing = true;
	stepVM ();
    } // pauseVM


    public synchronized static void resumeVM ()
    {
	// List all events
	EventRequestManager mgr = vm.eventRequestManager ();
	outputString (-10, mgr.stepRequests ().toString () + mgr.threadStartRequests ().toString ());
	vm.resume ();
    } // resumeVM


    // This method runs continuously once connected to a VM, handling
    // any events from the VM that we have requested.
    public void run ()
    {
	EventQueue queue = vm.eventQueue ();
	while (connected)
	{
	    try
	    {
		EventSet eventSet = queue.remove ();
		EventIterator iter = eventSet.eventIterator ();
		while (iter.hasNext ())
		{
		    Event event = iter.nextEvent ();
		    if (event instanceof StepEvent)
		    {
			handleStepEvent ((StepEvent) event);
		    }
		    else if (event instanceof ThreadStartEvent)
		    {
			handleThreadStartEvent ((ThreadStartEvent) event);
		    }
		    else if ((event instanceof VMDeathEvent) ||
			    (event instanceof VMDisconnectEvent))
		    {
			handleVMDeathOrDisconnectEvent ();
		    }
		}
	    }
	    catch (InterruptedException exc)
	    {
		// Ignore
	    }
	    catch (VMDisconnectedException discExc)
	    {
		handleDisconnectedException ();
		break;
	    }
	} // while (connected)
    } // run


    // This takes a suspended VM and makes a StepRequest on each of the
    // threads.  It also installs the eventRe to add new threads,
    // in which case
    // it adds a STEP_INTO
    private static void stepVM ()
    {
	EventRequestManager mgr = vm.eventRequestManager ();
	int stepSize = StepRequest.STEP_LINE;

	if (pausing)
	{
	    stepSize = StepRequest.STEP_MIN;
	}

	// Add a StepRequest for each thread
	List threads = vm.allThreads ();
	Iterator it = threads.iterator ();
	while (it.hasNext ())
	{
	    ThreadReference thread = (ThreadReference) it.next ();
	    StepRequest sr = mgr.createStepRequest (thread,
		    stepSize, StepRequest.STEP_INTO);
	    if (!pausing)
	    {
		for (int i = 0 ; i < excludes [excludeList].length ; ++i)
		{
		    sr.addClassExclusionFilter (excludes [excludeList] [i]);
		}
	    }
	    sr.setSuspendPolicy (EventRequest.SUSPEND_ALL);
	    sr.addCountFilter (1);
	    sr.enable ();
	}

	// Add a ThreadStart request
	ThreadStartRequest tsr = mgr.createThreadStartRequest ();
	tsr.setSuspendPolicy (EventRequest.SUSPEND_ALL);
	tsr.enable ();

	stepping = true;

	outputString (-10, "Added step event requests");
	vm.resume ();
    } // stepVM


    // Go through all the threads and return the list of locations in
    // of threads running user code.
    private static List FindUserFrame ()
    {
	List userLocs = new ArrayList ();

	// Let's look at all the stack frames and see how many have user
	// program frames in them.
	List threads = vm.allThreads ();
	Iterator it = threads.iterator ();

	while (it.hasNext ())
	{
	    ThreadReference thread = (ThreadReference) it.next ();

	    Location loc = FindUserFrameInThread (thread);
	    if (loc != null)
	    {
		userLocs.add (loc);
	    }
	} // while (each thread)

	return userLocs;
    } // FindUserFrame


    // Go through all the threads and return the list of locations in
    // of threads running user code.
    private static Location FindUserFrameInThread (ThreadReference thread)
    {
	int frameCount;

	// Index through the frames, starting at the bottom
	try
	{
	    frameCount = thread.frameCount ();
	}
	catch (IncompatibleThreadStateException e)
	{
	    // Don't get any location information on this thread
	    return null;
	}

	for (int frame = frameCount ; frame > 0 ; frame--)
	{
	    Location loc;
	    String sourcePath;

	    try
	    {
		loc = thread.frame (frame - 1).location ();
	    } // try
	    catch (com.sun.jdi.IncompatibleThreadStateException e)
	    {
		continue;
	    } // catch

	    try
	    {
		sourcePath = loc.sourcePath ();
	    } // try
	    catch (com.sun.jdi.AbsentInformationException e)
	    {
		continue;
	    } // catch

	    // Is this frame supposed to be hidden?
	    if (loc.lineNumber () != -1)
	    {
		boolean inHiddenPart = false;
		for (int cnt = 0 ; cnt < excludesBackslash [excludeList].length ; cnt++)
		{
		    if (sourcePath.startsWith (excludesBackslash [excludeList] [cnt]))
		    {
			inHiddenPart = true;
			break;
		    } // if
		} // for

		// Not found in any of the exclude lists
		if (!inHiddenPart)
		{
		    return loc;
		}
	    } // if
	} // for (each frame in the stack)
	return null;
    } // FindUserFrameInThread
} // ReadyDebug class


