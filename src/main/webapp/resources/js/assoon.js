$(function() {
	$("#btn-dl").button();
	// 設定ボタン
	$("#setting").click(function() {
		if ($('#setting-form').css('display') == 'block') {
			$("#setting-form").fadeOut(500);
		} else {
			$("#setting-form").fadeIn(500);
		}
	});
	$("#setting-form").hide();
	// RUNボタン
	$("#btn-upload").button();
	$("#btn-upload").click(function() {
		if (document.getElementById("file").value == "") {
			$("#dialog").dialog({
				modal : true,
				buttons : {
					OK : function() {
						$(this).dialog("close");
					}
				}
			});
		} else {
			$("#loading2").fadeIn(100);
			$("#setting-form").fadeOut(800);
			// ボタン無効化
			$("#setting").attr('disabled', true);
			$("#btn-run").attr('disabled', true);
			$("#btn-upload").attr('disabled', true);
			$("#submit2").click();
		}
	});
	// 変なパス対策
	$("#file").change(
			function() {
				document.getElementById("path").value = document
						.getElementById("file").value.replace("C:\\fakepath\\",
						"");
			});

	$("#btn-run").button();
	$("#btn-run").click(function() {
		$("#file").click();
	});

	$("#btn-dl").click(function() {
		$.get("/ldas/download");
	});

	$("#setting").button({
		icons : {
			primary : "ui-icon-gear",
		},
		text : false
	});

	// 画面ロード
	$(window).load(function() {
		$(".bordered").fadeIn(3000);
		$("#loading").fadeOut(3000);
	});

	// チェックボックス、ラジオボックス
	$(document).ready(function() {
		$('input').iCheck({
			checkboxClass : 'icheckbox_minimal',
			radioClass : 'iradio_minimal',
			increaseArea : '20%'
		});
	});
});