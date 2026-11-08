document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('#formulaForm, .formula-edit-form');
  const basis = document.getElementById('calculationBasis');
  const summary = document.getElementById('percentage-summary');
  const total = document.getElementById('percentage-total');
  if (!form || !basis || !summary || !total) return;
  const update = () => {
    summary.hidden = basis.value !== 'PERCENT_W_W';
    let sum = 0n;
    for (const input of form.querySelectorAll('.percent-input')) {
      if (!input.value) continue;
      const match = /^(\d+)(?:\.(\d{0,6}))?$/.exec(input.value);
      if (!match) { total.textContent = '—'; return; }
      sum += BigInt(match[1]) * 1000000n + BigInt((match[2] || '').padEnd(6, '0'));
    }
    const whole = sum / 1000000n;
    const fraction = (sum % 1000000n).toString().padStart(6, '0').replace(/0+$/, '');
    total.textContent = fraction ? `${whole}.${fraction}` : whole.toString();
  };
  form.addEventListener('input', update);
  form.addEventListener('change', update);
  const rows = document.getElementById('materialRows');
  if (rows) new MutationObserver(update).observe(rows, { childList: true });
  update();
});
