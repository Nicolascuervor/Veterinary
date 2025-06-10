let carrito = [];
let montoTotalPagar = 0;
let cantidadTotalProductos = 0;

function actualizarContadorCarritoNav() {
    const contador = carrito.reduce((total, producto) => total + producto.cantidad, 0);
    document.getElementById('contador-carrito-nav').innerText = contador;
}

function cargarResumenPedido() {
    carrito = JSON.parse(localStorage.getItem('vetcareCarrito')) || [];
    montoTotalPagar = 0;
    cantidadTotalProductos = 0;

    if (carrito.length === 0) {
        document.getElementById('totalProductosResumen').innerText = '0';
        document.getElementById('montoTotalResumen').innerText = '$0 COP';
        // Opcional: deshabilitar el formulario de pago o redirigir si no hay nada que pagar
        alert("Tu carrito está vacío. Serás redirigido a la página de productos.");
        window.location.href = 'lista_productos.html'; // Asume que así se llama tu página de productos
        return;
    }

    carrito.forEach(producto => {
        cantidadTotalProductos += producto.cantidad;
        montoTotalPagar += producto.precio * producto.cantidad;
    });

    document.getElementById('totalProductosResumen').innerText = cantidadTotalProductos;
    document.getElementById('montoTotalResumen').innerText = `$${montoTotalPagar.toLocaleString()} COP`;
    actualizarContadorCarritoNav();
}

function manejarSeleccionMetodoPago() {
    const detallesTarjetaDiv = document.getElementById('detalles-tarjeta');
    const mensajeEfectivoDiv = document.getElementById('mensaje-efectivo');
    const inputsTarjeta = detallesTarjetaDiv.querySelectorAll('input');

    if (document.getElementById('pagoTarjeta').checked) {
        detallesTarjetaDiv.style.display = 'block';
        mensajeEfectivoDiv.style.display = 'none';
        inputsTarjeta.forEach(input => input.required = true);
    } else if (document.getElementById('pagoEfectivo').checked) {
        detallesTarjetaDiv.style.display = 'none';
        mensajeEfectivoDiv.style.display = 'block';
        inputsTarjeta.forEach(input => input.required = false); // No son requeridos si se paga en efectivo
    }
}

// Event Listeners para los radio buttons de método de pago
document.querySelectorAll('input[name="metodoPago"]').forEach(radio => {
    radio.addEventListener('change', manejarSeleccionMetodoPago);
});

// Simulación de procesamiento de pago
const formularioPago = document.getElementById('formularioPago');
formularioPago.addEventListener('submit', function(event) {
    event.preventDefault(); // Evitar el envío real del formulario
    event.stopPropagation();

    // Aplicar validación de Bootstrap
    formularioPago.classList.add('was-validated');

    if (!formularioPago.checkValidity()) {
        alert("Por favor, corrige los errores en el formulario.");
        return;
    }

    const metodoSeleccionado = document.querySelector('input[name="metodoPago"]:checked').value;
    let mensajeExito = "";

    if (metodoSeleccionado === 'tarjeta') {
        const nombreTarjeta = document.getElementById('nombreTarjeta').value;
        const numeroTarjeta = document.getElementById('numeroTarjeta').value;
        // Aquí iría una validación más robusta o integración con pasarela de pago
        mensajeExito = `¡Pago con tarjeta procesado exitosamente!\nGracias por tu compra, ${nombreTarjeta}.\nTotal: $${montoTotalPagar.toLocaleString()} COP`;
    } else if (metodoSeleccionado === 'efectivo') {
        mensajeExito = `Pedido para pago en efectivo confirmado.\nNos pondremos en contacto contigo pronto.\nTotal: $${montoTotalPagar.toLocaleString()} COP`;
    }

    // Simulación de éxito
    alert(mensajeExito);

    // Limpiar carrito y redirigir (simulación)
    localStorage.removeItem('vetcareCarrito');
    carrito = [];
    actualizarContadorCarritoNav();
    cargarResumenPedido(); // Esto debería llevar a la redirección por carrito vacío

    // Idealmente, redirigir a una página de "Gracias por tu compra"
    // window.location.href = 'gracias.html';
    formularioPago.classList.remove('was-validated');
    formularioPago.reset(); // Limpiar el formulario
    manejarSeleccionMetodoPago(); // Restablecer la visibilidad de los campos de tarjeta
});

// Carga inicial
window.onload = () => {
    cargarResumenPedido();
    manejarSeleccionMetodoPago(); // Configurar visibilidad inicial de campos de tarjeta
};
