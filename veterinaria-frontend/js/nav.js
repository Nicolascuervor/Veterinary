// js/nav.js
document.addEventListener('DOMContentLoaded', async () => {
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

    async function checkTokenValidity() {
        const token = localStorage.getItem('token');
        if (!token) return false;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Math.floor(Date.now() / 1000);
            if (payload.exp && payload.exp < currentTime) {
                console.log('Token expirado');
                localStorage.removeItem('token');
                localStorage.removeItem('nombre');
                localStorage.removeItem('userRole');
                return false;
            }
            return true;
        } catch (error) {
            console.error('Error al decodificar el token:', error);
            localStorage.removeItem('token');
            localStorage.removeItem('nombre');
            localStorage.removeItem('userRole');
            return false;
        }
    }

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
            localStorage.removeItem('nombre');
            localStorage.removeItem('userRole');
            return false;
        }
        console.log('Usuario autenticado correctamente');
        return true;
    }

    const navContainer = document.getElementById('nav-container');
    if (!navContainer) {
        console.error('No se encontró el contenedor nav-container');
        return;
    }

    try {
        const publicNavHtml = await loadComponent('/Veterinary/product_service/components/nav-public.html' );
        let userNavHtml = await loadComponent('/Veterinary/product_service/components/nav.html');





        console.log('nav.html cargado correctamente'); // Mensaje simplificado





        // Crear un contenedor temporal para parsear el HTML
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = publicNavHtml + userNavHtml;

        // Insertar el HTML en el DOM antes de ejecutar los scripts
        navContainer.appendChild(tempDiv);

        // Ahora que el HTML está en el DOM, extraer y ejecutar los scripts
        const scripts = tempDiv.querySelectorAll('script');
        scripts.forEach(script => {
            const scriptContent = script.textContent;
            const newScript = document.createElement('script');
            newScript.textContent = scriptContent;
            document.head.appendChild(newScript);
        });

        // Eliminar los scripts del HTML original (ya los ejecutamos)
        scripts.forEach(script => script.remove());

        const publicNav = document.getElementById('public-nav');
        const userNav = document.getElementById('user-nav');
        console.log('Elementos del DOM:', { publicNav, userNav });

        if (!publicNav || !userNav) {
            console.error('No se encontraron los navbars después de cargarlos');
            return;
        }

        const isAuth = await isAuthenticated();
        if (isAuth) {
            userNav.classList.remove('d-none');
            publicNav.classList.add('d-none');
        } else {
            publicNav.classList.remove('d-none');
            userNav.classList.add('d-none');
        }

        const logoutButton = document.getElementById('logout');
        if (logoutButton) {
            logoutButton.addEventListener('click', () => {
                localStorage.removeItem('token');
                localStorage.removeItem('nombre');
                localStorage.removeItem('userRole');
                window.location.href = '/Veterinary/veterinaria-frontend/pages/login.html';
            });
        }

        if (isAuth && (window.location.pathname.includes('login.html') || window.location.pathname.includes('register.html'))) {
            window.location.href = '/Veterinary/veterinaria-frontend/pages/dashboard.html';
        }

        const protectedPages = ['dashboard.html', 'productos.html', 'carrito.html', 'citas.html', 'perfil.html', 'mis-mascotas.html'];
        if (!isAuth && protectedPages.some(page => window.location.pathname.includes(page))) {
            window.location.href = '/Veterinary/veterinaria-frontend/pages/login.html';
        }
    } catch (error) {
        console.error('Error al configurar los navbars:', error);
    }
});