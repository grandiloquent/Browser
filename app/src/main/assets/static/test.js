if (location.href.startsWith("https://m.youtube.com")) {
    const hidden = function (selector) {
        if (Array.isArray(selector)) {
            document.querySelectorAll(selector[0])
                .forEach(element => {
                    if (element.querySelector(selector[1])) {
                        element.style.display = 'none';
                    }
                });


        } else {
            const element = document.querySelector(selector);
            if (element) {
                element.style.display = 'none';
            }
        }

    };
    ['.slim-owner-subscribe-button',
     ['.scwnr-content', '.GoogleActiveViewElement'],
     ['.scwnr-content', '.clarification-container'],
     ['.scwnr-content', '.companion-ad-container'],
    ].forEach(i => hidden(i));
}
