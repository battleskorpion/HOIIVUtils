package test.hoi4utils.clausewitz_coding.focus;

import hoi4utils.clausewitz_coding.focus.Focus;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class FocusTest {

	@Test
	public void testFocusProperties() {
		Focus focus = mock();
		when(focus.id()).thenReturn("ALA_test_focus");
		when(focus.nameLocalization()).thenReturn("ALA_test_focus:0 \"Test Focus\"");
		when(focus.descLocalization()).thenReturn("ALA_test_focus_desc:0 \"\"");


	}

	@Test
	public void testSetDescLocalization() {

	}


}
