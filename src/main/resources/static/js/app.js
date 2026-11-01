(function(){
  const themeKey='fb_theme', langKey='fb_lang';
  const theme=localStorage.getItem(themeKey)||'light';
  document.documentElement.classList.toggle('fb-dark',theme==='dark');
  window.fbSetLanguage=function(lang){if(!['th','en'].includes(lang))return;const url=new URL(location.href);url.searchParams.set('lang',lang);location.assign(url);};
  window.fbToggleTheme=function(){const dark=!document.documentElement.classList.contains('fb-dark');localStorage.setItem(themeKey,dark?'dark':'light');document.documentElement.classList.toggle('fb-dark',dark);fbUpdateThemeButton();};
  window.fbUpdateThemeButton=function(){const b=document.getElementById('fb-theme-toggle');if(b)b.textContent=document.documentElement.classList.contains('fb-dark')?'☀':'☾';};
  const thaiText={
    'Create Formula':'สร้างสูตร','Formulas':'สูตรผลิต','Formula name':'ชื่อสูตร','Document No.':'เลขเอกสาร','Materials per 1 product unit':'วัตถุดิบต่อสินค้า 1 ชิ้น','Production steps (optional)':'ขั้นตอนการผลิต (ไม่บังคับ)','Temperature not required':'ไม่ต้องบันทึกอุณหภูมิ','Temperature required':'ต้องบันทึกอุณหภูมิ','Save Formula':'บันทึกสูตร','Remove':'ลบ','+ Add material':'+ เพิ่มวัตถุดิบ','+ Add step':'+ เพิ่มขั้นตอน','New material name':'ชื่อวัตถุดิบใหม่','New code':'รหัสใหม่','Qty / unit':'ปริมาณ / ชิ้น','Unit':'หน่วย','-- New material --':'-- สร้างวัตถุดิบใหม่ --','Create a new material here when it is not in the catalogue. Production steps run in order after the worker starts a batch.':'สร้างวัตถุดิบใหม่ได้ทันทีหากยังไม่มีในรายการ และขั้นตอนการผลิตจะทำตามลำดับหลังเริ่ม Batch แล้ว','Set target time in seconds. The worker must record temperature only for steps where it is required.':'กำหนดเวลาเป้าหมายเป็นวินาที และให้ Worker บันทึกอุณหภูมิเฉพาะขั้นตอนที่กำหนด',
    'Monthly Planning':'แผนรายเดือน','Planning Cycle':'แผนรายเดือน','Add task':'เพิ่มงาน','Lock monthly plan':'ล็อกแผนรายเดือน','Select confirmed formula':'เลือกสูตรที่ยืนยันแล้ว','Unassigned':'ยังไม่มอบหมาย','Date':'วันที่','Target':'เป้าหมาย','Actions':'การจัดการ','Print':'พิมพ์','Change':'เปลี่ยน','Production history':'ประวัติการผลิต','Completed':'เสร็จสิ้นเมื่อ','Formula':'สูตร','Planned':'แผนผลิต','Actual':'ผลิตจริง','Good':'ดี','Rejected':'เสีย',
    'Batch Execution':'ดำเนินการผลิต','My tasks':'งานของฉัน','Start entire batch':'เริ่มผลิตทั้ง Batch','Production steps':'ขั้นตอนการผลิต','Start step':'เริ่มขั้นตอน','Complete step':'จบขั้นตอน','Complete batch':'บันทึกผลผลิตทั้ง Batch','Actual produced units':'จำนวนผลิตจริง','Rejected/lost units':'จำนวนเสีย/สูญเสีย','Loss percentage (%)':'เปอร์เซ็นต์เผื่อการสูญเสีย (%)','Materials for this batch':'วัตถุดิบสำหรับ Batch นี้','Target':'เป้าหมาย','Status':'สถานะ','PENDING':'รอเริ่ม','IN_PROGRESS':'กำลังดำเนินการ','COMPLETED':'เสร็จสิ้น','Temperature (°C)':'อุณหภูมิ (°C)','Target seconds':'เวลาที่กำหนด (วินาที)','Instruction, e.g. Mix until uniform':'คำสั่ง เช่น ผสมจนเป็นเนื้อเดียวกัน'
  };
  function translateStaticUi(){if(!document.documentElement.lang.startsWith('th'))return;const walker=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT);const nodes=[];while(walker.nextNode())nodes.push(walker.currentNode);nodes.forEach(node=>{const raw=node.nodeValue;const key=raw.trim();if(thaiText[key])node.nodeValue=raw.replace(key,thaiText[key]);});document.querySelectorAll('[placeholder]').forEach(el=>{if(thaiText[el.placeholder])el.placeholder=thaiText[el.placeholder];});}
  document.addEventListener('DOMContentLoaded',function(){document.querySelectorAll('[data-lang]').forEach(b=>{const selected=b.dataset.lang===document.documentElement.lang;b.classList.toggle('active',selected);b.setAttribute('aria-pressed',String(selected));});document.querySelectorAll('.fb-sidebar nav a').forEach(a=>{const selected=location.pathname===a.pathname||location.pathname.startsWith(a.pathname+'/');a.classList.toggle('active',selected);if(selected)a.setAttribute('aria-current','page');});fbUpdateThemeButton();translateStaticUi();});
  document.addEventListener('DOMContentLoaded',function(){
    const token=(document.cookie.split('; ').find(v=>v.startsWith('XSRF-TOKEN='))||'').split('=')[1];
    if(!token)return;
    document.querySelectorAll('form[method="post"],form[method="POST"]').forEach(form=>{
      if(!form.querySelector('input[name="_csrf"]')){const input=document.createElement('input');input.type='hidden';input.name='_csrf';input.value=decodeURIComponent(token);form.append(input);}
    });
  });
  document.addEventListener('DOMContentLoaded',function(){
    const form=document.getElementById('complete-batch');
    if(form)form.addEventListener('submit',function(event){
      const actual=Number(form.elements.actualProduced.value), rejected=Number(form.elements.rejected.value);
      if(rejected<=actual && (actual-rejected<Number(form.dataset.target)||rejected>Number(form.dataset.allowance))) {
        let confirmation=form.querySelector('[name="confirmException"]');
        if(!confirmation){
          const label=document.createElement('label');label.className='error';
          confirmation=document.createElement('input');confirmation.type='checkbox';confirmation.name='confirmException';
          label.append(confirmation,document.createTextNode(form.dataset.warning));
          form.insertBefore(label,form.querySelector('button'));
          form.querySelectorAll('input[type="number"]').forEach(input=>input.addEventListener('input',()=>{confirmation.checked=false;}));
        }
        if(!confirmation.checked){event.preventDefault();confirmation.focus();}
      }
    });
  });
})();
