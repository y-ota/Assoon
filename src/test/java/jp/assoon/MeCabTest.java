package jp.assoon;

import org.junit.Before;
import org.junit.Test;

public class MeCabTest {

	MeCab mecab;
	
	@Before
	public void setUp() throws Exception {
		mecab = new MeCab(1, "src/main/webapp/WEB-INF/mecab.properties");
	}

	@Test
	public void check() {
		//1:名詞
		String[] hinshi = {"1"};
		mecab.run("src/test/java/jp/assoon/data/testdata1.txt", "target/testdata1_after.txt", hinshi);
	}

}
