# Etapa 1: Construcción
# Usamos Node 22 (LTS más reciente) para asegurar compatibilidad con tu Angular moderno
FROM node:22-alpine as build
WORKDIR /app

# Copiamos los package.json
COPY package*.json ./

# Instalamos dependencias
RUN npm install

# Copiamos el resto del código (Docker ignorará node_modules gracias al .dockerignore)
COPY . .

# Compilamos
RUN npm run build

# Etapa 2: Servidor Nginx
FROM nginx:alpine
# Ajusta 'portal-periodistico-frontend' si tu carpeta dist tiene otro nombre dentro
COPY --from=build /app/dist/portal-periodistico-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80