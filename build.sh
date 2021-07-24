#!/bin/bash
./gradlew clean build --exclude-task test
cd vsha-frontend
rm -rf node-modules
npm install --ignore-scripts
npm run build
cd ..
docker context use max
docker-compose down
docker-compose up -d --build
docker context use default
