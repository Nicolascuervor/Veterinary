document.addEventListener('DOMContentLoaded', () => {
    const contactForm = document.getElementById('contact-form');
    const formConfirmation = document.getElementById('form-confirmation');

    if (contactForm) {
        contactForm.addEventListener('submit', (e) => {
            e.preventDefault(); // Previene el envío real del formulario

            // Simulación de envío:
            // En un proyecto real, aquí iría la lógica para enviar los datos a un backend (API).

            // Validar que los campos no estén vacíos (validación simple)
            const name = document.getElementById('contact-name').value;
            const email = document.getElementById('contact-email').value;
            const message = document.getElementById('contact-message').value;

            if (name.trim() === '' || email.trim() === '' || message.trim() === '') {
                alert('Por favor, completa todos los campos requeridos.');
                return;
            }

            // Mostrar mensaje de confirmación
            formConfirmation.classList.remove('d-none');

            // Limpiar el formulario
            contactForm.reset();

            // Ocultar el mensaje después de 5 segundos
            setTimeout(() => {
                formConfirmation.classList.add('d-none');
            }, 5000);
        });
    }
});