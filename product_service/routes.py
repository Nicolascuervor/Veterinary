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
