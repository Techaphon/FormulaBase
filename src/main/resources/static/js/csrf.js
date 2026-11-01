(function () {
  function token() {
    const item = document.cookie.split('; ').find(value => value.startsWith('XSRF-TOKEN='));
    return item ? decodeURIComponent(item.substring('XSRF-TOKEN='.length)) : '';
  }
  function protectForms() {
    const value = token();
    if (!value) return;
    document.querySelectorAll('form[method="post"], form[method="POST"]').forEach(form => {
      let input = form.querySelector('input[name="_csrf"]');
      if (!input) {
        input = document.createElement('input');
        input.type = 'hidden'; input.name = '_csrf'; form.appendChild(input);
      }
      input.value = value;
    });
  }
  document.addEventListener('DOMContentLoaded', protectForms);
  document.addEventListener('submit', protectForms, true);
})();
