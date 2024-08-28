/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package org.apache.maven.plugin.dependency;

import java.io.IOException;
import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.dependency.AbstractResolveMojo;
import org.apache.maven.plugin.dependency.utils.DependencyStatusSets;
import org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.apache.maven.plugin.dependency.utils.filters.ResolveFileFilter;
import org.apache.maven.plugin.dependency.utils.markers.SourcesFileMarkerHandler;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.shared.artifact.filter.ScopeArtifactFilter;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;

@Mojo( name = "list-source", requiresDependencyResolution = ResolutionScope.TEST,
defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class SourceMojo extends AbstractResolveMojo{
	
	
    /**
     * If we should display the scope when resolving
     *
     * @since 2.0-alpha-2
     */
    @Parameter( property = "mdep.outputScope", defaultValue = "true" )
    protected boolean outputScope;

    /**
     * Only used to store results for integration test validation
     */
    DependencyStatusSets results;

    /**
     * Main entry into mojo. Gets the list of dependencies and iterates through displaying the resolved version.
     *
     * @throws MojoExecutionException with a message if an error occurs.
     */
	protected void doExecute()
        throws MojoExecutionException
    {
        try
        {
                       
            ArtifactResolutionResult result =
                this.artifactCollector.collect( project.getArtifacts(), project.getArtifact(), this.getLocal(),
                                                this.remoteRepos, this.artifactMetadataSource,
                                                new ScopeArtifactFilter( Artifact.SCOPE_TEST ),
                                                new ArrayList<ResolutionListener>() );
            List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
            Set<ArtifactRepository> remoteRepos = new HashSet<ArtifactRepository>();
            List<Artifact> artifacts = new ArrayList<Artifact>();
            Set<ResolutionNode> nodes = result.getArtifactResolutionNodes();
            
            for ( ResolutionNode node : nodes )
            {
            	repos.addAll( node.getRemoteRepositories());
            	remoteRepos.addAll(node.getRemoteRepositories());
            	artifacts.add(node.getArtifact());          	
                
            }
            this.getLog().info( "Repositories Used by this build:" );
            
           	for ( ArtifactRepository repo : remoteRepos ){
           		String[] urlParts = repo.getUrl().replace("http://", " ").split("/", 2);
           		String host = urlParts[0];
           		String uri = urlParts[1];
           		this.getLog().info( repo.getId().toString() );
           		this.getLog().info("Repository URI: " +  uri.toString() );
           		this.getLog().info("Repository Host: " +  host.toString() );
           		this.getLog().info("Repository Protocol: " +  repo.getProtocol().toString() );
           		this.getLog().info( "Full Details:" );
           		this.getLog().info( repo.toString() );
           		

            }
           	Object[] art = artifacts.toArray();
            this.getLog().info( "Dependencies Used by this build:" );
            this.getLog().info( "Ericsson Dependencies:" );
            for (int i = 0; i < art.length; i++){
            	if(art[i].toString().contains("ericsson")){
            			this.getLog().info( art[i].toString() + ":" +  repos.get(i).pathOf(artifacts.get(i)));
            		}
            }
            this.getLog().info( "3pp Dependencies:" );
            for (int i = 0; i < art.length; i++){
            	if(!art[i].toString().contains("ericsson")){
            		this.getLog().info( art[i].toString() + ":" +  repos.get(i).pathOf(artifacts.get(i)));
            	 }
            }
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Unable to resolve artifacts", e );
        }
    }

    /**
     * @return Returns the results.
     */
    public DependencyStatusSets getResults()
    {
        return this.results;
    }

    protected ArtifactsFilter getMarkedArtifactFilter()
    {
        return new ResolveFileFilter( new SourcesFileMarkerHandler( this.markersDirectory ) );
    }
	


}
