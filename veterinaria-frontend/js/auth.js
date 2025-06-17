// js/auth.js

// Manejar el formulario de login
document.getElementById('login-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const errorMessage = document.getElementById('error-message');
    // <<== LÓGICA DE CARGA (1/3): Obtener el botón y guardar su texto original
    const submitButton = e.target.querySelector('button[type="submit"]');
    const originalButtonText = submitButton.innerHTML;

    // <<== LÓGICA DE CARGA (2/3): Desactivar botón y mostrar spinner
    submitButton.disabled = true;
    submitButton.innerHTML = `
        <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        Iniciando...
    `;

    try {
        const response = await fetch('http://localhost:8081/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
        });

        // ... (resto del código de manejo de la respuesta sin cambios) ...
        const contentType = response.headers.get('Content-Type');
        let data;
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        if (response.ok) {
            let token, nombre, userRole, id;
            if (contentType && contentType.includes('application/json')) {
                token = data.token;
                nombre = data.nombre || 'Usuario';
                userRole = data.role || 'USER';
                id = data.id || null;
            } else {
                token = data;
                nombre = 'Usuario';
                userRole = 'USER';
                id = null;
            }
            localStorage.setItem('token', token);
            localStorage.setItem('nombre', nombre);
            localStorage.setItem('userRole', userRole);
            localStorage.setItem('id', id);

            // La redirección ocurrirá antes de que el botón se restaure, lo cual está bien.
            window.location.href = 'index.html';
        } else {
            errorMessage.textContent = typeof data === 'string' ? data : data.message || 'Error al iniciar sesión';
        }
    } catch (error) {
        errorMessage.textContent = 'Error de conexión con el servidor';
        console.error('Error en login:', error);
    } finally {
        // <<== LÓGICA DE CARGA (3/3): Restaurar el botón en cualquier caso (éxito o error)
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