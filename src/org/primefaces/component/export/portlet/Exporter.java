package org.primefaces.component.export.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.ValueHolder;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.primefaces.component.datatable.DataTable;

public abstract class Exporter {
	    
	protected enum ColumnType{
		HEADER("header"),
		FOOTER("footer");
        
        private final String facet;
        
        ColumnType(String facet) {
            this.facet = facet;
        }

        public String facet() {
            return facet;
        }
        
        @Override
        public String toString() {
            return facet;
        }
	};

    public abstract byte[] export(FacesContext facesContext, DataTable table,
			boolean pageOnly, boolean selectionOnly,
			String encodingType, MethodExpression preProcessor,
			MethodExpression postProcessor) throws IOException;

	
	protected List<UIColumn> getColumnsToExport(UIData table) {
        List<UIColumn> columns = new ArrayList<UIColumn>();
        int columnIndex = -1;

        for(UIComponent child : table.getChildren()) {
            if(child instanceof UIColumn) {
                UIColumn column = (UIColumn) child;
                columnIndex++;

                columns.add(column);
            }
        }

        return columns;
    }

    protected boolean hasColumnFooter(List<UIColumn> columns) {
        for(UIColumn column : columns) {
            if(column.getFooter() != null)
                return true;
        }

        return false;
    }

    protected String exportValue(FacesContext context, UIComponent component) {

        if(component instanceof HtmlCommandLink) {  //support for PrimeFaces and standard HtmlCommandLink
            HtmlCommandLink link = (HtmlCommandLink) component;
            Object value = link.getValue();

            if(value != null) {
                return String.valueOf(value);
            } 
            else {
                //export first value holder
                for(UIComponent child : link.getChildren()) {
                    if(child instanceof ValueHolder) {
                        return exportValue(context, child);
                    }
                }

                return "";
            }
        }
        else if(component instanceof ValueHolder) {
 
			if(component instanceof EditableValueHolder) {
				Object submittedValue = ((EditableValueHolder) component).getSubmittedValue();
				if (submittedValue != null) {
					return submittedValue.toString();
				}
			}

			ValueHolder valueHolder = (ValueHolder) component;
			Object value = valueHolder.getValue();
			if(value == null)
				return "";

			//first ask the converter
			if(valueHolder.getConverter() != null) {
				return valueHolder.getConverter().getAsString(context, component, value);
			}
			//Try to guess
			else {
				ValueExpression expr = component.getValueExpression("value");
				if(expr != null) {
					Class<?> valueType = expr.getType(context.getELContext());
					if(valueType != null) {
						Converter converterForType = context.getApplication().createConverter(valueType);

						if(converterForType != null)
							return converterForType.getAsString(context, component, value);
					}
				}
			}

			//No converter found just return the value as string
			return value.toString();
		} 
        else {
			//This would get the plain texts on UIInstructions when using Facelets
			String value = component.toString();

			if(value != null)
				return value.trim();
			else
				return "";
		}
    }
    
    public abstract String getContentType();
    
    public abstract String getFileName();
}
