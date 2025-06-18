document.addEventListener('DOMContentLoaded', () => {
    // --- CONFIGURACIÓN ---
    const userId = localStorage.getItem('id');
    const token = localStorage.getItem('token');
    const productApiUrl = 'http://127.0.0.1:5001';

    // ¡IMPORTANTE! Reemplaza esto con tu Clave Publicable de Stripe
    const stripePublicKey = 'pk_test_51Rb6uq4YKcv9a0spdKrl96lSVoxZAbrcfzHOFvSA4ZlGEK7eEhlajPDOXl9fd84pUdu5psIvGaRfmhMVmLIzKZBc00IDgkmPd1'; // Pega tu clave aquí
    const stripe = Stripe(stripePublicKey);

    // --- ELEMENTOS DEL DOM ---
    const summaryContainer = document.getElementById('order-summary-container');
    const checkoutButton = document.getElementById('checkout-button');
    const buttonText = document.getElementById('button-text');
    const buttonSpinner = document.getElementById('button-spinner');

    let currentCartItems = []; // Almacenará los items del carrito

    // --- VERIFICACIÓN ---
    if (!userId || !token) {
        alert("Debes iniciar sesión para proceder al pago.");
        window.location.href = 'login.html';
        return;
    }

    // --- LÓGICA PRINCIPAL ---

    /**
     * Carga el resumen del carrito desde nuestro backend.
     */
    const loadOrderSummary = async () => {
        try {
            const response = await fetch(`${productApiUrl}/cart/${userId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) throw new Error('No se pudo cargar el resumen del carrito.');

            currentCartItems = await response.json();

            if (currentCartItems.length === 0) {
                summaryContainer.innerHTML = '<div class="card-body text-center"><p class="text-danger">Tu carrito está vacío.</p></div>';
                checkoutButton.disabled = true;
                return;
            }

            renderSummary(currentCartItems);
            checkoutButton.disabled = false;

        } catch (error) {
            console.error("Error cargando resumen:", error);
            summaryContainer.innerHTML = '<div class="card-body text-center"><p class="text-danger">Error al cargar el resumen.</p></div>';
        }
    };

    /**
     * Renderiza el resumen del pedido en el DOM.
     */
    const renderSummary = (items) => {
        const total = items.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
        let itemsHtml = items.map(item => `
            <li class="list-group-item d-flex justify-content-between lh-sm">
                <div>
                    <h6 class="my-0">${item.product.name}</h6>
                    <small class="text-muted">Cantidad: ${item.quantity}</small>
                </div>
                <span class="text-muted">$${(item.product.price * item.quantity).toFixed(2)}</span>
            </li>
        `).join('');

        summaryContainer.innerHTML = `
            <ul class="list-group list-group-flush">
                ${itemsHtml}
                <li class="list-group-item d-flex justify-content-between bg-light">
                    <span class="fw-bold">Total (USD)</span>
                    <strong>$${total.toFixed(2)}</strong>
                </li>
            </ul>
        `;
        buttonText.textContent = `Pagar $${total.toFixed(2)}`;
    };

    /**
     * Maneja el clic en el botón de checkout.
     */
    checkoutButton.addEventListener('click', async () => {
        setLoadingState(true);

        try {
            // 1. Llama a nuestro backend para crear la sesión de Stripe
            const response = await fetch(`${productApiUrl}/checkout/create-session`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                // CAMBIO: Enviamos un objeto que contiene tanto los items como el userId
                body: JSON.stringify({
                    cartItems: currentCartItems,
                    userId: parseInt(userId)
                })
            });

            if (!response.ok) throw new Error('No se pudo iniciar la sesión de pago.');

            const session = await response.json();

            // 2. Redirige al usuario a la página de pago de Stripe
            const result = await stripe.redirectToCheckout({
                sessionId: session.id
            });

            if (result.error) {
                // Si hay un error en la redirección (poco común), lo mostramos.
                alert(result.error.message);
                setLoadingState(false);
            }
        } catch (error) {
            console.error("Error en el proceso de checkout:", error);
            alert("Hubo un error al intentar procesar el pago. Por favor, inténtalo de nuevo.");
            setLoadingState(false);
        }
    });

    const setLoadingState = (isLoading) => {
        checkoutButton.disabled = isLoading;
        buttonText.style.display = isLoading ? 'none' : 'inline';
        buttonSpinner.style.display = isLoading ? 'inline-block' : 'none';
    };

    // --- INICIO ---
    loadOrderSummary();
});