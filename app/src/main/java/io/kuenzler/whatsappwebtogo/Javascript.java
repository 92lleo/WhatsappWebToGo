package io.kuenzler.whatsappwebtogo;

public class Javascript {
    public final static String listenerJS = "javascript:var listenerAlreadyAdded; if(listenerAlreadyAdded==null){listenerAlreadyAdded = false;} var clickEventFunc = function(e){if(e.target && (e.target.className== 'pluggable-input-body copyable-text selectable-text' || e.target.id == 'input-chatlist-search')){TFCLI.hitSomething(e.target.className+''); e.target.click(); } else {TFCLI.hitNothing(e.target.className+'');}}; if(!listenerAlreadyAdded){document.addEventListener('click',clickEventFunc); listenerAlreadyAdded = true;}";
    // private static final String js = "function wrapFunc(name) {alert('works!); if (typeof window[name] == 'function') {var original = window['__' + name] = window[name]; window[name] = function() { var result = original.apply(this, arguments); Interceptor.reportCall(name, result.toString()); return result; } alert('yes'); } else { alert('no'); }}";


}
