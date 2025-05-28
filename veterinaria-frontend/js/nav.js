// js/nav.js
document.addEventListener('DOMContentLoaded', async () => {
    // Función para cargar un componente HTML desde una URL
    async function loadComponent(url) {
        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error(`No se pudo cargar ${url}`);
            return await response.text();
        } catch (error) {
            console.error('Error al cargar el componente:', error);
            return '';
        }
    }

    // Verificar validez del token
    async function checkTokenValidity() {
        const token = localStorage.getItem('token');
        return !!token; // Devuelve true si hay un token, false si no
    }

    // Verificar si el usuario está autenticado
    async function isAuthenticated() {
        const token = localStorage.getItem('token');
        if (!token) {
            console.log('No se encontró token en localStorage');
            return false;
        }

        const isValid = await checkTokenValidity();
        if (!isValid) {
            console.log('Token no válido, se eliminará');
            localStorage.removeItem('token');
            return false;
        }
        console.log('Usuario autenticado correctamente');
        return true;
    }

    // Cargar los navbars dinámicamente
    const navContainer = document.getElementById('nav-container');
    if (!navContainer) {
        console.error('No se encontró el contenedor nav-container');
        return;
    }

    try {
        // Cargar ambos navbars
        const publicNavHtml = await loadComponent('../components/nav-public.html');
        const userNavHtml = await loadComponent('../components/nav.html');

        // Insertar los navbars en el contenedor
        navContainer.innerHTML = publicNavHtml + userNavHtml;

        // Seleccionar los navbars después de insertarlos
        const publicNav = document.getElementById('public-nav');
        const userNav = document.getElementById('user-nav');

        // Verificar que los elementos existan
        if (!publicNav || !userNav) {
            console.error('No se encontraron los navbars después de cargarlos');
            return;
        }

        // Mostrar u ocultar navbars según autenticación
        if (await isAuthenticated()) {
            userNav.classList.remove('d-none');
            publicNav.classList.add('d-none');
        } else {
            publicNav.classList.remove('d-none');
            userNav.classList.add('d-none');
        }

        // Manejar cerrar sesión
        const logoutButton = document.getElementById('logout');
        if (logoutButton) {
            logoutButton.addEventListener('click', () => {
                localStorage.removeItem('token');
                window.location.href = '../pages/login.html';
            });
        }

        // Redirigir si el usuario ya está autenticado en login/register
        if (await isAuthenticated() && (window.location.pathname.includes('login.html') || window.location.pathname.includes('register.html'))) {
            window.location.href = '../pages/dashboard.html';
        }

        // Redirigir a login si el usuario no está autenticado en páginas protegidas
        const protectedPages = ['dashboard.html', 'productos.html', 'carrito.html', 'citas.html', 'perfil.html', 'mis-mascotas.html'];
        if (!(await isAuthenticated()) && protectedPages.some(page => window.location.pathname.includes(page))) {
            window.location.href = '../pages/login.html';
        }
    } catch (error) {
        console.error('Error al configurar los navbars:', error);
    }
});