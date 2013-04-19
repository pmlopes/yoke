
$(function(){
  $('code').each(function(){
    $(this).html(highlight($(this).text()));
  });
});

function highlight(java) {
  return java
    // special case to not mess with css class
    .replace(/\bclass\s+/g, '<span class="keyword">$1</span>')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\/\/(.*)/gm, '<span class="comment">//$1</span>')
    .replace(/(".*?")/gm, '<span class="string">$1</span>')
    .replace(/(@\w+)/gm, '<span class="extra">$1</span>')
    .replace(/(\d+\.\d+)/gm, '<span class="number">$1</span>')
    .replace(/(\d+)/gm, '<span class="number">$1</span>')
    .replace(/\bnew *(\w+)/gm, '<span class="keyword">new</span> <span class="init">$1</span>')
    .replace(/\b(import|int|while|if|for|void|boolean|abstract|byte|static|break|char|try|catch|case|const|continue|default|new|double|else|enum|extends|finally|float|final|goto|implements|instanceof|interface|long|native|package|private|public|protected|return|short|super|strictfp|switch|synchronized|this|throw|throws|transient|volatile|assert|true|false|null)\b/gm, '<span class="keyword">$1</span>')
}