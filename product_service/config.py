import os

class Config:
    SQLALCHEMY_DATABASE_URI = 'mysql+pymysql://root:3712@127.0.0.2:3307/veterinaria'
    SQLALCHEMY_TRACK_MODIFICATIONS = False

    UPLOAD_FOLDER = os.path.join(os.path.abspath(os.path.dirname(__file__)), 'static/uploads/products')
    ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}