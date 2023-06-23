package fr.insee.kraftwerk.core.extradata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.extradata.paradata.Event;
import fr.insee.kraftwerk.core.extradata.paradata.ParaDataUE;

class ParaDataUETest {
	
	ParaDataUE paraDataUE = new ParaDataUE();


	@Test
	void sortTest() {
		Event e1 = new Event("s1");
		e1.setIdParadataObject("A");
		e1.setTimestamp(1);

		Event e2 = new Event("s1");
		e2.setIdParadataObject("A");
		e2.setTimestamp(2);

		Event e3 = new Event("s1");
		e3.setIdParadataObject("B");
		e3.setTimestamp(1);
		
		Event e4 = new Event("s2"); //same as e3, but with different id => remove in final list
		e4.setIdParadataObject("B");
		e4.setTimestamp(1);


		List<Event> events = new ArrayList<>();
		events.add(e3);
		events.add(e1);
		events.add(e2);
		events.add(e4);

		paraDataUE.setEvents(events);

		paraDataUE.sortEvents();

		List<Event> sortedList = paraDataUE.getEvents();
		assertEquals(3, sortedList.size());
		
		assertEquals("A", sortedList.get(0).getIdParadataObject());
		assertEquals(1, sortedList.get(0).getTimestamp());
		assertEquals("B", sortedList.get(1).getIdParadataObject());
		assertEquals(1, sortedList.get(1).getTimestamp());
		assertEquals("A", sortedList.get(2).getIdParadataObject());
		assertEquals(2, sortedList.get(2).getTimestamp());
	}
	
}
