from flask import Blueprint, request, jsonify , request, render_template
from models import Producto, Category
from database import db


routes = Blueprint('routes', __name__)

@routes.route('/')
def index():
    return render_template('productos.html')


@routes.route('/panel')
def panel_admin():
    return render_template('admin_panel_complete.html')  


@routes.route('/productos', methods=['GET'])
def get_productos():
    productos = Producto.query.all()
    return jsonify([
        {
            'id': p.id,
            'name': p.name,
            'description': p.description,
            'price': p.price,
            'stock': p.stock,
            'image': p.image,
            'created_at': p.created_at,
            'category_id': p.category_id,
            'seller_id': p.seller_id
        } for p in productos
    ])
#ejemplo postman: 
#http://localhost:5001/productos 


@routes.route('/productos', methods=['POST'])
def crear_producto():
    data = request.json
    producto = Producto(
        name=data['name'],
        description=data['description'],
        price=data['price'],
        stock=data['stock'],
        image=data.get('image') or data.get('image_url', ''),
        category_id=data['category_id'],
        seller_id=data['seller_id']
    )
    db.session.add(producto)
    db.session.commit()
    return jsonify({'message': 'Producto creado con éxito'}), 201

#ejemplo postman.
#http://localhost:5001/productos 
'''
{
  "name": "Collar para perro",
  "description": "Collar ajustable para perro de raza mediana.",
  "price": 12.5,
  "stock": 100,
  "image": "https://example.com/collar.jpg",
  "category_id": 1,
  "seller_id": 101
}
'''


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

# Actualizar un producto
@routes.route('/productos/<int:producto_id>', methods=['PUT'])
def actualizar_producto(producto_id):
    producto = Producto.query.get(producto_id)
    if not producto:
        return jsonify({'error': 'Producto no encontrado'}), 404

    data = request.json
    producto.name = data.get('name', producto.name)
    producto.description = data.get('description', producto.description)
    producto.price = data.get('price', producto.price)
    producto.stock = data.get('stock', producto.stock)
    producto.image = data.get('image', producto.image)

    db.session.commit()
    return jsonify({'message': 'Producto actualizado con éxito'})

# Eliminar un producto
@routes.route('/productos/<int:producto_id>', methods=['DELETE'])
def eliminar_producto(producto_id):
    producto = Producto.query.get(producto_id)
    if not producto:
        return jsonify({'error': 'Producto no encontrado'}), 404

    db.session.delete(producto)
    db.session.commit()
    return jsonify({'message': 'Producto eliminado con éxito'})

