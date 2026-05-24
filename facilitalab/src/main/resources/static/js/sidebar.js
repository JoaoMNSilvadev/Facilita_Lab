// Carrega a sidebar a partir de um arquivo estático e define o item ativo.
// Substitui o th:replace + th:classappend do Thymeleaf.
async function carregarSidebar() {
    const res  = await fetch('/fragments/sidebar.html');
    const html = await res.text();
    document.getElementById('sidebar-container').innerHTML = html;

    const path = window.location.pathname;

    document.querySelectorAll('.nav-item').forEach(a => {
        const href = a.getAttribute('href');
        // /editar/* pertence à seção de lista
        const ativo = path === href || (href === '/lista' && path.startsWith('/editar'));
        a.classList.toggle('active', ativo);
    });
}

carregarSidebar();

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('nome');
    localStorage.removeItem('perfil');
    window.location.href = '/login';
}
