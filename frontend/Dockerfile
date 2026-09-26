# Dev-mode container for local docker-compose use only - production hosting is
# Vercel (Stage 10), which builds directly from the repo and doesn't use this.
FROM node:20-alpine
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
