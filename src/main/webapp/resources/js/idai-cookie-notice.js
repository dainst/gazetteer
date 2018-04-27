var IDaiCookieNotice = (function () {

    function setCookie() {

        var today = new Date();
        var expiry = new Date(today.getTime() + 30 * 24 * 3600 * 1000); // plus 30 days
        document.cookie = "idai-cookie-notice=1; path=/; expires=" + expiry.toGMTString();
    }

    function isCookieSet() {

        return document.cookie.indexOf("idai-cookie-notice=1") !== -1;
    }

    function dismiss() {

        var elem = document.getElementById('idai-cookie-notice');
        elem.parentNode.removeChild(elem);
        setCookie();
    }

    function create() {

        var userLang = navigator.language || navigator.userLanguage;

        if (userLang.startsWith("de")) {
            text = "Um unsere Webseite für Sie optimal zu gestalten und fortlaufend verbessern zu können, verwenden wir Cookies. Durch die weitere Nutzung der Webseite stimmen Sie der Verwendung von Cookies zu. Weitere Informationen zu Cookies erhalten Sie in unserer <a href='http://www.dainst.org/de/datenschutz' target='_blank'>Datenschutzerklärung<a>.";
            buttonLabel = "Akzeptieren";
        } else {
            text = "This website uses cookies to ensure the best possible experience. By continuing to use this website you are giving consent to cookies being used. For further information on cookies visit our <a href='http://www.dainst.org/en/datenschutz' target='_blank'>Privacy Policy</a>.";
            buttonLabel = "Got it!"
        }

        style = "background-color: #E6E2E2; bottom: 0; padding: 15px; position: fixed;";
        html = "<div style='" + style + "' class='bg-info' id='idai-cookie-notice'>"
            + "<button id='cookieBtn' class='btn btn-primary' onclick='IDaiCookieNotice.dismiss()' style='float:right;'>" + buttonLabel + "</button>"
            + text
            + "</div>";

        document.write(html);
    }


    if (!isCookieSet()) {
        create();
    }

    return {
        dismiss: dismiss
    }

})();