// js/auth.js

// Manejar el formulario de login
document.getElementById('login-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const errorMessage = document.getElementById('error-message');
    const submitButton = e.target.querySelector('button[type="submit"]');
    const originalButtonText = submitButton.innerHTML;

    // Iniciar estado de carga
    submitButton.disabled = true;
    submitButton.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Iniciando...`;

    try {
        // --- PASO 1: Autenticar y obtener el token ---
        const loginResponse = await fetch('http://localhost:8081/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
        });

        if (!loginResponse.ok) {
            const errorData = await loginResponse.json();
            throw new Error(errorData.message || 'Credenciales incorrectas');
        }

        const authData = await loginResponse.json();

        // Guardar los datos básicos de autenticación
        localStorage.setItem('token', authData.token);
        localStorage.setItem('nombre', authData.nombre || 'Usuario');
        localStorage.setItem('userRole', authData.role || 'USER');
        localStorage.setItem('id', authData.id);

        // --- PASO 2 (NUEVO): Obtener los datos del perfil para la foto ---
        console.log('Login exitoso. Obteniendo datos del perfil...');
        const perfilResponse = await fetch('http://localhost:8081/perfil/mi-perfil', {
            headers: { 'Authorization': `Bearer ${authData.token}` }
        });

        if (perfilResponse.ok) {
            const perfilData = await perfilResponse.json();
            // Guardamos la URL de la foto en localStorage
            localStorage.setItem('fotoPerfilUrl', perfilData.fotoPerfilUrl || '');
            console.log('URL de foto de perfil guardada en localStorage.');
        } else {
            // Si falla, no es crítico. Dejamos el localStorage vacío para la foto.
            console.warn('No se pudieron obtener los datos del perfil, se usará el avatar por defecto.');
            localStorage.setItem('fotoPerfilUrl', '');
        }

        // --- PASO 3: Redirigir al usuario ---
        window.location.href = 'index.html';

    } catch (error) {
        errorMessage.textContent = error.message;
        console.error('Error en login:', error);
    } finally {
        // Restaurar el botón solo si hubo un error (si no, la página redirige)
        submitButton.disabled = false;
        submitButton.innerHTML = originalButtonText;
    }
});
// Manejar el formulario de registro
document.getElementById('register-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    // <<== LÓGICA DE CARGA (1/3): Obtener el botón y guardar su texto original
    const submitButton = document.getElementById('register-button');
    const originalButtonText = submitButton.innerHTML;

    // <<== LÓGICA DE CARGA (2/3): Desactivar botón y mostrar spinner
    submitButton.disabled = true;
    submitButton.innerHTML = `
        <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        Registrando...
    `;

    const nombre = document.getElementById('nombre').value;
    const apellido = document.getElementById('apellido').value;
    const telefono = document.getElementById('telefono').value;
    const direccion = document.getElementById('direccion').value;
    const cedula = document.getElementById('cedula').value;
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    const role = "USER";

    const errorMessage = document.getElementById('error-message');

    if (password !== confirmPassword) {
        errorMessage.textContent = 'Las contraseñas no coinciden';
        // <<== LÓGICA DE CARGA (3/3) en caso de error temprano
        submitButton.disabled = false;
        submitButton.innerHTML = originalButtonText;
        return;
    }

    try {
        const response = await fetch('http://localhost:8081/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({nombre,apellido,telefono,direccion,cedula, username, password, role }),
        });

        const contentType = response.headers.get('Content-Type');
        let data;
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        if (response.ok) {
            alert('Registro exitoso. Por favor, inicia sesión.');
            window.location.href = 'login.html';
        } else {
            errorMessage.textContent = typeof data === 'string' ? data : data.message || 'Error al registrarse';
        }
    } catch (error) {
        errorMessage.textContent = 'Error de conexión con el servidor';
        console.error('Error en registro:', error);
    } finally {
        // <<== LÓGICA DE CARGA (3/3): Restaurar el botón en cualquier caso
        submitButton.disabled = false;
        submitButton.innerHTML = originalButtonText;
    }
});