let isDbNameChecked = false;

function checkDbNameDup() {
    const dbName = document.getElementById('dbServerName').value.trim();
    if (!dbName) { alert('이름을 입력하세요.'); return; }

    fetch(`/api/servers/check-name?name=${encodeURIComponent(dbName)}`)
        .then(res => res.json())
        .then(isDuplicate => {
            if (isDuplicate) {
                document.getElementById('dbNameCheckMsg').innerText = '❌ 이미 사용 중입니다.';
                isDbNameChecked = false;
            } else {
                document.getElementById('dbNameCheckMsg').innerText = '✅ 사용 가능합니다.';
                isDbNameChecked = true;
            }
        });
}

document.getElementById('orderForm').addEventListener('submit', function(e) {
    e.preventDefault();
    if (!isDbNameChecked) { alert('중복 확인을 먼저 해주세요.'); return; }

    const data = Object.fromEntries(new FormData(this).entries());
    fetch("/servers/request", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    }).then(() => {
        alert("신청 완료!");
        window.location.href = "/servers/my";
    });
});

window.onload = () => {
    isDbNameChecked = false;
    document.getElementById('dbServerName').addEventListener('input', () => {
        isDbNameChecked = false;
        document.getElementById('dbNameCheckMsg').innerText = '';
    });
};