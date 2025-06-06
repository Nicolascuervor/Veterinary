const productosDisponibles = [
    {
        id: 1,
        nombre: 'Thornton (Collar antipulgas para Perros)',
        precio: 19900,
        imagen: 'https://geant.vteximg.com.br/arquivos/ids/334507-1000-1000/632066.jpg?v=637714163326900000' // Cambiado a una imagen real
    },
    {
        id: 2,
        nombre: 'Comida Premium para Gatos Adultos 1kg',
        precio: 25900,
        imagen: 'https://www. میر.com.co/wp-content/uploads/2022/03/Mirringo-Gatos-Adultos.jpg' // Cambiado a una imagen real
    },
    {
        id: 3,
        nombre: 'Juguete Hueso de Goma para Perro',
        precio: 12500,
        imagen: 'https://http2.mlstatic.com/D_NQ_NP_779720-MCO47375374836_092021-O.webp' // Cambiado a una imagen real
    }
];

let carrito = []; // Este será nuestro carrito de compras

// Función para renderizar el carrito en la tabla HTML
function renderizarCarrito() {
    const tbody = document.getElementById('carrito-body');
    const tfoot = document.querySelector('table tfoot'); // Seleccionamos el tfoot para los totales
    tbody.innerHTML = ''; // Limpiar la tabla antes de renderizar
    if (tfoot) tfoot.innerHTML = ''; // Limpiar también el pie de tabla si existe

    let totalCantidadGeneral = 0;
    let totalPrecioGeneral = 0;

    if (carrito.length === 0) {
        const filaVacia = document.createElement('tr');
        filaVacia.innerHTML = `<td colspan="6" class="text-center p-4">Tu carrito está vacío. <a href="#">¡Empieza a comprar!</a></td>`;
        tbody.appendChild(filaVacia);
        document.getElementById('totalCantidadGeneral').innerText = '0';
        document.getElementById('totalPrecioGeneral').innerText = '$0 COP';
        return;
    }

    carrito.forEach(producto => {
        const subtotalProducto = producto.precio * producto.cantidad;
        totalCantidadGeneral += producto.cantidad;
        totalPrecioGeneral += subtotalProducto;

        const fila = document.createElement('tr');
        fila.setAttribute('id', `fila-producto-${producto.id}`);
        fila.innerHTML = `
            <td>
                <button class="btn-eliminar" onclick="eliminarDelCarrito(${producto.id})" title="Eliminar producto">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </td>
            <td><img src="${producto.imagen}" alt="${producto.nombre}" style="width: 70px; height: 70px; object-fit: cover; border-radius: 5px;"></td>
            <td class="text-start">${producto.nombre}</td>
            <td>
                <div class="d-flex justify-content-center align-items-center">
                    <button class="btn-cantidad" onclick="cambiarCantidadEnCarrito(${producto.id}, -1)" title="Disminuir cantidad">
                        <i class="fas fa-minus-circle"></i>
                    </button>
                    <input type="number" value="${producto.cantidad}" min="1" class="input-cantidad" readonly id="cantidad-${producto.id}">
                    <button class="btn-cantidad" onclick="cambiarCantidadEnCarrito(${producto.id}, 1)" title="Aumentar cantidad">
                        <i class="fas fa-plus-circle"></i>
                    </button>
                </div>
            </td>
            <td>$${producto.precio.toLocaleString()} COP</td>
            <td id="subtotal-${producto.id}">$${subtotalProducto.toLocaleString()} COP</td>
        `;
        tbody.appendChild(fila);
    });

    document.getElementById('totalCantidadGeneral').innerText = totalCantidadGeneral;
    document.getElementById('totalPrecioGeneral').innerText = `$${totalPrecioGeneral.toLocaleString()} COP`;

    // Crear y añadir fila de totales al tfoot
    const filaTotalTabla = document.createElement('tr');
    filaTotalTabla.classList.add('table-light', 'fw-bold'); // Clases de Bootstrap para destacar
    filaTotalTabla.innerHTML = `
        <td colspan="3" class="text-end">TOTALES:</td>
        <td class="text-center">${totalCantidadGeneral}</td>
        <td></td>
        <td class="text-center">$${totalPrecioGeneral.toLocaleString()} COP</td>
    `;
    if (tfoot) { // Asegurarse de que tfoot exista
        tfoot.appendChild(filaTotalTabla);
    } else { // Si tfoot no existe (por ejemplo, si se eliminó antes), crear uno nuevo y añadirlo
         const newTfoot = document.createElement('tfoot');
         newTfoot.appendChild(filaTotalTabla);
         document.querySelector('table').appendChild(newTfoot);
    }


}

// Función para agregar un producto al carrito
function agregarAlCarrito(productoId) {
    const productoAAgregar = productosDisponibles.find(p => p.id === productoId);
    if (!productoAAgregar) {
        console.error("Producto no encontrado");
        return;
    }

    const productoEnCarrito = carrito.find(p => p.id === productoId);
    if (productoEnCarrito) {
        productoEnCarrito.cantidad++;
    } else {
        // Agregamos una copia del producto con la cantidad inicial
        carrito.push({ ...productoAAgregar, cantidad: 1 });
    }
    console.log("Carrito después de agregar:", carrito);
    renderizarCarrito();
}

// Función para eliminar un producto del carrito
function eliminarDelCarrito(productoId) {
    const index = carrito.findIndex(p => p.id === productoId);
    if (index !== -1) {
        carrito.splice(index, 1);
    }
    console.log("Carrito después de eliminar:", carrito);
    renderizarCarrito();
}

// Función para cambiar la cantidad de un producto en el carrito
function cambiarCantidadEnCarrito(productoId, delta) {
    const producto = carrito.find(p => p.id === productoId);
    if (producto) {
        producto.cantidad += delta;
        if (producto.cantidad <= 0) {
            eliminarDelCarrito(productoId);
        } else {
            renderizarCarrito();
        }
    }
    console.log("Carrito después de cambiar cantidad:", carrito);
}

function procederAlPago() {
    if (carrito.length === 0) {
        alert("Tu carrito está vacío. Agrega algunos productos antes de proceder al pago.");
        return;
    }
    alert(`Procediendo al pago con un total de: $${document.getElementById('totalPrecioGeneral').innerText.replace('$','').replace(' COP','')}`);
    console.log("Redirigiendo a la página de métodos de pago...");
    // window.location.href = 'pagina_de_pago.html'; // Descomentar para redirigir
}

 //para prueba
window.onload = () => {
    agregarAlCarrito(1);
    agregarAlCarrito(2);
    agregarAlCarrito(2);
    agregarAlCarrito(3);
    console.log("Carrito inicializado:", carrito);
};
