 function checkDuplicateId() {
    // name="username" 인 입력칸의 값을 가져옵니다.
    // (만약 input 태그에 id="username" 속성이 있다면 그대로 두셔도 됩니다.)
    const usernameInput = document.querySelector('input[name="username"]');
    const username = usernameInput ? usernameInput.value.trim() : '';

    if(!username) {
    alert('아이디를 먼저 입력해주세요.');
    return;
}

    // 위에서 만든 MemberController의 API로 GET 요청 전송
    fetch(`/api/members/check-id?username=${username}`)
    .then(response => {
    if(!response.ok) {
    throw new Error("네트워크 응답이 정상이 아닙니다.");
}
    return response.json();
})
    .then(isDuplicate => {
    // isDuplicate 값이 true(중복)인지 false(사용가능)인지 판별
    if(isDuplicate) {
    alert('❌ 이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.');
} else {
    alert('✅ 사용 가능한 아이디입니다!');
}
})
    .catch(error => {
    console.error('Error:', error);
    alert('중복 확인 중 오류가 발생했습니다.');
});
}

function checkUsername() {
    const username = document.getElementById('usernameInput').value.trim();
    const msg = document.getElementById('dupMsg');
    if (!username) {
        msg.textContent = '아이디를 입력해주세요.';
        msg.className = 'dup-msg err';
        return;
    }
    fetch('/signup/check-username?username=' + encodeURIComponent(username))
        .then(r => r.json())
        .then(data => {
            if (data.duplicated) {
                msg.textContent = '❌ 이미 사용 중인 아이디입니다.';
                msg.className = 'dup-msg err';
            } else {
                msg.textContent = '✅ 사용 가능한 아이디입니다.';
                msg.className = 'dup-msg ok';
            }
        });
}