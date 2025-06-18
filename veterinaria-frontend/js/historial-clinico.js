document.addEventListener('DOMContentLoaded', () => {
    const mainContent = document.getElementById('main-content');
    const loadingSpinner = document.getElementById('loading-spinner');
    const errorContainer = document.getElementById('error-container');

    // --- CORRECCIÓN 1: Definir la URL base del Gateway ---
    // Esto nos permitirá construir las URLs de las imágenes correctamente.
    const API_GATEWAY_URL = 'http://localhost:8081';

    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const mascotaId = params.get('id');

    if (!mascotaId) {
        showError('No se ha especificado un ID de mascota.');
        return;
    }

    fetchHistorial(mascotaId);

    function showError(message) {
        loadingSpinner.classList.add('d-none');
        errorContainer.textContent = message;
        errorContainer.classList.remove('d-none');
    }

    async function fetchHistorial(id) {
        try {
            // Se usa la URL del Gateway, que ya apunta al backend correcto.
            const response = await fetch(`${API_GATEWAY_URL}/historial/mascota/${id}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error(`Error ${response.status}: No se pudo cargar el historial.`);
            }

            const data = await response.json();
            renderAll(data);

        } catch (error) {
            showError(error.message);
        } finally {
            loadingSpinner.classList.add('d-none');
            mainContent.classList.remove('d-none');
        }
    }

    function renderAll(data) {
        renderPerfil(data.perfilMascota);
        renderCitas(data.citas);
        renderVacunas(data.vacunaciones);
    }

    function renderPerfil(perfil) {
        document.getElementById('mascota-nombre').textContent = perfil.nombre;

        // --- CORRECCIÓN 1 (Aplicación): Construir la URL completa de la imagen ---
        const fotoUrl = perfil.fotoUrl
            ? `${API_GATEWAY_URL}${perfil.fotoUrl}`
            : 'https://via.placeholder.com/150?text=Sin+Foto'; // URL de respaldo
        document.getElementById('mascota-foto').src = fotoUrl;

        // --- CORRECCIÓN 2: Manejar valores nulos para evitar que se muestre "null" ---
        const infoParts = [
            perfil.especie,
            perfil.raza,
            perfil.sexo
        ].filter(part => part != null && part.trim() !== ''); // Filtra los elementos nulos o vacíos

        document.getElementById('mascota-info').textContent = infoParts.join(' - ');
        // --- FIN DE LA CORRECCIÓN 2 ---

        document.getElementById('propietario-info').textContent = `Propietario: ${perfil.propietarioNombre}`;
    }

    function renderCitas(citas) {
        const accordionContainer = document.getElementById('citas-accordion');
        if (!citas || citas.length === 0) {
            accordionContainer.innerHTML = '<p class="text-center text-muted">No hay citas registradas para esta mascota.</p>';
            return;
        }

        accordionContainer.innerHTML = citas.map((cita, index) => {
            const fecha = new Date(cita.fecha).toLocaleDateString('es-CO', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
            const statusBadge = getStatusBadge(cita.estadoCita);

            return `
                <div class="accordion-item">
                    <h2 class="accordion-header" id="heading-${index}">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse-${index}" aria-expanded="false" aria-controls="collapse-${index}">
                            <div class="d-flex justify-content-between w-100 align-items-center pe-3">
                                <span><strong>${fecha}</strong> - ${cita.motivoServicio}</span>
                                ${statusBadge}
                            </div>
                        </button>
                    </h2>
                    <div id="collapse-${index}" class="accordion-collapse collapse" aria-labelledby="heading-${index}" data-bs-parent="#citas-accordion">
                        <div class="accordion-body">
                            ${renderDetalleCita(cita)}
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    }

    function renderDetalleCita(cita) {
        let content = `<p><strong>Veterinario:</strong> ${cita.veterinarioNombre || 'No asignado'}</p>`;

        if (cita.encuentro) {
            const encuentro = cita.encuentro;
            content += `
                <div class="card detalle-encuentro-card mt-3">
                    <div class="card-header"><i class="fas fa-notes-medical me-2"></i>Detalles de la Consulta</div>
                    <div class="card-body">
                        <p><strong>Motivo:</strong> ${encuentro.motivoConsulta || 'N/A'}</p>
                        <p><strong>Anamnesis:</strong> ${encuentro.anamnesis || 'N/A'}</p>
                        <p><strong>Diagnóstico Principal:</strong> ${encuentro.diagnosticoPrincipal || 'N/A'}</p>
                        <p><strong>Tratamiento Sugerido:</strong> ${encuentro.tratamientoSugerido || 'N/A'}</p>
                        <p><strong>Observaciones:</strong> ${encuentro.observaciones || 'N/A'}</p>
                    </div>
                </div>
            `;
            if (encuentro.signosVitales) {
                const sv = encuentro.signosVitales;
                content += `
                    <div class="card detalle-encuentro-card mt-3">
                        <div class="card-header"><i class="fas fa-heartbeat me-2"></i>Signos Vitales</div>
                        <div class="card-body row">
                            <div class="col-md-6"><strong>Peso:</strong> ${sv.pesoKg || 'N/A'} kg</div>
                            <div class="col-md-6"><strong>Temperatura:</strong> ${sv.temperaturaCelsius || 'N/A'} °C</div>
                            <div class="col-md-6"><strong>Frec. Cardíaca:</strong> ${sv.frecuenciaCardiaca || 'N/A'} ppm</div>
                            <div class="col-md-6"><strong>Frec. Respiratoria:</strong> ${sv.frecuenciaRespiratoria || 'N/A'} rpm</div>
                        </div>
                    </div>
                `;
            }
            if (encuentro.prescripciones && encuentro.prescripciones.length > 0) {
                content += `
                    <div class="card detalle-encuentro-card mt-3">
                        <div class="card-header"><i class="fas fa-pills me-2"></i>Prescripciones Médicas</div>
                        <div class="card-body">
                            <ul class="list-group list-group-flush">
                            ${encuentro.prescripciones.map(p => `
                                <li class="list-group-item">
                                    <strong>${p.medicamento}</strong> (${p.dosis}) - ${p.frecuencia} por ${p.duracion}.<br>
                                    <small><em>Instrucciones: ${p.instrucciones}</em></small>
                                </li>
                            `).join('')}
                            </ul>
                        </div>
                    </div>
                 `;
            }
        } else {
            content += `<p class="text-muted mt-3">No hay detalles de consulta para esta cita.</p>`;
        }
        return content;
    }

    function renderVacunas(vacunas) {
        const tableBody = document.getElementById('vacunas-table-body');
        if (!vacunas || vacunas.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No hay vacunas registradas.</td></tr>';
            return;
        }
        tableBody.innerHTML = vacunas.map(v => `
            <tr>
                <td>${v.nombreVacuna}</td>
                <td>${new Date(v.fechaAplicacion).toLocaleDateString('es-CO')}</td>
                <td>${v.fechaProximaDosis ? new Date(v.fechaProximaDosis).toLocaleDateString('es-CO') : 'N/A'}</td>
                <td>${v.lote || 'N/A'}</td>
                <td>${v.veterinarioNombre || 'N/A'}</td>
            </tr>
        `).join('');
    }

    function getStatusBadge(status) {
        let badgeClass = 'bg-secondary'; // Clase por defecto
        let statusText = status.replace(/_/g, ' ').toLowerCase();
        statusText = statusText.charAt(0).toUpperCase() + statusText.slice(1);

        switch(status) {
            case 'REALIZADA':
                badgeClass = 'bg-success';
                break;
            case 'PENDIENTE':
            case 'CONFIRMADA':
                badgeClass = 'bg-warning text-dark';
                break;
            case 'NO_ASISTIO':
                badgeClass = 'bg-danger';
                break;
            case 'CANCELADA_PROPIETARIO':
            case 'CANCELADA_CLINICA':
                badgeClass = 'bg-secondary';
                statusText = 'Cancelada';
                break;
        }
        return `<span class="badge ${badgeClass}">${statusText}</span>`;
    }
});