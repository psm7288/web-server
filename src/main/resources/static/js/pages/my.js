function openDbConsole(serverId) {
    fetch('/api/servers/' + serverId + '/console', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => response.json())
        .then(data => {
            if (data.consoleUrl) {
                window.open(data.consoleUrl, "_blank", "width=1024,height=768");
            } else {
                alert("콘솔 주소를 불러오지 못했습니다.");
            }
        })
        .catch(err => alert("서버 통신 오류가 발생했습니다."));
}

function sendControlSignal(id, action) {
    alert(action + " 신호를 보냈습니다. (ID: " + id + ")");
}