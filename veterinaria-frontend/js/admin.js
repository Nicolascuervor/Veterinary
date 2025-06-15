// veterinaria-frontend/js/admin.js (Versión Completa y Definitiva)
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    const userRole = localStorage.getItem('userRole');

    // 1. Verificación de seguridad
    if (userRole !== 'ADMIN') {
        alert('Acceso denegado. Esta página es solo para administradores.');
        window.location.href = 'dashboard.html';
        return;
    }

    // --- 2. Preparación (URLs y Headers) ---
    const apiGatewayUrl = 'http://localhost:8081';
    const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };

    // --- 3. Referencias al DOM ---
    const userTableBody = document.getElementById('user-table-body');
    const productTableBody = document.getElementById('product-table-body');
    const productModal = new bootstrap.Modal(document.getElementById('product-modal'));
    const userRoleModal = new bootstrap.Modal(document.getElementById('user-role-modal'));
    const productForm = document.getElementById('product-form');
    const userRoleForm = document.getElementById('user-role-form');

    // --- 4. Funciones de Carga de Datos ---

    // Archivo: veterinaria-frontend/js/admin.js

    const loadUsers = async () => {
        try {
            const response = await fetch(`${apiGatewayUrl}/api/admin/users`, { headers });
            if (!response.ok) {
                throw new Error(`Error ${response.status} al cargar usuarios.`);
            }
            const users = await response.json();

            userTableBody.innerHTML = '';
            users.forEach(user => {
                const row = document.createElement('tr');

                // --- CORRECCIÓN AQUÍ: Usamos user.activo en lugar de user.enabled ---
                const statusBadge = user.enabled ? `<span class="badge bg-success">Activo</span>` : `<span class="badge bg-danger">Inactivo</span>`;

                row.innerHTML = `
                <td>${user.id}</td>
                <td>${user.username}</td>
                <td>${user.email}</td>
                <td>${user.role}</td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn btn-sm btn-warning btn-edit-role" data-user-id="${user.id}" data-current-role="${user.role}" title="Cambiar Rol">
                        <i class="fas fa-user-shield"></i>
                    </button>
                    <button class="btn btn-sm ${user.enabled ? 'btn-secondary' : 'btn-success'} btn-toggle-status" data-user-id="${user.id}" data-enabled="${user.enabled}" title="${user.enabled ? 'Desactivar' : 'Activar'}">
                        <i class="fas ${user.enabled ? 'fa-toggle-off' : 'fa-toggle-on'}"></i>
                    </button>
                    <button class="btn btn-sm btn-danger btn-delete-user" data-user-id="${user.id}" title="Eliminar Usuario">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
                userTableBody.appendChild(row);
            });
        } catch (error) {
            console.error(error);
            userTableBody.innerHTML = `<tr><td colspan="6" class="text-center text-danger">Error al cargar los usuarios. Revisa la consola (F12).</td></tr>`;
        }
    };

    // Cargar y "dibujar" productos en la tabla
    const loadProducts = async () => {
        try {
            // Nota: El GET de productos es público, pero enviamos el header por si se quisiera proteger en el futuro.
            const response = await fetch(`${apiGatewayUrl}/productos`, { headers });
            if (!response.ok) {
                throw new Error(`Error ${response.status} al cargar productos.`);
            }
            const products = await response.json();

            productTableBody.innerHTML = ''; // Limpiar
            products.forEach(product => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${product.id}</td>
                    <td>${product.name}</td>
                    <td>$${product.price.toFixed(2)}</td>
                    <td>${product.stock}</td>
                    <td>
                        <button class="btn btn-sm btn-info btn-edit-product" data-product-id="${product.id}" title="Editar">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-danger btn-delete-product" data-product-id="${product.id}" title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                `;
                productTableBody.appendChild(row);
            });
        } catch (error) {
            console.error(error);
            productTableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Error al cargar los productos. Revisa la consola (F12).</td></tr>`;
        }
    };

    // --- 5. Lógica de Eventos (Botones y Formularios) ---
    // (Aquí va toda la lógica para los clics en botones de editar, eliminar, etc., y el envío de los formularios de los modales)
    // Esta es la lógica completa que faltaba.

    // Eventos para la tabla de usuarios
    userTableBody.addEventListener('click', async (e) => {
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
            if (confirm(`¿Estás seguro de que quieres ${isEnabled ? 'desactivar' : 'activar'} a este usuario?`)) {
                try {
                    const response = await fetch(`${apiGatewayUrl}/api/admin/users/${userId}/status`, {
                        method: 'PUT', headers, body: JSON.stringify({ enabled: !isEnabled })
                    });
                    if (!response.ok) throw new Error('Falló la actualización de estado');
                    await loadUsers(); // Recargar la tabla
                } catch (error) {
                    alert('No se pudo actualizar el estado.');
                    console.error(error);
                }
            }
        }
    });

    // Evento para el formulario de cambio de rol
    userRoleForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const userId = document.getElementById('user-id-role').value;
        const newRole = document.getElementById('user-role-select').value;
        try {
            const response = await fetch(`${apiGatewayUrl}/api/admin/users/${userId}/role`, {
                method: 'PUT', headers, body: JSON.stringify({ role: newRole })
            });
            if (!response.ok) throw new Error(await response.text());
            userRoleModal.hide();
            await loadUsers(); // Recargar la tabla
        } catch (error) {
            alert(`Error al cambiar rol: ${error.message}`);
        }
    });

    // Eventos para la tabla de productos
    document.getElementById('btn-crear-producto').addEventListener('click', () => {
        productForm.reset();
        document.getElementById('product-modal-title').textContent = 'Crear Producto';
        document.getElementById('product-id').value = '';
        productModal.show();
    });

    productTableBody.addEventListener('click', async (e) => {
        const target = e.target.closest('button');
        if (!target) return;

        const productId = target.dataset.productId;

        if (target.classList.contains('btn-edit-product')) {

            const response = await fetch(`${apiGatewayUrl}/productos/${productId}`);

            const product = await response.json();

            // Llenamos el formulario
            document.getElementById('product-modal-title').textContent = 'Editar Producto';
            document.getElementById('product-id').value = product.id;
            document.getElementById('product-id').value = product.id;
            document.getElementById('product-name').value = product.name;
            document.getElementById('product-description').value = product.description;
            document.getElementById('product-price').value = product.price;
            document.getElementById('product-stock').value = product.stock;
            document.getElementById('product-category').value = product.category_id;
            document.getElementById('product-seller').value = product.seller_id;


            const imagePreviewContainer = document.getElementById('image-preview-container');
            const imagePreview = document.getElementById('image-preview');



            // --- LÍNEA CORRECTA ---
            if (product.image) {
                imagePreview.src = `${apiGatewayUrl}${product.image}`;
                imagePreviewContainer.style.display = 'block';
            } else {
                imagePreviewContainer.style.display = 'none';
            }

            // Limpiamos el campo de archivo, ya que no se puede pre-rellenar
            document.getElementById('product-image-file').value = '';

            productModal.show();
        }

        if (target.classList.contains('btn-delete-product')) {
            if (confirm('¿Estás seguro de que quieres eliminar este producto?')) {
                try {
                    const response = await fetch(`${apiGatewayUrl}/productos/${productId}`, { method: 'DELETE', headers });
                    if (!response.ok) throw new Error('Error al eliminar');
                    await loadProducts();
                } catch (error) {
                    alert('No se pudo eliminar el producto.');
                }
            }
        }
    });

    // Archivo: veterinaria-frontend/js/admin.js

    // Archivo: veterinaria-frontend/js/admin.js

    productForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const productId = document.getElementById('product-id').value;
        const isEditing = !!productId;

        // 1. Crear un objeto FormData
        const formData = new FormData();



        // 2. Añadir todos los campos de texto
        formData.append('name', document.getElementById('product-name').value);
        formData.append('description', document.getElementById('product-description').value);
        formData.append('price', document.getElementById('product-price').value);
        formData.append('stock', document.getElementById('product-stock').value);
        formData.append('category_id', document.getElementById('product-category').value);
        formData.append('seller_id', document.getElementById('product-seller').value);

        // 3. Añadir el archivo de imagen si se seleccionó uno
        const imageFile = document.getElementById('product-image-file').files[0];
        if (imageFile) {
            formData.append('image', imageFile);
        }

        const url = isEditing ? `${apiGatewayUrl}/productos/${productId}` : `${apiGatewayUrl}/productos`;
        const method = isEditing ? 'PUT' : 'POST';

        // 4. Modificar la cabecera y el cuerpo del fetch
        const fetchHeaders = new Headers();
        fetchHeaders.append('Authorization', `Bearer ${token}`);
        // NO AÑADIR 'Content-Type', el navegador lo hará automáticamente por nosotros con FormData

        try {
            const response = await fetch(url, { method, headers: fetchHeaders, body: formData });
            // ... (el resto del manejo de errores y respuesta es igual)
            const responseText = await response.text();
            if (!response.ok) throw new Error(`Error ${response.status}: ${responseText}`);
            alert(`Producto ${isEditing ? 'actualizado' : 'creado'} con éxito.`);
            productModal.hide();
            await loadProducts();
        } catch(error) {
            alert(`Error al guardar: ${error.message}`);
        }
    });

    // --- 6. Carga Inicial de Datos ---
    loadUsers();
    loadProducts();
});