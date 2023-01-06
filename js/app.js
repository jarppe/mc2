var shadow$provide = {};
(function(){
shadow$provide[0]=function(b,c,a,d){a.exports=PIXI};
/*

 Copyright The Closure Library Authors.
 SPDX-License-Identifier: Apache-2.0
*/
'use strict';var a=this||self;var e;a:{const b=a.navigator;if(b){const d=b.userAgent;if(d){e=d;break a}}e=""}let f=e;function g(b){return-1!=f.indexOf(b)};var h={},m={},n=[];function q(b,d){var p=m[b];if(void 0!==p)return p;try{n.push(b);var c=h[b],k=shadow$provide[b];if(void 0===c){if(void 0===k)throw"Module not provided: "+b;c={exports:{}};h[b]=c}if(k){delete shadow$provide[b];try{k.call(c,a,q,c,c.exports)}catch(u){throw console.warn("shadow-cljs - failed to load",b),u;}if(d){var l=d.globals;if(l)for(b=0;b<l.length;b++)window[l[b]]=c.exports}}}finally{n.pop()}return c.exports}q.cache={};q.resolve=function(b){return b};q(0,{});function r(){return g("iPhone")&&!g("iPod")&&!g("iPad")};g("Opera");g("Trident")||g("MSIE");g("Edge");!g("Gecko")||-1!=f.toLowerCase().indexOf("webkit")&&!g("Edge")||g("Trident")||g("MSIE")||g("Edge");-1!=f.toLowerCase().indexOf("webkit")&&!g("Edge")&&g("Mobile");g("Macintosh");g("Windows");g("Linux")||g("CrOS");var t=a.navigator||null;t&&(t.appVersion||"").indexOf("X11");g("Android");r();g("iPad");g("iPod");r()||g("iPad")||g("iPod");f.toLowerCase().indexOf("kaios");
}).call(this);