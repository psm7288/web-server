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