/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.osgi.util.NLS;
import org.jboss.tools.ssp.launching.VMInstallModel;

/**
 * Abstract implementation of a VM install type.
 * Subclasses should implement
 * <ul>
 * <li><code>IVMInstall doCreateVMInstall(String id)</code></li>
 * <li><code>String getName()</code></li>
 * <li><code>IStatus validateInstallLocation(File installLocation)</code></li>
 * </ul>
 * <p>
 * Clients implementing VM install types should subclass this class.
 * </p>
 */

public abstract class AbstractVMInstallType implements IVMInstallType {
	private static final String vmInstallType_duplicateVM="Duplicate VM: {0}";
	
	
	private List<IVMInstall> fVMs;
	private String fId;

	/**
	 * Constructs a new VM install type.
	 */
	protected AbstractVMInstallType() {
		fVMs = new ArrayList<>(10);
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#getVMs()
	 */
	@Override
	public synchronized IVMInstall[] getVMInstalls() {
		IVMInstall[] vms= new IVMInstall[fVMs.size()];
		return fVMs.toArray(vms);
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#disposeVM(String)
	 */
	@Override
	public void disposeVMInstall(String id) {
		IVMInstall removedVM = null;
		synchronized (this) {
			for (int i= 0; i < fVMs.size(); i++) {
				if (fVMs.get(i).getId().equals(id)) {
					removedVM = fVMs.remove(i);
					break;
				}
			}
		}
		if (removedVM != null) {
			fireVMRemoved(removedVM);
		}
	}

	protected void fireVMRemoved(IVMInstall install) {
		VMInstallModel.getDefault().removeVMInstall(install);
	}
	
	
	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#getVM(String)
	 */
	@Override
	public IVMInstall findVMInstall(String id) {
		synchronized (this) {
			for (int i = 0; i < fVMs.size(); i++) {
				IVMInstall vm = fVMs.get(i);
				if (vm.getId().equals(id)) {
					return vm;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#createVM(String)
	 */
	@Override
	public synchronized IVMInstall createVMInstall(String id) throws IllegalArgumentException {
		if (findVMInstall(id) != null) {
			String format= vmInstallType_duplicateVM;
			throw new IllegalArgumentException(NLS.bind(format, new String[] { id }));
		}
		IVMInstall vm = doCreateVMInstall(id);
		fVMs.add(vm);
		return vm;
	}

	/**
	 * Subclasses should return a new instance of the appropriate
	 * <code>IVMInstall</code> subclass from this method.
	 * @param	id	The vm's id. The <code>IVMInstall</code> instance that is created must
	 * 				return <code>id</code> from its <code>getId()</code> method.
	 * 				Must not be <code>null</code>.
	 * @return	the newly created IVMInstall instance. Must not return <code>null</code>.
	 */
	protected abstract IVMInstall doCreateVMInstall(String id);

	/**
	 * Initializes the id parameter from the "id" attribute
	 * in the configuration markup.
	 * Subclasses should not override this method.
	 * @param id the id
	 */
	public void setInitializationData(String id) {
		fId= id;
	}

	/* (non-Javadoc)
	 * Subclasses should not override this method.
	 * @see IVMType#getId()
	 */
	@Override
	public String getId() {
		return fId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#findVMInstallByName(java.lang.String)
	 */
	@Override
	public IVMInstall findVMInstallByName(String name) {
		synchronized (this) {
			for (int i = 0; i < fVMs.size(); i++) {
				IVMInstall vm = fVMs.get(i);
				if (Objects.equals(vm.getName(), name)) {
					return vm;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a URL for the default javadoc location of a VM installed at the
	 * given home location, or <code>null</code> if none. The default
	 * implementation returns <code>null</code>, subclasses must override as
	 * appropriate.
	 * <p>
	 * Note, this method would ideally be added to <code>IVMInstallType</code>,
	 * but it would have been a breaking API change between 2.0 and 2.1. Thus,
	 * it has been added to the abstract base class that VM install types should
	 * subclass.
	 * </p>
	 *
	 * @param installLocation home location
	 * @return default javadoc location or <code>null</code>
	 * @since 2.1
	 */
	public URL getDefaultJavadocLocation(File installLocation) {
		return null;
	}

	/**
	 * Returns a string of default VM arguments for a VM installed at the
	 * given home location, or <code>null</code> if none.
	 * The default implementation returns <code>null</code>, subclasses must override
	 * as appropriate.
	 * <p>
	 * Note, this method would ideally be added to <code>IVMInstallType</code>,
	 * but it would have been a breaking API change between 2.0 and 3.4. Thus,
	 * it has been added to the abstract base class that VM install types should
	 * subclass.
	 * </p>
	 * @param installLocation home location
	 * @return default VM arguments or <code>null</code> if none
	 * @since 3.4
	 */
	public String getDefaultVMArguments(File installLocation) {
		return null;
	}
}
