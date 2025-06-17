document.addEventListener('DOMContentLoaded', () => {
    // ===================================================================
    // 1. CONFIGURACIÓN INICIAL Y VARIABLES
    // ===================================================================
    const userId = localStorage.getItem('id');
    const token = localStorage.getItem('token');
    const apiBase = 'http://127.0.0.1:5001'; // URL del microservicio de productos/carrito

    // --- Selección de Elementos del DOM ---
    const cartContainer = document.getElementById('cart-items-container');
    const emptyCartMessage = document.getElementById('empty-cart-message');
    const loadingIndicator = document.getElementById('loading-indicator');
    const subtotalEl = document.getElementById('cart-subtotal');
    const totalEl = document.getElementById('cart-total');
    const checkoutBtn = document.getElementById('checkout-btn');

    // --- Verificación de Autenticación ---
    if (!userId || !token) {
        alert("Debes iniciar sesión para ver tu carrito.");
        window.location.href = 'login.html';
        return;
    }


    checkoutBtn.addEventListener('click', () => {

        window.location.href = 'pagos.html';
    });






    const loadCart = async () => {
        showLoading(true);
        try {
            const response = await fetch(`${apiBase}/cart/${userId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                throw new Error(`Error del servidor: ${response.status}`);
            }

            const cartItems = await response.json();
            renderCart(cartItems);

        } catch (error) {
            console.error("Error al cargar el carrito:", error);
            showErrorState();
        } finally {
            showLoading(false);
        }
    };

    /**
     * Renderiza los items del carrito en la página y calcula los totales.
     * @param {Array} items - La lista de items del carrito obtenida de la API.
     */
    const renderCart = (items) => {
        cartContainer.innerHTML = ''; // Limpiar el contenedor antes de renderizar

        if (items.length === 0) {
            emptyCartMessage.style.display = 'block';
            checkoutBtn.disabled = true;
            updateTotals([]);
            return;
        }

        emptyCartMessage.style.display = 'none';

        items.forEach(item => {
            const itemHtml = `
                <div class="row mb-4 cart-item" data-item-id="${item.cart_item_id}">
                    <div class="col-md-3 col-4">
                        <img src="${apiBase}${item.product.image}" class="img-fluid rounded" alt="${item.product.name}" onerror="this.onerror=null; this.src='https://via.placeholder.com/150?text=Sin+Imagen';">
                    </div>
                    <div class="col-md-9 col-8">
                        <h5>${item.product.name}</h5>
                        <p class="text-muted small d-none d-md-block">${item.product.description || ''}</p>
                        <div class="d-flex justify-content-between align-items-center mt-3">
                            <div class="quantity-control d-flex align-items-center">
                                <button class="btn btn-sm btn-outline-secondary" data-action="update-quantity" data-item-id="${item.cart_item_id}" data-new-quantity="${item.quantity - 1}">-</button>
                                <span class="mx-3 fw-bold">${item.quantity}</span>
                                <button class="btn btn-sm btn-outline-secondary" data-action="update-quantity" data-item-id="${item.cart_item_id}" data-new-quantity="${item.quantity + 1}">+</button>
                            </div>
                            <p class="fw-bold mb-0">$${(item.product.price * item.quantity).toFixed(2)}</p>
                            <button class="btn btn-sm btn-outline-danger" data-action="remove-item" data-item-id="${item.cart_item_id}" title="Eliminar producto">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
                <hr>`;
            cartContainer.innerHTML += itemHtml;
        });

        updateTotals(items);
        checkoutBtn.disabled = false;
    };

    // ===================================================================
    // 3. MANEJO DE EVENTOS (Delegación de Eventos)
    // ===================================================================

    // Un único listener en el contenedor principal para manejar todos los clics en los botones.
    cartContainer.addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return; // Si el clic no fue en un botón, no hacemos nada.

        const { action, itemId } = button.dataset;

        if (action === 'update-quantity') {
            const newQuantity = parseInt(button.dataset.newQuantity);
            handleUpdateQuantity(itemId, newQuantity);
        } else if (action === 'remove-item') {
            handleRemoveItem(itemId);
        }
    });

    // ===================================================================
    // 4. FUNCIONES PARA COMUNICARSE CON LA API
    // ===================================================================

    /**
     * Llama a la API para actualizar la cantidad de un producto.
     */
    async function handleUpdateQuantity(itemId, newQuantity) {
        if (newQuantity < 1) {
            // Si la cantidad llega a 0, se elimina el producto.
            handleRemoveItem(itemId);
            return;
        }
        try {
            const response = await fetch(`${apiBase}/cart/update_item`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify({ cart_item_id: parseInt(itemId), quantity: newQuantity })
            });
            if (!response.ok) throw new Error('Error al actualizar la cantidad');

            loadCart(); // Recargar todo el carrito para reflejar los cambios.
        } catch (error) {
            console.error('Error en handleUpdateQuantity:', error);
            alert('No se pudo actualizar la cantidad del producto.');
        }
    }

    /**
     * Llama a la API para eliminar un producto del carrito.
     */
    async function handleRemoveItem(itemId) {
        if (confirm('¿Estás seguro de que quieres eliminar este producto del carrito?')) {
            try {
                const response = await fetch(`${apiBase}/cart/remove_item`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify({ cart_item_id: parseInt(itemId) })
                });
                if (!response.ok) throw new Error('Error al eliminar el producto');

                loadCart(); // Recargar el carrito para reflejar los cambios.
            } catch (error) {
                console.error('Error en handleRemoveItem:', error);
                alert('No se pudo eliminar el producto del carrito.');
            }
        }
    }

    // ===================================================================
    // 5. FUNCIONES DE UTILIDAD
    // ===================================================================

    /**
     * Actualiza el resumen de la compra (subtotal y total).
     */
    const updateTotals = (items) => {
        const subtotal = items.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
        subtotalEl.textContent = `$${subtotal.toFixed(2)}`;
        totalEl.textContent = `$${subtotal.toFixed(2)}`; // Por ahora el total es igual al subtotal.
    };

    /**
     * Muestra u oculta el indicador de carga.
     */
    const showLoading = (isLoading) => {
        loadingIndicator.style.display = isLoading ? 'block' : 'none';
        cartContainer.style.display = isLoading ? 'none' : 'block';
        if (isLoading) {
            emptyCartMessage.style.display = 'none';
        }
    };

    /**
     * Muestra un mensaje de error si la carga inicial falla.
     */
    const showErrorState = () => {
        cartContainer.innerHTML = `<div class="text-center p-5"><p class="text-danger">No se pudo cargar tu carrito. Por favor, inténtalo de nuevo más tarde.</p></div>`;
    };

    // ===================================================================
    // 6. EJECUCIÓN INICIAL
    // ===================================================================
    loadCart();
});