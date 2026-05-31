function sendControlSignal(serverId, action) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/servers/control';

    let idInput = document.createElement('input');
    idInput.type = 'hidden';
    idInput.name = 'serverId';
    idInput.value = serverId;
    form.appendChild(idInput);

    let actionInput = document.createElement('input');
    actionInput.type = 'hidden';
    actionInput.name = 'action';
    actionInput.value = action;
    form.appendChild(actionInput);

    document.body.appendChild(form);
    form.submit();
}

window.onload = function() {
    updateSpecifications();
};

function updateSpecifications() {
    // 1. 웹 서버 선택 요소들
    const wCpu = document.querySelector('select[name="webCpu"]');
    const wRam = document.querySelector('select[name="webRam"]');
    const wSto = document.querySelector('select[name="webStorage"]');

    // 2. DB 서버 선택 요소들
    const dCpu = document.querySelector('select[name="dbCpu"]');
    const dRam = document.querySelector('select[name="dbRam"]');
    const dSto = document.querySelector('select[name="dbStorage"]');

    // 3. 가격 계산 (Data Attribute 활용)
    const basePrice = 27000; // 웹 서버 기본가
    const wExtra = parseInt(wCpu.options[wCpu.selectedIndex].dataset.price) +
        parseInt(wRam.options[wRam.selectedIndex].dataset.price) +
        parseInt(wSto.options[wSto.selectedIndex].dataset.price);

    const dExtra = parseInt(dCpu.options[dCpu.selectedIndex].dataset.price) +
        parseInt(dRam.options[dRam.selectedIndex].dataset.price) +
        parseInt(dSto.options[dSto.selectedIndex].dataset.price);

    const totalWebPrice = basePrice + wExtra;
    const total = totalWebPrice + dExtra;

    // 4. Flavor 문자열 생성 (예: c4.r8.d100)
    const webFlv = `c${wCpu.value}.r${wRam.value}.d${wSto.value}`;
    const dbFlv = `c${dCpu.value}.r${dRam.value}.d${dSto.value}`;

    // 5. Hidden 필드 데이터 동기화
    document.getElementById('webFlavor').value = webFlv;
    document.getElementById('dbFlavor').value = dbFlv;
    document.getElementById('webPrice').value = totalWebPrice;
    document.getElementById('dbPrice').value = dExtra;
    document.getElementById('totalPrice').value = total;

    // 6. 화면 표시 금액 업데이트
    document.getElementById('total-price-display').innerText = total.toLocaleString();
}

// 비밀번호 일치 확인 (간단 예시)
document.getElementById('dbPasswordCheck').addEventListener('keyup', function() {
    const pw = document.getElementById('dbPassword').value;
    const check = this.value;
    const msg = document.getElementById('pwCheckMsg');
    if(pw === check) {
        msg.innerText = "비밀번호가 일치합니다.";
        msg.style.color = "green";
    } else {
        msg.innerText = "비밀번호가 일치하지 않습니다.";
        msg.style.color = "red";
    }
});

// my.html 하단 스크립트 예시
function checkStatus(requestId, elementId) {
    const interval = setInterval(() => {
        fetch(`/api/servers/requests/${requestId}/status`)
            .then(res => res.json())
            .then(data => {
                const statusBadge = document.getElementById(elementId);
                statusBadge.innerText = data.status;

                if (data.status === 'ACTIVE' || data.status === 'FAILED') {
                    clearInterval(interval);
                    location.reload(); // 완료되면 전체 새로고침하여 버튼 활성화
                }
            });
    }, 5000); // 5초마다 체크
}



