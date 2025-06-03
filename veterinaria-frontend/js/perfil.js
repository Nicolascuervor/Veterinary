class PerfilService {
    constructor() {
        this.baseUrl = 'http://localhost:8081/perfil';
        this.token = localStorage.getItem('token');
    }

    async obtenerMiPerfil() {
        try {
            const response = await fetch(`${this.baseUrl}/mi-perfil`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                return await response.json();
            } else if (response.status === 404) {
                return null; // No existe perfil
            } else {
                throw new Error('Error al obtener el perfil');
            }
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

    async crearPerfil(datosUsuario) {
        try {
            const response = await fetch(`${this.baseUrl}/crear`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(datosUsuario)
            });

            const data = await response.json();

            if (response.ok) {
                return data;
            } else {
                throw new Error(data.error || 'Error al crear el perfil');
            }
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

    async actualizarPerfil(datosActualizacion) {
        try {
            const response = await fetch(`${this.baseUrl}/actualizar`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(datosActualizacion)
            });

            const data = await response.json();

            if (response.ok) {
                return data;
            } else {
                throw new Error(data.error || 'Error al actualizar el perfil');
            }
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

    async obtenerContactosEmergencia() {
        try {
            const response = await fetch(`${this.baseUrl}/contactos-emergencia`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                return data.contactos || [];
            } else {
                throw new Error('Error al obtener contactos de emergencia');
            }
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

    async agregarContactoEmergencia(contacto) {
        try {
            const response = await fetch(`${this.baseUrl}/contactos-emergencia`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(contacto)
            });

            const data = await response.json();

            if (response.ok) {
                return data;
            } else {
                throw new Error(data.error || 'Error al agregar contacto');
            }
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

    async obtenerPreferenciasPrivacidad() {
        try {
            const response = await fetch(`${this.baseUrl}/preferencias`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                return data.preferencias || [];
            } else {
                throw new Error('Error al obtener preferencias');
            }
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }

    async actualizarPreferencia(tipoPreferencia, valor) {
        try {
            const response = await fetch(`${this.baseUrl}/preferencias/${tipoPreferencia}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${this.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ valor })
            });

            const data = await response.json();

            if (response.ok) {
                return data;
            } else {
                throw new Error(data.error || 'Error al actualizar preferencia');
            }
        } catch (error) {
            console.error('Error:', error);
            throw error;
        }
    }
}

// Clase para manejar la UI del perfil
class PerfilUI {
    constructor() {
        this.perfilService = new PerfilService();
        this.init();
    }

    async init() {
        await this.cargarPerfil();
        this.configurarEventos();
    }

    async cargarPerfil() {
        try {
            const perfil = await this.perfilService.obtenerMiPerfil();

            if (perfil) {
                this.mostrarPerfil(perfil);
                await this.cargarContactosEmergencia();
                await this.cargarPreferenciasPrivacidad();
            } else {
                this.mostrarFormularioCrearPerfil();
            }
        } catch (error) {
            this.mostrarError('Error al cargar el perfil');
        }
    }

    mostrarPerfil(perfil) {
        const container = document.getElementById('perfil-container');
        if (!container) return;

        container.innerHTML = `
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h3>Mi Perfil</h3>
                    <button class="btn btn-outline-primary" onclick="perfilUI.editarPerfil()">
                        <i class="fas fa-edit"></i> Editar
                    </button>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-3 text-center">
                            <img src="${perfil.fotoPerfilUrl || 'https://via.placeholder.com/150'}" 
                                 class="rounded-circle mb-3" width="150" height="150" alt="Foto de perfil">
                            <div class="badge ${perfil.perfilCompleto ? 'bg-success' : 'bg-warning'}">
                                ${perfil.perfilCompleto ? 'Perfil Completo' : 'Perfil Incompleto'}
                            </div>
                        </div>
                        <div class="col-md-9">
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong>Nombre:</strong> ${perfil.nombre}</p>
                                    <p><strong>Apellido:</strong> ${perfil.apellido}</p>
                                    <p><strong>Email:</strong> ${perfil.email}</p>
                                    <p><strong>Teléfono:</strong> ${perfil.telefono || 'No especificado'}</p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong>Dirección:</strong> ${perfil.direccion || 'No especificada'}</p>
                                    <p><strong>Fecha de Nacimiento:</strong> ${perfil.fechaNacimiento || 'No especificada'}</p>
                                    <p><strong>Género:</strong> ${perfil.genero || 'No especificado'}</p>
                                    <p><strong>Ocupación:</strong> ${perfil.ocupacion || 'No especificada'}</p>
                                </div>
                            </div>
                            ${perfil.biografia ? `<div class="mt-3"><strong>Biografía:</strong><p>${perfil.biografia}</p></div>` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    mostrarFormularioCrearPerfil() {
        const container = document.getElementById('perfil-container');
        if (!container) return;

        container.innerHTML = `
            <div class="card">
                <div class="card-header">
                    <h3>Crear Mi Perfil</h3>
                </div>
                <div class="card-body">
                    <form id="crear-perfil-form">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="nombre" class="form-label">Nombre *</label>
                                    <input type="text" class="form-control" id="nombre" required>
                                </div>
                                <div class="mb-3">
                                    <label for="apellido" class="form-label">Apellido *</label>
                                    <input type="text" class="form-control" id="apellido" required>
                                </div>
                                <div class="mb-3">
                                    <label for="email" class="form-label">Email *</label>
                                    <input type="email" class="form-control" id="email" required>
                                </div>
                                <div class="mb-3">
                                    <label for="telefono" class="form-label">Teléfono</label>
                                    <input type="tel" class="form-control" id="telefono" pattern="[0-9]{10}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="direccion" class="form-label">Dirección</label>
                                    <input type="text" class="form-control" id="direccion">
                                </div>
                                <div class="mb-3">
                                    <label for="fechaNacimiento" class="form-label">Fecha de Nacimiento</label>
                                    <input type="date" class="form-control" id="fechaNacimiento">
                                </div>
                                <div class="mb-3">
                                    <label for="genero" class="form-label">Género</label>
                                    <select class="form-control" id="genero">
                                        <option value="">Seleccionar...</option>
                                        <option value="MASCULINO">Masculino</option>
                                        <option value="FEMENINO">Femenino</option>
                                        <option value="OTRO">Otro</option>
                                        <option value="PREFIERO_NO_DECIR">Prefiero no decir</option>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label for="ocupacion" class="form-label">Ocupación</label>
                                    <input type="text" class="form-control" id="ocupacion">
                                </div>
                            </div>
                        </div>
                        <div class="text-end">
                            <button type="submit" class="btn btn-primary">Crear Perfil</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
    }

    async cargarContactosEmergencia() {
        try {
            const contactos = await this.perfilService.obtenerContactosEmergencia();
            this.mostrarContactosEmergencia(contactos);
        } catch (error) {
            console.error('Error al cargar contactos:', error);
        }
    }

    mostrarContactosEmergencia(contactos) {
        const container = document.getElementById('contactos-emergencia-container');
        if (!container) return;

        let html = `
            <div class="card mt-4">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h4>Contactos de Emergencia</h4>
                    <button class="btn btn-outline-success btn-sm" onclick="perfilUI.mostrarFormularioContacto()">
                        <i class="fas fa-plus"></i> Agregar Contacto
                    </button>
                </div>
                <div class="card-body">
        `;

        if (contactos.length === 0) {
            html += '<p class="text-muted">No hay contactos de emergencia registrados.</p>';
        } else {
            html += '<div class="row">';
            contactos.forEach(contacto => {
                html += `
                    <div class="col-md-6 mb-3">
                        <div class="card ${contacto.esContactoPrincipal ? 'border-primary' : ''}">
                            <div class="card-body">
                                <h6 class="card-title">
                                    ${contacto.nombre}
                                    ${contacto.esContactoPrincipal ? '<span class="badge bg-primary ms-2">Principal</span>' : ''}
                                </h6>
                                <p class="card-text">
                                    <strong>Relación:</strong> ${contacto.relacion}<br>
                                    <strong>Teléfono:</strong> ${contacto.telefono}<br>
                                    ${contacto.email ? `<strong>Email:</strong> ${contacto.email}` : ''}
                                </p>
                            </div>
                        </div>
                    </div>
                `;
            });
            html += '</div>';
        }

        html += '</div></div>';
        container.innerHTML = html;
    }

    configurarEventos() {
        // Configurar evento para crear perfil
        document.addEventListener('submit', async (e) => {
            if (e.target.id === 'crear-perfil-form') {
                e.preventDefault();
                await this.crearPerfil(e.target);
            }
        });
    }

    async crearPerfil(form) {
        try {
            const formData = new FormData(form);
            const userId = this.obtenerUserIdDesdeToken();

            const datosUsuario = {
                userId: userId,
                nombre: formData.get('nombre'),
                apellido: formData.get('apellido'),
                email: formData.get('email'),
                telefono: formData.get('telefono'),
                direccion: formData.get('direccion'),
                fechaNacimiento: formData.get('fechaNacimiento'),
                genero: formData.get('genero'),
                ocupacion: formData.get('ocupacion')
            };

            const resultado = await this.perfilService.crearPerfil(datosUsuario);
            this.mostrarExito('Perfil creado exitosamente');
            await this.cargarPerfil();
        } catch (error) {
            this.mostrarError(error.message);
        }
    }

    obtenerUserIdDesdeToken() {
        try {
            const token = localStorage.getItem('token');
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.id;
        } catch (error) {
            console.error('Error al decodificar token:', error);
            return null;
        }
    }

    mostrarExito(mensaje) {
        // Implementar toast o alert de éxito
        alert(mensaje);
    }

    mostrarError(mensaje) {
        // Implementar toast o alert de error
        alert('Error: ' + mensaje);
    }
}

// Inicializar cuando se carga la página
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('perfil-container')) {
        window.perfilUI = new PerfilUI();
    }
});