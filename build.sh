mvn install:install-file -Dfile=Chesspresso-lib.jar -DgroupId=chesspresso -DartifactId=chesspresso -Dversion=0.9.2 -Dpackaging=jar
mvn clean install
cp target/rwchess-1.0-SNAPSHOT.war jetty-distribution-9.4.39.v20210325/webapps/root.war
cd jetty-distribution-9.4.39.v20210325
java -jar start.jar
