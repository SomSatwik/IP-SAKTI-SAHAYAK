"""
IP-SAKTI SAHAYAK
Flask Application Server
========================
Main application entry point. Configures Flask app, registers blueprints,
sets up CORS, custom error handlers, and logging.
"""

import logging
from pathlib import Path
from flask import Flask, render_template, jsonify, request
from flask_cors import CORS
from config import Config
from database.db import init_db
from routes import chat_bp, search_bp, source_bp, admin_bp
from database.models import QueryLog
from database.db import get_db_session

# Configure Logging
logging.basicConfig(
    level=logging.INFO if Config.DEBUG else logging.WARNING,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("ip_sakti")


def create_app() -> Flask:
    """Application factory for IP-SAKTI SAHAYAK."""
    app = Flask(
        __name__,
        template_folder=str(Config.BASE_DIR / "templates"),
        static_folder=str(Config.BASE_DIR / "static")
    )
    app.config.from_object(Config)

    # Enable CORS for API routes
    CORS(app, resources={r"/api/*": {"origins": "*"}})

    # Register Blueprints
    app.register_blueprint(chat_bp)
    app.register_blueprint(search_bp)
    app.register_blueprint(source_bp)
    app.register_blueprint(admin_bp)

    # Initialize Database tables
    with app.app_context():
        init_db()

    # Core Page Routes
    @app.route("/", methods=["GET"])
    def index():
        """Render public landing page."""
        return render_template("index.html")

    @app.route("/dashboard", methods=["GET"])
    def dashboard():
        """Render research dashboard with metrics and recent queries."""
        recent_queries = []
        try:
            with get_db_session() as session:
                logs = session.query(QueryLog).order_by(QueryLog.id.desc()).limit(5).all()
                recent_queries = [
                    {
                        "query": l.query_text,
                        "domain": l.detected_domain,
                        "jurisdiction": l.detected_jurisdiction,
                        "confidence_pct": int(l.confidence_score * 100),
                        "level": l.confidence_level,
                        "created_at": l.created_at.strftime("%b %d, %H:%M")
                    }
                    for l in logs
                ]
        except Exception:
            pass

        return render_template(
            "dashboard.html",
            recent_queries=recent_queries,
            source_count=len(Config.OFFICIAL_SOURCES)
        )

    # Error Handlers
    @app.errorhandler(404)
    def not_found(error):
        if request.path.startswith("/api/"):
            return jsonify({"success": False, "error": "Endpoint not found."}), 404
        return render_template("index.html"), 404

    @app.errorhandler(500)
    def server_error(error):
        logger.error("Internal Server Error: %s", str(error))
        if request.path.startswith("/api/"):
            return jsonify({"success": False, "error": "An internal system error occurred. Stack traces suppressed."}), 500
        return render_template("index.html"), 500

    return app


app = create_app()

if __name__ == "__main__":
    logger.info("Starting IP-SAKTI SAHAYAK server on port %d...", Config.PORT)
    app.run(host="0.0.0.0", port=Config.PORT, debug=Config.DEBUG)
