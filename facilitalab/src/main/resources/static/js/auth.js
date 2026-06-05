function authFetch(url, options = {}) {
    if (localStorage.getItem('token') === null) {
        window.location.href = '/login';
        return;
    }
    return fetch(url, {
        ...options,
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        }
    });
}
