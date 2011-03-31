# This file builds webapp for local running

# makes the dir for downloaded files
mkdir cache 
cd cache

# download dependencies
wget http://googleappengine.googlecode.com/files/appengine-java-sdk-1.4.3.zip
wget http://mirrors.ibiblio.org/pub/mirrors/maven2/commons-fileupload/commons-fileupload/1.2.1/commons-fileupload-1.2.1.jar
wget http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/commons/commons-io/1.3.2/commons-io-1.3.2.jar
wget http://prdownloads.sourceforge.net/nanoxml/nanoxml-lite-2.2.1.jar

# unzipping the GAE SDK and bliki
echo "unpacking AppEngine SDK..."
unzip -d ../ appengine-java-sdk-1.4.3.zip > /dev/null

# copying Apache Commons libraries
cp *.jar ../war/WEB-INF/lib

# go to the build directory and run Ant build script
#rm list
cd ../build
ant compile && echo "Done, enjoy!"



