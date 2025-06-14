from flask import Flask
from config import Config
from database import db
from routes import routes
import os


def create_app():
    app = Flask(
        __name__,
        static_folder=os.path.join(os.path.dirname(__file__), 'static'),
        template_folder=os.path.join(os.path.dirname(__file__), 'templates')
    )
    app.config.from_object(Config)

    db.init_app(app)
    app.register_blueprint(routes)

    with app.app_context():
        db.create_all()

    return app

if __name__ == "__main__":
    app = create_app()

    app.run(debug=True, port=5001)
