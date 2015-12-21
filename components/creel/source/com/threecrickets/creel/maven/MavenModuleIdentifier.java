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

package com.threecrickets.creel.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.exception.IncompatibleIdentifierException;
import com.threecrickets.creel.exception.IncompatiblePlatformException;
import com.threecrickets.creel.maven.internal.Version;

/**
 * @author Tal Liron
 */
public class MavenModuleIdentifier extends ModuleIdentifier
{
	//
	// Static operations
	//

	public static MavenModuleIdentifier cast( Object object )
	{
		if( object == null )
			throw new NullPointerException();
		if( !( object instanceof MavenModuleIdentifier ) )
			throw new IncompatiblePlatformException();
		return (MavenModuleIdentifier) object;
	}

	//
	// Construction
	//

	public MavenModuleIdentifier( MavenRepository repository, String group, String name, String version )
	{
		super( repository );
		this.group = group == null ? "" : group.trim();
		this.name = name == null ? "" : name.trim();
		this.version = version == null ? "" : version.trim();
	}

	//
	// Attributes
	//

	public String getGroup()
	{
		return group;
	}

	public String getName()
	{
		return name;
	}

	public String getVersion()
	{
		return version;
	}

	public Version getParsedVersion()
	{
		if( parsedVersion == null )
			parsedVersion = new Version( getVersion() );
		return parsedVersion;
	}

	//
	// ModuleIdentifier
	//

	public Iterable<Artifact> getArtifacts( File directory, boolean flat )
	{
		MavenRepository repository = (MavenRepository) getRepository();
		Collection<Artifact> artifacts = new ArrayList<Artifact>();
		artifacts.add( new Artifact( repository.getUrl( this, "jar" ), repository.getFile( this, "jar", directory, flat ) ) );
		return Collections.unmodifiableCollection( artifacts );
	}

	//
	// Comparable
	//

	public int compareTo( ModuleIdentifier moduleIdentifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = cast( moduleIdentifier );
		if( getGroup().equals( mavenModuleIdentifier.getGroup() ) && getName().equals( mavenModuleIdentifier.getName() ) )
			return getParsedVersion().compareTo( mavenModuleIdentifier.getParsedVersion() );
		throw new IncompatibleIdentifierException();
	}

	//
	// Cloneable
	//

	@Override
	public MavenModuleIdentifier clone()
	{
		return new MavenModuleIdentifier( (MavenRepository) getRepository(), getGroup(), getName(), getVersion() );
	}

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( !super.equals( object ) )
			return false;
		MavenModuleIdentifier mavenModuleIdentifier = (MavenModuleIdentifier) object;
		return getGroup().equals( mavenModuleIdentifier.getGroup() ) && getName().equals( mavenModuleIdentifier.getName() ) && getVersion().equals( mavenModuleIdentifier.getVersion() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( super.hashCode(), getGroup(), getName(), getVersion() );
	}

	@Override
	public String toString()
	{
		return "maven:" + getGroup() + ":" + getName() + ":" + getVersion();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String group;

	private final String name;

	private final String version;

	private volatile Version parsedVersion;
}