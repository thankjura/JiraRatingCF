function updateRateOver(val, cfId) {
    var stars = jQuery(".rater-" + cfId);
    var AC = true;
    for (var i = 0; i < stars.length; i++) {
        if (AC) {
            jQuery(stars[i]).addClass("star-rating-hover");
        } else {
            jQuery(stars[i]).removeClass("star-rating-hover");
        }
        if (stars[i].children[0].innerHTML == val) { AC = false;}
    }
    return false;
}

function updateRateOverOff(cfId) {
    var stars = jQuery(".rater-" + cfId);
    for (var i = 0; i < stars.length; i++) {
        jQuery(stars[i]).removeClass("star-rating-hover");
    }
    return false;
}

function updateRate(val, cfId) {
    jQuery('input[name=' + cfId + ']')[0].value=val;
    var stars = jQuery(".rater-" + cfId);
    var AC = true;
    if ( val == -1 ) {AC = false;}
    for (var i = 0; i < stars.length; i++) {
        if (AC) {
            jQuery(stars[i]).addClass("star-rating-on");
        } else {
            jQuery(stars[i]).removeClass("star-rating-on");
        }
        if (stars[i].children[0].innerHTML == val) { AC = false;}
    }
    return false;
}