# Mongo init scripts

`.js` files placed here are run once by the official `mongo` Docker image on first container start
(mounted to `/docker-entrypoint-initdb.d` in Stage 9's `docker-compose.yml`). Used for seed data
(sample problems/contests) added in later stages — empty for now.
