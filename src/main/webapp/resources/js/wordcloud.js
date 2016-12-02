function show(id,wordCloud,texts,simcount,topic){
	var fill = d3.scale.category20();
    // var fill = d3.scale.linear()
    // .domain([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19])
    // .range(["#FF00A1", "#2b9af3", "#8FC31F", "#13AE67", "#172A88","#423729"
    // ,"#EA5514","#036EB8",
    // "#FF00A1", "#2b9af3", "#8FC31F", "#31328E", "#172A88","#423729"
    // ,"#EA5514","#036EB8",
     // "#FF00A1", "#2b9af3", "#13AE67", "#31328E"]);
	var countMax = d3.max(wordCloud, function(d){ return d.prop} );
	var sizeScale = d3.scale.linear().domain([0, countMax]).range([12, 33])
	var words = wordCloud.map(function(d) {
		return {
			text: d.word,
			size: sizeScale(d.prop)
		};
	});
	
	var layout = d3.layout.cloud()
	.size([300, 130])
	.words(words)
	.padding(0.7)
	.rotate(function() { return 0 ; })
	// .font("Impact")
	.fontSize(function(d) { return d.size; })
	.on("end", draw);

	var newTr = d3.select("#output").append("tr");
	newTr.append("td").append("div").attr("id","number").text(id);
	layout.start();
	// var text = newTr.append("td").attr("id","text").append("ul");
	var text = newTr.append("td").attr("id","text");
	for(i = 0; i<1;i++){
		// text.append("li").html(texts[i]);
		text.html(texts[i]);
	}
	var simc = newTr.append("td").attr("id","simcount");
	simc.html("<span id=\"fontsim\">類似</span><br><span id=\"fontsim\">"+simcount+"</span>件");

	function draw(words) {
		var newTd = newTr.append("td"); 
		newTd.append("div").append("svg")
		.attr("width", layout.size()[0])
		.attr("height", layout.size()[1])
		.append("g")
		.attr("transform", "translate(" + layout.size()[0] / 2 + "," + layout.size()[1] / 2 + ")")
		.selectAll("text")
		.data(words)
		.enter().append("text")
		.style("font-size", function(d) { return d.size + "px"; })
		// .style("font-family", "Impact")
		.style("fill", function(d, i) { return fill(i); })
		.attr("text-anchor", "middle")
		.attr("transform", function(d) {
			return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
		})
		.text(function(d) { return d.text; });
		newTd.append("div").attr("id","topicnum").text("TOPIC"+topic);
	}

}
