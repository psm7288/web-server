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