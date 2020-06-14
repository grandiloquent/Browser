if (location.href.startsWith("https://m.youtube.com")) {
    const hidden = function (selector) {
        if (Array.isArray(selector)) {
            const element = document.querySelector(selector[0]);
            if (element.querySelector(selector[1])) {
                element.style.display = 'none';
            }
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
    ].forEach(i => hidden(i));
}
