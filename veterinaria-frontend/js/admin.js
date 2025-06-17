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

    const contentArea = document.getElementById('admin-content-area');
    const navLinks = document.querySelectorAll('#sidebar-wrapper .list-group-item');
    const modalsContainer = document.getElementById('modals-container');

    let userRoleModal, productModal;

    // --- 2. LÓGICA DE CARGA ---
    const loadModals = async () => {
        try {
            const response = await fetch('admin_modals.html');
            modalsContainer.innerHTML = await response.text();
            userRoleModal = new bootstrap.Modal(document.getElementById('user-role-modal'));
            productModal = new bootstrap.Modal(document.getElementById('product-modal'));

            document.getElementById('user-role-form').addEventListener('submit', handleRoleFormSubmit);
            document.getElementById('product-form').addEventListener('submit', handleProductFormSubmit);
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
        } catch (error) { contentArea.innerHTML = `<div class="alert alert-danger">${error.message}</div>`; }
    };

    // --- 3. INICIALIZACIÓN POR SECCIÓN ---
    const initializeDashboardSection = () => {
        document.getElementById('total-users-kpi').textContent = allUsers.length;
        document.getElementById('total-products-kpi').textContent = allProducts.length;
    };
    const initializeUsersSection = () => {
        renderUsersTable(allUsers);
        document.getElementById('apply-user-filters').addEventListener('click', applyUserFilters);
        document.getElementById('clear-user-filters').addEventListener('click', clearUserFilters);
        document.getElementById('user-table-body').addEventListener('click', handleUserActionClick);
    };
    const initializeProductsSection = () => {
        renderProductsTable(allProducts);
        document.getElementById('apply-product-filters').addEventListener('click', applyProductFilters);
        document.getElementById('clear-product-filters').addEventListener('click', clearProductFilters);
        document.getElementById('product-table-body').addEventListener('click', handleProductActionClick);
        document.getElementById('btn-crear-producto').addEventListener('click', openNewProductModal);
    };

    // --- 4. RENDERIZADO DE TABLAS ---
    const renderUsersTable = (users) => {
        const userTableBody = document.getElementById('user-table-body');
        userTableBody.innerHTML = '';
        users.forEach(user => {
            const statusBadge = user.enabled ? '<span class="badge bg-success">Activo</span>' : '<span class="badge bg-danger">Inactivo</span>';
            const toggleButton = `<button class="btn btn-sm ${user.enabled ? 'btn-secondary' : 'btn-success'} btn-toggle-status" data-user-id="${user.id}" data-enabled="${user.enabled}" title="${user.enabled ? 'Desactivar' : 'Activar'}"><i class="fas fa-toggle-${user.enabled ? 'on' : 'off'}"></i></button>`;
            userTableBody.innerHTML += `<tr>
                <td>${user.id}</td><td>${user.username}</td><td>${user.email}</td><td>${user.role}</td><td>${statusBadge}</td>
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
        const categoryValue = document.getElementById('product-category-filter').value;
        const stockValue = document.getElementById('product-stock-filter').value;
        renderProductsTable(allProducts.filter(product => {
            const matchesSearch = product.name.toLowerCase().includes(searchValue);
            const matchesCategory = !categoryValue || product.category_name === categoryValue;
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
            document.getElementById('product-category').value = product.category_id.name;
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

    // --- 7. ARRANQUE Y REFRESCO DE DATOS ---
    const refreshData = async (sectionToRender) => {
        try {
            if (sectionToRender === 'users') {
                allUsers = await (await fetch(`${apiGatewayUrl}/api/admin/users`, { headers })).json();
                renderUsersTable(allUsers);
            } else if (sectionToRender === 'products') {
                allProducts = await (await fetch(`${apiGatewayUrl}/productos`)).json();
                renderProductsTable(allProducts);
            }
        } catch (error) { console.error("Error al refrescar datos:", error); }
    };

    const initApp = async () => {
        try {
            await loadModals();
            [allUsers, allProducts] = await Promise.all([
                (await fetch(`${apiGatewayUrl}/api/admin/users`, { headers })).json(),
                (await fetch(`${apiGatewayUrl}/productos`)).json()
            ]);

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