// js/auth.js
// Manejar el formulario de login
document.getElementById('login-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    const errorMessage = document.getElementById('error-message');

    try {
        const response = await fetch('http://localhost:8081/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
        });

        const contentType = response.headers.get('Content-Type');
        let data;
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        if (response.ok) {
            let token, nombre, userRole;
            if (contentType && contentType.includes('application/json')) {
                token = data.token;
                nombre = data.nombre || 'Usuario'; // "Cuervo" en este caso
                userRole = data.role || 'USER'; // "USER" en este caso
            } else {
                token = data;
                nombre = 'Usuario';
                userRole = 'USER';
            }
            localStorage.setItem('token', token); // Usar localStorage
            localStorage.setItem('nombre', nombre);
            localStorage.setItem('userRole', userRole);
            alert('Login exitoso.');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 100);
        } else {
            errorMessage.textContent = typeof data === 'string' ? data : data.message || 'Error al iniciar sesión';
        }
    } catch (error) {
        errorMessage.textContent = 'Error de conexión con el servidor';
        console.error('Error en login:', error);
    }
});

// Manejar el formulario de registro
document.getElementById('register-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const nombre = document.getElementById('nombre').value;
    const apellido = document.getElementById('apellido').value;
    const telefono = document.getElementById('telefono').value;
    const direccion = document.getElementById('direccion').value;
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    const role = "USER";

    const errorMessage = document.getElementById('error-message');

    if (password !== confirmPassword) {
        errorMessage.textContent = 'Las contraseñas no coinciden';
        return;
    }
    
    try {
        const response = await fetch('http://localhost:8081/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({nombre,apellido,telefono,direccion, username, password, role }),
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
    }
});