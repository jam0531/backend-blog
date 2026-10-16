//点击出现字体效果
var a_idx = 0;
    jQuery(document).ready(function ($) {
        $("body").click(function (e) {
            var a = new Array("富强", "民主", "文明", "和谐", "自由", "平等", "蕾姆","❤","公正", "法治", "爱国", "敬业", "诚信", "友善");
            var $i = $("<span/>").text(a[a_idx]);
            a_idx = (a_idx + 1) % a.length;
            var x = e.pageX,
                y = e.pageY;
            $i.css({
                "z-index": 9999999999,
                "top": y - 20,
                "left": x,
                "position": "absolute",
                "font-weight": "bold",
                "color": randomColor(),
            });
            function randomColor() {
                return "rgb(" + (~~(Math.random() * 255)) + "," + (~~(Math.random() * 255)) + "," + (~~(Math.random() * 255)) +
                    ")";
            }
            $("body").append($i);
            $i.animate({
                "top": y - 180,
                "opacity": 0
            },
                1500,
                function () {
                    $i.remove();
                });
        });
    });

    var normal_title;
    document.addEventListener('visibilitychange', function () {if (document.visibilityState == 'hidden') {
        normal_title = document.title;document.title = '( ´͈ ᵕ `͈ )◞♡ ';
    }else document.title = normal_title;
    });
