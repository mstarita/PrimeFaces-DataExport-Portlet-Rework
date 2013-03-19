package org.primefaces.component.export.portlet.resourcehandler;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;


public class ExportResourceHandler extends ResourceHandlerWrapper {

	public static final String LIBRARY_NAME = "pf4p";
	
	private ResourceHandler wrappedResourceHandler;
	
	public ExportResourceHandler(ResourceHandler wrappedResourceHandler) {
		this.wrappedResourceHandler = wrappedResourceHandler;
	}

	@Override
	public Resource createResource(String resourceName, String libraryName) {

		if (LIBRARY_NAME.equals(libraryName)) {

			if (ExportResource.RESOURCE_NAME.equals(resourceName)) {				
				return new ExportResource();
			}
			else {
				return wrappedResourceHandler.createResource(resourceName, libraryName);
			}
		}
		else {
			return wrappedResourceHandler.createResource(resourceName, libraryName);
		}
	}
	
	@Override
	public boolean libraryExists(String libraryName) {

		if (LIBRARY_NAME.equals(libraryName)) {
			return true;
		}
		else {
			return super.libraryExists(libraryName);
		}
	}
	
	@Override
	public ResourceHandler getWrapped() {
		return wrappedResourceHandler;
	}
	
	
}
