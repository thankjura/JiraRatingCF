function updateRateOver(optionId, cfId) {
	var stars = jQuery(".rater-" + cfId);
	var tip = jQuery(".rating-tip-" + cfId);
	tip[0].data = tip.html();
	var AC = true;

	if (optionId == -1) {
		tip.html('Cancel Rating');
	} else {
		for (var i = 0; i < stars.length; i++) {
			if (AC) {
				jQuery(stars[i]).addClass("star-rating-hover");
			} else {
				jQuery(stars[i]).removeClass("star-rating-hover");
			}
			if (stars[i].children[0].innerHTML == optionId) {
				AC = false;
				tip.html(jQuery(stars[i]).find("a").attr("title") || "");
			}
		}
	}
	return false;
}

function updateRateOverOff(cfId) {
	var stars = jQuery(".rater-" + cfId);
	var tip = jQuery(".rating-tip-" + cfId);
	var voted = -1;
	
	for (var i = 0; i < stars.length; i++) {
		jQuery(stars[i]).removeClass("star-rating-hover");
		if(jQuery(stars[i]).hasClass("star-rating-on")){
			voted = i;
		}
	}

	if(voted > -1){
		tip.html(jQuery(stars[voted]).find("a").attr("title") || "");
	} else {
		tip.html("");
	}

	return false;
}

function updateRate(optionId, cfId) {
	jQuery('input[name=' + cfId + ']')[0].value = optionId;
	var stars = jQuery(".rater-" + cfId);
	var AC = true;
	
	if (optionId == -1) {
		AC = false;
	}
	for (var i = 0; i < stars.length; i++) {
		if (AC) {
			jQuery(stars[i]).addClass("star-rating-on");
		} else {
			jQuery(stars[i]).removeClass("star-rating-on");
		}
		if (stars[i].children[0].innerHTML == optionId) {
			AC = false;
		}
	}
	return false;
}