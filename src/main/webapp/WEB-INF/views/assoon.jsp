<%@page import="jp.assoon.lda.TopicInfo"%>
<%@page import="jp.assoon.lda.WordProp"%>
<%@page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="jp">
<head>
<meta charset="UTF-8">
<title>Assoon</title>
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/style.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/skins/minimal/minimal.css" />" />
<link rel="stylesheet"
	href="<c:url value="/resources/css/jquery-ui.css" />" />
<link rel="icon" type="image/png"
	href="<c:url value="/resources/image/asoq_logo.png" />" sizes="32x32">
<link href="https://fonts.googleapis.com/css?family=Nova+Oval" rel="stylesheet">
<script src='<c:url value="/resources/js/d3.js" />'></script>
<script src='<c:url value="/resources/js/d3.layout.cloud.js" />'></script>
<script src='<c:url value="/resources/js/wordcloud.js" />'></script>
<script src='<c:url value="/resources/js/jquery-1.8.3.js" />'></script>
<script src='<c:url value="/resources/js/jquery-ui.js" />'></script>
<script src='<c:url value="/resources/js/icheck.js" />'></script>
<script src='<c:url value="/resources/js/assoon.js" />'></script>
</head>
<body>
	<div id="demo"></div>
	<div id="container">
		<h1><a href="./" style="text-decoration:none;">
			<img src="<c:url value="/resources/image/asoq_logo.png" />" class="icon">Assoon
		</a></h1>
		<div class="top">
			<p>
				Assoonは自由記述アンケートの回答を自動的に要約するWebアプリです。<br>
				テキストファイルをアップロードするだけで簡単にご利用できます。
			</p>
		</div>
		<form id="form" method="post" enctype="multipart/form-data">
			<table id="run">
				<tr>
					<td>
						<div class="ui-widget">
							<input id="path" readonly="readonly" size="70" />
						</div>
					</td>
					<td><input type="button" name="btn-run" id="btn-run"
						value="OPEN" /> <input type="file" name="file" id="file" value="" />
						<input type="submit" name="submit2" id="submit2" value="qaa"
						style="display: none" /></td>
					<td><input type="button" name="btn-upload" id="btn-upload"
						value="RUN" /></td>
					<td>
						<Button type="button" id="setting">Setting</Button>
					</td>
				</tr>
			</table>
			<p />

			<div id="setting-form">
				<input type="checkbox" name="word" value="1" id="check1" checked="checked">名詞
				<input type="checkbox" name="word" value="2" id="check2">動詞 <input
					type="checkbox" name="word" value="3" id="check3">形容詞 <input
					type="checkbox" name="word" value="4" id="check4">副詞
				<p></p>
				<label for="topic">トピック数</label> <input type="text" name="topic"
					id="topic" value="10" maxlength='3' required
					style="font-size: 18px;">
			</div>
         
			<c:if test="${empty postFlg}">
				<div id="step_container">
					<div id="step-div">
						<span id="text-word">STEP1 ファイル選択</span>
					</div>
					<p>
						OPENを押してテキストファイルを選択します。<br>
						テキストファイルの形式は、1行につき1意見です。(サンプルファイル：<a href="./sample">ダウンロード</a>)
					</p>
					<div class="divider_line">&nbsp;</div>

					<div id="step-div">
						<span id="text-word">STEP2 実行</span>
					</div>
					<p>
						RUNを押すと処理が始まり、要約結果が表示されます。
					</p>
					<img src="<c:url value="/resources/image/summary2.png" />"
						width="80%" height="80%">
					<div class="divider_line">&nbsp;</div>

					<div id="step-div">
						<span id="text-word">オプション</span>
					</div>
					<p>
						<img src="<c:url value="/resources/image/confbtn.png" />"
							width="35" height="35" class="stepbtn">で必要に応じて設定を変更します。
					</p>
					<div id="demo">
					<div class="divider_line">&nbsp;</div>
					<p></p>
					<p></p>
					<p><span id="demo_text">DEMOを押して、Assoonを試してみましょう。</span><br>データ提供元：<a href="http://mamapro.jp/posttree/">日本財団ママプロ</a></p>
					<input type="button" name="btn-demo" id="btn-demo" value="DEMO" />
                    <input type="text" name="demoval" id="demoval" style="display: none" />
					</div>
				</div>
			</c:if>
		</form>
		<p />
		<div id="loading2">
			<img src="<c:url value="/resources/image/load.gif" />">
		</div>
		<c:if test="${!empty postFlg}">
			<div id="loading">
				<img src="<c:url value="/resources/image/load.gif" />">
			</div>
			<div id="summary-div">SUMMARY</div>
			<table class="bordered">

				<tbody id="output"></tbody>
			</table>
		</c:if>
		<div class="footer">
			<p>© 2016-2021 Assoon All Rights Reserved.</p>
		</div>
	</div>
	<c:if test="${!empty postFlg}">
		<script type="text/javascript">
 			<%List<TopicInfo> list = (List<TopicInfo>) request.getAttribute("topicInfo");
				int rankingNum = 0;
				for (TopicInfo topicInfo : list) {
					int num = 0;%>
				 var texts = [];
				 var wordCloud = new Array();
			<%for (WordProp wordProp : topicInfo.getWordProp()) {%>
				      wordCloud[<%=num%>] = { word : "<%=wordProp.getWord()%>", prop : "<%=wordProp.getProp()%>"};
			<%num++;
					}
					for (String text : topicInfo.getDocument()) {%>
					  texts.push("<%=text%>");
			<%}%>
		    <%int topic = topicInfo.getNum() + 1;%>
		    <%int simNum = topicInfo.getSimcount();%>
		    <%rankingNum++;%>
		    show("<%=rankingNum%>", wordCloud, texts,
		<%=simNum%>
			,
		<%=topic%>
			);
		<%}%>
			
		</script>
	</c:if>
	<div id="dialog" title="Error">
		<p>ファイルを選択してください。</p>
	</div>
    <div id="topic_dialog" title="Error">
		<p>トピック数は20以下の整数を指定してください。</p>
	</div>
	<div id="word_dialog" title="Error">
		<p>品詞は1つ以上チェックしてください。</p>
	</div>
</body>
</html>
