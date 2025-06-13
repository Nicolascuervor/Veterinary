document.addEventListener('DOMContentLoaded', () => {
    const servicesGrid = document.getElementById('services-grid');
    const searchInput = document.getElementById('search-input');
    const noResults = document.getElementById('no-results');

    // Array de ejemplos de servicios (reemplaza la llamada a la API)
    const servicesData = [
        {
            nombre: 'Consulta General',
            descripcion: 'Revisión completa del estado de salud de tu mascota, ideal para chequeos preventivos y diagnóstico temprano.',
            costo: 55000,
            duracionEstimada: 30
        },
        {
            nombre: 'Vacunación Anual',
            descripcion: 'Paquete de inmunización esencial para proteger a tu compañero contra las enfermedades más comunes y peligrosas.',
            costo: 85000,
            duracionEstimada: 20
        },
        {
            nombre: 'Limpieza Dental Profunda',
            descripcion: 'Procedimiento profesional para eliminar sarro y placa, previniendo enfermedades periodontales y mal aliento.',
            costo: 150000,
            duracionEstimada: 45
        },
        {
            nombre: 'Cirugía Menor',
            descripcion: 'Procedimientos quirúrgicos de baja complejidad como esterilizaciones, castraciones y sutura de heridas.',
            costo: 250000,
            duracionEstimada: 60
        },
        {
            nombre: 'Consulta de Emergencia',
            descripcion: 'Atención prioritaria para casos urgentes y situaciones críticas que ponen en riesgo la vida de tu mascota.',
            costo: 120000,
            duracionEstimada: 40
        },
        {
            nombre: 'Peluquería y Baño',
            descripcion: 'Servicio completo de estética que incluye baño medicado, corte de pelo, limpieza de oídos y corte de uñas.',
            costo: 60000,
            duracionEstimada: 75
        },
        {
            nombre: 'Exámenes de Laboratorio',
            descripcion: 'Análisis de sangre, orina y otras muestras para un diagnóstico preciso y detallado de la salud interna.',
            costo: 95000,
            duracionEstimada: 15
        },
        {
            nombre: 'Fisioterapia y Rehabilitación',
            descripcion: 'Terapias para ayudar en la recuperación de lesiones, cirugías o para mejorar la movilidad en mascotas mayores.',
            costo: 70000,
            duracionEstimada: 50
        }
    ];

    // Mapeo de iconos para cada tipo de servicio
    const getIconForService = (serviceName) => {
        const name = serviceName.toLowerCase();
        if (name.includes('consulta')) return 'fa-stethoscope';
        if (name.includes('vacunación')) return 'fa-syringe';
        if (name.includes('cirugía')) return 'fa-scalpel-path';
        if (name.includes('dental')) return 'fa-tooth';
        if (name.includes('emergencia')) return 'fa-heart-pulse';
        if (name.includes('laboratorio')) return 'fa-vial-virus';
        if (name.includes('peluquería') || name.includes('baño')) return 'fa-scissors';
        if (name.includes('fisio')) return 'fa-person-walking';
        return 'fa-paw'; // Icono por defecto
    };

    const renderServices = (services) => {
        servicesGrid.innerHTML = ''; // Limpiar el grid
        if (services.length === 0) {
            noResults.classList.remove('d-none');
        } else {
            noResults.classList.add('d-none');
        }

        services.forEach(service => {
            const formattedCost = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(service.costo);
            const cardHtml = `
                <div class="col-md-6 col-lg-4 mb-4">
                    <div class="service-card">
                        <div class="card-body text-center p-4">
                            <div class="icon-container">
                                <i class="fas ${getIconForService(service.nombre)}"></i>
                            </div>
                            <h5 class="card-title">${service.nombre}</h5>
                            <p class="card-text">${service.descripcion}</p>
                            <ul class="service-details text-start">
                                <li><i class="fa-solid fa-dollar-sign"></i><strong>Costo:</strong> ${formattedCost}</li>
                                <li><i class="fa-solid fa-clock"></i><strong>Duración:</strong> ${service.duracionEstimada} min</li>
                            </ul>
                            <a href="citas.html" class="btn btn-primary mt-auto">Agendar Servicio</a>
                        </div>
                    </div>
                </div>
            `;
            servicesGrid.insertAdjacentHTML('beforeend', cardHtml);
        });
    };

    // Evento para filtrar los servicios en tiempo real
    searchInput.addEventListener('keyup', (e) => {
        const searchTerm = e.target.value.toLowerCase();
        const filteredServices = servicesData.filter(service =>
            service.nombre.toLowerCase().includes(searchTerm) ||
            service.descripcion.toLowerCase().includes(searchTerm)
        );
        renderServices(filteredServices);
    });

    // Renderizar los servicios estáticos al cargar la página
    renderServices(servicesData);
});