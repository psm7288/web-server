// 🛠️ 프론트엔드 검증 플래그 할당
let isDbNameChecked = false;

// 1. DB 서버 이름 비동기 중복 체크 기능 (Fetch)
function checkDbNameDup() {
    const dbNameInput = document.getElementById('dbName');
    const dbName = dbNameInput.value.trim();
    const msgSpan = document.getElementById('dbNameCheckMsg');

    if (!dbName) {
        alert('DB 서버 이름을 먼저 입력해 주세요.');
        dbNameInput.focus();
        return;
    }

    // 컨트롤러 비동기 엔드포인트 호출
    fetch(`/servers/check-db-dup?dbName=${encodeURIComponent(dbName)}`)
        .then(res => res.json())
        .then(isDuplicate => {
            if (isDuplicate) {
                msgSpan.style.color = '#dc2626'; // 빨간색
                msgSpan.innerText = '❌ 이미 사용 중인 DB 서버 이름입니다.';
                isDbNameChecked = false;
            } else {
                msgSpan.style.color = '#16a34a'; // 초록색
                msgSpan.innerText = ' canUse ➔ 사용 가능한 DB 서버 이름입니다.';
                isDbNameChecked = true;
            }
        })
        .catch(err => {
            console.error(err);
            alert('중복 검사 시스템 통신에 실패했습니다.');
        });
}

// DB 이름을 새로 수정하기 시작하면 다시 중복 체크 유도
document.getElementById('dbName').addEventListener('input', function() {
    isDbNameChecked = false;
    document.getElementById('dbNameCheckMsg').innerText = '';
});

// 2. DB 비밀번호 입력 필드 실시간 대조 검증 기능
const pwInput = document.getElementById('dbPassword');
const pwCheckInput = document.getElementById('dbPasswordCheck');
const pwMsg = document.getElementById('pwCheckMsg');

function validatePassword() {
    if (!pwInput.value || !pwCheckInput.value) {
        pwMsg.innerText = '';
        return false;
    }
    if (pwInput.value === pwCheckInput.value) {
        pwMsg.style.color = '#16a34a';
        pwMsg.innerText = ' canUse ➔ 비밀번호가 일치합니다.';
        return true;
    } else {
        pwMsg.style.color = '#dc2626';
        pwMsg.innerText = '❌ 비밀번호가 실മായി 일치하지 않습니다.';
        return false;
    }
}

pwInput.addEventListener('input', validatePassword);
pwCheckInput.addEventListener('input', validatePassword);


// 3. 기존 가격 측정 엔진 스크립트 기반 동작 유지
function updatePrice() {
    const basePrice = 45000;
    let additionalPrice = 0;

    const selects = document.querySelectorAll('.config-select');
    selects.forEach(select => {
        const selectedOption = select.options[select.selectedIndex];
        const price = parseInt(selectedOption.getAttribute('data-price')) || 0;
        additionalPrice += price;
    });

    const totalPrice = basePrice + additionalPrice;
    document.getElementById('total-price-display').innerText = totalPrice.toLocaleString();
}

window.onload = function() {
    updatePrice();
};

// 4. Form 전송 전 유효성 검사 차단 인터셉터
document.querySelector('form').addEventListener('submit', function(e) {
    if (!isDbNameChecked) {
        e.preventDefault();
        alert('DB 서버 이름 중복 확인을 완료해 주세요.');
        return;
    }
    if (!validatePassword()) {
        e.preventDefault();
        alert('DB 비밀번호 재확인 입력이 올바르지 않습니다.');
        pwCheckInput.focus();
        return;
    }
});