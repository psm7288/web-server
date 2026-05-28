let currentCost = 0;

function getSelectedCost() {
    let total = 0;
    const selects = document.querySelectorAll('.select-ctrl');
    selects.forEach(sel => {
        total += parseInt(sel.options[sel.selectedIndex].getAttribute('data-cost')) || 0;
    });
    return total;
}

// 💡 교정 완료: 타임리프가 최초 선택된(selected) 항목들을 렌더링한 직후의 정확한 비용을 기점으로 삼도록 보완
window.onload = function() {
    currentCost = getSelectedCost();
};

function calcDiffPrice() {
    let nextCost = getSelectedCost();
    let diff = nextCost - currentCost;

    if (diff < 0) diff = 0;

    document.getElementById('modalDiffDisplay').innerText = diff.toLocaleString();
    document.getElementById('payAmountInput').value = diff;
}

function openPaymentModal() {
    calcDiffPrice();
    document.getElementById('payModal').style.display = 'flex';
}

function closePaymentModal() {
    document.getElementById('payModal').style.display = 'none';
}

function submitUpgrade() {
    document.getElementById('upgradeForm').submit();
}