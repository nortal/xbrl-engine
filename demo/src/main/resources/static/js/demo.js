(function ($) {
    // handle custom file inputs
    $('body').on('change', 'input[type="file"][data-toggle="custom-file"]', function (ev) {

        var $input = $(this);
        var target = $input.data('target');
        var $target = $(target);

        if (!$target.length)
            return console.error('Invalid target for custom file', $input);

        if (!$target.attr('data-content'))
            return console.error('Invalid `data-content` for custom file target', $input);

        // set original content so we can revert if user deselects file
        if (!$target.attr('data-original-content'))
            $target.attr('data-original-content', $target.attr('data-content'));

        var input = $input.get(0);

        var name = typeof input === "object"
            && typeof input.files === "object"
            && typeof input.files[0] === "object"
            && typeof input.files[0].name === "string" ? input.files[0].name : $input.val();

        if (name === null || name === '')
            name = $target.attr('data-original-content');

        $target.attr('data-content', name);
    });

    // enable popovers
    $(function () {
        $('[data-toggle="popover"]').popover();
    });
})(jQuery);

function updateHasValueStatus(select) {
    var $select = jQuery(select);
    if ($select.find('option:selected').attr('data-hasValues') === 'true') {
        $select.closest('tr').find('.status-has-value').css({"display" : "inline"});
        $select.closest('tr').find('.status-no-value').css({"display" : "none"});
    } else {
        $select.closest('tr').find('.status-has-value').css({"display" : "none"});
        $select.closest('tr').find('.status-no-value').css({"display" : "inline"});
    }
}
