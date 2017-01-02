package jp.assoon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MeCabTest {

	private MeCab mecab;
	private static final String inputFilePath = "target/testdata1.txt";
	private static final String outputFilePath = "target/testdata1_after.txt";
	private static Utility utility = new Utility();
	private static final List<String> correctList;
	
	static{
		//テスト用のファイル作成
		List<String> tempList = new ArrayList<>();
		tempList.add("妊娠した時に仕事を９ヶ月ぐらいまでしていたので、なかなか赤ちゃんの必要なものが買えなかったのと、出産費用の事で色々買ってあげれなかったので出産費用をもう少し安くするかなにか必要な物をプレゼントしてほしい。");
		tempList.add("近くに住むママ友がいません。近くに住むママと知りあえるきかいがほしいです。");
		tempList.add("育休復帰は新しく仕事を始めるよりとっても楽でしたよ！");
		tempList.add("残業多いけど子どものためにも頑張ります");
		tempList.add("子育ても仕事もムリなく働ける場所をみつけたい！");
		tempList.add("人間関係改善");
		tempList.add("ママが子どもがいても働きやすい職場をもっと多くなりますように！");
		tempList.add("子どもを保育園にかよう間できる仕事があればたすかる");
		tempList.add("小学校終了後に子供を預かってもらえる制度がほしい。");
		tempList.add("うたっておどれるダンサー");
		tempList.add("夢のマイホーム");
		tempList.add("早く仕事がしたい。");
		tempList.add("もっと、働きやすい環境になってほしい。");
		tempList.add("家族の協力が一番えんりょせずにたよろう（じじばばを使う）");
		utility.write(tempList, inputFilePath);
		
		//結果確認用のリスト
		tempList = new ArrayList<>();
		tempList.add("14");
		tempList.add("妊娠 仕事 赤ちゃん 出産 費用 出産 費用 プレゼント");
		tempList.add("ママ 友 ママ");
		tempList.add("育休 復帰 仕事");
		tempList.add("残業 子ども");
		tempList.add("子育て 仕事 場所");
		tempList.add("人間 関係 改善");
		tempList.add("ママ 子ども 職場");
		tempList.add("子ども 保育園 間 仕事");
		tempList.add("小学校 終了 子供 制度");
		tempList.add("ダンサー");
		tempList.add("夢 マイホーム");
		tempList.add("仕事");
		tempList.add("環境");
		tempList.add("家族 協力 じじ ばば");
		correctList = tempList;
		Collections.unmodifiableList(correctList);
	}
	
	@Before
	public void setUp() throws Exception {
		mecab = new MeCab(1, "src/main/webapp/WEB-INF/mecab.properties");
	}

	@Test
	public void check() {
		//名詞(1)でinputFileを形態素解析の結果が正しいか確認する
		String[] hinshi = {"1"};
		mecab.run(inputFilePath, outputFilePath, hinshi);
		List<String> outputFileList = utility.readText(outputFilePath);
		Assert.assertTrue("correctList:" + correctList.toString() +"\n outputFileList:" + outputFileList.toString(), correctList.equals(outputFileList));
		
		
		
	}

}
