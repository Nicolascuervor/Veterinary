from flask import Blueprint, request, jsonify
from models import Producto, Category
from database import db

routes = Blueprint('routes', __name__)

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

@routes.route('/productos', methods=['POST'])
def crear_producto():
    data = request.json
    producto = Producto(
        name=data['name'],
        description=data['description'],
        price=data['price'],
        stock=data['stock'],
        image=data['image'],
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