(function(){
  const themeKey='fb_theme', langKey='fb_lang';
  const theme=localStorage.getItem(themeKey)||'light';
  document.documentElement.classList.toggle('fb-dark',theme==='dark');
  window.fbSetLanguage=function(lang){if(!['th','en'].includes(lang))return;const url=new URL(location.href);url.searchParams.set('lang',lang);location.assign(url);};
  window.fbToggleTheme=function(){const dark=!document.documentElement.classList.contains('fb-dark');localStorage.setItem(themeKey,dark?'dark':'light');document.documentElement.classList.toggle('fb-dark',dark);fbUpdateThemeButton();};
  window.fbUpdateThemeButton=function(){const b=document.getElementById('fb-theme-toggle');if(b)b.textContent=document.documentElement.classList.contains('fb-dark')?'☀':'☾';};
  document.addEventListener('DOMContentLoaded',function(){document.querySelectorAll('[data-lang]').forEach(b=>{const selected=b.dataset.lang===document.documentElement.lang;b.classList.toggle('active',selected);b.setAttribute('aria-pressed',String(selected));});document.querySelectorAll('.fb-sidebar nav a').forEach(a=>{const selected=location.pathname===a.pathname||location.pathname.startsWith(a.pathname+'/');a.classList.toggle('active',selected);if(selected)a.setAttribute('aria-current','page');});fbUpdateThemeButton();});
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
