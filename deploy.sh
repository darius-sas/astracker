mvn clean compile package -DskipTests
docker build --tag astracker-web:latest .
ns=astracker; tag=latest; img=astracker-web
docker login registry.webhosting.rug.nl
docker tag ${img} registry.webhosting.rug.nl/${ns}/${img}:${tag}
docker push registry.webhosting.rug.nl/${ns}/${img}:${tag}
