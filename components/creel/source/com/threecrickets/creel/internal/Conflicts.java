/**
 * Copyright 2015-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.internal;

import java.util.ArrayList;
import java.util.LinkedList;

import com.threecrickets.creel.Engine;
import com.threecrickets.creel.Module;
import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.exception.IncompatibleIdentifiersException;

/**
 * Manages a collection of conflicts.
 * 
 * @author Tal Liron
 */
public class Conflicts extends ArrayList<com.threecrickets.creel.Conflict>
{
	//
	// Operations
	//

	/**
	 * Finds all conflicts.
	 * 
	 * @param modules
	 *        The modules
	 */
	public void find( Iterable<Module> modules )
	{
		LinkedList<Module> potentialConflicts = new LinkedList<Module>();
		for( Module module : modules )
			potentialConflicts.add( module );

		clear();
		while( !potentialConflicts.isEmpty() )
		{
			Module module = potentialConflicts.pop();
			Conflict conflict = new Conflict();
			conflict.add( module );
			for( Module otherModule : potentialConflicts )
			{
				try
				{
					if( module.getIdentifier().compareTo( otherModule.getIdentifier() ) != 0 )
						conflict.add( otherModule );
				}
				catch( IncompatibleIdentifiersException x )
				{
				}
			}
			if( conflict.size() > 1 )
			{
				conflict.sort();
				add( conflict );
			}
		}
	}

	/**
	 * Resolves all conflicts according to the conflict resolution policy.
	 * 
	 * @param policy
	 *        The policy
	 * @param notifier
	 *        The notifier or null
	 */
	public void resolve( Engine.ConflictPolicy policy, Notifier notifier )
	{
		if( notifier == null )
			notifier = new Notifier();

		for( com.threecrickets.creel.Conflict conflict : this )
		{
			Conflict theConflict = (Conflict) conflict;
			theConflict.choose( policy );
			notifier.info( "Resolved " + ( theConflict.size() + 1 ) + "-way conflict to " + conflict.getChosen().getIdentifier() + " in " + conflict.getChosen().getIdentifier().getRepository().getId() + " repository" );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
