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

        // Verificar si la respuesta es texto plano (token) en lugar de JSON
        const contentType = response.headers.get('Content-Type');
        let data;
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text(); // Tratar la respuesta como texto (token)
        }

        if (response.ok) {
            alert('Login exitoso.');
            window.location.href = 'dashboard.html'; // Redirigir al dashboard
        } else {
            // Si la respuesta es texto, mostrarla directamente; si es JSON, usar data.message
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

        // Verificar si la respuesta es texto plano (token) en lugar de JSON
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
            // Si la respuesta es texto, mostrarla directamente; si es JSON, usar data.message
            errorMessage.textContent = typeof data === 'string' ? data : data.message || 'Error al registrarse';
        }
    } catch (error) {
        errorMessage.textContent = 'Error de conexión con el servidor';
        console.error('Error en registro:', error);
    }
});