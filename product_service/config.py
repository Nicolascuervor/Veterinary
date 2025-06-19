import os
from dotenv import load_dotenv

# Carga las variables de un archivo .env si existe (para desarrollo local)
load_dotenv()

class Config:
    SECRET_KEY = os.environ.get('FLASK_SECRET_KEY') or 'una-clave-secreta-muy-dificil'

    # --- Configuración de la Base de Datos ---
    # Lee la variable de entorno 'DATABASE_URL'. Si no existe, usa la cadena de localhost.
    SQLALCHEMY_DATABASE_URI = os.environ.get(
        'DATABASE_URL',
        'mysql+pymysql://root:3712@localhost:3307/veterinaria'
    )
    SQLALCHEMY_TRACK_MODIFICATIONS = False

    # --- Configuración de Stripe ---
    STRIPE_PUBLIC_KEY = os.environ.get('STRIPE_PUBLIC_KEY')
    STRIPE_SECRET_KEY = os.environ.get('STRIPE_SECRET_KEY')
    STRIPE_WEBHOOK_SECRET = os.environ.get('STRIPE_WEBHOOK_SECRET')

    # --- Configuración de Carga de Archivos ---
    UPLOAD_FOLDER = os.path.join(os.path.abspath(os.path.dirname(__file__)), 'static/uploads/products')
    ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}