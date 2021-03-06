/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.kernel.deployer.core.internal;

import org.eclipse.virgo.kernel.core.AbortableSignal;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;


/**
 * <p>
 * A <code>Signal</code> implementation that blocks until notified of completion or abortion.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public final class BlockingAbortableSignal implements AbortableSignal {

	private final BlockingSignal blockingSignal;
	
	private boolean aborted = false;
	
	public BlockingAbortableSignal(boolean synchronous) {
		this.blockingSignal = new BlockingSignal(synchronous);
	}
	
    /**
     * {@inheritDoc}
     */
	public void signalSuccessfulCompletion() {
		this.blockingSignal.signalSuccessfulCompletion();
	}

    /**
     * {@inheritDoc}
     */
	public void signalFailure(Throwable cause) {
		this.blockingSignal.signalFailure(cause);
	}

    /**
     * {@inheritDoc}
     */
	public void signalAborted() {
		this.aborted = true;
	}

	public boolean isAborted(){
		return this.aborted;
	}
	
	public boolean awaitCompletion(long timeInSeconds) throws DeploymentException {
		return this.blockingSignal.awaitCompletion(timeInSeconds);
	}

	public boolean checkComplete() throws DeploymentException {
		boolean complete = this.blockingSignal.checkComplete();
		if(this.aborted){
			return false;
		}
		return complete;
	}

}
