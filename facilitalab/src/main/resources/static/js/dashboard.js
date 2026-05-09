async function carregarDashboard() {
    try {
        const res = await fetch('/usuarios');
        const lista = await res.json();
        document.getElementById('totalUsuarios').textContent = lista.length;
    } catch (e) {
        document.getElementById('totalUsuarios').textContent = '—';
    }
}

carregarDashboard();