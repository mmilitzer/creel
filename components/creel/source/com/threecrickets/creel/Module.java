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

package com.threecrickets.creel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents information about a module.
 * <p>
 * Modules are specified by a {@link ModuleSpecification}, and may optionally
 * have a {@link ModuleIdentifier}, meaning that they have been identified.
 * <p>
 * A module can have one or more dependencies (modules that it needs) as well as
 * one or more supplicants (modules that have this module as a dependency).
 * <p>
 * An "explicit" module is one that was explicitly listed as a dependency. An
 * "implicit" module is a dependency of another module. So, explicit modules
 * represent the roots of the dependency tree.
 * <p>
 * Note that an explicit module can still have supplicants: it could have been
 * explicitly listed and <i>also</i> listed as a dependency of another module.
 * 
 * @author Tal Liron
 */
public class Module
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param explicit
	 *        Whether the module is explicit
	 * @param identifier
	 *        The identifier or null
	 * @param specification
	 *        The specification
	 */
	public Module( boolean explicit, ModuleIdentifier identifier, ModuleSpecification specification )
	{
		this.explicit = explicit;
		this.identifier = identifier;
		this.specification = specification;
	}

	//
	// Attributes
	//

	/**
	 * Whether the module is explicit
	 * 
	 * @return True if explicit
	 */
	public boolean isExplicit()
	{
		return explicit;
	}

	/**
	 * Whether the module is explicit
	 * 
	 * @param explicit
	 *        True if explicit
	 */
	public void setExplicit( boolean explicit )
	{
		this.explicit = explicit;
	}

	/**
	 * The module identifier.
	 * 
	 * @return The module identifier or null
	 */
	public ModuleIdentifier getIdentifier()
	{
		return identifier;
	}

	/**
	 * The module specification
	 * 
	 * @return The module specification
	 */
	public ModuleSpecification getSpecification()
	{
		return specification;
	}

	/**
	 * The module's dependencies.
	 * 
	 * @return The dependencies
	 */
	public synchronized Iterable<Module> getDependencies()
	{
		return Collections.unmodifiableCollection( new ArrayList<Module>( dependencies ) );
	}

	/**
	 * The module's supplicants.
	 * 
	 * @return The supplicants
	 */
	public synchronized Iterable<Module> getSupplicants()
	{
		return Collections.unmodifiableCollection( new ArrayList<Module>( supplicants ) );
	}

	//
	// Operations
	//

	/**
	 * Sets another module as a dependency of this module.
	 * 
	 * @param dependency
	 *        The dependency
	 */
	public synchronized void addDependency( Module dependency )
	{
		dependencies.add( dependency );
	}

	/**
	 * Sets another module as a supplicant of this module. Makes sure that
	 * duplicate supplicants are not added.
	 * 
	 * @param module
	 *        The supplicant module
	 */
	public synchronized void addSupplicant( Module module )
	{
		boolean found = false;
		for( Module supplicant : getSupplicants() )
			if( module.getIdentifier().equals( supplicant.getIdentifier() ) )
			{
				found = true;
				break;
			}
		if( !found )
			supplicants.add( module );
	}

	/**
	 * Sets another module to not be a supplicant of this module.
	 * 
	 * @param module
	 *        The supplicant module
	 */
	public synchronized void removeSupplicant( Module module )
	{
		for( ListIterator<Module> i = supplicants.listIterator(); i.hasNext(); )
		{
			Module supplicant = i.next();
			if( module.getIdentifier().equals( supplicant.getIdentifier() ) )
			{
				i.remove();
				break;
			}
		}
	}

	/**
	 * Copies identifier, repository, and dependencies from another module
	 * instance.
	 * 
	 * @param module
	 *        The other module
	 */
	public synchronized void copyIdentificationFrom( Module module )
	{
		identifier = module.getIdentifier().clone();
		dependencies.clear();
		for( Module dependency : module.getDependencies() )
			dependencies.add( dependency );
	}

	/**
	 * Adds all supplicants of another module, and makes us explicit if the
	 * other module is explicit.
	 * 
	 * @param module
	 *        The other module
	 */
	public synchronized void mergeSupplicants( Module module )
	{
		if( module.isExplicit() )
			setExplicit( true );
		for( Module supplicant : module.getSupplicants() )
			addSupplicant( supplicant );
	}

	/**
	 * Replaces a module with another one in the dependency tree.
	 * 
	 * @param oldModule
	 *        The old module
	 * @param newModule
	 *        The new module
	 * @param recursive
	 *        True if we should recurse replacing in dependencies
	 */
	public synchronized void replaceModule( Module oldModule, Module newModule, boolean recursive )
	{
		removeSupplicant( oldModule );
		for( ListIterator<Module> i = dependencies.listIterator(); i.hasNext(); )
		{
			Module dependency = i.next();
			if( oldModule.getIdentifier().equals( dependency.getIdentifier() ) )
			{
				dependency = newModule;
				i.set( dependency );
				dependency.addSupplicant( this );
			}

			if( recursive )
				dependency.replaceModule( oldModule, newModule, true );
		}
	}

	/**
	 * Represents the module as a string.
	 * 
	 * @param longForm
	 *        True to use the long form
	 * @return The string representation
	 */
	public String toString( boolean longForm )
	{
		StringBuilder r = new StringBuilder(), prefix = new StringBuilder();
		if( getIdentifier() != null )
		{
			r.append( "id=" );
			r.append( getIdentifier() );
		}
		if( ( longForm || ( getIdentifier() != null ) ) && ( getSpecification() != null ) )
		{
			if( r.length() != 0 )
				r.append( ", " );
			r.append( "spec=" );
			r.append( getSpecification() );
		}
		if( longForm )
		{
			prefix.append( isExplicit() ? '*' : '+' ); // explicit?
			prefix.append( getIdentifier() != null ? '!' : '?' ); // identified?
			Iterator<?> i = getDependencies().iterator();
			if( i.hasNext() )
			{
				if( r.length() != 0 )
					r.append( ", " );
				r.append( "dependencies=" );
				int size = 0;
				for( ; i.hasNext(); i.next() )
					size++;
				r.append( size );
			}
			i = getSupplicants().iterator();
			if( i.hasNext() )
			{
				if( r.length() != 0 )
					r.append( ", " );
				r.append( "supplicants=" );
				int size = 0;
				for( ; i.hasNext(); i.next() )
					size++;
				r.append( size );
			}
		}
		if( prefix.length() != 0 )
		{
			r.insert( 0, ' ' );
			r.insert( 0, prefix );
		}
		return r.toString();
	}

	//
	// Objects
	//

	@Override
	public String toString()
	{
		return toString( true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private volatile boolean explicit;

	private volatile ModuleIdentifier identifier;

	private volatile ModuleSpecification specification;

	private final List<Module> dependencies = new ArrayList<Module>();

	private final List<Module> supplicants = new ArrayList<Module>();
}
