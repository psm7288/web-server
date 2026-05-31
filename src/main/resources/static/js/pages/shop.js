// 🛠️ 프론트엔드 검증 플래그
let isDbNameChecked = false;

// 1. DB 서버 이름 중복 체크 (Fetch 방식)
function checkDbNameDup() {
    // HTML의 id가 dbServerName인지 dbName인지 확인 필요 (기존 HTML 기반 dbServerName으로 매핑)
    const dbNameInput = document.getElementById('dbServerName');
    const dbName = dbNameInput.value.trim();
    const msgSpan = document.getElementById('dbNameCheckMsg');

    if (!dbName) {
        alert('DB 서버 이름을 먼저 입력해 주세요.');
        dbNameInput.focus();
        return;
    }

    // 백엔드 API 호출 (주소 확인: /api/servers/check-name)
    fetch(`/api/servers/check-name?name=${encodeURIComponent(dbName)}`)
        .then(res => res.json())
        .then(isDuplicate => {
            if (isDuplicate) {
                msgSpan.style.color = '#dc2626';
                msgSpan.innerText = '❌ 이미 사용 중인 이름입니다.';
                isDbNameChecked = false;
            } else {
                msgSpan.style.color = '#16a34a';
                msgSpan.innerText = '✅ 사용 가능한 이름입니다.';
                isDbNameChecked = true;
            }
        })
        .catch(err => {
            console.error(err);
            alert('중복 검사 시스템 통신 실패');
        });
}

// 2. 가격 및 사양 업데이트 엔진
function updateSpecifications() {
    const wCpu = document.querySelector('select[name="webCpu"]');
    const wRam = document.querySelector('select[name="webRam"]');
    const wSto = document.querySelector('select[name="webStorage"]');
    const dCpu = document.querySelector('select[name="dbCpu"]');
    const dRam = document.querySelector('select[name="dbRam"]');
    const dSto = document.querySelector('select[name="dbStorage"]');

    if(!wCpu || !dCpu) return; // 요소 로드 전 실행 방지

    const basePrice = 27000;
    const wExtra = parseInt(wCpu.options[wCpu.selectedIndex].dataset.price || 0) +
        parseInt(wRam.options[wRam.selectedIndex].dataset.price || 0) +
        parseInt(wSto.options[wSto.selectedIndex].dataset.price || 0);

    const dExtra = parseInt(dCpu.options[dCpu.selectedIndex].dataset.price || 0) +
        parseInt(dRam.options[dRam.selectedIndex].dataset.price || 0) +
        parseInt(dSto.options[dSto.selectedIndex].dataset.price || 0);

    const total = basePrice + wExtra + dExtra;

    // Hidden 필드 데이터 동기화
    document.getElementById('webFlavor').value = `c${wCpu.value}.r${wRam.value}.d${wSto.value}`;
    document.getElementById('dbFlavor').value = `c${dCpu.value}.r${dRam.value}.d${dSto.value}`;
    document.getElementById('webPrice').value = basePrice + wExtra;
    document.getElementById('dbPrice').value = dExtra;
    document.getElementById('totalPrice').value = total;

    // 디스플레이 업데이트
    document.getElementById('total-price-display').innerText = total.toLocaleString();
}

// 3. 비밀번호 대조 검증
function validatePassword() {
    const pw = document.getElementById('dbPassword').value;
    const check = document.getElementById('dbPasswordCheck').value;
    const msg = document.getElementById('pwCheckMsg');

    if (!pw || !check) { msg.innerText = ''; return false; }

    if (pw === check) {
        msg.innerText = "✅ 비밀번호가 일치합니다.";
        msg.style.color = "green";
        return true;
    } else {
        msg.innerText = "❌ 비밀번호가 일치하지 않습니다.";
        msg.style.color = "red";
        return false;
    }
}

// 4. [중요] Form 전송 인터셉터 (JSON 전송 및 페이지 이동 실패 방지)
document.getElementById('orderForm').addEventListener('submit', function(e) {
    e.preventDefault(); // 페이지 이동 방지

    if (!isDbNameChecked) {
        alert('DB 서버 이름 중복 확인을 완료해 주세요.');
        return;
    }
    if (!validatePassword()) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    const submitBtn = this.querySelector('.btn-submit');
    submitBtn.disabled = true;
    submitBtn.innerText = "⏳ 서버 생성 요청 중...";

    const formData = new FormData(this);
    const data = Object.fromEntries(formData.entries());

    fetch("/api/servers/requests", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(result => {
            alert("서버 신청이 접수되었습니다!");
            window.location.href = "/servers/my"; // 성공 시 마이 서버 페이지로 이동
        })
        .catch(err => {
            alert("신청 중 오류가 발생했습니다.");
            submitBtn.disabled = false;
            submitBtn.innerText = "💳 다음 단계: 결제 확인";
        });
});

// 이벤트 리스너 등록
window.onload = function() {
    updateSpecifications();

    // 입력창 수정 시 중복체크 초기화
    document.getElementById('dbServerName').addEventListener('input', () => {
        isDbNameChecked = false;
        document.getElementById('dbNameCheckMsg').innerText = '';
    });

    document.getElementById('dbPasswordCheck').addEventListener('input', validatePassword);
};