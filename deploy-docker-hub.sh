mvn clean compile package -DskipTests
docker build --tag astracker-web:latest .
repo=fenn/apps; img=astracker-web
docker tag ${img} fenn/apps:${img}
docker push ${repo}:{img}
