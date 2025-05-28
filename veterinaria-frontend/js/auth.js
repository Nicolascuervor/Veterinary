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

        // Verificar si la respuesta es texto plano (token) o JSON
        const contentType = response.headers.get('Content-Type');
        let data;
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text(); // Tratar la respuesta como texto (token)
        }

        if (response.ok) {
            // Extraer el token dependiendo del formato de la respuesta
            let token;
            if (contentType && contentType.includes('application/json')) {
                token = data.token; // Extraer el token del objeto JSON
            } else {
                token = data; // Usar el texto directamente
            }
            localStorage.setItem('token', token); // Guardar el token en localStorage
            alert('Login exitoso.');
            // Agregar un pequeño retraso para asegurar que el token se guarde
            setTimeout(() => {
                window.location.href = 'dashboard.html';
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
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const role = "USER";
    const confirmPassword = document.getElementById('confirm-password').value;
    const errorMessage = document.getElementById('error-message');

    // Validar que las contraseñas coincidan
    if (password !== confirmPassword) {
        errorMessage.textContent = 'Las contraseñas no coinciden';
        return;
    }
    
    try {
        const response = await fetch('http://localhost:8081/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, role }),
        });

        // Verificar si la respuesta es texto plano (token) o JSON
        const contentType = response.headers.get('Content-Type');
        let data;
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text(); // Tratar la respuesta como texto (token)
        }

        if (response.ok) {
            alert('Registro exitoso. Por favor, inicia sesión.');
            window.location.href = 'login.html'; // Redirigir al login
        } else {
            errorMessage.textContent = typeof data === 'string' ? data : data.message || 'Error al registrarse';
        }
    } catch (error) {
        errorMessage.textContent = 'Error de conexión con el servidor';
        console.error('Error en registro:', error);
    }
});