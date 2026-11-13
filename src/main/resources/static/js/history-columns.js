(() => {
 const table=document.getElementById('history-table'); if(!table)return;
 const widths=[155,155,260,160,100,190,160,110,110,160,100,190];
 const group=document.createElement('colgroup'); const cols=widths.map(w=>{const c=document.createElement('col');c.style.width=w+'px';group.append(c);return c;}); table.prepend(group);
 const update=()=>table.style.width=cols.reduce((sum,c)=>sum+parseFloat(c.style.width),0)+'px'; update();
 table.querySelectorAll('th').forEach((th,i)=>{
  const handle=document.createElement('span');handle.className='column-resizer';handle.tabIndex=0;handle.setAttribute('role','separator');handle.setAttribute('aria-orientation','vertical');handle.setAttribute('aria-label',th.textContent);handle.setAttribute('aria-valuemin','90');handle.setAttribute('aria-valuemax','600');
  const set=w=>{w=Math.max(90,Math.min(600,w));cols[i].style.width=w+'px';handle.setAttribute('aria-valuenow',String(w));update();};set(widths[i]);
  handle.addEventListener('pointerdown',e=>{e.preventDefault();handle.setPointerCapture(e.pointerId);const x=e.clientX,w=parseFloat(cols[i].style.width);const move=ev=>set(w+ev.clientX-x);const end=()=>{handle.removeEventListener('pointermove',move);handle.removeEventListener('pointerup',end);handle.removeEventListener('pointercancel',end);};handle.addEventListener('pointermove',move);handle.addEventListener('pointerup',end);handle.addEventListener('pointercancel',end);});
  handle.addEventListener('keydown',e=>{if(e.key==='ArrowLeft'||e.key==='ArrowRight'){e.preventDefault();set(parseFloat(cols[i].style.width)+(e.key==='ArrowRight'?20:-20));}});th.append(handle);
 });
 document.getElementById('reset-history-widths').addEventListener('click',()=>{cols.forEach((c,i)=>{c.style.width=widths[i]+'px';table.querySelectorAll('.column-resizer')[i].setAttribute('aria-valuenow',widths[i]);});update();});
})();
