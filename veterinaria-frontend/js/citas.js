document.addEventListener('DOMContentLoaded', () => {
    // Estado inicial del formulario
    let state = {
        service: null,
        mascotaId: null,
        veterinarioId: null,
        fecha: null,
        hora: null,
        comentarios: ''
    };

    // Constantes de configuración
    const API_BASE = 'http://localhost:8081';
    const token = localStorage.getItem('token');
    const authHeaders = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };

    // Elementos principales del DOM
    const formContainer = document.getElementById('form-container');
    const successState = document.getElementById('success-state');
    const errorState = document.getElementById('error-state');
    const progressBar = document.getElementById('progressBar');
    const progressLabel = document.getElementById('progressLabel');
    const progressContainer = document.getElementById('progress-container');

    /**
     * Función principal que se ejecuta al cargar la página.
     */
    function init() {
        if (!token) {
            alert('Debes iniciar sesión para agendar una cita');
            window.location.href = 'login.html';
            return;
        }
        loadInitialData();
        setupEventListeners();
        updateUI();
    }

    /**
     * Carga los datos iniciales necesarios para el formulario.
     */
    async function loadInitialData() {
        await loadServicios();
        await loadMascotas();
    }

    /**
     * Configura todos los listeners de eventos para los elementos interactivos.
     */
    function setupEventListeners() {
        document.getElementById('veterinario').addEventListener('change', e => handleSelectChange('veterinarioId', e.target.value));
        document.getElementById('mascota').addEventListener('change', e => handleSelectChange('mascotaId', e.target.value));
        document.getElementById('submitButton').addEventListener('click', handleSubmit);
    }

    /**
     * Actualiza el estado global de la aplicación y refresca la UI.
     * @param {object} newState - Un objeto con las nuevas propiedades del estado a fusionar.
     */
    function setState(newState) {
        state = { ...state, ...newState };
        updateUI();
    }

    /**
     * Actualiza la interfaz de usuario basándose en el estado actual.
     * Controla la visibilidad de los pasos y el estado de la barra de progreso.
     */
    function updateUI() {
        const step1 = document.getElementById('step1');
        const step2 = document.getElementById('step2');
        const step3 = document.getElementById('step3');
        const submitButton = document.getElementById('submitButton');

        let progress = 25;
        let progressText = 'Paso 1 de 4: Selecciona un servicio';

        step2.style.display = 'none';
        step3.style.display = 'none';
        submitButton.style.display = 'none';

        if (state.service) {
            step2.style.display = 'block';
            progress = 50;
            progressText = 'Paso 2 de 4: Completa los detalles';
        }
        if (state.veterinarioId && state.fecha) {
            progress = 75;
            progressText = 'Paso 3 de 4: Elige una hora';
        }
        if (state.hora) {
            step3.style.display = 'block';
            submitButton.style.display = 'inline-block';
            progress = 100;
            progressText = 'Paso 4 de 4: Confirma tu cita';
            prepareConfirmation();
        }

        document.getElementById('diasSection').style.display = state.veterinarioId ? 'block' : 'none';
        document.getElementById('horariosSection').style.display = state.fecha ? 'block' : 'none';

        progressBar.style.width = `${progress}%`;
        progressBar.setAttribute('aria-valuenow', progress);
        progressLabel.textContent = progressText;
    }

    /**
     * Maneja el cambio en los selects de mascota y veterinario.
     * @param {string} key - La clave del estado a actualizar ('mascotaId' o 'veterinarioId').
     * @param {string} value - El nuevo valor seleccionado.
     */
    function handleSelectChange(key, value) {
        const newState = { [key]: value, fecha: null, hora: null };

        if (key === 'veterinarioId') {
            document.getElementById('horariosContainer').innerHTML = '';
            document.getElementById('diasDisponiblesContainer').innerHTML = '';
            if(value) loadDiasDisponibles(value);
        }
        setState(newState);
    }

    // --- FUNCIONES DE CARGA DE DATOS (API CALLS) ---

    async function loadServicios() {
        toggleLoading('serviciosLoading', true);
        try {
            const servicios = await makeRequest(`${API_BASE}/servicios`);
            const container = document.getElementById('serviciosContainer');
            container.innerHTML = servicios.map(s => `
                <div class="col-md-6 mb-3">
                    <div class="service-card" data-service-id="${s.id}">
                        <h5 class="mb-1">${s.nombre}</h5>
                        <p class="text-muted mb-0 small">${s.descripcion || 'Servicio profesional'}</p>
                    </div>
                </div>
            `).join('');
            document.querySelectorAll('.service-card').forEach(card => card.addEventListener('click', () => selectService(card.dataset.serviceId, servicios)));
        } finally {
            toggleLoading('serviciosLoading', false);
        }
    }

    async function loadMascotas() {
        try {
            const propietarioId = localStorage.getItem('id');
            const mascotas = await makeRequest(`${API_BASE}/mascotas/propietario/${propietarioId}`);
            const select = document.getElementById('mascota');
            select.innerHTML = '<option value="">Selecciona tu mascota</option>' + mascotas.map(m => `<option value="${m.id}">${m.nombre}</option>`).join('');
        } catch(e) {
            showAlert('No se pudieron cargar tus mascotas.', 'danger');
        }
    }

    async function loadVeterinariosPorServicio(servicioId) {
        const select = document.getElementById('veterinario');
        select.innerHTML = '<option value="">Cargando...</option>';
        try {
            const veterinarios = await makeRequest(`${API_BASE}/veterinarios/servicio/${servicioId}`);
            select.innerHTML = '<option value="">Selecciona un veterinario</option>' + veterinarios.map(v => `<option value="${v.id}">Dr. ${v.nombre} ${v.apellido || ''}</option>`).join('');
        } catch (e) {
            select.innerHTML = '<option value="">Error al cargar</option>';
        }
    }

    async function loadDiasDisponibles(veterinarioId) {
        toggleLoading('diasLoading', true);
        try {
            const dias = await makeRequest(`${API_BASE}/disponibilidad/veterinario/${veterinarioId}/proximos-dias-disponibles`);
            const container = document.getElementById('diasDisponiblesContainer');
            container.innerHTML = dias.length > 0 ? dias.map(d => createDayChip(new Date(d + 'T00:00:00'))).join('') : '<p class="text-muted text-center col-12">No hay días disponibles.</p>';
            document.querySelectorAll('.day-chip').forEach(c => c.addEventListener('click', () => selectDay(c.dataset.date)));
        } finally {
            toggleLoading('diasLoading', false);
        }
    }

    async function loadHorarios(veterinarioId, fecha) {
        toggleLoading('horariosLoading', true);
        try {
            const horarios = await makeRequest(`${API_BASE}/disponibilidad/veterinario/${veterinarioId}?fecha=${fecha}`);
            const container = document.getElementById('horariosContainer');
            container.innerHTML = horarios.length > 0 ? horarios.map(h => `<button class="btn btn-outline-secondary horario-btn">${h.horaInicio.substring(0, 5)}</button>`).join('') : '<p class="text-muted text-center">No hay horarios para este día.</p>';
            document.querySelectorAll('.horario-btn').forEach(btn => btn.addEventListener('click', () => selectHorario(btn.textContent, btn)));
        } finally {
            toggleLoading('horariosLoading', false);
        }
    }

    // --- FUNCIONES DE SELECCIÓN Y MANEJO DE ESTADO ---

    function selectService(serviceId, servicios) {
        setState({ service: servicios.find(s => s.id == serviceId), veterinarioId: null, fecha: null, hora: null });
        document.querySelectorAll('.service-card').forEach(c => c.classList.remove('selected'));
        document.querySelector(`[data-service-id="${serviceId}"]`).classList.add('selected');
        loadVeterinariosPorServicio(serviceId);
    }

    function selectDay(dateString) {
        setState({ fecha: dateString, hora: null });
        document.querySelectorAll('.day-chip').forEach(c => c.classList.remove('selected'));
        document.querySelector(`[data-date="${dateString}"]`).classList.add('selected');
        loadHorarios(state.veterinarioId, dateString);
    }

    function selectHorario(hora, btn) {
        setState({ hora });
        document.querySelectorAll('.horario-btn').forEach(b => b.classList.replace('btn-primary', 'btn-outline-secondary'));
        btn.classList.replace('btn-outline-secondary', 'btn-primary');
    }

    function prepareConfirmation() {
        const fechaObj = new Date(state.fecha + 'T00:00:00');
        document.getElementById('confirmFecha').textContent = fechaObj.toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
        document.getElementById('confirmHora').textContent = state.hora;
        document.getElementById('confirmMascota').textContent = document.getElementById('mascota').options[document.getElementById('mascota').selectedIndex].text;
        document.getElementById('confirmVeterinario').textContent = document.getElementById('veterinario').options[document.getElementById('veterinario').selectedIndex].text;
        document.getElementById('confirmServicio').textContent = state.service.nombre;
        document.getElementById('confirmComentarios').textContent = document.getElementById('comentarios').value || 'Ninguno';
    }

    // --- LÓGICA DE ENVÍO Y FEEDBACK ---

    async function handleSubmit() {
        const btn = document.getElementById('submitButton');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Confirmando...';

        try {
            const citaData = {
                fecha: state.fecha,
                hora: state.hora,
                motivo: document.getElementById('comentarios').value || state.service.nombre,
                mascotaId: parseInt(state.mascotaId),
                veterinarioId: parseInt(state.veterinarioId),
                servicioId: state.service.id
            };

            await makeRequest(`${API_BASE}/cita/registrar`, {
                method: 'POST',
                body: JSON.stringify(citaData)
            });

            // Mostrar la tarjeta de éxito
            document.getElementById('successMascota').textContent = document.getElementById('mascota').options[document.getElementById('mascota').selectedIndex].text;
            document.getElementById('successServicio').textContent = state.service.nombre;

            // ===== LÍNEA CORREGIDA =====
            // Usamos toLocaleString() en lugar de toLocaleDateString()
            const fechaHora = new Date(`${state.fecha}T${state.hora}`).toLocaleString('es-ES', {
                dateStyle: 'full',
                timeStyle: 'short'
            });
            document.getElementById('successFechaHora').textContent = fechaHora;
            // ===== FIN DE LA CORRECCIÓN =====

            document.getElementById('successVeterinario').textContent = document.getElementById('veterinario').options[document.getElementById('veterinario').selectedIndex].text;
            showFinalState('success');

        } catch (error) {
            // Mostrar la tarjeta de error
            document.getElementById('errorMessage').textContent = `Lo sentimos, no pudimos procesar tu solicitud. Error: ${error.message}`;
            showFinalState('error');

        } finally {
            // Restaurar el botón solo si hubo un error, ya que en caso de éxito se muestra otra pantalla
            if (document.getElementById('error-state').style.display === 'block') {
                btn.disabled = false;
                btn.innerHTML = 'Confirmar Cita <i class="fas fa-arrow-right ms-1"></i>';
            }
        }
    }

    window.resetForm = function() {
        state = { service: null, mascotaId: null, veterinarioId: null, fecha: null, hora: null, comentarios: '' };
        document.querySelectorAll('.service-card.selected').forEach(c => c.classList.remove('selected'));
        document.getElementById('mascota').value = '';
        document.getElementById('veterinario').innerHTML = '<option value="">Selecciona un veterinario</option>';
        document.getElementById('diasDisponiblesContainer').innerHTML = '';
        document.getElementById('horariosContainer').innerHTML = '';
        document.getElementById('comentarios').value = '';

        formContainer.style.display = 'block';
        progressContainer.style.display = 'block';
        successState.style.display = 'none';
        errorState.style.display = 'none';
        updateUI();
    }

    function showFinalState(stateName) {
        formContainer.style.display = 'none';
        progressContainer.style.display = 'none';
        successState.style.display = 'none';
        errorState.style.display = 'none';

        if(stateName === 'success') successState.style.display = 'block';
        if(stateName === 'error') errorState.style.display = 'block';
    }

    // --- FUNCIONES UTILITARIAS ---

    async function makeRequest(url, options = {}) {
        try {
            const response = await fetch(url, { headers: authHeaders, ...options });
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: `Error HTTP ${response.status}` }));
                throw new Error(errorData.message || `Error HTTP ${response.status}`);
            }
            // Maneja el caso de respuesta 204 No Content
            if (response.status === 204) {
                return [];
            }
            return response.json();
        } catch (error) {
            console.error(`Error en la petición a ${url}:`, error);
            throw error;
        }
    }

    function showAlert(message, type) {
        const container = document.getElementById('alertContainer');
        const alertId = 'alert_' + Date.now();
        if (container) {
            container.innerHTML = `<div class="alert alert-${type} alert-dismissible fade show" id="${alertId}" role="alert">${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>`;
            setTimeout(() => document.getElementById(alertId)?.remove(), 5000);
        }
    }

    function toggleLoading(elementId, show) {
        const element = document.getElementById(elementId);
        if (element) {
            element.style.display = show ? 'flex' : 'none';
        }
    }

    function createDayChip(date) {
        const dateString = date.toISOString().split('T')[0];
        const dayName = date.toLocaleDateString('es-ES', { weekday: 'short' });
        const dayNum = date.getDate();
        const monthName = date.toLocaleDateString('es-ES', { month: 'short' });
        return `<div class="day-chip" data-date="${dateString}"><div class="day-name">${dayName.replace('.', '')}</div><div class="day-num">${dayNum}</div><div class="month-name">${monthName.replace('.','')}</div></div>`;
    }

    // Arrancar la aplicación
    init();
});