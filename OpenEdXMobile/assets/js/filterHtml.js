function filterHtmlByClass(className, notFoundMessage) {
    var text = '';
    var divs = document.getElementsByClassName(className);
    if (divs && divs.length > 0) {
        for (i = 0; i < divs.length; i++) {
            text += divs[i].outerHTML;
        }
        document.getElementsByTagName('body')[0].innerHTML = text;
        var style = document.createElement('style');
        style.innerHTML = 'body { padding-left: 20px; padding-top: 30px; padding-right: 0px }';
        document.head.appendChild(style);
        document.body.style.backgroundColor = 'white';
        document.getElementsByTagName('body')[0].style.minHeight = 'auto';
        document.title = '';
    } else {
        // No element found of specified class
        if (window.JsInterface) {
            window.JsInterface.showErrorMessage(notFoundMessage);
        } else {
            document.getElementsByTagName('body')[0].innerHTML = notFoundMessage;
        }
    }
}
