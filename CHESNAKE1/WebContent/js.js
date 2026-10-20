/* Home page carousel: switches the hero images and focus dots */
(function () {
    function ready(fn) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', fn);
        } else {
            fn();
        }
    }

    ready(function () {
        var container = document.querySelector('.container');
        if (!container) {
            return;
        }
        var imgs = Array.prototype.slice.call(container.querySelectorAll('img'));
        var dots = Array.prototype.slice.call(container.querySelectorAll('ul li'));
        var leftBtn = container.querySelector('.left');
        var rightBtn = container.querySelector('.right');
        if (imgs.length === 0) {
            return;
        }

        var current = 0;
        var timer = null;

        function show(index) {
            current = (index + imgs.length) % imgs.length;
            imgs.forEach(function (img, i) {
                img.className = i === current ? 'on' : '';
            });
            dots.forEach(function (dot, i) {
                dot.className = i === current ? 'active' : '';
            });
        }

        function next() { show(current + 1); }
        function prev() { show(current - 1); }

        function start() {
            if (timer) { clearInterval(timer); }
            timer = setInterval(next, 3000);
        }

        if (leftBtn) { leftBtn.addEventListener('click', function () { prev(); start(); }); }
        if (rightBtn) { rightBtn.addEventListener('click', function () { next(); start(); }); }
        dots.forEach(function (dot, i) {
            dot.addEventListener('click', function () { show(i); start(); });
        });

        show(0);
        start();
    });
})();
