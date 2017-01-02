package jp.assoon;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class MeCabTest {

	MeCab mecab;
	static final String inputFilePath = "target/testdata1.txt";
	
	@Before
	public void setUp() throws Exception {
		mecab = new MeCab(1, "src/main/webapp/WEB-INF/mecab.properties");
		//ファイル作成
		List<String> list = new ArrayList<>();
		list.add("育休復帰は新しく仕事を始めるよりとっても楽でしたよ！");
		list.add("残業多いけど子どものためにも頑張ります");
		list.add("子育ても仕事もムリなく働ける場所をみつけたい！");
		list.add("人間関係改善");
		list.add("ママが子どもがいても働きやすい職場をもっと多くなりますように！");
		list.add("子どもを保育園にかよう間できる仕事があればたすかる");
		list.add("小学校終了後に子供を預かってもらえる制度がほしい。");
		list.add("うたっておどれるダンサー");
		list.add("夢のマイホーム");
		list.add("早く仕事がしたい。");
		list.add("もっと、働きやすい環境になってほしい。");
		list.add("家族の協力が一番えんりょせずにたよろう（じじばばを使う）");
		Utility utility = new Utility();
		utility.write(list, inputFilePath);

	}

	@Test
	public void check() {
		//1:名詞
		String[] hinshi = {"1"};
		mecab.run(inputFilePath, "target/testdata1_after.txt", hinshi);
	}

}
