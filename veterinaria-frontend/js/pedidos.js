document.addEventListener('DOMContentLoaded', () => {
    // --- CONFIGURACIÓN ---
    const userId = localStorage.getItem('id');
    const token = localStorage.getItem('token');
    const apiBase = 'http://127.0.0.1:5001';

    // --- ELEMENTOS DEL DOM ---
    const ordersContainer = document.getElementById('orders-accordion-container');
    const loadingIndicator = document.getElementById('loading-indicator');
    const noOrdersMessage = document.getElementById('no-orders-message');

    // --- VERIFICACIÓN ---
    if (!userId || !token) {
        alert("Debes iniciar sesión para ver tu historial de pedidos.");
        window.location.href = 'login.html';
        return;
    }

    // --- LÓGICA PRINCIPAL ---
    const loadOrders = async () => {
        loadingIndicator.style.display = 'block';
        ordersContainer.style.display = 'none';
        noOrdersMessage.style.display = 'none';

        try {
            const response = await fetch(`${apiBase}/orders/user/${userId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) throw new Error('No se pudo cargar el historial de pedidos.');

            const orders = await response.json();
            renderOrders(orders);

        } catch (error) {
            console.error("Error al cargar pedidos:", error);
            ordersContainer.innerHTML = `<div class="alert alert-danger">Error al cargar el historial.</div>`;
        } finally {
            loadingIndicator.style.display = 'none';
        }
    };

    const renderOrders = (orders) => {
        if (orders.length === 0) {
            noOrdersMessage.style.display = 'block';
            return;
        }

        ordersContainer.style.display = 'block';
        ordersContainer.innerHTML = ''; // Limpiar

        orders.forEach(order => {
            const orderStatusClass = order.status === 'Pagado' ? 'success' : 'warning';
            const orderHtml = `
                <div class="accordion-item">
                    <h2 class="accordion-header" id="heading-${order.id}">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse-${order.id}">
                            <div class="d-flex justify-content-between w-100 pe-3">
                                <span><strong>Pedido #${order.id}</strong></span>
                                <span class="text-muted d-none d-md-inline">Fecha: ${order.order_date}</span>
                                <span class="badge bg-${orderStatusClass}">${order.status}</span>
                                <span class="fw-bold">$${order.total_price.toFixed(2)}</span>
                            </div>
                        </button>
                    </h2>
                    <div id="collapse-${order.id}" class="accordion-collapse collapse" data-bs-parent="#orders-accordion-container">
                        <div class="accordion-body">
                            <h6>Detalles del Pedido:</h6>
                            <ul class="list-group list-group-flush">
                                ${order.order_items.map(item => `
                                    <li class="list-group-item d-flex justify-content-between align-items-center">
                                        <div>
                                            <img src="${apiBase}${item.image}" class="img-thumbnail me-3" style="width: 60px;" alt="${item.product_name}">
                                            <span>${item.product_name} (x${item.quantity})</span>
                                        </div>
                                        <span>$${(item.price * item.quantity).toFixed(2)}</span>
                                    </li>
                                `).join('')}
                            </ul>
                        </div>
                    </div>
                </div>
            `;
            ordersContainer.innerHTML += orderHtml;
        });
    };

    // --- INICIO ---
    loadOrders();
});