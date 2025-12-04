# Etapa 1: Construcción
FROM node:22-alpine as build
WORKDIR /app

# Copiamos los package.json
COPY package*.json ./

# Instalamos dependencias
RUN npm install

# Copiamos el resto del código
COPY . .

# Compilamos
RUN npm run build

# Etapa 2: Servidor Nginx
FROM nginx:alpine

# Copiamos los archivos compilados (RUTA CORREGIDA)
COPY --from=build /app/dist/portal-periodistico-frontend/browser /usr/share/nginx/html

# Copiamos la configuración de nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80