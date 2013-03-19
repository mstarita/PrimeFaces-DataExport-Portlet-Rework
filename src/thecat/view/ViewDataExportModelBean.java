package thecat.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.component.datatable.DataTable;

public class ViewDataExportModelBean implements Serializable {

private static final long serialVersionUID = 1L;
	
	private List<Person> personList;
	
	private DataTable personTable;

	public ViewDataExportModelBean() {
		personList = new ArrayList<Person>();
		
		for (int i=0; i<100; i++) {
			Person person = new Person();
			
			person.setFirstname("firstname-" + i);
			person.setLastname("lastname-" + i);
			person.setAddress("address-" + i);
			
			personList.add(person);
			
		}
	}
	
	public List<Person> getPersonList() {
		return personList;
	}

	public DataTable getPersonTable() {
		return personTable;
	}

	public void setPersonTable(DataTable personTable) {
		this.personTable = personTable;
	}
}
