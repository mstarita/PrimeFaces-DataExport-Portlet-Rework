package org.primefaces.component.export.portlet.resourcehandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import javax.portlet.PortletSession;

public class ExportResource extends Resource {

	public static final String RESOURCE_NAME = "export";
	
	public static final String KEY_NAME_EXPORT_TYPE = "exportType";
	public static final String KEY_NAME_EXPORT_ID = "exportId";
	public static final String KEY_NAME_FILE_NAME = "fileName";
	
	public static final String KEY_NAME_EXPORT_DATA = "exportData_";

	private String requestPath;
	
	private String exportType;
	private Long exportId;
	private String fileName;
	
	public ExportResource() {
		setLibraryName(ExportResourceHandler.LIBRARY_NAME);
		setResourceName(RESOURCE_NAME);
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
    	FacesContext facesContext = FacesContext.getCurrentInstance();
		PortletSession portletSession = (PortletSession) facesContext.getExternalContext().getSession(false);
		Map<String, String> requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
    	
		exportType = requestParameterMap.get(KEY_NAME_EXPORT_TYPE);
		exportId = Long.parseLong(requestParameterMap.get(KEY_NAME_EXPORT_ID));
		fileName = requestParameterMap.get(KEY_NAME_FILE_NAME);
		
		InputStream is = new ByteArrayInputStream(
				(byte[]) portletSession.getAttribute(ExportResource.KEY_NAME_EXPORT_DATA + exportId, PortletSession.APPLICATION_SCOPE));
		
		portletSession.removeAttribute(KEY_NAME_EXPORT_DATA + exportId, PortletSession.APPLICATION_SCOPE);
		
		return is;
	}

	@Override
	public String getRequestPath() {

		if (requestPath == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();
			
			StringBuilder buf = new StringBuilder();
			buf.append(ResourceHandler.RESOURCE_IDENTIFIER);
			buf.append("/");
			buf.append(getResourceName());
			buf.append("?ln=");
			buf.append(getLibraryName());
	        
			buf.append("&");
			buf.append(KEY_NAME_EXPORT_TYPE);
			buf.append("=");
			buf.append((String) requestMap.get(KEY_NAME_EXPORT_TYPE));
			
			buf.append("&");
			buf.append(KEY_NAME_EXPORT_ID);
			buf.append("=");
			buf.append((Long) requestMap.get(KEY_NAME_EXPORT_ID));
			
			buf.append("&");
			buf.append(KEY_NAME_FILE_NAME);
			buf.append("=");
			buf.append((String) requestMap.get(KEY_NAME_FILE_NAME));
			
			requestPath = buf.toString();
		}

		return FacesContext.getCurrentInstance().getExternalContext().encodeResourceURL(requestPath);
	}

	public static String getRequestPath(String exportType, long exportId, String fileName) {
	
		StringBuilder buf = new StringBuilder();
		buf.append(ResourceHandler.RESOURCE_IDENTIFIER);
		buf.append("/");
		buf.append(ExportResource.RESOURCE_NAME);
		buf.append("?ln=");
		buf.append(ExportResourceHandler.LIBRARY_NAME);
	    
		buf.append("&");
		buf.append(KEY_NAME_EXPORT_TYPE);
		buf.append("=");
		buf.append(exportType);
		
		buf.append("&");
		buf.append(KEY_NAME_EXPORT_ID);
		buf.append("=");
		buf.append(exportId);
		
		buf.append("&");
		buf.append(KEY_NAME_FILE_NAME);
		buf.append("=");
		buf.append(fileName);
		
		String requestPath = buf.toString();
		
		return FacesContext.getCurrentInstance().getExternalContext().encodeResourceURL(requestPath);
		
	}
	
	@Override
	public Map<String, String> getResponseHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
	    headers.put("Expires", "0");
	    headers.put("Cache-Control","must-revalidate, post-check=0, pre-check=0");
	    headers.put("Pragma", "public");
	    headers.put("Content-disposition", "attachment;" + "filename=" + fileName);
	    
	    return headers;
	}

	@Override
	public URL getURL() {
		return null;
	}

	@Override
	public boolean userAgentNeedsUpdate(FacesContext arg0) {
		return true;
	}

	@Override
	public String getContentType() {
		return exportType;
	}
}
