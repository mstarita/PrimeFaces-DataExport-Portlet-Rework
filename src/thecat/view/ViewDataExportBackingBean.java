package thecat.view;

import org.primefaces.component.export.portlet.resourcehandler.ExportResource;


public class ViewDataExportBackingBean {
	
	public String getResourceURL() {
		return new ExportResource().getRequestPath();
	}
}
