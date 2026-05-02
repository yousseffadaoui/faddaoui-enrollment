FROM node:20.19 AS build

WORKDIR /app
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM node:20.19

WORKDIR /app
COPY --from=build /app .

EXPOSE 4200

CMD ["npm", "run", "start", "--", "--host", "0.0.0.0"]
