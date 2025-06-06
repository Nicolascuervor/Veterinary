// Datos de ejemplo (simularía lo que obtendrías de una base de datos)
const catalogoProductos = [
    {
        id: 1,
        nombre: 'Thornton (Collar antipulgas para Perros)',
        precio: 19900,
        imagen: 'https://geant.vteximg.com.br/arquivos/ids/334507-1000-1000/632066.jpg?v=637714163326900000',
        categoria: 'accesorios'
    },
    {
        id: 2,
        nombre: 'Comida Premium para Gatos Adultos 1kg',
        precio: 25900,
        imagen: 'https://www. میر.com.co/wp-content/uploads/2022/03/Mirringo-Gatos-Adultos.jpg',
        categoria: 'snacks' // O 'alimentos' si tienes esa categoría
    },
    {
        id: 3,
        nombre: 'Juguete Hueso de Goma para Perro',
        precio: 12500,
        imagen: 'https://http2.mlstatic.com/D_NQ_NP_779720-MCO47375374836_092021-O.webp',
        categoria: 'juguetes'
    },
    {
        id: 4,
        nombre: 'Shampoo Medicado para Piel Sensible',
        precio: 35000,
        imagen: 'https://laika.com.co/cdn-cgi/image/onerror=redirect,format=auto,fit=scale-down,width=600,quality=80/https://laikapp.s3.amazonaws.com/images_products/COPROVET_SHAMPOO_MEDICADO_PARA_PERRO_Y_GATO_250_ML_1698683663_0_1200x1200.jpg',
        categoria: 'medicamentos'
    },
    {
        id: 5,
        nombre: 'Snacks Dentales para Perro Pequeño',
        precio: 15000,
        imagen: 'https://cdn.shopify.com/s/files/1/0560/7188/7952/products/PedigreeDentastixRazasPequenas28und.jpg?v=1670008870',
        categoria: 'snacks'
    },
    {
        id: 6,
        nombre: 'Rascador para Gatos con Pelota',
        precio: 45000,
        imagen: 'https://http2.mlstatic.com/D_NQ_NP_918239-MCO53567961854_012023-O.webp',
        categoria: 'accesorios'
    }
];

let carrito = JSON.parse(localStorage.getItem('vetcareCarrito')) || [];
let categoriaActual = 'todos';
let terminoBusquedaActual = '';

function guardarCarritoEnStorage() {
    localStorage.setItem('vetcareCarrito', JSON.stringify(carrito));
    actualizarContadorCarrito();
}

function actualizarContadorCarrito() {
    const contador = carrito.reduce((total, producto) => total + producto.cantidad, 0);
    document.getElementById('contador-carrito').innerText = contador;
}


// Función para renderizar los productos en tarjetas
function renderizarProductos(productosARenderizar) {
    const listaProductosBody = document.getElementById('lista-productos-body');
    listaProductosBody.innerHTML = ''; // Limpiar antes de renderizar

    if (productosARenderizar.length === 0) {
        listaProductosBody.innerHTML = '<p class="text-center text-muted col-12">No se encontraron productos que coincidan con tu búsqueda o filtro.</p>';
        return;
    }

    productosARenderizar.forEach(producto => {
        const productoCard = document.createElement('div');
        productoCard.classList.add('col-lg-3', 'col-md-4', 'col-sm-6'); // Columnas responsivas Bootstrap

        productoCard.innerHTML = `
            <div class="producto-card">
                <img src="${producto.imagen}" class="card-img-top" alt="${producto.nombre}">
                <div class="card-body text-center">
                    <h5 class="card-title">${producto.nombre}</h5>
                    <p class="card-text">$${producto.precio.toLocaleString()} COP</p>
                    <button class="btn btn-agregar-carrito" onclick="agregarAlCarrito(${producto.id})">
                        <i class="fas fa-cart-plus"></i> Agregar al Carrito
                    </button>
                </div>
            </div>
        `;
        listaProductosBody.appendChild(productoCard);
    });
}

// Función para agregar un producto al carrito (similar a la de la página del carrito)
function agregarAlCarrito(productoId) {
    const productoAAgregar = catalogoProductos.find(p => p.id === productoId);
    if (!productoAAgregar) return;

    const productoEnCarrito = carrito.find(p => p.id === productoId);
    if (productoEnCarrito) {
        productoEnCarrito.cantidad++;
    } else {
        carrito.push({ ...productoAAgregar, cantidad: 1 });
    }
    guardarCarritoEnStorage();
    // Opcional: Mostrar una notificación de que se agregó el producto
    alert(`"${productoAAgregar.nombre}" agregado al carrito.`);
}

// Función para filtrar y buscar productos
function aplicarFiltrosYBusqueda() {
    let productosFiltrados = catalogoProductos;

    // Filtrar por categoría
    if (categoriaActual !== 'todos') {
        productosFiltrados = productosFiltrados.filter(p => p.categoria === categoriaActual);
    }

    // Filtrar por término de búsqueda
    if (terminoBusquedaActual) {
        productosFiltrados = productosFiltrados.filter(p =>
            p.nombre.toLowerCase().includes(terminoBusquedaActual.toLowerCase())
        );
    }
    renderizarProductos(productosFiltrados);
}


// Función para manejar el clic en los botones de categoría
function filtrarProductos(categoria) {
    categoriaActual = categoria;
    // Actualizar clase 'active' en botones de categoría
    document.querySelectorAll('.categorias-section .btn-categoria').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active'); // event.target se refiere al botón que fue clickeado

    aplicarFiltrosYBusqueda();
}

// Función para manejar la búsqueda desde la barra
function buscarProductos() {
    terminoBusquedaActual = document.getElementById('barraBusqueda').value;
    aplicarFiltrosYBusqueda();
}

// Añadir listener para búsqueda al presionar Enter
document.getElementById('barraBusqueda').addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
        buscarProductos();
    }
});


// Carga inicial de productos y contador del carrito
window.onload = () => {
    aplicarFiltrosYBusqueda(); // Renderiza todos los productos inicialmente
    actualizarContadorCarrito();
};
