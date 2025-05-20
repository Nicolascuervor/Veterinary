from flask import Flask
from config import Config
from database import db
from routes import routes

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    db.init_app(app)
    app.register_blueprint(routes)

    with app.app_context():
        db.create_all()

    return app

if __name__ == "__main__":
    app = create_app()

    app.run(debug=True, port=5001)
