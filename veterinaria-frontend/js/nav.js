// veterinaria-frontend/js/nav.js (Versión Unificada y Funcional)

document.addEventListener('DOMContentLoaded', async () => {

    // --- 1. Función de Autenticación Robusta (con chequeo de expiración) ---
    async function checkTokenValidity() {
        const token = localStorage.getItem('token');
        if (!token) return false;

        try {
            // Decodifica el payload del token para leer la fecha de expiración
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Math.floor(Date.now() / 1000);

            if (payload.exp && payload.exp < currentTime) {
                console.log('Token expirado. Limpiando sesión.');
                // Limpia todo el localStorage relacionado con la sesión
                localStorage.removeItem('token');
                localStorage.removeItem('nombre');
                localStorage.removeItem('userRole');
                localStorage.removeItem('id');
                return false;
            }
            return true;
        } catch (error) {
            console.error('Error al decodificar el token. Limpiando sesión.', error);
            localStorage.clear(); // Limpia todo si el token es inválido
            return false;
        }
    }

    // --- 2. Lógica de Protección de Páginas ---
    const isAuth = await checkTokenValidity();
    const currentPage = window.location.pathname.split('/').pop();
    const protectedPages = ['admin.html', 'index.html', 'citas.html', 'perfil.html', 'misMascotas.html'];

    if (!isAuth && protectedPages.includes(currentPage)) {
        // Si no está autenticado y está en una página protegida, lo enviamos al login.
        // Usamos una ruta absoluta para que funcione desde cualquier página.
        window.location.href = '/Veterinary/veterinaria-frontend/pages/login.html';
        return;
    }

    if (isAuth && (currentPage === 'login.html' || currentPage === 'register.html')) {
        // Si ya está autenticado y va a login/register, lo enviamos a su dashboard.
        window.location.href = '/Veterinary/veterinaria-frontend/pages/index.html';
        return;
    }

    // --- 3. Carga y Configuración del Navbar ---
    const navbarContainer = document.getElementById('nav-container');
    if (!navbarContainer) return; // Si la página no tiene un contenedor para el nav, no hacemos nada.

    // Usamos rutas absolutas desde la raíz del servidor para que siempre funcionen.
    const basePath = '/Veterinary/veterinaria-frontend/components/';
    const navPath = isAuth ? `${basePath}nav.html` : `${basePath}nav-public.html`;

    try {
        const response = await fetch(navPath);
        if (!response.ok) throw new Error(`No se encontró el navbar en ${navPath}`);
        const html = await response.text();

        navbarContainer.innerHTML = html;

        // Si el usuario está autenticado, configuramos los elementos dinámicos.
        if (isAuth) {
            const nombreUsuario = localStorage.getItem('nombre');
            const userRole = localStorage.getItem('userRole');

            // Actualizar nombre y rol en el menú
            const userNameElement = document.getElementById('user-name');
            if (userNameElement) userNameElement.textContent = nombreUsuario || 'Usuario';

            const userRoleElement = document.getElementById('user-role');
            if (userRoleElement) {
                userRoleElement.textContent = userRole === 'USER' ? 'Propietario' : userRole;
            }

            // ▼▼▼ LÍNEA CORREGIDA Y AÑADIDA ▼▼▼
            // Primero, obtenemos el elemento del avatar del DOM
            const userAvatarElement = document.getElementById('user-avatar');

            const fotoUrl = localStorage.getItem('fotoPerfilUrl');

            console.log(`[NAV.JS LOG] fotoUrl de localStorage: `, fotoUrl);


            if (userAvatarElement) {
                if (fotoUrl && fotoUrl.trim() !== '') {
                    // CORRECCIÓN: Apuntamos al backend que sirve las imágenes (puerto 8085)
                    const fullAvatarUrl = `http://localhost:8085${fotoUrl}`;
                    console.log(`[NAV.JS LOG] Construyendo URL de avatar: `, fullAvatarUrl);
                    userAvatarElement.src = fullAvatarUrl;
                } else {
                    // CORRECCIÓN: Se añade https:// al avatar genérico
                    userAvatarElement.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(nombreUsuario)}&background=667eea&color=fff&size=35`;
                }
            }


            // Mostrar el enlace al "Panel Admin" si el rol es ADMIN
            const adminLinkPlaceholder = document.getElementById('admin-link-placeholder');
            if (adminLinkPlaceholder) {
                if (userRole === 'ADMIN') {
                    const adminListItem = document.createElement('li');
                    adminListItem.className = 'nav-item';
                    adminListItem.innerHTML = `<a class="nav-link" href="/Veterinary/veterinaria-frontend/pages/admin.html"><i class="fas fa-cogs me-1"></i>Panel Admin</a>`;
                    adminLinkPlaceholder.replaceWith(adminListItem);
                } else {
                    adminLinkPlaceholder.remove();
                }
            }

            // Configurar el botón de cerrar sesión
            const logoutButton = document.getElementById('logout');
            if (logoutButton) {
                logoutButton.addEventListener('click', () => {
                    localStorage.clear(); // Limpia toda la sesión
                    window.location.href = '/Veterinary/veterinaria-frontend/pages/login.html';
                });
            }

            // Ejecutar scripts que puedan venir dentro del navbar cargado (ej. para el dropdown)
            const navScripts = navbarContainer.querySelectorAll('script');
            navScripts.forEach(script => {
                const newScript = document.createElement('script');
                newScript.textContent = script.textContent;
                document.body.appendChild(newScript).remove(); // Añade, ejecuta y remueve
            });
        }

    } catch (error) {
        console.error('Error fatal al cargar la barra de navegación:', error);
        if (navbarContainer) navbarContainer.innerHTML = `<div class="alert alert-danger">Error al cargar el menú.</div>`;
    }
});