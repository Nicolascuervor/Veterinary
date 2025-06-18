document.addEventListener('DOMContentLoaded', () => {
    // --- 1. CONFIGURACIÓN Y VERIFICACIÓN DE SEGURIDAD ---
    const token = localStorage.getItem('token');
    const userRole = localStorage.getItem('userRole');

    if (userRole !== 'ADMIN') {
        alert('Acceso denegado. Esta página es solo para administradores.');
        window.location.href = 'dashboard.html';
        return;
    }

    const apiGatewayUrl = 'http://localhost:8081';
    const headers = { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };

    let allUsers = [];
    let allProducts = [];
    let allMascotas = []; // Variable para mascotas
    let allCategories = [];


    const contentArea = document.getElementById('admin-content-area');
    const navLinks = document.querySelectorAll('#sidebar-wrapper .list-group-item');
    const modalsContainer = document.getElementById('modals-container');

    let userRoleModal, productModal, mascotaModal;

    // --- 2. LÓGICA DE CARGA
    const loadModals = async () => {
        try {
            const response = await fetch('admin_modals.html');
            modalsContainer.innerHTML = await response.text();
            userRoleModal = new bootstrap.Modal(document.getElementById('user-role-modal'));
            productModal = new bootstrap.Modal(document.getElementById('product-modal'));
            mascotaModal = new bootstrap.Modal(document.getElementById('mascota-modal'));

            document.getElementById('user-role-form').addEventListener('submit', handleRoleFormSubmit);
            document.getElementById('product-form').addEventListener('submit', handleProductFormSubmit);
            document.getElementById('mascota-form').addEventListener('submit', handleMascotaFormSubmit);


        } catch (error) { console.error("No se pudieron cargar los modales:", error); }
    };


    const loadSection = async (section) => {
        contentArea.innerHTML = '<div class="text-center p-5"><div class="spinner-border text-primary"></div></div>';
        try {
            const response = await fetch(`admin_${section}.html`);
            if (!response.ok) throw new Error(`Vista no encontrada: admin_${section}.html`);
            contentArea.innerHTML = await response.text();

            if (section === 'users') initializeUsersSection();
            else if (section === 'products') initializeProductsSection();
            else if (section === 'dashboard') initializeDashboardSection();
            else if (section === 'pets') initializeMascotasSection();

        } catch (error) { contentArea.innerHTML = `<div class="alert alert-danger">${error.message}</div>`; }
    };



    // --- 3. INICIALIZACIÓN POR SECCIÓN ---
    const initializeDashboardSection = () => {
        document.getElementById('total-users-kpi').textContent = allUsers.length;
        document.getElementById('total-products-kpi').textContent = allProducts.length;
        document.getElementById('total-mascotas-kpi').textContent = allMascotas.length;
    };


    const initializeUsersSection = () => {
        renderUsersTable(allUsers);
        document.getElementById('apply-user-filters').addEventListener('click', applyUserFilters);
        document.getElementById('clear-user-filters').addEventListener('click', clearUserFilters);
        document.getElementById('user-table-body').addEventListener('click', handleUserActionClick);
    };


    const initializeProductsSection = () => {
        populateCategoryDropdowns('product-category-filter');
        renderProductsTable(allProducts);

        document.getElementById('apply-product-filters').addEventListener('click', applyProductFilters);
        document.getElementById('clear-product-filters').addEventListener('click', clearProductFilters);
        document.getElementById('product-table-body').addEventListener('click', handleProductActionClick);
        document.getElementById('btn-crear-producto').addEventListener('click', openNewProductModal);
    };

    // <<== NUEVO: Función reutilizable para poblar los 'select' de categorías.
    function populateCategoryDropdowns(selectElementId, selectedId = null) {
        const select = document.getElementById(selectElementId);
        select.innerHTML = `<option value="">${selectElementId.includes('filter') ? 'Todas las Categorías' : 'Seleccione una categoría...'}</option>`;
        allCategories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            if (selectedId && category.id == selectedId) {
                option.selected = true;
            }
            select.appendChild(option);
        });
    }


    const initializeMascotasSection = () => {
        renderMascotasTable(allMascotas);
        document.getElementById('apply-mascota-filters').addEventListener('click', applyMascotaFilters);
        document.getElementById('clear-mascota-filters').addEventListener('click', clearMascotaFilters);
        document.getElementById('mascotas-table-body').addEventListener('click', handleMascotaActionClick);
        document.getElementById('btn-crear-mascota').addEventListener('click', openNewMascotaModal);
    };



    // --- 4. RENDERIZADO DE TABLAS ---
    const renderUsersTable = (users) => {
        const userTableBody = document.getElementById('user-table-body');
        userTableBody.innerHTML = '';
        users.forEach(user => {
            const statusBadge = user.enabled ? '<span class="badge bg-success">Activo</span>' : '<span class="badge bg-danger">Inactivo</span>';
            const toggleButton = `<button class="btn btn-sm ${user.enabled ? 'btn-secondary' : 'btn-success'} btn-toggle-status" data-user-id="${user.id}" data-enabled="${user.enabled}" title="${user.enabled ? 'Desactivar' : 'Activar'}"><i class="fas fa-toggle-${user.enabled ? 'on' : 'off'}"></i></button>`;
            userTableBody.innerHTML += `<tr>
                <td>${user.id}</td>
                <td>${user.username}
                </td><td>${user.role}</td>
                <td>${statusBadge}</td>
                <td><button class="btn btn-sm btn-warning btn-edit-role" data-user-id="${user.id}" data-current-role="${user.role}" title="Cambiar Rol"><i class="fas fa-user-shield"></i></button> ${toggleButton}</td>
            </tr>`;
        });
    };

    const renderProductsTable = (products) => {
        const productTableBody = document.getElementById('product-table-body');
        productTableBody.innerHTML = '';
        products.forEach(product => {
            // LOG: Muestra el objeto de producto que se está renderizando.
            console.log('[Admin Panel] Renderizando producto:', product);

            const categoryName = allCategories.find(c => c.id === product.category_id)?.name || 'N/A';

            const stockBadge = product.stock > 10 ? `<span class="badge bg-success">En Stock (${product.stock})</span>`
                : product.stock > 0 ? `<span class="badge bg-warning">Stock Bajo (${product.stock})</span>`
                    : `<span class="badge bg-danger">Agotado</span>`;

            const actionButtons = `<button class="btn btn-sm btn-warning btn-edit-product" data-product-id="${product.id}" title="Editar Producto"><i class="fas fa-edit"></i></button>`;

            productTableBody.innerHTML += `<tr>
                <td>${product.id}</td>
                <td>${product.name}</td>
                <td>${product.category_name || 'N/A'}</td>
                <td>$${product.price.toLocaleString('es-CO')}</td>
                <td>${stockBadge}</td>
                <td>${actionButtons}</td>
            </tr>`;
        });
    };



    /**
     * Calcula la edad en años y meses a partir de una fecha de nacimiento.
     * @param {string} fechaNacimientoStr - La fecha de nacimiento en formato 'YYYY-MM-DD'.
     * @returns {string} La edad formateada, por ejemplo: "3 años y 2 meses".
     */
    const calcularEdad = (fechaNacimientoStr) => {
        if (!fechaNacimientoStr) {
            return 'N/A';
        }
        const fechaNacimiento = new Date(fechaNacimientoStr);
        const hoy = new Date();

        let anios = hoy.getFullYear() - fechaNacimiento.getFullYear();
        let meses = hoy.getMonth() - fechaNacimiento.getMonth();

        if (meses < 0 || (meses === 0 && hoy.getDate() < fechaNacimiento.getDate())) {
            anios--;
            meses += 12;
        }

        // Si la mascota tiene menos de un año, mostrar solo los meses.
        if (anios === 0) {
            return `${meses} mes${meses !== 1 ? 'es' : ''}`;
        }

        return `${anios} año${anios !== 1 ? 's' : ''}`;
    };

    /**
     * Renderiza la tabla de mascotas en el panel de administración.
     * @param {Array<Object>} mascotas - El arreglo de objetos de mascotas a renderizar.
     */
    const renderMascotasTable = (mascotas) => {
        const mascotasTableBody = document.getElementById('mascotas-table-body');
        mascotasTableBody.innerHTML = '';

        if (!mascotas || mascotas.length === 0) {
            mascotasTableBody.innerHTML = `<tr><td colspan="11" class="text-center">No se encontraron mascotas.</td></tr>`; // Aumentado a 11 columnas
            return;
        }

        mascotas.forEach(mascota => {
            const edadCalculada = calcularEdad(mascota.fechaNacimiento);
            const nombrePropietario = mascota.propietario ? mascota.propietario.nombre : 'No asignado';

            const actionButtons = `
            <button class="btn btn-sm btn-info btn-view-history" data-mascota-id="${mascota.id}" title="Ver Historial Clínico"><i class="fas fa-notes-medical"></i></button>
            <button class="btn btn-sm btn-warning btn-edit-mascota" data-mascota-id="${mascota.id}" title="Editar Mascota"><i class="fas fa-edit"></i></button>
            <button class="btn btn-sm btn-danger btn-delete-mascota" data-mascota-id="${mascota.id}" title="Eliminar Mascota"><i class="fas fa-trash"></i></button>
        `;

            // Se añade la nueva celda <td> para el sexo
            mascotasTableBody.innerHTML += `<tr>
            <td>${mascota.id}</td>
            <td>${mascota.nombre}</td>
            <td>${mascota.especie}</td>
            <td>${mascota.raza || 'N/A'}</td>
            <td>${edadCalculada}</td>
            <td>${mascota.fechaNacimiento || 'N/A'}</td>
            <td>${mascota.color || 'N/A'}</td>
            <td>${mascota.peso || 0} kg</td>
            <td>${mascota.sexo || 'No especificado'}</td> <td>${nombrePropietario}</td>
            <td>${actionButtons}</td>
        </tr>`;
        });
    };






    // --- 5. LÓGICA DE FILTROS ---
    const applyUserFilters = () => {
        const searchValue = document.getElementById('user-search-input').value.toLowerCase();
        const roleValue = document.getElementById('user-role-filter').value;
        const statusValue = document.getElementById('user-status-filter').value;
        renderUsersTable(allUsers.filter(user =>
            (user.username.toLowerCase().includes(searchValue) || user.email.toLowerCase().includes(searchValue)) &&
            (!roleValue || user.role === roleValue) &&
            (!statusValue || String(user.enabled) === statusValue)
        ));
    };
    const clearUserFilters = () => {
        document.getElementById('user-search-input').value = '';
        document.getElementById('user-role-filter').value = '';
        document.getElementById('user-status-filter').value = '';
        renderUsersTable(allUsers);
    };


    const applyProductFilters = () => {
        const searchValue = document.getElementById('product-search-input').value.toLowerCase();
        const categoryIdValue = document.getElementById('product-category-filter').value;
        const stockValue = document.getElementById('product-stock-filter').value;
        renderProductsTable(allProducts.filter(product => {
            const matchesSearch = product.name.toLowerCase().includes(searchValue);
            const matchesCategory = !categoryIdValue || product.category_id == categoryIdValue;
            let matchesStock = !stockValue || (stockValue === 'in-stock' && product.stock > 10) || (stockValue === 'low-stock' && product.stock > 0 && product.stock <= 10) || (stockValue === 'out-of-stock' && product.stock === 0);
            return matchesSearch && matchesCategory && matchesStock;
        }));
    };



    const clearProductFilters = () => {
        document.getElementById('product-search-input').value = '';
        document.getElementById('product-category-filter').value = '';
        document.getElementById('product-stock-filter').value = '';
        renderProductsTable(allProducts);
    };


    // --- LÓGICA DE FILTROS PARA MASCOTAS ---

        const getAgeInYears = (fechaNacimientoStr) => {
        if (!fechaNacimientoStr) return -1; // Retorna -1 si no hay fecha
        const today = new Date();
        const birthDate = new Date(fechaNacimientoStr);
        let age = today.getFullYear() - birthDate.getFullYear();
        const m = today.getMonth() - birthDate.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        return age;
    };


    const applyMascotaFilters = () => {
        const searchValue = document.getElementById('mascota-search-input').value.toLowerCase();
        const speciesValue = document.getElementById('mascota-species-filter').value;
        const genderValue = document.getElementById('mascota-gender-filter').value; // <-- Obtenemos el nuevo filtro
        const ageValue = document.getElementById('mascota-age-filter').value;

        const filteredMascotas = allMascotas.filter(mascota => {
            const ownerName = mascota.propietario ? mascota.propietario.nombre.toLowerCase() : '';
            const matchesSearch = mascota.nombre.toLowerCase().includes(searchValue) ||
                mascota.raza.toLowerCase().includes(searchValue) ||
                ownerName.includes(searchValue);

            const matchesSpecies = !speciesValue || mascota.especie === speciesValue;
            const matchesGender = !genderValue || mascota.sexo === genderValue; // <-- Añadimos la condición de género

            let matchesAge = true;
            if (ageValue) {
                const ageInYears = getAgeInYears(mascota.fechaNacimiento); // El nombre del campo es fechaNacimiento en el DTO
                switch (ageValue) {
                    case 'cachorro': matchesAge = ageInYears >= 0 && ageInYears < 1; break;
                    case 'adulto': matchesAge = ageInYears >= 1 && ageInYears <= 7; break;
                    case 'senior': matchesAge = ageInYears > 7; break;
                }
            }

            return matchesSearch && matchesSpecies && matchesGender && matchesAge; // <-- Se añade matchesGender
        });

        renderMascotasTable(filteredMascotas);
    };

    /**
     * Limpia todos los filtros de la sección de mascotas y muestra la tabla completa.
     */
    const clearMascotaFilters = () => {
        document.getElementById('mascota-search-input').value = '';
        document.getElementById('mascota-species-filter').value = '';
        document.getElementById('mascota-gender-filter').value = ''; // <-- Reseteamos el nuevo filtro
        document.getElementById('mascota-age-filter').value = '';

        // Vuelve a renderizar la tabla con la lista completa de mascotas
        renderMascotasTable(allMascotas);
    };








    // --- 6. LÓGICA DE ACCIONES ---
    const handleUserActionClick = async (e) => {
        const target = e.target.closest('button');
        if (!target) return;
        const userId = target.dataset.userId;
        if (target.classList.contains('btn-edit-role')) {
            document.getElementById('user-id-role').value = userId;
            document.getElementById('user-role-select').value = target.dataset.currentRole;
            userRoleModal.show();
        }
        if (target.classList.contains('btn-toggle-status')) {
            const isEnabled = target.dataset.enabled === 'true';
            if (confirm(`¿Seguro que quieres ${isEnabled ? 'DESACTIVAR' : 'ACTIVAR'} a este usuario?`)) {
                try {
                    // FIX: URL Corregida con la sintaxis ${...}
                    const response = await fetch(`${apiGatewayUrl}/api/admin/users/${userId}/status`, {
                        method: 'PUT', headers, body: JSON.stringify({ enabled: !isEnabled })
                    });
                    if (!response.ok) throw new Error('Falló la actualización de estado');
                    await refreshData('users');
                } catch (error) { console.error(error); alert('No se pudo actualizar el estado.'); }
            }
        }
    };
    const handleRoleFormSubmit = async (e) => {
        e.preventDefault();
        const userId = document.getElementById('user-id-role').value;
        const newRole = document.getElementById('user-role-select').value;
        try {
            // FIX: URL Corregida con la sintaxis ${...}
            const response = await fetch(`${apiGatewayUrl}/api/admin/users/${userId}/role`, {
                method: 'PUT', headers, body: JSON.stringify({ role: newRole })
            });
            if (!response.ok) throw new Error(await response.text());
            userRoleModal.hide();
            await refreshData('users');
        } catch (error) { alert(`Error al cambiar rol: ${error.message}`); }
    };

    const handleProductActionClick = async (e) => {
        const target = e.target.closest('button.btn-edit-product');
        if (!target) return;
        const productId = target.dataset.productId;
        try {
            // FIX: URL Corregida con la sintaxis ${...}
            const response = await fetch(`${apiGatewayUrl}/productos/${productId}`);
            if (!response.ok) throw new Error('Producto no encontrado');
            const product = await response.json();
            document.getElementById('product-modal-title').textContent = 'Editar Producto';
            document.getElementById('product-id').value = product.id;
            document.getElementById('product-name').value = product.name;
            document.getElementById('product-description').value = product.description;
            document.getElementById('product-price').value = product.price;
            document.getElementById('product-stock').value = product.stock;
            populateCategoryDropdowns('product-category', product.category_id);
            document.getElementById('product-image-file').value = '';
            productModal.show();
        } catch (error) {
            console.error("Error al obtener detalles del producto:", error);
            alert("No se pudieron cargar los datos para editar.");
        }
    };



    const openNewProductModal = () => {
        document.getElementById('product-form').reset();
        document.getElementById('product-modal-title').textContent = 'Crear Nuevo Producto';
        document.getElementById('product-id').value = '';
        populateCategoryDropdowns('product-category');
        productModal.show();
    };


    const handleProductFormSubmit = async (e) => {
        e.preventDefault();
        console.log('[Admin Panel] Se ha enviado el formulario de producto.');

        const form = document.getElementById('product-form');
        const productId = form.querySelector('#product-id').value;
        const isEditing = !!productId;

        const formData = new FormData(form);

        // LOG: Muestra todos los datos que se van a enviar al backend.
        console.log('[Admin Panel] Datos a enviar en FormData:');
        for (let [key, value] of formData.entries()) {
            // Si es un archivo, muestra su nombre y tamaño.
            if (value instanceof File) {
                console.log(`  ${key}: ${value.name} (Tamaño: ${value.size} bytes)`);
            } else {
                console.log(`  ${key}: ${value}`);
            }
        }

        const url = isEditing ? `${apiGatewayUrl}/productos/${productId}` : `${apiGatewayUrl}/productos`;
        const method = isEditing ? 'PUT' : 'POST';

        console.log(`[Admin Panel] Enviando petición: ${method} a ${url}`);

        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Authorization': `Bearer ${token}` },
                body: formData
            });

            console.log(`[Admin Panel] Respuesta del servidor recibida con estado: ${response.status}`);

            if (!response.ok) {
                // LOG: Si la respuesta no es JSON (como una página de error HTML), la mostramos.
                const errorText = await response.text();
                console.error('[Admin Panel] La respuesta del servidor no fue JSON. Contenido:', errorText);
                throw new Error(`El servidor respondió con un error ${response.status}. Revisa la consola de tu IDE para ver el error del backend.`);
            }

            productModal.hide();
            await refreshData('products');
            alert(`Producto ${isEditing ? 'actualizado' : 'creado'} con éxito.`);

        } catch (error) {
            alert(`Error al guardar el producto: ${error.message}`);
            console.error(error);
        }
    };



    // Lógica de acciones para Mascotas
    const openNewMascotaModal = () => {
        document.getElementById('mascota-form').reset();
        document.getElementById('mascota-modal-title').textContent = 'Registrar Nueva Mascota';
        document.getElementById('mascota-id').value = '';
        mascotaModal.show();
    };

    const handleMascotaActionClick = async (e) => {
        const target = e.target.closest('button');
        if (!target) return;
        const mascotaId = target.dataset.mascotaId;

        if (target.classList.contains('btn-edit-mascota')) {
            try {
                const response = await fetch(`${apiGatewayUrl}/mascotas/${mascotaId}`, { headers });
                if (!response.ok) throw new Error('Mascota no encontrada');
                const mascota = await response.json();

                console.log('[DEBUG] Datos de mascota para editar:', mascota);

                document.getElementById('mascota-modal-title').textContent = 'Editar Mascota';
                document.getElementById('mascota-id').value = mascota.id;
                document.getElementById('mascota-nombre').value = mascota.nombre;
                document.getElementById('mascota-especie').value = mascota.especie;
                document.getElementById('mascota-raza').value = mascota.raza || '';
                document.getElementById('mascota-fecha-nacimiento').value = mascota.fechaNacimiento || '';
                document.getElementById('mascota-color').value = mascota.color || '';
                document.getElementById('mascota-peso').value = mascota.peso || '';
                document.getElementById('mascota-sexo').value = mascota.sexo || '';



                // CORRECCIÓN: Comprobar si el propietario existe antes de acceder a su id
                if (mascota.propietario) {
                    document.getElementById('mascota-propietario-id').value = mascota.propietario.id;
                } else {
                    console.warn(`La mascota con ID ${mascotaId} no tiene un propietario asociado en la respuesta.`);
                    document.getElementById('mascota-propietario-id').value = ''; // Dejar el select en blanco
                }


                mascotaModal.show();
            } catch (error) {
                console.error("Error al obtener detalles de la mascota:", error);
                alert("No se pudieron cargar los datos de la mascota para editar.");
            }
        }

        if (target.classList.contains('btn-delete-mascota')) {
            if (confirm(`¿Estás seguro de que quieres eliminar esta mascota?`)) {
                try {
                    const response = await fetch(`${apiGatewayUrl}/mascotas/${mascotaId}`, { method: 'DELETE', headers });
                    if (!response.ok) throw new Error('Falló al eliminar la mascota');
                    alert('Mascota eliminada con éxito.');
                    await refreshData('mascotas');
                } catch (error) {
                    console.error(error);
                    alert('No se pudo eliminar la mascota.');
                }
            }
        }
    };

    const handleMascotaFormSubmit = async (e) => {
        e.preventDefault();
        const form = document.getElementById('mascota-form');
        const mascotaId = form.querySelector('#mascota-id').value;
        const isEditing = !!mascotaId;

        // Se añade 'sexo' a los datos que se envían
        const mascotaData = {
            nombre: form.querySelector('#mascota-nombre').value,
            especie: form.querySelector('#mascota-especie').value,
            raza: form.querySelector('#mascota-raza').value,
            sexo: form.querySelector('#mascota-sexo').value, // <<< CAMPO AÑADIDO
            fechaNacimiento: form.querySelector('#mascota-fecha-nacimiento').value,
            color: form.querySelector('#mascota-color').value,
            peso: parseFloat(form.querySelector('#mascota-peso').value),
            propietarioId: parseInt(form.querySelector('#mascota-propietario-id').value)
        };

        const formData = new FormData();
        formData.append('mascota', JSON.stringify(mascotaData));
        const imageFile = form.querySelector('#mascota-imagen-file').files[0];
        if (imageFile) {
            formData.append('file', imageFile);
        }

        const url = isEditing ? `${apiGatewayUrl}/mascotas/${mascotaId}` : `${apiGatewayUrl}/mascotas`;
        const method = isEditing ? 'PUT' : 'POST';

        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Authorization': `Bearer ${token}` },
                body: formData
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || `Error del servidor: ${response.status}`);
            }

            mascotaModal.hide();
            await refreshData('mascotas');
            alert(`Mascota ${isEditing ? 'actualizada' : 'registrada'} con éxito.`);
        } catch (error) {
            alert(`Error al guardar la mascota: ${error.message}`);
            console.error(error);
        }
    };




    // --- 7. ARRANQUE Y REFRESCO DE DATOS (Corregido) ---
    const refreshData = async (sectionToRender) => {
        try {
            if (sectionToRender === 'users') {
                const response = await fetch(`${apiGatewayUrl}/api/admin/users`, { headers });
                if (!response.ok) throw new Error(`Error ${response.status} al obtener usuarios`);
                allUsers = await response.json();
                renderUsersTable(allUsers);
            } else if (sectionToRender === 'products') {
                const response = await fetch(`${apiGatewayUrl}/productos`);
                if (!response.ok) throw new Error(`Error ${response.status} al obtener productos`);
                allProducts = await response.json();
                renderProductsTable(allProducts);
            } else if (sectionToRender === 'mascotas') {
                const response = await fetch(`${apiGatewayUrl}/mascotas`, { headers });
                if (!response.ok) throw new Error(`Error ${response.status} al obtener mascotas`);
                allMascotas = await response.json();

                console.log('[DEBUG] Array de mascotas completo recibido:', allMascotas);

                renderMascotasTable(allMascotas);
            }
        } catch (error) {
            console.error(`Error al refrescar datos de ${sectionToRender}:`, error);
        }
    };

    const initApp = async () => {
        try {
            await loadModals();


            const [usersData, productsData, mascotasData, categoriesData] = await Promise.all([
                fetch(`${apiGatewayUrl}/api/admin/users`, { headers }).then(res => res.json()),
                fetch(`${apiGatewayUrl}/productos`).then(res => res.json()),
                fetch(`${apiGatewayUrl}/mascotas`, { headers }).then(res => res.json()),
                fetch(`${apiGatewayUrl}/categorias`).then(res => res.json())

            ]);

            allUsers = usersData;
            allProducts = productsData;
            allMascotas = mascotasData;
            allCategories = categoriesData; // <<== NUEVO: Almacenar categorías.

            navLinks.forEach(link => {
                link.addEventListener('click', (e) => {
                    e.preventDefault();
                    navLinks.forEach(l => l.classList.remove('active'));
                    e.currentTarget.classList.add('active');
                    loadSection(e.currentTarget.dataset.section);
                });
            });

            loadSection('dashboard');
            document.querySelector('[data-section="dashboard"]').classList.add('active');
        } catch (error) {
            contentArea.innerHTML = `<div class="alert alert-danger">Error fatal al cargar los datos iniciales.</div>`;
            console.error("Error de inicialización:", error);
        }
    };

    initApp();
});