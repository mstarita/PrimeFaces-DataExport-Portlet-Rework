package org.primefaces.component.export.portlet;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.MethodExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.component.api.DynamicColumn;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.util.Constants;

public class CSVExporter extends Exporter {

	private String fileName;
	
	public CSVExporter(String fileName) {
		this.fileName = fileName + ".cvs";
	}
	
    @Override
	public byte[] export(FacesContext context, DataTable table, boolean pageOnly, boolean selectionOnly, String encodingType, MethodExpression preProcessor, MethodExpression postProcessor) throws IOException {
        Writer writer = new StringWriter();
			
    	addColumnFacets(writer, table, ColumnType.HEADER);
    	
        if(pageOnly) {
            exportPageOnly(context, table, writer);
        }
        else if(selectionOnly) {
            exportSelectionOnly(context, table, writer);
        }
        else {
            exportAll(context, table, writer);
        }
        
        if(table.hasFooterColumn()) {
            addColumnFacets(writer, table, ColumnType.FOOTER);
        }
            	        
        writer.flush();
        writer.close();
        
    	return writer.toString().getBytes();
	}
    
    protected void addColumnFacets(Writer writer, DataTable table, ColumnType columnType) throws IOException {
        boolean firstCellWritten = false;
        
        for(UIColumn col : table.getColumns()) {
            if(!col.isRendered()) {
                continue;
            }
                        
            if(col instanceof DynamicColumn) {
                ((DynamicColumn) col).applyModel();
            }
            
            if(col.isExportable()) {
                if(firstCellWritten) {
                    writer.write(",");
                }
                
                addColumnValue(writer, col.getFacet(columnType.facet()));
                firstCellWritten = true;
            }
        }
	
		writer.write("\n");
    }
    
    protected void exportPageOnly(FacesContext context, DataTable table, Writer writer) throws IOException {
        int first = table.getFirst();
    	int rowsToExport = first + table.getRows();
        
        for(int rowIndex = first; rowIndex < rowsToExport; rowIndex++) {                
            exportRow(table, writer, rowIndex);
        }
    }
    
    protected void exportSelectionOnly(FacesContext context, DataTable table, Writer writer) throws IOException {
        Object selection = table.getSelection();
        String var = table.getVar();
        
        if(selection != null) {
            Map<String,Object> requestMap = context.getExternalContext().getRequestMap();
            
            if(selection.getClass().isArray()) {
                int size = Array.getLength(selection);
                
                for(int i = 0; i < size; i++) {
                    requestMap.put(var, Array.get(selection, i));
                    
                    exportCells(table, writer);
                }
            }
            else {
                requestMap.put(var, selection);
                
                exportCells(table, writer);
            }
        }
    }
    
    protected void exportAll(FacesContext context, DataTable table, Writer writer) throws IOException {
        int first = table.getFirst();
    	int rowCount = table.getRowCount();
        int rows = table.getRows();
        boolean lazy = table.isLazy();
        
        if(lazy) {
            for(int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                if(rowIndex % rows == 0) {
                    table.setFirst(rowIndex);
                    table.loadLazyData();
                }

                exportRow(table, writer, rowIndex);
            }
     
            //restore
            table.setFirst(first);
            table.loadLazyData();
        } 
        else {
            for(int rowIndex = 0; rowIndex < rowCount; rowIndex++) {                
                exportRow(table, writer, rowIndex);
            }
            
            //restore
            table.setFirst(first);
        }
    }
    
    protected void exportRow(DataTable table, Writer writer, int rowIndex) throws IOException {
        table.setRowIndex(rowIndex);
        
        if(!table.isRowAvailable()) {
            return;
        }
       
        exportCells(table, writer);
        
        writer.write("\n");
    }
    
    protected void exportCells(DataTable table, Writer writer) throws IOException {
        boolean firstCellWritten = false;
        
        for(UIColumn col : table.getColumns()) {
            if(!col.isRendered()) {
                continue;
            }
                        
            if(col instanceof DynamicColumn) {
                ((DynamicColumn) col).applyModel();
            }
            
            if(col.isExportable()) {
                if(firstCellWritten) {
                    writer.write(",");
                }
                
                addColumnValue(writer, col.getChildren());
                firstCellWritten = true;
            }
        }
    }
    
    protected void configureResponse(ExternalContext externalContext, String filename) {
        externalContext.setResponseContentType("text/csv");
		externalContext.setResponseHeader("Expires", "0");
		externalContext.setResponseHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
		externalContext.setResponseHeader("Pragma", "public");
		externalContext.setResponseHeader("Content-disposition", "attachment;filename="+ filename + ".csv");
		externalContext.addResponseCookie(Constants.DOWNLOAD_COOKIE, "true", new HashMap<String, Object>());
    }
    
	protected void addColumnValues(Writer writer, List<UIColumn> columns) throws IOException {
		for(Iterator<UIColumn> iterator = columns.iterator(); iterator.hasNext();) {
            addColumnValue(writer, iterator.next().getChildren());

            if(iterator.hasNext())
                writer.write(",");
		}
	}

	protected void addColumnValue(Writer writer, UIComponent component) throws IOException {
		String value = component == null ? "" : exportValue(FacesContext.getCurrentInstance(), component);
        
        //escape double quotes
        value = value.replaceAll("\"", "\"\"");
        
        writer.write("\"" + value + "\"");
	}
	
	protected void addColumnValue(Writer writer, List<UIComponent> components) throws IOException {
		StringBuilder builder = new StringBuilder();
        FacesContext context = FacesContext.getCurrentInstance();
		
		for(UIComponent component : components) {
			if(component.isRendered()) {
				String value = exportValue(context, component);

                //escape double quotes
                value = value.replaceAll("\"", "\"\"");
                
				builder.append(value);
			}
		}
		
		writer.write("\"" + builder.toString() + "\"");
	}
	
	@Override
	public String getContentType() {
    	return "text/cvs";
    }
	
	@Override
	public String getFileName() {
    	return fileName;
    }
}
