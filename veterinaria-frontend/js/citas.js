document.getElementById('citas-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fecha = document.getElementById('fecha').value;
    const hora = document.getElementById('hora').value;
    const mascota = document.getElementById('mascota').value;
    const errorMessage = document.getElementById('error-message');

    try {
        const response = await fetch('/api/citas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem('token')}`,
            },
            body: JSON.stringify({ fecha, hora, mascota }),
        });

        if (response.ok) {
            alert('Cita agendada');
            window.location.reload();
        } else {
            errorMessage.textContent = 'Error al agendar cita';
        }
    } catch (error) {
        errorMessage.textContent = 'Error de conexión';
    }
});

document.getElementById('logout').addEventListener('click', () => {
    localStorage.removeItem('token');
    window.location.href = '../index.html';
});