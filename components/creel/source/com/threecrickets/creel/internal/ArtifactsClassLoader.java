/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

import com.threecrickets.creel.Artifact;

/**
 * @author Tal Liron
 */
public class ArtifactsClassLoader extends URLClassLoader
{
	//
	// Construction
	//

	public ArtifactsClassLoader( Iterable<Artifact> artifacts )
	{
		super( gatherJars( artifacts ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static URL[] gatherJars( Iterable<Artifact> artifacts )
	{
		Collection<URL> urls = new ArrayList<URL>();
		for( Artifact artifact : artifacts )
		{
			File file = artifact.getFile();
			if( file.getName().toLowerCase().endsWith( ".jar" ) )
			{
				try
				{
					urls.add( file.toURI().toURL() );
				}
				catch( MalformedURLException x )
				{
				}
			}
		}
		return urls.toArray( new URL[urls.size()] );
	}
}
