const SOI=new Uint8Array(3);SOI[0]=255,SOI[1]=219,SOI[2]=216;const CONTENT_LENGTH="CONTENT-LENGTH",TYPE_JPEG="image/jpeg";let controller=new AbortController;function stop_streaming(){controller.abort(),controller=new AbortController}function start_streaming(e,t,r,o){interrupt=!1,fetch(e,{method:"STREAM",body:JSON.stringify({deviceId:t,deviceSubId:r}),signal:controller.signal}).then(e=>{if(!e.ok)throw Error(e.status+" "+e.statusText);if(!e.body)throw Error("ReadableStream not yet supported in this browser.");const t=e.body.getReader();let r="",n=-1,l=null,s=0,a=new FileReader;a.onload=function(){document.getElementById(o).src=a.result};const c=()=>{t.read().then(({done:e,value:t})=>{if(e)controller.close();else{for(let e=0;e<t.length;e++)if(t[e]===SOI[0]&&t[e+1]===SOI[2]&&(n=getLength(r),l=new Uint8Array(n)),n<=0)r+=String.fromCharCode(t[e]);else if(s<n)l[s++]=t[e];else{let e=new Blob([l.buffer],{type:TYPE_JPEG});a.readAsDataURL(e),n=0,s=0,r=""}c()}}).catch(e=>{console.error(e)})};c()}).catch(e=>{console.error(e)})}const getLength=e=>{let t=0;return e.split("\r\n"),e.split("\r\n").forEach((e,r)=>{const o=e.split(":");o[0].toUpperCase()===CONTENT_LENGTH&&(t=parseInt(o[1],10))}),t};