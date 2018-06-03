(function($) {
    function initRatingField(field) {
        field.addClass("init");
        field.disableSelection();
        var tip = field.find(".slie-rating-tip");

        function hoverOption(option) {
            tip.text(option.data("option-value"));
            var options = field.find(".slie-star");
            options.removeClass("slie-star-hover");

            if (option.hasClass("slie-star-clear")) {
                return;
            }

            for (var i = 0; i < options.length; i++) {
                var o = $(options[i]);
                o.addClass("slie-star-hover");
                if (o.is(option)) {
                    break;
                }
            }
        }

        function hoverOutOption() {
            var options = field.find(".slie-star");
            options.removeClass("slie-star-hover");

            var selected = field.find(".slie-star.slie-star-selected");
            var tipText = "";
            if (selected.length > 0) {
                tipText = selected.data("option-value");
            }
            tip.text(tipText);
        }

        function selectOption(option) {
            var options = field.find(".slie-star");
            options.removeClass("slie-star-gold");
            options.removeClass("slie-star-selected");

            if (!option.hasClass("slie-star-clear")) {
                for (var i = 0; i < options.length; i++) {
                    var o = $(options[i]);
                    o.addClass("slie-star-gold");
                    if (o.is(option)) {
                        o.addClass("slie-star-selected");
                        break;
                    }
                }
            }

            field.find(".slie-rating-input").val(option.data("option-id"));
        }

        field.find(".slie-star").click(function () {
            selectOption($(this));
        });

        field.find(".slie-star").hover(function () {
            hoverOption($(this));
        }, function () {
            hoverOutOption();
        });

        field.find(".slie-rating-input").change(function () {
            var value = $(this).val();
            var option;

            if (value) {
                option = field.find(".slie-star[data-option-id='" + value + "']")
            }

            if (!option || option.length === 0) {
                option = field.find(".slie-star.slie-star-clear");
            }

            selectOption(option);
        });
    }

    function initRatingFields($context) {
        var fields = $(".slie-rating-field.editable:not(.init)", $context);
        fields.each(function () {
            initRatingField($(this));
        });
    }

    AJS.toInit(function ($context) {
        initRatingFields($context[0]);
    });

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {
        initRatingFields(context);
    });
})(AJS.$);