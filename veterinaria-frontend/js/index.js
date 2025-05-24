// Ejemplo: Enviar formulario de contacto (si lo agregas en index.html)
document.getElementById('contact-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('contact-email').value;
    const mensaje = document.getElementById('contact-mensaje').value;

    try {
        const response = await fetch('/api/contacto', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, mensaje }),
        });
        if (response.ok) {
            alert('Mensaje enviado');
        } else {
            alert('Error al enviar el mensaje');
        }
    } catch (error) {
        console.error('Error:', error);
    }
});