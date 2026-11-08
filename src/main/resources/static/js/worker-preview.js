document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('.worker-start-form');
  if (!form) return;
  const input = form.querySelector('[name="lossPercentage"]');
  const preview = document.getElementById('loss-preview');
  const output = document.getElementById('loss-preview-value');
  const update = () => {
    const match = /^(\d{1,3})(?:\.(\d{0,2}))?$/.exec(input.value);
    if (!match || !input.validity.valid || Number(input.value) > 100) { preview.hidden = true; return; }
    const percentage = BigInt(match[1]) * 100n + BigInt((match[2] || '').padEnd(2, '0'));
    const target = BigInt(form.dataset.target);
    const extra = (target * percentage + 9999n) / 10000n;
    output.textContent = `${extra} / ${target + extra}`;
    preview.hidden = false;
  };
  input.addEventListener('input', update);
  update();
});
