document.addEventListener('DOMContentLoaded', async () => {
    try {
        const isAuthPage = window.location.pathname.includes('login.html') || window.location.pathname.includes('register.html') || window.location.pathname.includes('index.html');
        const navFile = isAuthPage ? '../components/nav-public.html' : '../components/nav.html';

        const response = await fetch(navFile);
        const navHtml = await response.text();
        document.getElementById('nav-container').innerHTML = navHtml;

        const logoutButton = document.getElementById('logout');
        if (logoutButton) {
            logoutButton.addEventListener('click', () => {
                localStorage.removeItem('token');
                window.location.href = '../index.html';
            });
        }
    } catch (error) {
        console.error('Error al cargar la navegación:', error);
    }
});