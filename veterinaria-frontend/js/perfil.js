// ============== SERVICIO PARA COMUNICARSE CON LA API ==============
class PerfilService {
    constructor() {
        this.baseUrl = 'http://localhost:8081/perfil';
        this.token = localStorage.getItem('token');
        if (!this.token) {
            console.error("Token no encontrado. Redirigiendo a login.");
            window.location.href = 'login.html';
        }
    }

    // --- Métodos para interactuar con el backend ---

    async obtenerMiPerfil() {
        try {
            const response = await fetch(`${this.baseUrl}/mi-perfil`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${this.token}`, 'Content-Type': 'application/json' }
            });
            if (response.ok) return await response.json();
            // Con la nueva lógica del backend, un 404 ya no debería ocurrir para un usuario logueado.
            // Se maneja como un error genérico si aun así sucede.
            throw new Error('No se pudo obtener el perfil del usuario.');
        } catch (error) {
            console.error('Error en obtenerMiPerfil:', error);
            throw error;
        }
    }

    async actualizarPerfil(datosActualizacion) {
        try {
            const response = await fetch(`${this.baseUrl}/actualizar`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${this.token}`, 'Content-Type': 'application/json' },
                body: JSON.stringify(datosActualizacion)
            });
            const data = await response.json();
            if (response.ok) return data;
            throw new Error(data.error || 'Error al actualizar el perfil.');
        } catch (error) {
            console.error('Error en actualizarPerfil:', error);
            throw error;
        }
    }

    async agregarContactoEmergencia(contacto) {
        try {
            const response = await fetch(`${this.baseUrl}/contactos-emergencia`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${this.token}`, 'Content-Type': 'application/json' },
                body: JSON.stringify(contacto)
            });
            const data = await response.json();
            if (response.ok) return data;
            throw new Error(data.error || 'Error al agregar el contacto.');
        } catch (error) {
            console.error('Error en agregarContactoEmergencia:', error);
            throw error;
        }
    }
}


// ============== CLASE PARA MANEJAR LA INTERFAZ DE USUARIO ==============
class PerfilUI {
    constructor() {
        this.perfilService = new PerfilService();
        this.perfil = null;

        this.contactoModal = new bootstrap.Modal(document.getElementById('contactoModal'));
        this.editarPerfilModal = new bootstrap.Modal(document.getElementById('editarPerfilModal'));
        this.toastElement = document.getElementById('notification-toast');
        this.toast = new bootstrap.Toast(this.toastElement);

        this.init();
    }

    async init() {
        this.mostrarLoading(true);
        this.configurarEventos();
        await this.cargarYRenderizarTodo();
        this.mostrarLoading(false);
    }

    configurarEventos() {
        document.getElementById('contacto-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.guardarContactoEmergencia();
        });

        document.getElementById('editar-perfil-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.guardarEdicionPerfil();
        });
    }

    // --- Lógica de Carga y Renderizado ---

    async cargarYRenderizarTodo() {
        try {
            this.perfil = await this.perfilService.obtenerMiPerfil();

            if (this.perfil) {
                this.renderizarPerfil();
                this.renderizarContactos();
                this.renderizarEstadisticas();
            } else {
                this.mostrarToast("No se pudo cargar tu perfil. Por favor, contacta a soporte.", "error");
            }
        } catch (error) {
            this.mostrarToast('Error crítico al cargar los datos del perfil.', 'error');
        }
    }

    renderizarPerfil() {
        const container = document.getElementById('perfil-container');
        if (!container || !this.perfil) return;

        container.innerHTML = `
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h3>Mi Perfil</h3>
                    <button class="btn btn-outline-primary" onclick="ui.mostrarModalEdicion()">
                        <i class="fas fa-edit"></i> Editar
                    </button>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-3 text-center">
                            <img src="${this.perfil.fotoPerfilUrl || 'https://via.placeholder.com/150'}" class="rounded-circle mb-3" width="150" height="150" alt="Foto de perfil">
                            <div class="badge ${this.perfil.perfilCompleto ? 'bg-success' : 'bg-warning'}">
                                ${this.perfil.perfilCompleto ? 'Perfil Completo' : 'Perfil Incompleto'}
                            </div>
                        </div>
                        <div class="col-md-9">
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong>Nombre:</strong> ${this.perfil.nombre || ''}</p>
                                    <p><strong>Apellido:</strong> ${this.perfil.apellido || ''}</p>
                                    <p><strong>Email:</strong> ${this.perfil.email || ''}</p>
                                    <p><strong>Teléfono:</strong> ${this.perfil.telefono || 'No especificado'}</p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong>Dirección:</strong> ${this.perfil.direccion || 'No especificada'}</p>
                                    <p><strong>Fecha de Nacimiento:</strong> ${this.perfil.fechaNacimiento || 'No especificada'}</p>
                                    <p><strong>Género:</strong> ${this.perfil.genero || 'No especificado'}</p>
                                    <p><strong>Ocupación:</strong> ${this.perfil.ocupacion || 'No especificada'}</p>
                                </div>
                            </div>
                            ${this.perfil.biografia ? `<div class="mt-3"><strong>Biografía:</strong><p>${this.perfil.biografia}</p></div>` : ''}
                        </div>
                    </div>
                </div>
            </div>`;
    }

    renderizarContactos() {
        const container = document.getElementById('contactos-emergencia-container');
        const contactos = this.perfil.contactosEmergencia || [];
        if (!container) return;

        let html = `
            <div class="card mt-4">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h4>Contactos de Emergencia</h4>
                    <button class="btn btn-outline-success btn-sm" onclick="ui.mostrarModalContacto()">
                        <i class="fas fa-plus"></i> Agregar Contacto
                    </button>
                </div>
                <div class="card-body">`;

        if (contactos.length === 0) {
            html += '<p class="text-muted">No hay contactos de emergencia registrados.</p>';
        } else {
            html += '<div class="row">';
            contactos.forEach(contacto => {
                html += `
                    <div class="col-md-6 mb-3">
                        <div class="card ${contacto.esContactoPrincipal ? 'border-primary' : ''}">
                            <div class="card-body">
                                <h6 class="card-title">${contacto.nombre} ${contacto.esContactoPrincipal ? '<span class="badge bg-primary ms-2">Principal</span>' : ''}</h6>
                                <p class="card-text mb-0"><strong>Relación:</strong> ${contacto.relacion}</p>
                                <p class="card-text mb-0"><strong>Teléfono:</strong> ${contacto.telefono}</p>
                                ${contacto.email ? `<p class="card-text mb-0"><strong>Email:</strong> ${contacto.email}</p>` : ''}
                            </div>
                        </div>
                    </div>`;
            });
            html += '</div>';
        }
        html += '</div></div>';
        container.innerHTML = html;
    }

    renderizarEstadisticas() {
        if(!this.perfil) return;
        document.getElementById('perfil-status').textContent = this.perfil.perfilCompleto ? 'Completo' : 'Incompleto';
        document.getElementById('contactos-count').textContent = this.perfil.contactosEmergencia?.length || 0;
        document.getElementById('ultima-actualizacion').textContent = new Date(this.perfil.fechaUltimaActualizacion).toLocaleDateString();
    }

    // --- Lógica de Modales y Formularios ---

    mostrarModalContacto() {
        document.getElementById('contacto-form').reset();
        this.contactoModal.show();
    }

    mostrarModalEdicion() {
        if (!this.perfil) return;
        document.getElementById('edit-nombre').value = this.perfil.nombre || '';
        document.getElementById('edit-apellido').value = this.perfil.apellido || '';
        document.getElementById('edit-email').value = this.perfil.email || '';
        document.getElementById('edit-telefono').value = this.perfil.telefono || '';
        document.getElementById('edit-direccion').value = this.perfil.direccion || '';
        document.getElementById('edit-fechaNacimiento').value = this.perfil.fechaNacimiento || '';
        document.getElementById('edit-genero').value = this.perfil.genero || '';
        document.getElementById('edit-ocupacion').value = this.perfil.ocupacion || '';
        document.getElementById('edit-biografia').value = this.perfil.biografia || '';
        this.editarPerfilModal.show();
    }

    async guardarContactoEmergencia() {
        this.mostrarLoading(true);
        try {
            const contacto = {
                nombre: document.getElementById('contacto-nombre').value,
                telefono: document.getElementById('contacto-telefono').value,
                relacion: document.getElementById('contacto-relacion').value,
                email: document.getElementById('contacto-email').value,
                esContactoPrincipal: document.getElementById('contacto-principal').checked
            };
            await this.perfilService.agregarContactoEmergencia(contacto);
            this.contactoModal.hide();
            await this.cargarYRenderizarTodo();
            this.mostrarToast('Contacto agregado exitosamente.', 'success');
        } catch (error) {
            this.mostrarToast(error.message, 'error');
        } finally {
            this.mostrarLoading(false);
        }
    }

    async guardarEdicionPerfil() {
        this.mostrarLoading(true);
        try {
            const datosActualizacion = {
                nombre: document.getElementById('edit-nombre').value,
                apellido: document.getElementById('edit-apellido').value,
                email: document.getElementById('edit-email').value,
                telefono: document.getElementById('edit-telefono').value,
                direccion: document.getElementById('edit-direccion').value,
                fechaNacimiento: document.getElementById('edit-fechaNacimiento').value,
                genero: document.getElementById('edit-genero').value,
                ocupacion: document.getElementById('edit-ocupacion').value,
                biografia: document.getElementById('edit-biografia').value
            };
            Object.keys(datosActualizacion).forEach(key => {
                if (!datosActualizacion[key]) delete datosActualizacion[key];
            });

            await this.perfilService.actualizarPerfil(datosActualizacion);
            this.editarPerfilModal.hide();
            await this.cargarYRenderizarTodo();
            this.mostrarToast('Perfil actualizado con éxito.', 'success');
        } catch (error) {
            this.mostrarToast(error.message, 'error');
        } finally {
            this.mostrarLoading(false);
        }
    }

    // --- Funciones de Utilidad ---

    mostrarLoading(show) {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) overlay.style.display = show ? 'flex' : 'none';
    }

    mostrarToast(mensaje, tipo = 'info') {
        const toastMessage = document.getElementById('toast-message');
        const toastHeaderIcon = this.toastElement.querySelector('.toast-header i');

        toastMessage.textContent = mensaje;
        toastHeaderIcon.className = `fas me-2 ${
            tipo === 'success' ? 'fa-check-circle text-success' :
                tipo === 'error'   ? 'fa-exclamation-circle text-danger' :
                    'fa-info-circle text-primary'
        }`;
        this.toast.show();
    }
}

// Inicializar la UI cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    // Asegurarse de que el script solo se ejecuta en la página de perfil
    if (document.getElementById('perfil-container')) {
        window.ui = new PerfilUI();
    }
});