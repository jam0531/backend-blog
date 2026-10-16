//返回页面顶部
var timer = null;
function backTop(){
  cancelAnimationFrame(timer);
  timer = requestAnimationFrame(function fn(){
      var oTop = document.body.scrollTop || document.documentElement.scrollTop;
      if(oTop > 0){
      document.body.scrollTop = document.documentElement.scrollTop = oTop - 50;
      timer = requestAnimationFrame(fn);
      }else{
      cancelAnimationFrame(timer);
      } 
  });
}

// 滚轮下滑的时候出现backTop按钮
window.onscroll = function () {
    backTopShow();
  // menuShow();
  
};

function backTopShow(){
  var dTop = document.body.scrollTop || document.documentElement.scrollTop;
  if(dTop > 60){
      $('#backTop').css("display", "block")
  }else{
      $('#backTop').css("display", "none")
  }
}


//浮动图标点击移动到正文内容处
function scrollToDest (name, offset = 0) {
  var scrollOffset = $(name).offset()
  $('body,html').animate({
    scrollTop: scrollOffset.top - offset
  })
}

$('.scroll-down-bar').on('click', function () {
  scrollToDest('#main')
})

