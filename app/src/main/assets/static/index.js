function substringAfter(string, delimiter) {
    const index = string.indexOf(delimiter);
    if (index !== -1)
        return string.substring(index + delimiter.length);
    return string;
}

function substringAfterLast(string, delimiter) {
    const index = string.lastIndexOf(delimiter);
    if (index !== -1)
        return string.substring(index + delimiter.length);
    return string;
}

function substringBefore(string, delimiter) {
    const index = string.indexOf(delimiter);
    if (index !== -1)
        return string.substring(0, index);
    return string;
}

function substringBeforeLast(string, delimiter) {
    const index = string.lastIndexOf(delimiter);
    if (index !== -1)
        return string.substring(0, index);
    return string;
}

class Application {
    constructor() {
        this.applyMenu();

        this.items = document.querySelectorAll('.item-section-renderer');
        for (let i = 0; i < this.items.length; i++) {
            const menuButton = this.items[i].querySelector('.menu>button');
            const src = this.items[i].querySelector('.large-media-item>a').href
            menuButton.addEventListener('click', evt => {
                this.menu.style.display = 'flex';
                this.menuDelete.setAttribute('data-src', substringAfter(src, '='));

            })
        }
    }

    applyMenu() {
        this.menu = document.getElementById('menu');
        this.menu.querySelector('.c3-overlay').addEventListener('click', this.closeMenu.bind(this))
        this.menu.querySelector('.menu-cancel-button').addEventListener('click', this.closeMenu.bind(this));
        this.menuDelete = this.menu.querySelector('.menu-service-item-renderer .menu-item-button');
        this.menuDelete.addEventListener('click', evt => {
            const filepath = this.menuDelete.getAttribute('data-src');
            fetch('/remove?v=' + filepath);
            this.closeMenu();
            Array.from(this.items).filter(element => {
                return element.querySelector('a').href.endsWith(filepath)
            }).forEach(element => element.remove());

            this.items=document.querySelectorAll('.item-section-renderer');
        })
    }

    closeMenu() {
        this.menu.style.display = 'none';
        this.menuDelete.removeAttribute('data-src');
    }
}

window.addEventListener('DOMContentLoaded', (event) => {
    new Application();
});