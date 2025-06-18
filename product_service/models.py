from database import db
from datetime import datetime
from sqlalchemy.orm import validates

class Category(db.Model):
    __tablename__ = 'category'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), unique=True, nullable=False)
    description = db.Column(db.Text)
    slug = db.Column(db.String(150), unique=True)

    def __init__(self, name, description):
        self.name = name
        self.description = description
        self.slug = self.generate_slug(name)

    def generate_slug(self, name):
        return name.lower().replace(" ", "-")

    def __repr__(self):
        return f"<Category {self.name}>"

class Producto(db.Model):
    __tablename__ = 'producto'

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(255), nullable=False)
    description = db.Column(db.Text)
    price = db.Column(db.Float)
    stock = db.Column(db.Integer)
    image = db.Column(db.String(2083))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    category_id = db.Column(db.Integer, db.ForeignKey('category.id'), nullable=False)
    category = db.relationship('Category', backref=db.backref('products', lazy=True))

    seller_id = db.Column(db.Integer, nullable=False)  # Se asume integración externa

    def __repr__(self):
        return f"<Producto {self.name}>"

class Order(db.Model):
    __tablename__ = 'order'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, nullable=False)
    total_price = db.Column(db.Float, nullable=False, default=0.0)
    order_date = db.Column(db.DateTime, default=datetime.utcnow)
    status = db.Column(db.String(50), default='Pending')

    def __repr__(self):
        return f"<Order {self.id} by user {self.user_id}>"

class OrderItem(db.Model):
    __tablename__ = 'order_item'

    id = db.Column(db.Integer, primary_key=True)
    order_id = db.Column(db.Integer, db.ForeignKey('order.id'), nullable=False)
    product_id = db.Column(db.Integer, db.ForeignKey('producto.id'), nullable=False)
    quantity = db.Column(db.Integer, default=1)
    price = db.Column(db.Float)

    order = db.relationship('Order', backref=db.backref('order_items', lazy=True))
    product = db.relationship('Producto', backref=db.backref('order_items', lazy=True))

    def __repr__(self):
        return f"<OrderItem {self.id} for Order {self.order_id}>"

class Cart(db.Model):
    __tablename__ = 'cart'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, nullable=False)
    product_id = db.Column(db.Integer, db.ForeignKey('producto.id'), nullable=False)
    quantity = db.Column(db.Integer, default=1)
    added_at = db.Column(db.DateTime, default=datetime.utcnow)

    product = db.relationship('Producto', backref=db.backref('cart_items', lazy=True))

    def __repr__(self):
        return f"<Cart {self.id} for user {self.user_id}>"
