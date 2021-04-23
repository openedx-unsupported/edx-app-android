javascript: (function setupNativeAjaxCallback() {
    javascript: $(document).ajaxComplete(function(event, request, settings) {
        var ajaxData = {
            "status": request.status,
            "url": settings.url,
            "response_text": request.responseText
        };
        nativeAjaxCallback.ajaxDone(JSON.stringify(ajaxData));
    });
}());
