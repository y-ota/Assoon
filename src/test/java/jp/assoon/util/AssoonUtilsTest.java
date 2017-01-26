package jp.assoon.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class AssoonUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMaxValueTopic() {
		double[] values = new double[]{0.0,0.5,10.5,3.0};
		assertThat(2, equalTo(AssoonUtils.maxValueTopic(values)));  
	}
	
	@Test
	public void testMaxValueTopicWithDuplicateValues() {
		double[] values = new double[]{0.0,10.5,10.5,3.0};
		assertThat(null, equalTo(AssoonUtils.maxValueTopic(values)));  
	}

}
