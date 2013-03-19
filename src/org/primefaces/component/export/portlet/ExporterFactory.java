package org.primefaces.component.export.portlet;

import javax.faces.FacesException;

import org.primefaces.component.export.ExporterType;

public class ExporterFactory {

	public static Exporter getExporterForType(String type, String fileName) {
        Exporter exporter = null;
        
        try {
            ExporterType exporterType = ExporterType.valueOf(type.toUpperCase());

            switch(exporterType) {
                case XLS:
                    exporter = new ExcelExporter(fileName);
                break;
                
                case PDF:
                    exporter = new PDFExporter(fileName);
                break; 
                
                case CSV:
                    exporter = new CSVExporter(fileName);
                break; 
                
                case XML:
                    exporter = new XMLExporter(fileName);
                break;
            }
        }
        catch(IllegalArgumentException e) {
            throw new FacesException(e);
        } 
        
        return exporter;
	}
}
