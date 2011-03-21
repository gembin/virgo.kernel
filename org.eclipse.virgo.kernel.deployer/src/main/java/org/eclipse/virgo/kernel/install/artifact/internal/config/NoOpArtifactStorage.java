package org.eclipse.virgo.kernel.install.artifact.internal.config;

import java.net.URI;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;

/**
 * Factory Configuration Artifact does not have a backing file/uri and does not require storage.
 */
public final class NoOpArtifactStorage implements ArtifactStorage {

    /** 
     * {@inheritDoc}
     */
    @Override
    public void synchronize() {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void synchronize(URI sourceUri) {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void rollBack() {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void delete() {            
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public ArtifactFS getArtifactFS() {
        return null;
    }
    
}