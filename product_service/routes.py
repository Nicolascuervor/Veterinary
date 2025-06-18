from flask import Blueprint, request, jsonify, current_app
# FIX: Se importa el modelo con el nombre correcto: 'Producto'
from models import Producto, Category, Cart
from database import db
from werkzeug.utils import secure_filename
import os
import uuid

import stripe
from models import Order, OrderItem


routes = Blueprint('routes', __name__)

def allowed_file(filename):
    return '.' in filename and \
        filename.rsplit('.', 1)[1].lower() in current_app.config['ALLOWED_EXTENSIONS']

def product_to_dict(p):
    return {
        'id': p.id,
        'name': p.name,
        'description': p.description,
        'price': p.price,
        'stock': p.stock,
        'image': p.image,
        'created_at': p.created_at,
        'category_id': p.category_id,
        'seller_id': p.seller_id,
        'category_name': p.category.name if p.category else None
    }

@routes.route('/productos', methods=['GET'])
def get_productos():
    # FIX: Se usa la clase 'Producto' correcta en la consulta
    productos = db.session.query(Producto).options(db.joinedload(Producto.category)).all()
    return jsonify([product_to_dict(p) for p in productos])

@routes.route('/productos/<int:producto_id>', methods=['GET'])
def get_producto(producto_id):
    # FIX: Se usa la clase 'Producto' correcta en la consulta
    producto = db.session.query(Producto).options(db.joinedload(Producto.category)).get_or_404(producto_id)
    return jsonify(product_to_dict(producto))

@routes.route('/productos', methods=['POST'])
def create_product():
    try:
        seller_id = request.headers.get('X-User-Id')
        if not seller_id:
            return jsonify({'error': 'Cabecera X-User-Id no encontrada'}), 400

        data = request.form
        if not all(k in data for k in ('name', 'price', 'stock', 'category_id')):
            return jsonify({'error': 'Faltan campos obligatorios'}), 400

        image_path = None
        if 'image' in request.files:
            file = request.files['image']
            if file and file.filename and allowed_file(file.filename):
                filename = secure_filename(file.filename)
                unique_filename = str(uuid.uuid4()) + os.path.splitext(filename)[1]
                upload_folder = current_app.config['UPLOAD_FOLDER']
                os.makedirs(upload_folder, exist_ok=True)
                file.save(os.path.join(upload_folder, unique_filename))
                image_path = f'/static/uploads/products/{unique_filename}'

        # FIX: Se instancia la clase 'Producto' correcta
        new_product = Producto(
            name=data['name'],
            description=data.get('description', ''),
            price=float(data['price']),
            stock=int(data['stock']),
            image=image_path,
            category_id=int(data['category_id']),
            seller_id=int(seller_id)
        )
        db.session.add(new_product)
        db.session.commit()

        return jsonify(product_to_dict(new_product)), 201

    except Exception as e:
        db.session.rollback()
        return jsonify({'error': f'Error interno del servidor: {str(e)}'}), 500

@routes.route('/productos/<int:producto_id>', methods=['PUT'])
def update_product(producto_id):
    try:
        # FIX: Se usa la clase 'Producto' correcta en la consulta
        producto = Producto.query.get_or_404(producto_id)
        data = request.form

        producto.name = data.get('name', producto.name)
        producto.description = data.get('description', producto.description)
        producto.price = float(data.get('price', producto.price))
        producto.stock = int(data.get('stock', producto.stock))
        producto.category_id = int(data.get('category_id', producto.category_id))

        if 'image' in request.files:
            file = request.files['image']
            if file and file.filename and allowed_file(file.filename):
                filename = secure_filename(file.filename)
                unique_filename = str(uuid.uuid4()) + os.path.splitext(filename)[1]
                file.save(os.path.join(current_app.config['UPLOAD_FOLDER'], unique_filename))
                producto.image = f'/static/uploads/products/{unique_filename}'

        db.session.commit()
        return jsonify(product_to_dict(producto))

    except Exception as e:
        db.session.rollback()
        return jsonify({'error': f'Error interno del servidor: {str(e)}'}), 500




@routes.route('/categorias', methods=['POST'])
def crear_categoria():
    data = request.json
    categoria = Category(
        name=data['name'],
        description=data.get('description', '')
    )
    db.session.add(categoria)
    db.session.commit()
    return jsonify({'message': 'Categoría creada con éxito', 'id': categoria.id}), 201


#para probar postman ejemplo: 
#http://localhost:5001/categorias

#{
  #"name": "Accesorios",
 # "description": "Productos de tienda para mascotas"
#}

# Obtener todas las categorías
@routes.route('/categorias', methods=['GET'])
def listar_categorias():
    categorias = Category.query.all()
    return jsonify([
        {
            'id': c.id,
            'name': c.name,
            'description': c.description,
            'slug': c.slug,
            'total_productos': len(c.products)
        } for c in categorias
    ])

# Eliminar una categoría
@routes.route('/categorias/<int:categoria_id>', methods=['DELETE'])
def eliminar_categoria(categoria_id):
    categoria = Category.query.get(categoria_id)
    if not categoria:
        return jsonify({'error': 'Categoría no encontrada'}), 404

    db.session.delete(categoria)
    db.session.commit()
    return jsonify({'message': 'Categoría eliminada con éxito'})


# ===================================================================
# ================= RUTAS PARA EL CARRITO DE COMPRAS ================
# ===================================================================

@routes.route('/cart/add', methods=['POST'])
def add_to_cart():
    """
    Añade un producto al carrito de un usuario.
    Si el producto ya está en el carrito del usuario, actualiza la cantidad.
    De lo contrario, crea una nueva entrada en el carrito.
    """
    # 1. Obtener los datos del cuerpo de la petición
    data = request.get_json()
    user_id = data.get('user_id')
    product_id = data.get('product_id')
    quantity = data.get('quantity', 1)

    # 2. Validar que los datos necesarios fueron enviados
    if not user_id or not product_id:
        return jsonify({'message': 'Error: user_id y product_id son requeridos'}), 400

    # 3. Verificar que el producto existe y tiene stock suficiente
    producto = Producto.query.get(product_id)
    if not producto:
        return jsonify({'message': 'Error: Producto no encontrado'}), 404

    if producto.stock < quantity:
        return jsonify({'message': f'Error: Stock insuficiente para {producto.name}. Disponibles: {producto.stock}'}), 400

    # 4. CORRECCIÓN: Buscar si ya existe una entrada para este usuario Y este producto
    cart_item = Cart.query.filter_by(user_id=user_id, product_id=product_id).first()

    if cart_item:
        # Si ya existe, simplemente incrementamos la cantidad
        cart_item.quantity += quantity
    else:
        # Si no existe, creamos una nueva entrada en la tabla Cart
        cart_item = Cart(user_id=user_id, product_id=product_id, quantity=quantity)
        db.session.add(cart_item)

    # 5. Actualizar el stock del producto
    producto.stock -= quantity

    # 6. Confirmar todos los cambios en la base de datos
    db.session.commit()

    return jsonify({'message': 'Producto agregado al carrito exitosamente'}), 200




@routes.route('/cart/<int:user_id>', methods=['GET'])
def get_cart_contents(user_id):
    """
    Obtiene todos los productos en el carrito de un usuario específico.
    Para cada item, devuelve sus detalles y los del producto asociado.
    """
    # 1. Buscar todos los items en la tabla 'cart' que pertenezcan al usuario.
    #    Usamos 'options(db.joinedload(Cart.product))' para cargar los datos del producto
    #    en la misma consulta (más eficiente que hacer una consulta por cada item).
    cart_items = Cart.query.filter_by(user_id=user_id).options(db.joinedload(Cart.product)).all()

    # 2. Si no se encuentran items, devolver una lista vacía.
    #    Esto es importante para que el frontend sepa que el carrito está vacío.
    if not cart_items:
        return jsonify([]), 200

    # 3. Preparar la respuesta.
    #    Convertimos la lista de objetos 'CartItem' en una lista de diccionarios (JSON).
    #    Para cada item, incluimos los detalles completos del producto.
    result = []
    for item in cart_items:
        result.append({
            'cart_item_id': item.id,  # El ID del registro en la tabla 'cart'
            'quantity': item.quantity,
            # 'product' será un diccionario con toda la info del producto
            # gracias a la relación 'product' en el modelo Cart y a la carga eficiente.
            'product': product_to_dict(item.product)
        })

    # 4. Devolver la lista de items del carrito como JSON.
    return jsonify(result), 200


@routes.route('/cart/update_item', methods=['POST', 'OPTIONS'])
def update_cart_item():
    """
    Actualiza la cantidad de un item específico en el carrito.
    """
    if request.method == 'OPTIONS':
        # Manejo explícito de la petición OPTIONS para CORS
        return jsonify({'status': 'ok'}), 200

    data = request.get_json()
    cart_item_id = data.get('cart_item_id')
    new_quantity = data.get('quantity')

    if not cart_item_id or not isinstance(new_quantity, int) or new_quantity < 1:
        return jsonify({'message': 'Error: Se requiere un cart_item_id y una cantidad válida (mayor a 0)'}), 400

    cart_item = Cart.query.get(cart_item_id)
    if not cart_item:
        return jsonify({'message': 'Error: Item del carrito no encontrado'}), 404

    # Lógica para ajustar el stock del producto
    producto = Producto.query.get(cart_item.product_id)
    stock_difference = new_quantity - cart_item.quantity

    if producto.stock < stock_difference:
        return jsonify({'message': f'Error: Stock insuficiente. Disponibles: {producto.stock}'}), 400

    producto.stock -= stock_difference
    cart_item.quantity = new_quantity
    db.session.commit()

    return jsonify({'message': 'Cantidad actualizada correctamente'}), 200


@routes.route('/cart/remove_item', methods=['POST', 'OPTIONS'])
def remove_cart_item():
    """
    Elimina un item específico del carrito.
    """
    if request.method == 'OPTIONS':
        # Manejo explícito de la petición OPTIONS para CORS
        return jsonify({'status': 'ok'}), 200

    data = request.get_json()
    cart_item_id = data.get('cart_item_id')

    if not cart_item_id:
        return jsonify({'message': 'Error: Se requiere cart_item_id'}), 400

    cart_item = Cart.query.get(cart_item_id)
    if not cart_item:
        return jsonify({'message': 'Error: Item del carrito no encontrado'}), 404

    # Devolver el stock al producto antes de eliminar el item
    producto = Producto.query.get(cart_item.product_id)
    if producto:
        producto.stock += cart_item.quantity

    db.session.delete(cart_item)
    db.session.commit()

    return jsonify({'message': 'Item eliminado del carrito'}), 200

@routes.route('/checkout/create-session', methods=['POST'])
def create_checkout_session():
    """
    Crea una sesión de pago en Stripe con los items del carrito.
    """
    try:
        stripe.api_key = current_app.config['STRIPE_SECRET_KEY']

        # CAMBIO: Recibimos el objeto completo y extraemos las partes
        data = request.get_json()
        cart_items = data.get('cartItems')
        user_id = data.get('userId')

        if not cart_items or not user_id:
            return jsonify({'error': 'Faltan datos (cartItems o userId)'}), 400

        line_items = []
        for item in cart_items:
            # ... (la lógica para crear line_items no cambia) ...
            if item.get('product') and item['product'].get('price') and item['product']['price'] > 0:
                line_items.append({
                    'price_data': { 'currency': 'usd', 'product_data': { 'name': item['product']['name'] }, 'unit_amount': int(item['product']['price'] * 100) },
                    'quantity': item['quantity'],
                })

        if not line_items:
            return jsonify({'error': 'No hay productos con precio válido en el carrito.'}), 400

        # CAMBIO: Definimos la URL base del frontend explícitamente
        frontend_url = 'http://localhost:8080/Veterinary/microservices-example/veterinaria-frontend'
        success_url = f"{frontend_url}/pages/payment_success.html?session_id={{CHECKOUT_SESSION_ID}}"
        cancel_url = f"{frontend_url}/pages/payment_cancel.html"

        checkout_session = stripe.checkout.Session.create(
            payment_method_types=['card'],
            line_items=line_items,
            mode='payment',
            success_url=success_url,
            cancel_url=cancel_url,
            # CAMBIO: Usamos el userId recibido del frontend
            metadata={
                'user_id': user_id
            }
        )

        return jsonify({'id': checkout_session.id})

    except Exception as e:
        current_app.logger.error(f"Error creando sesión de Stripe: {e}")
        return jsonify(error=str(e)), 500




@routes.route('/stripe-webhook', methods=['POST'])
def stripe_webhook():
    """
    Escucha las notificaciones de Stripe para confirmar los pedidos.
    """
    payload = request.data
    sig_header = request.headers.get('Stripe-Signature')
    endpoint_secret = current_app.config['STRIPE_WEBHOOK_SECRET']
    event = None

    # 1. Verificar que el evento viene de Stripe y no es una falsificación
    try:
        event = stripe.Webhook.construct_event(
            payload, sig_header, endpoint_secret
        )
    except ValueError as e:
        # Payload inválido
        return 'Invalid payload', 400
    except stripe.error.SignatureVerificationError as e:
        # Firma inválida
        return 'Invalid signature', 400

    # 2. Manejar el evento 'checkout.session.completed'
    if event['type'] == 'checkout.session.completed':
        session = event['data']['object']
        user_id = session.get('metadata', {}).get('user_id')

        if user_id:
            print(f"✅ Pago exitoso para el usuario: {user_id}. Creando pedido...")
            # 3. Lógica para crear el pedido en la base de datos
            try:
                # Obtener los items del carrito para este usuario
                cart_items = Cart.query.filter_by(user_id=user_id).all()
                if not cart_items:
                    print(f"⚠️  El carrito para el usuario {user_id} ya estaba vacío.")
                    return 'OK', 200

                # Calcular el precio total
                total_price = sum(item.product.price * item.quantity for item in cart_items)

                # Crear el nuevo pedido (Order)
                new_order = Order(user_id=int(user_id), status='Pagado', total_price=total_price)
                db.session.add(new_order)
                db.session.flush() # Para obtener el ID del nuevo pedido

                # Mover los items del carrito a items del pedido (OrderItem)
                for item in cart_items:
                    order_item = OrderItem(
                        order_id=new_order.id,
                        product_id=item.product_id,
                        quantity=item.quantity,
                        price=item.product.price
                    )
                    db.session.add(order_item)

                # Vaciar el carrito
                Cart.query.filter_by(user_id=user_id).delete()

                db.session.commit()
                print(f"📦 Pedido {new_order.id} creado exitosamente.")

            except Exception as e:
                db.session.rollback()
                print(f"❌ Error al crear el pedido para el usuario {user_id}: {e}")
                return 'Error interno al procesar el pedido', 500

    return 'OK', 200



@routes.route('/orders/user/<int:user_id>', methods=['GET'])
def get_user_orders(user_id):
    """
    Obtiene el historial de pedidos para un usuario específico.
    Devuelve una lista de pedidos, y cada pedido contiene sus items y productos.
    """
    # 1. Consultar los pedidos del usuario, ordenados del más reciente al más antiguo.
    #    Usamos 'joinedload' para cargar eficientemente los 'order_items' y
    #    a su vez, los 'product' de cada item, todo en una sola consulta.
    orders = Order.query.filter_by(user_id=user_id) \
        .options(db.joinedload(Order.order_items).joinedload(OrderItem.product)) \
        .order_by(Order.order_date.desc()) \
        .all()

    if not orders:
        return jsonify([]), 200 # Si no hay pedidos, devolver una lista vacía.

    # 2. Convertir los pedidos y sus detalles a un formato JSON limpio.
    result = []
    for order in orders:
        order_data = {
            'id': order.id,
            'order_date': order.order_date.strftime('%d-%m-%Y %H:%M'), # Formatear fecha
            'status': order.status,
            'total_price': order.total_price,
            'order_items': []
        }
        for item in order.order_items:
            order_data['order_items'].append({
                'product_name': item.product.name,
                'quantity': item.quantity,
                'price': item.price,
                'image': item.product.image
            })
        result.append(order_data)

    return jsonify(result), 200