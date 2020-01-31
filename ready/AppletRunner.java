// The AppletRunner class is a slightly modified version of the MainFrame
// class by Jef Poskanzer <jef@acme.com>.  While the comments have not been
// changed, this is not the original class, it has been modified.  You
// can get the original at
//
//          http://www.acme.com/java/software/Acme.MainFrame.html
//
// Thank you Jef!

/*************************************************************************/
// MainFrame - run an Applet as an application
//
// Copyright (C)1996,1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// Visit the ACME Labs Java page for up-to-date versions of this and other
// fine Java utilities: http://www.acme.com/java/
/*************************************************************************/

package ready;

import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;


public class AppletRunner extends Frame
    implements Runnable, AppletStub, AppletContext
{
    private String [] args = null;
    private static int instances = 0;
    private String name;
    private Applet applet;
    private Label label = null;
    private Dimension appletSize;
    private String codeBase, documentBase;
    
    private static final String PARAM_PROP_PREFIX = "parameter.";

    /// Constructor with everything specified.
    public AppletRunner (Applet applet, String [] args, int width, int height,
    			 String codeBase, String documentBase)
    {
	++instances;
	this.applet = applet;
	this.args = args;
	this.codeBase = codeBase;
	this.documentBase = documentBase;
	applet.setStub (this);
	name = applet.getClass ().getName ();
	setTitle (name);

	// Set up properties.
	Properties props = System.getProperties ();
	props.put ("browser", "Acme.MainFrame");
	props.put ("browser.version", "11jul96");
	props.put ("browser.vendor", "Acme Laboratories");
	props.put ("browser.vendor.url", "http://www.acme.com/");

	// Turn args into parameters by way of the properties list.
	if (args != null)
	    parseArgs (args, props);

	// If width and height are specified in the parameters, override
	// the compiled-in values.
	String widthStr = getParameter ("width");
	if (widthStr != null)
	    width = Integer.parseInt (widthStr);
	String heightStr = getParameter ("height");
	if (heightStr != null)
	    height = Integer.parseInt (heightStr);

	// Were width and height specified somewhere?
	if (width == -1 || height == -1)
	{
	    System.err.println ("Width and height must be specified.");
	    return;
	}

	// Lay out components.
	setLayout (new BorderLayout ());
	add ("Center", applet);

	label = new Label ("");
	label.setBackground (Color.lightGray);
	add ("South", label);

	// Set up size.
	pack ();
	validate ();
	appletSize = applet.size ();
	applet.resize (width, height);

	// Place frame in the upper-right corner of the screen
	java.awt.Dimension screenSize = getToolkit ().getScreenSize ();
	Point loc;
	loc = new java.awt.Point ((screenSize.width - getSize ().width), 0);
	setLocation (loc);

	// Start a separate thread to call the applet's init() and start()
	// methods, in case they take a long time.
	(new Thread (this)).start ();
    } // AppletRunner constructor


    // Turn command-line arguments into Applet parameters, by way of the
    // properties list.
    private static void parseArgs (String [] args, Properties props)
    {
	for (int i = 0 ; i < args.length ; ++i)
	{
	    String arg = args [i];
	    int ind = arg.indexOf ('=');
	    if (ind == -1)
		props.put (PARAM_PROP_PREFIX + arg.toLowerCase (), "");
	    else
		props.put (
			PARAM_PROP_PREFIX + arg.substring (0, ind).toLowerCase (),
			arg.substring (ind + 1));
	}
    } // parseArgs method


    /// Event handler for the menu bar.
    public boolean handleEvent (Event evt)
    {
	switch (evt.id)
	{
		/*
			    case Event.ACTION_EVENT:
				if (evt.arg.equals ("Restart"))
				{
				    applet.stop ();
				    applet.destroy ();
				    Thread thread = new Thread (this);
				    thread.start ();
				}
				else if (evt.arg.equals ("Clone"))
				{
				    try
				    {
					new MainFrame (
						(Applet) applet.getClass ().newInstance (), args,
						appletSize.width, appletSize.height);
				    }
				    catch (IllegalAccessException e)
				    {
					showStatus (e.getMessage ());
				    }
				    catch (InstantiationException e)
				    {
					showStatus (e.getMessage ());
				    }
				}
				else if (evt.arg.equals ("Close"))
				{
				    setVisible (false);
				    remove (applet);
				    applet.stop ();
				    applet.destroy ();
				    if (label != null)
					remove (label);
				    dispose ();
				    --instances;
				    if (instances == 0)
					System.exit (0);
				}
				else if (evt.arg.equals ("Quit"))
				    System.exit (0);
				break;
		*/
	    case Event.WINDOW_DESTROY:
		System.exit (0);
		break;
	}
	return super.handleEvent (evt);
    } // handleEvent method


    // Methods from Runnable.

    /// Separate thread to call the applet's init() and start() methods.
    public void run ()
    {
	showStatus (name + " initializing...");
	applet.init ();
	validate ();
	show ();
	showStatus (name + " starting...");
	applet.start ();
	validate ();
	showStatus (name + " running...");
    } // run mehtod


    // Methods from AppletStub.

    public boolean isActive ()
    {
	return true;
    } // isActive method


    public URL getDocumentBase ()
    {
	// Returns the directory passed into the Applet runner initialization.
	String urlDir = documentBase.replace (File.separatorChar, '/');
	try
	{
	    return new URL ("file:" + urlDir + "/");
	}
	catch (MalformedURLException e)
	{
	    return null;
	}
    } // getDocumentBase method


    public URL getCodeBase ()
    {
	// Returns the directory passed into the Applet runner initialization.
	String urlDir = codeBase.replace (File.separatorChar, '/');
	try
	{
	    return new URL ("file:" + urlDir + "/");
	}
	catch (MalformedURLException e)
	{
	    return null;
	}
    } // getCodeBase method


    public String getParameter (String name)
    {
	// Return a parameter via the munged names in the properties list.
	return System.getProperty (PARAM_PROP_PREFIX + name.toLowerCase ());
    } // getParameter method


    public void appletResize (int width, int height)
    {
	// Change the frame's size by the same amount that the applet's
	// size is changing.
	Dimension frameSize = size ();
	// Dimension frameSize = getSize();
	frameSize.width += width - appletSize.width;
	frameSize.height += height - appletSize.height;
	resize (frameSize);
	// setSize( frameSize );
	appletSize = applet.size ();
	// appletSize = applet.getSize();
    } // appletResize method


    public AppletContext getAppletContext ()
    {
	return this;
    } // getAppletContext class


    // Methods from AppletContext.

    public AudioClip getAudioClip (URL url)
    {
	// This is an internal undocumented routine.  However, it
	// also provides needed functionality not otherwise available.
	// I suspect that in a future release, JavaSoft will add an
	// audio content handler which encapsulates this, and then
	// we can just do a getContent just like for images.
	return new sun.applet.AppletAudioClip (url);
    }


    public Image getImage (URL url)
    {
	Toolkit tk = Toolkit.getDefaultToolkit ();
	try
	{
	    ImageProducer prod = (ImageProducer) url.getContent ();
	    return tk.createImage (prod);
	}
	catch (IOException e)
	{
	    return null;
	}
    } // getImage method


    public Applet getApplet (String name)
    {
	// Returns this Applet or nothing.
	if (name.equals (this.name))
	    return applet;
	return null;
    } // getApplet method


    public Enumeration getApplets ()
    {
	// Just yields this applet.
	Vector v = new Vector ();
	v.addElement (applet);
	return v.elements ();
    } // getApplets method


    public void showDocument (URL url)
    {
	// Ignore.
    } // showDocument method


    public void showDocument (URL url, String target)
    {
	// Ignore.
    } // showDocument method


    public void showStatus (String status)
    {
	if (label != null)
	    label.setText (status);
    } // showStatus method
} // AppletRunner class
