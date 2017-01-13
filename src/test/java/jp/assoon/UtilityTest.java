package jp.assoon;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class UtilityTest {

	Utility utility = new Utility();
	
	private final String TEST_DATA_PATH = this.getClass().getResource("/jp/assoon/data/fullspace.txt").getPath().replaceAll("^/(\\w.+)", "$1"); 

	@Test
	public void test() {
		
	}

}
