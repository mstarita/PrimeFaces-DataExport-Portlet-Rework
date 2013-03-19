package org.primefaces.component.export.portlet;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.portlet.PortletSession;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.export.portlet.resourcehandler.ExportResource;
import org.primefaces.context.RequestContext;

public class DataExporter implements ActionListener, StateHolder {

	private ValueExpression target;
	private ValueExpression type;
	private ValueExpression fileName;
	private ValueExpression encoding;
	private ValueExpression pageOnly;
	private ValueExpression selectionOnly;
	private MethodExpression preProcessor;
	private MethodExpression postProcessor;
	
	public DataExporter() {}

	public DataExporter(ValueExpression target, ValueExpression type, 
			ValueExpression fileName, ValueExpression pageOnly, 
			ValueExpression selectionOnly, ValueExpression encoding, 
			MethodExpression preProcessor, MethodExpression postProcessor) {
		this.target = target;
		this.type = type;
		this.fileName = fileName;
		this.pageOnly = pageOnly;
		this.selectionOnly = selectionOnly;
		this.preProcessor = preProcessor;
		this.postProcessor = postProcessor;
		this.encoding = encoding;
	}

	public void processAction(ActionEvent event){
		FacesContext context = FacesContext.getCurrentInstance();
		ELContext elContext = context.getELContext();
		
		String tableId = (String) target.getValue(elContext);
		String exportAs = (String) type.getValue(elContext);
		String outputFileName = (String) fileName.getValue(elContext);
	
		String encodingType = "UTF-8";
		if(encoding != null) {
			encodingType = (String) encoding.getValue(elContext);
        }

		boolean isPageOnly = false;
		if(pageOnly != null) {
			isPageOnly = pageOnly.isLiteralText() ? Boolean.valueOf(pageOnly.getValue(context.getELContext()).toString()) : (Boolean) pageOnly.getValue(context.getELContext());
		}
		
        boolean isSelectionOnly = false;
		if(selectionOnly != null) {
			isSelectionOnly = selectionOnly.isLiteralText() ? Boolean.valueOf(selectionOnly.getValue(context.getELContext()).toString()) : (Boolean) selectionOnly.getValue(context.getELContext());
		}
        
		UIComponent component = event.getComponent().findComponent(tableId);
		if(component == null) {
			throw new FacesException("Cannot find component \"" + tableId + "\" in view.");
        }
        
		if(!(component instanceof DataTable)) {
			throw new FacesException("Unsupported datasource target:\"" + component.getClass().getName() + "\", exporter must target a PrimeFaces DataTable.");
        }
        
		DataTable table = (DataTable) component;
	
		long exportId = System.currentTimeMillis();

		FacesContext fc = FacesContext.getCurrentInstance();
		
		Exporter exporter = ExporterFactory.getExporterForType(exportAs, outputFileName);
		
		try {
			byte[] exportBytes = exporter.export(fc, table, isPageOnly, isSelectionOnly, encodingType, preProcessor, postProcessor);
			
			PortletSession portletSession = (PortletSession) fc.getExternalContext().getSession(false);
			portletSession.setAttribute(ExportResource.KEY_NAME_EXPORT_DATA + exportId, exportBytes, PortletSession.APPLICATION_SCOPE);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		String url = ExportResource.getRequestPath(exportAs, exportId, exporter.getFileName());
        
        RequestContext.getCurrentInstance().execute(
        		"window.location.href = '" + url + "'");
	}

	public boolean isTransient() {
		return false;
	}

	public void setTransient(boolean value) {
		//NoOp
	}
	
	 public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;

		target = (ValueExpression) values[0];
		type = (ValueExpression) values[1];
		fileName = (ValueExpression) values[2];
		pageOnly = (ValueExpression) values[3];
		selectionOnly = (ValueExpression) values[4];
		preProcessor = (MethodExpression) values[5];
		postProcessor = (MethodExpression) values[6];
		encoding = (ValueExpression) values[7];
	}

	public Object saveState(FacesContext context) {
		Object values[] = new Object[8];

		values[0] = target;
		values[1] = type;
		values[2] = fileName;
		values[3] = pageOnly;
		values[4] = selectionOnly;
		values[5] = preProcessor;
		values[6] = postProcessor;
		values[7] = encoding;
		
		return ((Object[]) values);
	}
}
