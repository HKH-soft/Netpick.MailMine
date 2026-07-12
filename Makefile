.PHONY: dev prod down logs clean secrets validate

# Development
dev:
	docker-compose up -d

# Production
prod:
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml config

# Deploy to swarm
deploy:
	docker stack deploy -c docker-stack.yml mailmine

# Stop services
down:
	docker-compose down -v

# View logs
logs:
	docker-compose logs -f

# Clean up
clean:
	docker system prune -f
	docker volume prune -f

# Generate secrets
secrets:
	pwsh -File scripts/generate-secrets.ps1

# Validate compose files
validate:
	docker-compose config
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml config

# Run tests
test:
	cd Backend && ./mvnw test
	cd Frontend && npm test

# Build images
build:
	docker-compose build --no-cache