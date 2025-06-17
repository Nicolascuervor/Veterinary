from flask import Blueprint, request, jsonify, current_app
# FIX: Se importa el modelo con el nombre correcto: 'Producto'
from models import Producto, Category
from database import db
from werkzeug.utils import secure_filename
import os
import uuid

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



